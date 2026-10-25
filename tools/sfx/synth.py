"""Procedural SFX synthesis primitives for Killer Ranking.

Everything is mono float32 at SR. Sounds are built from a few primitives
(filtered noise, pitch-swept sines, inharmonic metal partials, low thumps)
and shaped with envelopes. `master()` normalizes and soft-clips.
"""
from __future__ import annotations

import numpy as np
from scipy import signal

SR = 44100


# ---------------------------------------------------------------- basics
def t(dur: float) -> np.ndarray:
    return np.arange(int(dur * SR)) / SR


def silence(dur: float) -> np.ndarray:
    return np.zeros(int(dur * SR), dtype=np.float32)


def noise(dur: float, seed: int | None = None) -> np.ndarray:
    rng = np.random.default_rng(seed)
    return rng.standard_normal(int(dur * SR)).astype(np.float32)


def sine(freq, dur: float, phase: float = 0.0) -> np.ndarray:
    """freq may be a scalar or an array (instantaneous frequency)."""
    tt = t(dur)
    if np.isscalar(freq):
        return np.sin(2 * np.pi * freq * tt + phase).astype(np.float32)
    freq = np.asarray(freq, dtype=np.float64)
    ph = 2 * np.pi * np.cumsum(freq) / SR
    return np.sin(ph + phase).astype(np.float32)


def sweep(f0: float, f1: float, dur: float, curve: float = 1.0) -> np.ndarray:
    """Instantaneous-frequency array moving f0 -> f1 (curve>1 = slow start)."""
    x = np.linspace(0, 1, int(dur * SR)) ** curve
    return f0 + (f1 - f0) * x


def saw(freq: float, dur: float) -> np.ndarray:
    return signal.sawtooth(2 * np.pi * freq * t(dur)).astype(np.float32)


# ---------------------------------------------------------------- envelopes
def env_adsr(dur: float, a=0.005, d=0.05, s=0.6, r=0.1) -> np.ndarray:
    n = int(dur * SR)
    na, nd, nr = int(a * SR), int(d * SR), int(r * SR)
    ns = max(0, n - na - nd - nr)
    e = np.concatenate([
        np.linspace(0, 1, max(na, 1)),
        np.linspace(1, s, max(nd, 1)),
        np.full(ns, s),
        np.linspace(s, 0, max(nr, 1)),
    ])
    return _fit(e, n)


def env_exp(dur: float, decay: float = 8.0, attack: float = 0.002) -> np.ndarray:
    """Exponential decay with a tiny linear attack."""
    tt = t(dur)
    e = np.exp(-decay * tt)
    na = max(1, int(attack * SR))
    e[:na] *= np.linspace(0, 1, na)
    return e.astype(np.float32)


def env_hump(dur: float, peak: float = 0.4, sharp: float = 2.0) -> np.ndarray:
    """Smooth rise to `peak` (fraction of dur) then fall; sharp>1 = tighter."""
    x = np.linspace(0, 1, int(dur * SR))
    up = np.clip(x / max(peak, 1e-4), 0, 1)
    down = np.clip((1 - x) / max(1 - peak, 1e-4), 0, 1)
    e = (np.sin(up * np.pi / 2) * np.sin(down * np.pi / 2)) ** sharp
    return e.astype(np.float32)


def _fit(e: np.ndarray, n: int) -> np.ndarray:
    if len(e) < n:
        e = np.concatenate([e, np.zeros(n - len(e))])
    return e[:n].astype(np.float32)


# ---------------------------------------------------------------- filters
def bandpass(x: np.ndarray, lo: float, hi: float, order: int = 2) -> np.ndarray:
    lo = max(20, min(lo, SR / 2 - 100))
    hi = max(lo + 10, min(hi, SR / 2 - 50))
    sos = signal.butter(order, [lo, hi], btype="band", fs=SR, output="sos")
    return signal.sosfilt(sos, x).astype(np.float32)


def lowpass(x: np.ndarray, fc: float, order: int = 2) -> np.ndarray:
    sos = signal.butter(order, min(fc, SR / 2 - 50), btype="low", fs=SR, output="sos")
    return signal.sosfilt(sos, x).astype(np.float32)


def highpass(x: np.ndarray, fc: float, order: int = 2) -> np.ndarray:
    sos = signal.butter(order, max(fc, 20), btype="high", fs=SR, output="sos")
    return signal.sosfilt(sos, x).astype(np.float32)


def moving_bandpass(x: np.ndarray, centers: np.ndarray, q: float = 1.5, blocks: int = 48) -> np.ndarray:
    """Noise 'whoosh' filter: bandpass whose centre frequency moves over time.
    Implemented block-wise with cross-fades (cheap and artifact-free enough)."""
    n = len(x)
    out = np.zeros(n, dtype=np.float32)
    edges = np.linspace(0, n, blocks + 1).astype(int)
    win = int(SR * 0.004)
    for i in range(blocks):
        a, b = edges[i], edges[i + 1]
        if b <= a:
            continue
        a0, b0 = max(0, a - win), min(n, b + win)
        fc = float(centers[min(n - 1, (a + b) // 2)])
        lo, hi = fc / (1 + 1 / (2 * q)), fc * (1 + 1 / (2 * q))
        seg = bandpass(x[a0:b0], lo, hi, order=2)
        w = np.ones(b0 - a0, dtype=np.float32)
        if a0 < a:
            w[: a - a0] = np.linspace(0, 1, a - a0)
        if b0 > b:
            w[-(b0 - b):] = np.linspace(1, 0, b0 - b)
        out[a0:b0] += seg * w
    return out


# ---------------------------------------------------------------- building blocks
def whoosh(dur: float, f0: float, f1: float, *, q=1.4, peak=0.45, sharp=1.6, curve=1.0, seed=None, gain=1.0) -> np.ndarray:
    n = noise(dur, seed)
    c = sweep(f0, f1, dur, curve)
    w = moving_bandpass(n, c, q=q)
    return (w * env_hump(dur, peak, sharp) * gain).astype(np.float32)


def thump(dur: float, f0: float, f1: float, *, decay=14.0, gain=1.0, drive=1.5) -> np.ndarray:
    s = sine(sweep(f0, f1, dur, curve=0.5), dur)
    s = np.tanh(s * drive)
    return (s * env_exp(dur, decay) * gain).astype(np.float32)


def click(dur: float = 0.012, *, lo=1500, hi=9000, seed=None, gain=1.0) -> np.ndarray:
    n = bandpass(noise(dur, seed), lo, hi, order=1)
    return (n * env_exp(dur, 180) * gain).astype(np.float32)


def metal(dur: float, partials, decays, *, gain=1.0, detune=0.0, seed=0) -> np.ndarray:
    """Sum of inharmonic sine partials with individual decays (bells/clangs)."""
    rng = np.random.default_rng(seed)
    out = np.zeros(int(dur * SR), dtype=np.float32)
    for f, d in zip(partials, decays):
        f2 = f * (1 + detune * rng.uniform(-1, 1))
        out += sine(f2, dur, rng.uniform(0, 2 * np.pi)) * env_exp(dur, d) * (1.0 / (1 + np.log1p(f2 / 300)))
    return (out * gain).astype(np.float32)


def shimmer(dur: float, f0: float, f1: float, *, gain=0.4, decay=12.0) -> np.ndarray:
    """Thin, airy sine sweep that gives blades their 'shing'."""
    s = sine(sweep(f0, f1, dur, curve=0.7), dur)
    return (s * env_exp(dur, decay, attack=0.004) * gain).astype(np.float32)


def amod(x: np.ndarray, rate: float, depth: float = 0.8) -> np.ndarray:
    m = 1 - depth * 0.5 * (1 + np.sign(np.sin(2 * np.pi * rate * t(len(x) / SR))))
    return (x * m).astype(np.float32)


def crackle(dur: float, density: float = 400, *, lo=800, hi=6000, seed=None, gain=1.0) -> np.ndarray:
    """Sparse random impulses -> fire/spark/tear texture."""
    rng = np.random.default_rng(seed)
    n = int(dur * SR)
    imp = np.zeros(n, dtype=np.float32)
    k = int(density * dur)
    idx = rng.integers(0, n, k)
    imp[idx] = rng.uniform(0.3, 1.0, k) * rng.choice([-1, 1], k)
    return (bandpass(imp, lo, hi, 1) * gain).astype(np.float32)


# ---------------------------------------------------------------- composition
def mix(*parts, offsets=None) -> np.ndarray:
    """Sum signals; `offsets` = start time in seconds for each part."""
    offsets = offsets or [0.0] * len(parts)
    n = max(int(o * SR) + len(p) for p, o in zip(parts, offsets))
    out = np.zeros(n, dtype=np.float32)
    for p, o in zip(parts, offsets):
        s = int(o * SR)
        out[s : s + len(p)] += p
    return out


def delay(x: np.ndarray, time: float, feedback: float = 0.3, mixv: float = 0.25, taps: int = 3) -> np.ndarray:
    d = int(time * SR)
    out = np.concatenate([x, np.zeros(d * taps, dtype=np.float32)])
    acc = out.copy()
    g = mixv
    for i in range(1, taps + 1):
        acc[i * d : i * d + len(x)] += x * g
        g *= feedback
    return acc


def reverb(x: np.ndarray, size: float = 0.12, mixv: float = 0.18, seed: int = 1) -> np.ndarray:
    """Tiny noise-burst convolution reverb; cheap and decent for short FX."""
    rng = np.random.default_rng(seed)
    n = int(size * SR)
    ir = rng.standard_normal(n) * np.exp(-np.linspace(0, 8, n))
    ir = lowpass(ir.astype(np.float32), 6000)
    total = len(x) + n
    wet = signal.fftconvolve(x, ir).astype(np.float32)
    wet = np.concatenate([wet, np.zeros(max(0, total - len(wet)), dtype=np.float32)])[:total]
    wet /= max(1e-6, np.max(np.abs(wet)))
    dry = np.concatenate([x, np.zeros(n, dtype=np.float32)])[:total]
    return (dry * (1 - mixv) + wet * mixv * np.max(np.abs(x))).astype(np.float32)


def master(x: np.ndarray, peak_db: float = -1.5, tail: float = 0.02, hp: float = 40) -> np.ndarray:
    x = highpass(x, hp, 1)
    x = np.concatenate([x, np.zeros(int(tail * SR), dtype=np.float32)])
    m = np.max(np.abs(x)) or 1.0
    x = x / m
    x = np.tanh(x * 1.15) / np.tanh(1.15)
    x *= 10 ** (peak_db / 20)
    # final fade to avoid clicks at the end
    nf = min(len(x), int(0.004 * SR))
    x[-nf:] *= np.linspace(1, 0, nf)
    return x.astype(np.float32)
