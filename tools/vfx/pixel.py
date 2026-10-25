"""Tiny pixel-art toolkit: integer-grid drawing with hard edges, no antialiasing.

All drawing happens on numpy RGBA uint8 arrays (H, W, 4). Colors are (r,g,b,a).
"""
from __future__ import annotations

import math

import numpy as np
from PIL import Image


def canvas(w: int, h: int) -> np.ndarray:
    return np.zeros((h, w, 4), dtype=np.uint8)


def put(img: np.ndarray, x: int, y: int, color, alpha_mul: float = 1.0):
    h, w = img.shape[:2]
    if 0 <= x < w and 0 <= y < h:
        r, g, b, a = color
        a = int(a * alpha_mul)
        if a <= 0:
            return
        # "over" compositing in pixel space keeps overlaps crisp but blended
        dst = img[y, x].astype(np.int32)
        da = dst[3]
        if da == 0 or a >= 255:
            img[y, x] = (r, g, b, min(255, a))
            return
        sa, wa = a / 255.0, (da / 255.0) * (1 - a / 255.0)
        oa = sa + wa
        img[y, x, 0] = min(255, int(round((r * sa + dst[0] * wa) / oa)))
        img[y, x, 1] = min(255, int(round((g * sa + dst[1] * wa) / oa)))
        img[y, x, 2] = min(255, int(round((b * sa + dst[2] * wa) / oa)))
        img[y, x, 3] = min(255, int(round(oa * 255)))


def line(img, x0, y0, x1, y1, color, thickness: int = 1, alpha_mul=1.0):
    """Bresenham line; thickness grows perpendicular-ish by stamping squares."""
    x0, y0, x1, y1 = int(round(x0)), int(round(y0)), int(round(x1)), int(round(y1))
    dx, dy = abs(x1 - x0), -abs(y1 - y0)
    sx, sy = (1 if x0 < x1 else -1), (1 if y0 < y1 else -1)
    err = dx + dy
    r = thickness // 2
    while True:
        for oy in range(-r, thickness - r):
            for ox in range(-r, thickness - r):
                put(img, x0 + ox, y0 + oy, color, alpha_mul)
        if x0 == x1 and y0 == y1:
            break
        e2 = 2 * err
        if e2 >= dy:
            err += dy
            x0 += sx
        if e2 <= dx:
            err += dx
            y0 += sy


def disc(img, cx, cy, radius, color, alpha_mul=1.0, hollow: float | None = None):
    r2 = radius * radius
    inner = None if hollow is None else hollow * hollow
    for y in range(int(cy - radius) - 1, int(cy + radius) + 2):
        for x in range(int(cx - radius) - 1, int(cx + radius) + 2):
            d = (x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2
            if d <= r2 and (inner is None or d >= inner):
                put(img, x, y, color, alpha_mul)


def rect(img, x, y, w, h, color, alpha_mul=1.0):
    for yy in range(int(y), int(y + h)):
        for xx in range(int(x), int(x + w)):
            put(img, xx, yy, color, alpha_mul)


def arc_ribbon(img, cx, cy, r_outer, r_inner, a0, a1, color, alpha_mul=1.0, steps=None):
    """Filled annular sector between angles a0..a1 (radians, CCW, 0 = +x)."""
    steps = steps or max(8, int(abs(a1 - a0) * r_outer * 1.5))
    for y in range(int(cy - r_outer) - 1, int(cy + r_outer) + 2):
        for x in range(int(cx - r_outer) - 1, int(cx + r_outer) + 2):
            px, py = x + 0.5 - cx, y + 0.5 - cy
            d = math.hypot(px, py)
            if r_inner <= d <= r_outer:
                ang = math.atan2(py, px)
                # normalise to a0..a1 range
                lo, hi = min(a0, a1), max(a0, a1)
                for k in (-2 * math.pi, 0, 2 * math.pi):
                    if lo <= ang + k <= hi:
                        put(img, x, y, color, alpha_mul)
                        break


def sparks(img, cx, cy, count, r_lo, r_hi, color, rng, length=1, alpha_mul=1.0, angle_bias=None, spread=math.pi):
    for i in range(count):
        if angle_bias is None:
            a = rng.uniform(0, 2 * math.pi)
        else:
            a = angle_bias + rng.uniform(-spread, spread)
        r = rng.uniform(r_lo, r_hi)
        x, y = cx + math.cos(a) * r, cy + math.sin(a) * r
        x2, y2 = x + math.cos(a) * length, y + math.sin(a) * length
        line(img, x, y, x2, y2, color, 1, alpha_mul)


def shade(color, k: float):
    """Darken (<1) or lighten (>1) rgb, keep alpha."""
    r, g, b, a = color
    if k <= 1:
        return (int(r * k), int(g * k), int(b * k), a)
    return (min(255, int(r + (255 - r) * (k - 1))), min(255, int(g + (255 - g) * (k - 1))), min(255, int(b + (255 - b) * (k - 1))), a)


def with_alpha(color, a):
    return (color[0], color[1], color[2], int(a))


def save(img: np.ndarray, path):
    Image.fromarray(img, "RGBA").save(path)


def sheet_vertical(frames: list[np.ndarray]) -> np.ndarray:
    """Stack frames top-to-bottom (frame 0 at the top), as the trail renderer expects."""
    return np.concatenate(frames, axis=0)


def sheet_grid(frames: list[np.ndarray], cols: int) -> np.ndarray:
    h, w = frames[0].shape[:2]
    rows = (len(frames) + cols - 1) // cols
    out = canvas(w * cols, h * rows)
    for i, f in enumerate(frames):
        r, c = divmod(i, cols)
        out[r * h : (r + 1) * h, c * w : (c + 1) * w] = f
    return out


def preview(img: np.ndarray, scale: int = 4, bg=(40, 40, 40)) -> Image.Image:
    im = Image.fromarray(img, "RGBA")
    im = im.resize((im.width * scale, im.height * scale), Image.NEAREST)
    back = Image.new("RGBA", im.size, bg + (255,))
    back.paste(im, (0, 0), im)
    return back
