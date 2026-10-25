"""Mirror of the JUnit animation-shape checks for the generated basic-attack clips.

Replicates KeyframeAnimationPlayer sampling (per-tick keyframes, LINEAR easing: value at tick t + delta
is the lerp between keyframes t and t+1) and the joml matrix chains the tests build, so the clips can be
checked here before the GitHub Actions build runs the real tests.

Run from repo root:  python3 tools/anim/check_clips.py
"""
from __future__ import annotations
import json, math, sys
from pathlib import Path
import numpy as np
sys.path.insert(0, str(Path(__file__).parent))
from anim_math import part_rotation, HELD_ITEM, Ry, unit  # noqa

ROOT = Path(__file__).resolve().parents[2]
ANIM = ROOT / "src/client/resources/assets/killerranking/player_animations"

CH = ("x", "y", "z", "pitch", "yaw", "roll", "scaleX", "scaleY", "scaleZ")


class Clip:
    def __init__(self, name):
        self.doc = json.loads((ANIM / f"{name}.json").read_text(encoding="utf-8"))
        e = self.doc["emote"]
        self.stop = e["stopTick"]
        self.frames = {m["tick"]: m for m in e["moves"]}
        self.last = max(self.frames)

    def value(self, part, ch, tick, delta, default):
        def at(t):
            t = min(t, self.last)
            m = self.frames.get(t)
            if m is None or part not in m or ch not in m[part]:
                return None
            return float(m[part][ch])
        a, b = at(tick), at(tick + 1)
        if a is None and b is None:
            return default
        if a is None: a = default
        if b is None: b = a
        return a + (b - a) * delta

    def rot(self, part, tick, delta=0.0, default=(0, 0, 0)):
        return tuple(self.value(part, c, tick, delta, d) for c, d in zip(("pitch", "yaw", "roll"), default))

    def pos(self, part, tick, delta=0.0, default=(0, 0, 0)):
        return tuple(self.value(part, c, tick, delta, d) for c, d in zip(("x", "y", "z"), default))

    def scale(self, part, tick, delta=0.0, default=(1, 1, 1)):
        return tuple(self.value(part, c, tick, delta, d) for c, d in zip(("scaleX", "scaleY", "scaleZ"), default))


def blade(clip, tick, delta=0.0, sign=1.0):
    a = clip.rot("rightArm", tick, delta); g = clip.rot("rightItem", tick, delta)
    M = part_rotation(a[0], sign * a[1], sign * a[2]) @ HELD_ITEM @ part_rotation(g[0], sign * g[1], sign * g[2])
    return M


def palm_drift(clip, model_grip, tick, delta):
    """Distance of the handle's palm point from where the clip's final (rest) pose puts it."""
    r = clip.rot("rightItem", tick, delta); p = clip.pos("rightItem", tick, delta)
    q = part_rotation(*r) @ np.array(model_grip, dtype=float) + np.array(p)
    r0 = clip.rot("rightItem", clip.last); p0 = clip.pos("rightItem", clip.last)
    home = part_rotation(*r0) @ np.array(model_grip, dtype=float) + np.array(p0)
    return float(np.linalg.norm(q - home))


def check(name, I, kind, two=False):
    c = Clip(name)
    L = c.doc["emote"]["endTick"]
    fails = []
    def need(cond, msg):
        if not cond: fails.append(msg)
    base = (0.12, 0.23, 0.34)
    worst_jump = 0.0; gate_ticks = []; max_yaw = 0.0; yaw_w = yaw_e = 0.0; max_leg = 0.0
    for t in range(0, L + 1):
        for d in (0.0, 0.5):
            # head and walking pitch stay free
            for ch in ("pitch", "yaw", "roll", "x", "y", "z"):
                need(abs(c.value("head", ch, t, d, 0.12) - 0.12) < 1e-4, f"head {ch} touched @t{t}")
            for leg in ("leftLeg", "rightLeg"):
                need(abs(c.value(leg, "pitch", t, d, 0.34) - 0.34) < 1e-4, f"{leg} pitch replaced @t{t}")
                need(abs(c.value(leg, "x", t, d, 0.12) - 0.12) < 1e-4, f"{leg} moved @t{t}")
                sy = c.value(leg, "scaleY", t, d, 1.0); need(0.98 <= sy <= 1.0 + 1e-9, f"{leg} scale @t{t}")
                max_leg = max(max_leg, abs(c.value(leg, "yaw", t, d, 0)), abs(c.value(leg, "roll", t, d, 0)))
            need(palm_drift(c, (0, -2, 0), t, d) < 0.5, f"palm drift {palm_drift(c, (0, -2, 0), t, d):.2f} @t{t}+{d}")
            sc = c.scale("rightArm", t, d); need(abs(sc[1] - 1) < 1e-6, f"arm scale @t{t}")
            sp = c.pos("rightArm", t, d, (-5, 2, 0)); need(abs(sp[0] + 5) < 2 and abs(sp[1] - 2) < 1.5 and abs(sp[2]) <= 3.5, f"shoulder {sp} @t{t}")
            by = c.value("body", "yaw", t, d, 0); max_yaw = max(max_yaw, abs(by))
            need(abs(c.value("body", "pitch", t, d, 0)) < 0.3 and abs(by) < 0.8 and abs(c.value("body", "roll", t, d, 0)) < 0.15, f"body bounds @t{t}")
        # per-tick continuity of the weapon arm (the renderer lerps Euler angles, so a wrap jump reads as a spin)
        if t < L:
            a0 = c.rot("rightArm", t); a1 = c.rot("rightArm", t + 1); g0 = c.rot("rightItem", t); g1 = c.rot("rightItem", t + 1)
            # the player clamps read-back rotations to (-pi, pi], so compare the wrapped difference like the JUnit test
            j = max(abs((x - y + math.pi) % (2 * math.pi) - math.pi) for x, y in zip(a0 + g0, a1 + g1)); worst_jump = max(worst_jump, j)
        if c.value("nightBatTrail", "y", t, 0, 0) > 0.5: gate_ticks.append(t)
        if t == I - 2: yaw_w = c.value("body", "yaw", t, 0, 0)
        if t == I + 2: yaw_e = c.value("body", "yaw", t, 0, 0)
    need(worst_jump < 2.0, f"arm/grip jump {worst_jump:.2f} rad in one tick")
    need(gate_ticks == list(range(I - 3, I + 4)), f"trail gate ticks {gate_ticks}")
    need(max_leg <= 0.5, f"foot pivot {max_leg:.2f}")
    # rest at the end tick
    for part in ("body", "torso", "leftLeg", "rightLeg"):
        r = c.rot(part, L); need(all(abs(v) < 1e-3 for v in r), f"{part} not at rest @t{L} {r}")
    need(abs(c.value("rightArm", "scaleY", L, 0, 1) - 1) < 1e-3, "arm scale at end")
    # blade path
    bI = blade(c, I) @ np.array([0, 1, 0.0]); b_before = blade(c, I - 1) @ np.array([0, 1, 0.0]); b_after = blade(c, I + 2) @ np.array([0, 1, 0.0])
    if kind == "sweep":
        need(bI[2] < -0.9 and abs(bI[1]) < 0.2, f"impact dir {bI.round(2)}")
        need(b_before[0] * b_after[0] < -0.25, f"sweep across: before.x {b_before[0]:.2f} after.x {b_after[0]:.2f}")
        need(max_yaw > 0.3 and yaw_w * yaw_e < 0, f"hips turn with the swing: max {max_yaw:.2f}, W {yaw_w:.2f}, E {yaw_e:.2f}")
    elif kind == "cleave":
        need(bI[2] < -0.8 and 0.1 < bI[1] < 0.6, f"impact dir {bI.round(2)}")
        need(b_before[1] < -0.3 and b_after[1] > 0.4, f"overhead to low: before.y {b_before[1]:.2f} after.y {b_after[1]:.2f}")
    elif kind == "stab":
        need(bI[2] < -0.99 and abs(bI[1]) < 0.01, f"stab dir {bI.round(3)}")
    elif kind == "diag":
        need(bI[2] < -0.9, f"impact dir {bI.round(2)}")
        need(b_before[0] * b_after[0] < -0.25 and b_before[1] < 0 < b_after[1], f"diagonal: before {b_before.round(2)} after {b_after.round(2)}")
    return fails, dict(jump=worst_jump, yaw=max_yaw, leg=max_leg)


def hatchet_edge(name, left):
    c = Clip(name); sign = -1.0 if left else 1.0
    def M(t):
        return blade(c, t, 0.0, sign) @ Ry(math.pi / 2)
    I = CLIPS[name][0]
    before = M(I - 1) @ np.array([0, 1, 0.0]); after = M(I + 2) @ np.array([0, 1, 0.0]); edge = unit(M(I) @ np.array([1, 0, 0.0]))
    return float(np.dot(unit(after - before), edge))


CLIPS = {
    "night_bat_forehand": (10, "sweep"), "night_bat_backhand": (10, "sweep"), "night_bat_slam": (10, "cleave"),
    "eunbal_hatchet_forehand": (10, "sweep"), "eunbal_hatchet_backhand": (10, "sweep"), "eunbal_hatchet_cleave": (10, "cleave"),
    "gossi_knife_slash_right": (10, "sweep"), "gossi_knife_slash_left": (10, "sweep"), "gossi_knife_stab": (10, "stab"),
    "salmosa_dagger_forehand": (11, "sweep"), "salmosa_dagger_backhand": (11, "sweep"), "salmosa_dagger_stab": (11, "stab"),
    "yuhyeolmoki_saw_forehand": (14, "sweep"), "yuhyeolmoki_saw_backhand": (14, "sweep"), "yuhyeolmoki_saw_cleave": (14, "cleave"),
    "pawn6_katana_slash_right": (13, "sweep"), "pawn6_katana_slash_left": (13, "sweep"), "pawn6_katana_thrust": (13, "diag"),
}

if __name__ == "__main__":
    bad = 0
    for name, (I, kind) in CLIPS.items():
        fails, info = check(name, I, kind)
        status = "ok " if not fails else "FAIL"
        print(f"{status} {name:26s} jump {info['jump']:.2f} yaw {info['yaw']:.2f} foot {info['leg']:.2f}" + ("" if not fails else "\n      " + "\n      ".join(fails[:6])))
        bad += bool(fails)
    for name in ("eunbal_hatchet_forehand", "eunbal_hatchet_backhand", "eunbal_hatchet_cleave"):
        for left in (False, True):
            print(f"edge {name:26s} left={left!s:5s} dot {hatchet_edge(name, left):+.2f}")
    sys.exit(1 if bad else 0)
