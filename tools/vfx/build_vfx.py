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
def streak_row(w, h, *, core, edge, glow, head_len, body_len, thickness, taper, frag, rng, jag=0.0, tip_bias=0.3, dots=0):
    """One trail row: a right-anchored streak. `frag` 0..1 breaks it into fragments (age)."""
    img = canvas(w, h)
    cy = h * (0.5 - tip_bias * 0.5)
    start = w - 1 - body_len
    # organic wobble: smoothed random walk, scaled by jag
    walk = np.cumsum(rng.normal(0, 1, w))
    walk = np.convolve(walk, np.ones(9) / 9, mode="same")
    walk = (walk - walk.mean()) / (walk.std() + 1e-6) * jag * 0.6
    # fragmentation mask: random runs of gaps whose density grows with age
    gap = np.zeros(w, dtype=bool)
    x = int(start)
    while x < w:
        if rng.random() < frag * 0.35:
            run = rng.integers(1, 3 + int(frag * 5))
            gap[x : x + run] = True
            x += run
        x += 1
    for x in range(max(0, int(start)), w):
        u = (x - start) / max(1, body_len)          # 0 at tail, 1 at head
        # crescent-ish profile: thin tail, thick toward the head, slight thinning at the very head
        prof = taper + (1 - taper) * (u ** 0.7) * (1 - 0.25 * max(0, (u - 0.85) / 0.15))
        th = thickness * prof * (1 - frag * 0.45)
        if gap[x] or th < 0.6:
            continue
        top = cy - th / 2 + walk[x]
        for y in range(int(top) - 1, int(top + th) + 2):
            d = abs((y + 0.5) - (top + th / 2)) / max(th / 2, 0.5)
            if d > 1.0:
                continue
            if d < 0.45 and u > 0.2:
                col = core
            elif d < 0.8:
                col = edge
            else:
                col = glow
            a = 1.0 if d < 0.8 else 0.6
            a *= 0.45 + 0.55 * u
            put(img, x, y, col, a)
    # bright head streak on the newest rows
    if frag < 0.5:
        hx = w - 1
        for i in range(head_len):
            a = (1 - i / head_len) * (1 - frag * 2)
            put(img, hx - i, int(cy + walk[hx - i]), shade(core, 1.3), a)
            put(img, hx - i, int(cy + walk[hx - i]) - 1, core, a * 0.6)
    # loose dots behind the stroke
    for _ in range(dots):
        x = rng.integers(int(start), w)
        y = int(cy + walk[x] + rng.integers(-thickness, thickness + 1))
        put(img, x, y, edge, rng.uniform(0.3, 0.8))
    return img


def trail_strip(name, w, h, rows_fn):
    frames = [rows_fn(i) for i in range(ROWS)]
    sheet = sheet_vertical(frames)
    save(sheet, VFX / f"{name}.png")
    preview(sheet, 2).save(PREVIEW / f"vfx_{name}.png")
    print(f"  {name}: {sheet.shape[1]}x{sheet.shape[0]}")


def build_trails():
    # --- katana (pawn 6): thin, clean, white-steel with icy edge
    def katana(i):
        rng = np.random.default_rng(100 + i)
        age = i / (ROWS - 1)
        return streak_row(128, 32, core=(255, 255, 255, 255), edge=(214, 236, 255, 230), glow=(140, 190, 240, 120),
                          head_len=18, body_len=int(120 - 40 * age), thickness=int(10 - 5 * age) + 2, taper=0.15,
                          frag=age * 0.9, rng=rng, jag=0.0, tip_bias=0.4, dots=int(10 * age))
    trail_strip("trail_katana", 128, 32, katana)

    # --- saw knife: blood-red, jagged tooth edge, drips
    def saw(i):
        rng = np.random.default_rng(200 + i)
        age = i / (ROWS - 1)
        img = streak_row(128, 32, core=(255, 96, 72, 255), edge=(190, 28, 32, 235), glow=(110, 10, 20, 130),
                         head_len=12, body_len=int(118 - 30 * age), thickness=int(12 - 5 * age) + 3, taper=0.3,
                         frag=age * 0.8, rng=rng, jag=1.2, tip_bias=0.2, dots=int(16 * age))
        # teeth on the tip edge
        for x in range(20, 128, 5):
            if rng.random() < 0.8 - age * 0.5:
                y = 9 - (x % 10 == 0)
                put(img, x, y, (255, 150, 120, 255), 0.9)
                put(img, x, y - 1, (255, 200, 180, 255), 0.7)
        # drips downward with age
        for _ in range(int(6 * age)):
            x = rng.integers(30, 126)
            for k in range(rng.integers(2, 6)):
                put(img, x, 22 + k, (160, 16, 24, 255), 0.8 - k * 0.12)
        return img
    trail_strip("trail_saw", 128, 32, saw)

    # --- dagger / knife: very thin, short, silver with violet tint
    def dagger(i):
        rng = np.random.default_rng(300 + i)
        age = i / (ROWS - 1)
        return streak_row(128, 32, core=(255, 255, 255, 255), edge=(222, 210, 255, 220), glow=(150, 120, 220, 110),
                          head_len=14, body_len=int(90 - 35 * age), thickness=int(5 - 2 * age) + 1, taper=0.1,
                          frag=age * 1.0, rng=rng, jag=0.0, tip_bias=0.45, dots=int(8 * age))
    trail_strip("trail_dagger", 128, 32, dagger)

    # --- hatchet: thick, chunky amber with dark red core (madness)
    def hatchet(i):
        rng = np.random.default_rng(400 + i)
        age = i / (ROWS - 1)
        img = streak_row(128, 32, core=(255, 214, 120, 255), edge=(236, 120, 48, 235), glow=(150, 40, 30, 130),
                         head_len=10, body_len=int(100 - 25 * age), thickness=int(16 - 6 * age) + 4, taper=0.45,
                         frag=age * 0.7, rng=rng, jag=1.0, tip_bias=0.1, dots=int(12 * age))
        # embers
        for _ in range(int(6 + 10 * age)):
            x = rng.integers(40, 128); y = rng.integers(4, 28)
            put(img, x, y, (255, 180, 80, 255), rng.uniform(0.4, 0.9))
        return img
    trail_strip("trail_hatchet", 128, 32, hatchet)

    # --- bat: cream dust, wide and soft, with wire sparks
    def bat(i):
        rng = np.random.default_rng(500 + i)
        age = i / (ROWS - 1)
        img = streak_row(128, 32, core=(255, 248, 226, 235), edge=(236, 220, 180, 200), glow=(200, 180, 140, 110),
                         head_len=8, body_len=int(118 - 20 * age), thickness=int(18 - 8 * age) + 4, taper=0.5,
                         frag=age * 0.75, rng=rng, jag=1.4, tip_bias=0.0, dots=int(20 * age))
        for _ in range(int(5 + 6 * age)):
            x = rng.integers(60, 128); y = rng.integers(3, 29)
            put(img, x, y, (255, 255, 255, 255), rng.uniform(0.5, 1.0))
        return img
    trail_strip("trail_bat", 128, 32, bat)

    # --- fist: short translucent cream-green puff (tteokbal), used for bare-hand strokes
    def fist(i):
        rng = np.random.default_rng(600 + i)
        age = i / (ROWS - 1)
        return streak_row(128, 32, core=(240, 255, 232, 210), edge=(170, 230, 170, 170), glow=(90, 160, 110, 90),
                          head_len=6, body_len=int(70 - 30 * age), thickness=int(14 - 6 * age) + 4, taper=0.6,
                          frag=age * 0.9, rng=rng, jag=1.6, tip_bias=0.0, dots=int(14 * age))
    trail_strip("trail_fist", 128, 32, fist)

    # --- generic fallback (kept name): cream, used by anything without its own style
    def generic(i):
        rng = np.random.default_rng(700 + i)
        age = i / (ROWS - 1)
        return streak_row(128, 32, core=(255, 252, 240, 255), edge=(240, 228, 190, 220), glow=(190, 170, 130, 110),
                          head_len=12, body_len=int(118 - 30 * age), thickness=int(12 - 5 * age) + 3, taper=0.3,
                          frag=age * 0.8, rng=rng, jag=0.6, tip_bias=0.2, dots=int(14 * age))
    trail_strip("weapon_swing_strip", 128, 32, generic)

    # --- neungsa katana: violet, rows 0..3 grow with stroke length, rows 4..7 fade
    def neungsa(i):
        rng = np.random.default_rng(800 + i)
        if i < 4:
            grow = (i + 1) / 4
            body = int(60 + 190 * grow)
            return streak_row(256, 64, core=(246, 232, 255, 255), edge=(176, 108, 255, 240), glow=(96, 40, 190, 150),
                              head_len=36, body_len=body, thickness=22, taper=0.08, frag=0.0, rng=rng, jag=0.0, tip_bias=0.35, dots=0)
        age = (i - 4) / 3
        return streak_row(256, 64, core=(246, 232, 255, 255), edge=(176, 108, 255, 240), glow=(96, 40, 190, 150),
                          head_len=int(30 - 20 * age), body_len=int(250 - 120 * age), thickness=int(22 - 12 * age) + 2, taper=0.08,
                          frag=0.3 + age * 0.7, rng=rng, jag=0.0, tip_bias=0.35, dots=int(30 * age))
    trail_strip("neungsa_swing_strip", 256, 64, neungsa)


# ----------------------------------------------------------------------------- hit sheets
def hit_sheet(name, frame_fn, size=32, frames=8, cols=4):
    fr = [frame_fn(i, frames) for i in range(frames)]
    sheet = sheet_grid(fr, cols)
    save(sheet, VFX / f"{name}.png")
    preview(sheet, 3).save(PREVIEW / f"vfx_{name}.png")
    print(f"  {name}: {sheet.shape[1]}x{sheet.shape[0]} ({frames} frames)")


def _crescent(img, cx, cy, L, bend, thick, color, alpha, rng, jag=0.0, frag=0.0, gap_mask=None):
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


def slash_frame(i, n, *, core, edge, spray, rng_seed, width=1, jag=0.0, double=False, spray_n=10):
    img = canvas(32, 32)
    rng = np.random.default_rng(rng_seed + i)
    t = i / (n - 1)
    cx = cy = 16
    L = 15
    # cut visibility: appears whole by frame 1, dissolves after 0.4
    cut_alpha = 1.0 if t <= 0.4 else max(0, 1 - (t - 0.4) / 0.45)
    frag = 0 if t < 0.35 else min(1, (t - 0.35) / 0.4)
    gap = None
    if frag > 0:
        gap = rng.random(64) < frag * 0.7
    if t == 0:
        # first frame: flash + short seed cut
        disc(img, cx, cy, 4, (255, 255, 255, 255), 1.0)
        _crescent(img, cx, cy, 6, 2, width + 1, core, 1.0, rng)
    else:
        if cut_alpha > 0:
            _crescent(img, cx, cy, L, 4, width + 3, edge, 0.45 * cut_alpha, rng, jag, frag, gap)
            _crescent(img, cx, cy, L, 4, width + 1, core, cut_alpha, rng, jag, frag, gap)
            if double:
                _crescent(img, cx - 4, cy + 2, L - 3, 3, width + 1, edge, 0.7 * cut_alpha, rng, jag, frag, gap)
        if t < 0.3:
            disc(img, cx, cy, 3 - t * 8, (255, 255, 255, 255), 1 - t / 0.3)
    # spray sparks outward along the cut normal, flying further with time
    k = int(spray_n * min(1, 0.4 + t))
    for bias in (-math.pi / 4, 3 * math.pi / 4):
        sparks(img, cx, cy, k, 2 + t * 11, 5 + t * 14, spray, rng, length=2 if t < 0.5 else 1,
               alpha_mul=(1 - t) * 0.95, angle_bias=bias, spread=0.9)
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
    hit_sheet("hit_blade", lambda i, n: slash_frame(i, n, core=(255, 255, 255, 255), edge=(170, 220, 255, 255), spray=(230, 245, 255, 255), rng_seed=1000, width=1))
    hit_sheet("hit_saw", lambda i, n: slash_frame(i, n, core=(255, 120, 90, 255), edge=(170, 20, 30, 255), spray=(200, 30, 40, 255), rng_seed=1100, width=1, jag=0.9, spray_n=16))
    hit_sheet("hit_pierce", lambda i, n: burst_frame(i, n, core=(255, 255, 255, 255), ring=(210, 200, 255, 255), dust=(230, 220, 255, 255), rng_seed=1200, rays=4, ring_width=1, dust_n=6))
    hit_sheet("hit_blunt", lambda i, n: burst_frame(i, n, core=(255, 250, 220, 255), ring=(240, 225, 180, 255), dust=(200, 185, 150, 255), rng_seed=1300, rays=8, ring_width=2, dust_n=16))
    hit_sheet("hit_fist", lambda i, n: burst_frame(i, n, core=(240, 255, 230, 255), ring=(170, 230, 170, 255), dust=(220, 240, 215, 255), rng_seed=1400, rays=6, ring_width=2, dust_n=8))
    hit_sheet("hit_knuckle", lambda i, n: burst_frame(i, n, core=(255, 240, 200, 255), ring=(255, 190, 90, 255), dust=(230, 200, 160, 255), rng_seed=1500, rays=8, ring_width=3, dust_n=14))
    hit_sheet("hit_hatchet", lambda i, n: slash_frame(i, n, core=(255, 225, 150, 255), edge=(220, 90, 40, 255), spray=(180, 40, 30, 255), rng_seed=1600, width=2, jag=0.4, double=True, spray_n=14))
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
