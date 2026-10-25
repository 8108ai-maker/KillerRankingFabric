"""Generates per-weapon pixel trail strips and hit-impact sprite sheets.

Run from repo root:  python3 tools/vfx/build_vfx.py
Outputs go to src/client/resources/assets/killerranking/textures/vfx/.

Trail strip format (read by NightBatTrail): 8 rows stacked vertically, row 0 = newest.
Inside a row, u=0 is the stroke start (oldest point), u=1 the blade's latest position;
v top = blade tip edge, v bottom = inner (hand side) edge.  Rows 0..7 fade with age.
Neungsa strip keeps its special meaning: rows 0..3 grow with stroke length, 4..7 fade.

Hit sheet format: 8 frames of 32x32 laid out 4 columns x 2 rows (128x64), frame 0 first.
"""
from __future__ import annotations

import math
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).parent))
from pixel import *  # noqa: E402,F401,F403

ROOT = Path(__file__).resolve().parents[2]
VFX = ROOT / "src/client/resources/assets/killerranking/textures/vfx"
PREVIEW = Path("/tmp/claude-0/-home-claude/e833bfb2-d29c-51f6-a33f-3d380d47ee7e/scratchpad/preview")

ROWS = 8


# ----------------------------------------------------------------------------- trails
def band_row(w, h, *, bands, body_len, thickness, taper, age, rng, tip_bias=0.0, head_round=True, chunks=0):
    """One clean trail row in the reference's style: a solid, right-anchored sweep made of hard-edged
    colour bands (bands[0] = outermost rim ... bands[-1] = innermost core). No noise, no gaps.
    `age` 0..1 thins the band and pulls its tail back; the last rows keep only the core plus a few
    detached 2px chunks."""
    img = canvas(w, h)
    cy = h * (0.5 - tip_bias * 0.5)
    start = w - 1 - body_len
    n = len(bands)
    for x in range(max(0, int(start)), w):
        u = (x - start) / max(1, body_len)
        # thin tail -> thick body -> rounded head
        prof = taper + (1 - taper) * (u ** 0.65)
        if head_round and u > 0.9:
            prof *= (1 - ((u - 0.9) / 0.1) ** 2) ** 0.5 * 0.9 + 0.1
        th = thickness * prof
        if th < 1:
            continue
        top = cy - th / 2
        # stepped integer edges every few px so the stairs read as pixels
        top = int(top) if (x // 3) % 2 == 0 else int(top + 0.5)
        th_i = max(1, int(round(th)))
        for y in range(top, top + th_i):
            d = abs((y + 0.5) - (top + th_i / 2)) / (th_i / 2)    # 0 centre .. 1 edge
            # band index: core when d small, rim when d near 1; older rows lose the inner bands
            k = int((1 - d) * n)          # 0 = rim ... n-1 = core
            k = min(n - 1, k)
            col = bands[k]
            put(img, x, y, col, 1.0)
    # detached chunks at the tail for the oldest rows
    for _ in range(chunks):
        x = rng.integers(int(start) - 12, int(start) + 20)
        y = int(cy + rng.integers(-thickness // 2, thickness // 2 + 1))
        sz = 2 if rng.random() < 0.7 else 3
        rect(img, x, y, sz, sz, bands[min(n - 1, 1)], 1.0)
    return img


def trail_strip(name, w, h, rows_fn):
    frames = [rows_fn(i) for i in range(ROWS)]
    sheet = sheet_vertical(frames)
    save(sheet, VFX / f"{name}.png")
    preview(sheet, 2).save(PREVIEW / f"vfx_{name}.png")
    print(f"  {name}: {sheet.shape[1]}x{sheet.shape[0]}")


def build_trails():
    # Rows are stretched over the whole ribbon width in game, so `thickness` is the fraction of the
    # sweep that is painted. Rows 0-7 = newest .. oldest: the band thins, the tail recedes, the core goes.
    def rowset(name, w, h, bands, *, th0, th_end, body0, body_end, taper, tip_bias, seed, extra=None):
        def row(i):
            rng = np.random.default_rng(seed + i)
            age = i / (ROWS - 1)
            # older rows drop the inner (brightest) bands: rim stays, core disappears
            keep = max(2, int(round(len(bands) * (1 - age * 0.6))))
            use = bands[:keep] if keep < len(bands) else bands
            img = band_row(w, h, bands=use, body_len=int(body0 + (body_end - body0) * age),
                           thickness=int(th0 + (th_end - th0) * age), taper=taper, age=age, rng=rng,
                           tip_bias=tip_bias, chunks=int(6 * max(0, age - 0.4)))
            if extra:
                extra(img, rng, age)
            return img
        trail_strip(name, w, h, row)

    H = 32
    # band order: rim -> ... -> core
    rowset("trail_katana", 128, H, [(96, 150, 235, 255), (150, 205, 255, 255), (214, 238, 255, 255), (255, 255, 255, 255)],
           th0=26, th_end=6, body0=124, body_end=70, taper=0.18, tip_bias=0.2, seed=100)
    def saw_extra(img, rng, age):
        # serrated outer edge: teeth sitting on the band's actual top edge, fewer as it ages
        for x in range(12, 128, 4):
            col = img[:, x, 3]
            tops = np.nonzero(col)[0]
            if len(tops) == 0 or rng.random() > 0.9 - age * 0.6:
                continue
            top = int(tops[0])
            put(img, x, top - 1, (214, 28, 40, 255), 1.0); put(img, x, top - 2, (255, 120, 110, 255), 1.0)
            put(img, x + 1, top - 1, (214, 28, 40, 255), 1.0)
    rowset("trail_saw", 128, H, [(120, 8, 22, 255), (190, 24, 36, 255), (240, 80, 70, 255), (255, 190, 170, 255)],
           th0=28, th_end=8, body0=124, body_end=80, taper=0.3, tip_bias=0.1, seed=200, extra=saw_extra)
    rowset("trail_dagger", 128, H, [(150, 110, 230, 255), (205, 185, 255, 255), (255, 255, 255, 255)],
           th0=16, th_end=4, body0=104, body_end=52, taper=0.12, tip_bias=0.3, seed=300)
    rowset("trail_hatchet", 128, H, [(150, 36, 28, 255), (235, 110, 40, 255), (255, 185, 80, 255), (255, 236, 170, 255)],
           th0=30, th_end=10, body0=116, body_end=74, taper=0.4, tip_bias=0.05, seed=400)
    rowset("trail_bat", 128, H, [(196, 176, 136, 255), (232, 216, 178, 255), (250, 242, 220, 255), (255, 255, 250, 255)],
           th0=30, th_end=10, body0=124, body_end=92, taper=0.45, tip_bias=0.0, seed=500)
    rowset("trail_fist", 128, H, [(90, 170, 115, 255), (170, 230, 170, 255), (244, 255, 236, 255)],
           th0=24, th_end=6, body0=80, body_end=36, taper=0.5, tip_bias=0.0, seed=600)
    rowset("weapon_swing_strip", 128, H, [(190, 170, 130, 255), (235, 222, 185, 255), (255, 250, 236, 255), (255, 255, 255, 255)],
           th0=26, th_end=8, body0=124, body_end=84, taper=0.3, tip_bias=0.2, seed=700)

    # neungsa katana: violet; rows 0..3 grow with stroke length, 4..7 fade
    def neungsa(i):
        rng = np.random.default_rng(800 + i)
        bands = [(100, 40, 200, 255), (160, 96, 250, 255), (210, 170, 255, 255), (250, 240, 255, 255)]
        if i < 4:
            grow = (i + 1) / 4
            return band_row(256, 64, bands=bands, body_len=int(60 + 190 * grow), thickness=50, taper=0.1, age=0, rng=rng, tip_bias=0.25)
        age = (i - 4) / 3
        keep = max(2, int(round(4 * (1 - age * 0.6))))
        return band_row(256, 64, bands=bands[:keep], body_len=int(250 - 110 * age), thickness=int(50 - 36 * age), taper=0.1,
                        age=age, rng=rng, tip_bias=0.25, chunks=int(8 * age))
    trail_strip("neungsa_swing_strip", 256, 64, neungsa)


# ----------------------------------------------------------------------------- hit sheets
def hit_sheet(name, frame_fn, size=32, frames=8, cols=4):
    fr = [frame_fn(i, frames) for i in range(frames)]
    sheet = sheet_grid(fr, cols)
    save(sheet, VFX / f"{name}.png")
    preview(sheet, 3).save(PREVIEW / f"vfx_{name}.png")
    print(f"  {name}: {sheet.shape[1]}x{sheet.shape[0]} ({frames} frames)")


def _crescent(img, cx, cy, L, bend, thick, color, alpha, rng, jag=0.0, frag=0.0, gap_mask=None):  # noqa: keep signature
    """Quadratic crescent from lower-left to upper-right, thick in the middle, thin at the ends."""
    x0, y0 = cx - L, cy + L
    x1, y1 = cx + L, cy - L
    mx, my = cx - bend, cy - bend           # control point off the chord (bulges up-left)
    n = int(L * 6) + 4
    for k in range(n + 1):
        u = k / n
        if gap_mask is not None and gap_mask[int(u * (len(gap_mask) - 1))]:
            continue
        x = (1 - u) ** 2 * x0 + 2 * (1 - u) * u * mx + u ** 2 * x1 + rng.uniform(-jag, jag)
        y = (1 - u) ** 2 * y0 + 2 * (1 - u) * u * my + u ** 2 * y1 + rng.uniform(-jag, jag)
        w = max(1, int(round(thick * math.sin(math.pi * u) ** 0.8 * (1 - frag * 0.5))))
        r = w // 2
        for oy in range(-r, w - r):
            for ox in range(-r, w - r):
                put(img, int(x) + ox, int(y) + oy, color, alpha)


def slash_frame(i, n, *, bands, spray, rng_seed, width=4, double=False, spray_n=10):
    """Bold banded crescent (rim .. core), whole by frame 1, thinning to the rim by the end, with 2px shards."""
    img = canvas(32, 32)
    rng = np.random.default_rng(rng_seed + i)
    t = i / (n - 1)
    cx = cy = 16
    L = 15
    nb = len(bands)
    keep = nb if t < 0.4 else max(1, int(round(nb * (1 - (t - 0.4) / 0.6))))
    thick = width if t < 0.45 else max(1, width * (1 - (t - 0.45) / 0.6))
    if t == 0:
        disc(img, cx, cy, 4, (255, 255, 255, 255), 1.0)
        for k in range(nb):
            _crescent(img, cx, cy, 7, 2, max(1, int(width - k * (width / nb))), bands[k], 1.0, rng)
    elif t < 0.95:
        for k in range(keep):
            w = max(1, int(round(thick * (1 - k / nb))))
            _crescent(img, cx, cy, L, 5, w, bands[k], 1.0, rng)
        if double:
            for k in range(max(1, keep - 1)):
                w = max(1, int(round(thick * 0.7 * (1 - k / nb))))
                _crescent(img, cx - 5, cy + 3, L - 4, 4, w, bands[k], 1.0, rng)
        if t < 0.3:
            disc(img, cx, cy, 3 - t * 8, (255, 255, 255, 255), 1.0)
    # 2px square shards flying off along the cut normal
    k = int(spray_n * min(1, 0.4 + t))
    for bias in (-math.pi / 4, 3 * math.pi / 4):
        for _ in range(k):
            a = bias + rng.uniform(-0.9, 0.9)
            r = rng.uniform(3 + t * 10, 6 + t * 15)
            x, y = int(cx + math.cos(a) * r), int(cy + math.sin(a) * r)
            sz = 2 if rng.random() < 0.6 else 1
            rect(img, x, y, sz, sz, spray if rng.random() < 0.6 else bands[-1], 1.0 if t < 0.7 else 0.7)
    return img


def burst_frame(i, n, *, core, ring, dust, rng_seed, rays=8, ring_width=2, dust_n=12, star=True):
    img = canvas(32, 32)
    rng = np.random.default_rng(rng_seed + i)
    t = i / (n - 1)
    cx = cy = 16
    fade = 1 - t
    # expanding ring that breaks into dashes as it grows
    r = 3 + t * 12
    if t < 0.75:
        for a in np.linspace(0, 2 * math.pi, int(r * 7), endpoint=False):
            if t > 0.3 and (int(a * 6 + t * 10) % 3 == 0):
                continue
            for rr in np.linspace(max(0.5, r - ring_width), r, ring_width + 1):
                put(img, int(cx + math.cos(a) * rr), int(cy + math.sin(a) * rr), ring, 0.9 * fade)
    # star rays, short and uneven
    if star and t < 0.6:
        L = 4 + 8 * min(1, t * 2.5)
        for k in range(rays):
            a = k * 2 * math.pi / rays + rng.uniform(-0.15, 0.15)
            l = L * (1.0 if k % 2 == 0 else 0.55)
            line(img, cx + math.cos(a) * 2, cy + math.sin(a) * 2, cx + math.cos(a) * l, cy + math.sin(a) * l, core, 1, (1 - t / 0.6))
    # flash core
    if t < 0.4:
        disc(img, cx, cy, 4 - t * 8, (255, 255, 255, 255), 1 - t / 0.4)
    # dust puffs drifting out and down
    for _ in range(int(dust_n * min(1, 0.3 + t))):
        a = rng.uniform(0, 2 * math.pi)
        d = rng.uniform(4, 6) + t * 11
        x = cx + math.cos(a) * d; y = cy + math.sin(a) * d + t * 3
        s = 1 if rng.random() < 0.6 else 2
        rect(img, int(x), int(y), s, s, dust, rng.uniform(0.4, 1.0) * fade)
    return img


def build_hits():
    hit_sheet("hit_blade", lambda i, n: slash_frame(i, n, bands=[(96, 150, 235, 255), (170, 222, 255, 255), (255, 255, 255, 255)], spray=(230, 245, 255, 255), rng_seed=1000, width=5))
    hit_sheet("hit_saw", lambda i, n: slash_frame(i, n, bands=[(120, 8, 22, 255), (214, 28, 40, 255), (255, 150, 130, 255)], spray=(200, 30, 40, 255), rng_seed=1100, width=6, spray_n=16))
    hit_sheet("hit_pierce", lambda i, n: burst_frame(i, n, core=(255, 255, 255, 255), ring=(210, 200, 255, 255), dust=(230, 220, 255, 255), rng_seed=1200, rays=4, ring_width=2, dust_n=8))
    hit_sheet("hit_blunt", lambda i, n: burst_frame(i, n, core=(255, 250, 220, 255), ring=(240, 225, 180, 255), dust=(200, 185, 150, 255), rng_seed=1300, rays=8, ring_width=3, dust_n=18))
    hit_sheet("hit_fist", lambda i, n: burst_frame(i, n, core=(240, 255, 230, 255), ring=(170, 230, 170, 255), dust=(220, 240, 215, 255), rng_seed=1400, rays=6, ring_width=3, dust_n=10))
    hit_sheet("hit_knuckle", lambda i, n: burst_frame(i, n, core=(255, 240, 200, 255), ring=(255, 190, 90, 255), dust=(230, 200, 160, 255), rng_seed=1500, rays=8, ring_width=4, dust_n=16))
    hit_sheet("hit_hatchet", lambda i, n: slash_frame(i, n, bands=[(150, 36, 28, 255), (235, 110, 40, 255), (255, 236, 170, 255)], spray=(180, 40, 30, 255), rng_seed=1600, width=6, double=True, spray_n=14))
    # a lethal overlay: big white cross flash + red bloom, played on killing blows
    def lethal(i, n):
        img = canvas(32, 32); t = i / (n - 1); cx = cy = 16
        fade = 1 - t
        disc(img, cx, cy, 2 + t * 14, (200, 30, 40, 255), 0.55 * fade, hollow=max(0, t * 14 - 2))
        L = 5 + 11 * min(1, t * 1.8)
        for a in (0, math.pi / 2):
            line(img, cx - math.cos(a) * L, cy - math.sin(a) * L, cx + math.cos(a) * L, cy + math.sin(a) * L, (255, 255, 255, 255), 1, fade)
        if t < 0.3:
            disc(img, cx, cy, 5 - t * 10, (255, 255, 255, 255), 1 - t / 0.3)
        return img
    hit_sheet("hit_lethal", lethal)


if __name__ == "__main__":
    VFX.mkdir(parents=True, exist_ok=True)
    PREVIEW.mkdir(parents=True, exist_ok=True)
    print("trails:")
    build_trails()
    print("hits:")
    build_hits()
