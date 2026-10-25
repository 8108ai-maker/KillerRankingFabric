"""Procedural basic-attack clips in the reference style: big wind-up, 4-tick whip, held follow-through.

Coordinates (raw model space, as NightBatAnimationTest measures the blade): x+ = player's left,
y+ = DOWN (the renderer flips Y), z- = forward.

Each swing is authored on the ARM's raw Euler channels (the same pitch/yaw/roll the renderer lerps between
keyframes, so what is designed is exactly what plays): the impact pose is solved so the blade points along the
swing's impact direction with the arm extended a little below the blade line, and the wind-up and follow-through
poses are yaw/pitch offsets from it.  One fixed wrist bend (the strike grip) is derived at impact so the blade's
flat faces the direction of travel - the cutting edge leads the cut - and the arm carries the blade rigidly
through the whip.  The wrist eases from the weapon's authored rest grip into that strike grip during the wind-up
and back during the recovery; the item is translated so the palm point never leaves the hand.

Run from repo root:  python3 tools/anim/build_swings.py [clip-prefix ...]   (writes player_animations/*.json)
"""
from __future__ import annotations
import json, math, sys
from pathlib import Path
import numpy as np
sys.path.insert(0, str(Path(__file__).parent))
from anim_math import *  # noqa

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/client/resources/assets/killerranking/player_animations"

# ---------------------------------------------------------------- swing shapes (right hand)
# M: blade direction at impact.  tilt: how far the arm sits below the blade line at impact (radians; negative =
# blade dips below the arm).  W/E: (pitch, yaw) offsets of the arm from the impact pose at wind-up / end
# (arm yaw + = swept toward the player's right).  yaw/pitch/roll: body channels at W / M / E (+yaw = turned to
# the right side).  edge_hint: direction the flat faces when the path is too short to define travel (stabs).
SWINGS = {
    "sweep_rl": dict(M=(0.0, -0.05, -1.0), tilt=0.60, W=(-0.25, 2.05), E=(0.12, -2.10),
                     yaw=(0.50, -0.10, -0.62), pitch=(-0.04, 0.10, 0.08), roll=(0.04, 0.0, -0.05)),
    "sweep_lr": dict(M=(0.0, -0.05, -1.0), tilt=0.60, W=(-0.25, -2.05), E=(0.12, 2.10),
                     yaw=(-0.50, 0.10, 0.62), pitch=(-0.04, 0.10, 0.08), roll=(-0.04, 0.0, 0.05)),
    "cleave":   dict(M=(0.05, 0.30, -0.95), tilt=-0.12, W=(-1.95, 0.28), E=(0.65, -0.12),
                     yaw=(0.22, 0.0, -0.12), pitch=(-0.10, 0.14, 0.20), roll=(0.0, 0.0, 0.0)),
    "diag_rl":  dict(M=(0.05, 0.15, -0.99), tilt=0.35, W=(-0.95, 1.80), E=(0.70, -1.80),
                     yaw=(0.45, -0.05, -0.50), pitch=(-0.06, 0.12, 0.16), roll=(0.03, 0.0, -0.04)),
    "stab":     dict(M=(0.0, 0.0, -1.0), tilt=0.22, W=(0.28, 0.35), E=(-0.04, -0.05),
                     yaw=(0.32, -0.22, -0.20), pitch=(-0.03, 0.14, 0.10), roll=(0.0, 0.0, 0.0),
                     edge_hint=(1.0, 0.0, 0.0)),
}
# shoulder (absolute right-arm pivot; default -5,2,0) at W / M / E
SHOULDER = {
    "default": ((-5.6, 1.3, 1.6), (-4.4, 2.4, -1.6), (-4.0, 2.6, -2.2)),
    "stab":    ((-5.2, 2.0, 2.8), (-4.6, 2.2, -3.2), (-4.6, 2.2, -2.6)),
    "cleave":  ((-5.4, 1.0, 1.2), (-4.6, 2.5, -1.8), (-4.3, 2.8, -2.6)),
}

# ---------------------------------------------------------------- weapons
# clips: clip -> swing; length/stop: clip ticks; upswing: Better Combat upswing fraction (impact tick = length*upswing);
# grip: authored rest rightItem; two: two-handed; rest: authored rest rightArm rotation;
# edge: item-space direction the blade's flat faces (flat blade quads sit in the model XY plane; models displayed
# with rotation [0,90,0] put model +X at item -Z).  Round weapons use the same virtual edge so the arm spin matches.
FLAT_EDGE = (0.0, 0.0, -1.0)
WEAPONS = {
    "night_bat": dict(clips={"night_bat_forehand": "sweep_rl", "night_bat_backhand": "sweep_lr", "night_bat_slam": "cleave"},
                      length=20, stop=26, upswing=0.5, grip=dict(x=0, y=0, z=0, pitch=0, yaw=0, roll=0), two=False,
                      rest=dict(pitch=-0.35, yaw=0, roll=-0.15), heavy=True, edge=FLAT_EDGE),
    "eunbal_hatchet": dict(clips={"eunbal_hatchet_forehand": "sweep_rl", "eunbal_hatchet_backhand": "sweep_lr", "eunbal_hatchet_cleave": "cleave"},
                      length=20, stop=26, upswing=0.5, grip=dict(x=0, y=0, z=0, pitch=0, yaw=0, roll=0), two=False,
                      rest=dict(pitch=-0.35, yaw=0, roll=-0.15), heavy=True, edge=FLAT_EDGE),
    "gossi_knife": dict(clips={"gossi_knife_slash_right": "sweep_rl", "gossi_knife_slash_left": "sweep_lr", "gossi_knife_stab": "stab"},
                      length=20, stop=26, upswing=0.5, grip=dict(x=0, y=0, z=0, pitch=0, yaw=0, roll=0), two=False,
                      rest=dict(pitch=-0.35, yaw=0, roll=-0.06), heavy=False, edge=FLAT_EDGE),
    "salmosa_dagger": dict(clips={"salmosa_dagger_forehand": "sweep_rl", "salmosa_dagger_backhand": "sweep_lr", "salmosa_dagger_stab": "stab"},
                      length=24, stop=28, upswing=0.46, grip=dict(x=0, y=0, z=0, pitch=0, yaw=0, roll=0), two=False,
                      rest=dict(pitch=-0.35, yaw=0, roll=-0.15), heavy=False, edge=FLAT_EDGE),
    "yuhyeolmoki_saw": dict(clips={"yuhyeolmoki_saw_forehand": "sweep_rl", "yuhyeolmoki_saw_backhand": "sweep_lr", "yuhyeolmoki_saw_cleave": "cleave"},
                      length=28, stop=34, upswing=0.5, grip=dict(x=0, y=0, z=0, pitch=0, yaw=0, roll=0), two=False,
                      rest=dict(pitch=-0.35, yaw=0, roll=-0.15), heavy=True, edge=FLAT_EDGE),
    "pawn6_katana": dict(clips={"pawn6_katana_slash_right": "sweep_rl", "pawn6_katana_slash_left": "sweep_lr", "pawn6_katana_thrust": "diag_rl"},
                      length=26, stop=26, upswing=0.5,
                      grip=dict(x=-0.283482, y=-0.055552, z=0.372504, pitch=0.278028, yaw=0.824924, roll=0.351418), two=True,
                      rest=dict(pitch=-0.794396, yaw=-0.426953, roll=-0.38628), heavy=False, edge=(1.0, 0.0, 0.0)),
}

YHAT = np.array([0.0, 1.0, 0.0])
DOWN = np.array([0.0, 1.0, 0.0])
SUB = 4  # pose samples per tick
STRIKE_ROLL = -0.15  # arm roll held through every strike (the yaw axis tilts 8.6 deg, a natural slight diagonal)


def key_times(length, upswing, heavy):
    I = int(round(length * upswing))
    # wind-up ends, hold ends, impact, strike ends, follow-through ends (then recovery to `length`)
    return dict(I=I, w_end=I - (5 if heavy else 4), h_end=I - 2, s_end=I + 2, f_end=I + (6 if heavy else 5), L=length)


def lerp(a, b, u): return a + (b - a) * u


def phase(t, k):
    """(segment, u): which part of the swing tick t is in and the eased progress through it."""
    if t <= k["w_end"]:
        return "windup", smoothstep(t / max(1, k["w_end"]))
    if t <= k["h_end"]:
        return "hold_w", 1.0
    if t <= k["I"]:
        u = (t - k["h_end"]) / max(1, k["I"] - k["h_end"]); return "strike1", ease_in(u) * 0.55 + u * 0.45
    if t <= k["s_end"]:
        u = (t - k["I"]) / max(1, k["s_end"] - k["I"]); return "strike2", ease_out(u)
    if t <= k["f_end"]:
        return "hold_e", 1.0
    return "recover", smoothstep((t - k["f_end"]) / max(1, k["L"] - k["f_end"]))


def phase_value(t, k, vW, vM, vE, vRest):
    seg, u = phase(t, k)
    return {"windup": lambda: lerp(vRest, vW, u), "hold_w": lambda: vW, "strike1": lambda: lerp(vW, vM, u),
            "strike2": lambda: lerp(vM, vE, u), "hold_e": lambda: vE, "recover": lambda: lerp(vE, vRest, u)}[seg]()


def frame(blade, edge_dir, edge_item):
    """Item orientation whose +Y is `blade` and whose `edge_item` axis faces `edge_dir` (orthogonalised)."""
    d = unit(blade)
    g = np.asarray(edge_dir, dtype=float); g = g - np.dot(g, d) * d; g = unit(g)
    n = np.cross(g, d)
    u1 = unit(edge_item); u2 = YHAT; u3 = np.cross(u1, u2)
    return np.column_stack([g, d, n]) @ np.column_stack([u1, u2, u3]).T


def tangent_at(a, b):
    """Unit tangent at direction a of the great-circle arc from a toward b."""
    a = unit(a); b = unit(b); t = b - np.dot(b, a) * a
    return unit(t) if np.linalg.norm(t) > 1e-6 else None


def r6(v): return float(round(float(v), 6))


def build_clip(name, swing_name, w):
    sw = SWINGS[swing_name]
    k = key_times(w["length"], w["upswing"], w["heavy"])
    grip = w["grip"]; grot = (grip["pitch"], grip["yaw"], grip["roll"])
    rest_rot = (w["rest"]["pitch"], w["rest"]["yaw"], w["rest"]["roll"])
    A_rest = part_rotation(*rest_rot); G_rest = part_rotation(*grot)
    edge_item = unit(w["edge"])
    M = unit(sw["M"])
    # impact arm pose: extends `tilt` below the blade with the authored roll; strike grip makes the flat face travel
    pM, yM, rM = solve_arm_axis(tilt_toward(M, DOWN, sw["tilt"]), STRIKE_ROLL, (rest_rot[0], rest_rot[1]))
    A_M = part_rotation(pM, yM, rM)
    arm_W = (pM + sw["W"][0], yM + sw["W"][1], rM)
    arm_E = (pM + sw["E"][0], yM + sw["E"][1], rM)
    # direction of travel at impact: where the arm's yaw/pitch change carries the (rigidly attached) blade
    dW = np.array([sw["E"][0] - sw["W"][0], sw["E"][1] - sw["W"][1], 0.0]) * 0.02
    A_plus = part_rotation(pM + dW[0], yM + dW[1], rM); A_minus = part_rotation(pM - dW[0], yM - dW[1], rM)
    travel = (A_plus @ A_M.T @ M) - (A_minus @ A_M.T @ M)
    travel = travel if np.linalg.norm(travel) > 1e-4 else np.asarray(sw.get("edge_hint", (1.0, 0.0, 0.0)), dtype=float)
    F_M = frame(M, travel, edge_item)
    G_strike = HELD_ITEM.T @ A_M.T @ F_M     # item rotation inside the arm at impact
    grip_strike = nearest_euler(G_strike, grot)  # wrist channels, on the branch closest to the authored rest grip
    sh = SHOULDER["stab" if swing_name == "stab" else "cleave" if swing_name == "cleave" else "default"]
    rest_sh = (-5.0, 2.0, 0.0) if not w["two"] else (-5.333415, 1.579263, 0.20806)

    def pose(t):
        """(arm euler, grip euler) at a possibly fractional tick, strike phases only (raw channels, eased)."""
        seg, u = phase(t, k)
        if seg == "windup":
            return tuple(lerp(a, b, u) for a, b in zip(rest_rot, arm_W)), tuple(lerp(a, b, u) for a, b in zip(grot, grip_strike))
        if seg == "hold_w":
            return arm_W, grip_strike
        if seg == "strike1":
            return tuple(lerp(a, b, u) for a, b in zip(arm_W, (pM, yM, rM))), grip_strike
        if seg == "strike2":
            return tuple(lerp(a, b, u) for a, b in zip((pM, yM, rM), arm_E)), grip_strike
        return arm_E, grip_strike

    arm_e, grip_e, mids = [], [], []
    pa, pg = rest_rot, grot
    hold_a = hold_g = None
    worst_mid = 0.0
    for i in range(0, w["length"] * SUB + 1):
        t = i / SUB
        seg, u = phase(t, k)
        if seg != "recover":
            pa, pg = pose(t)
            hold_a, hold_g = pa, pg
            A, G = part_rotation(*pa), part_rotation(*pg)
        else:
            # recovery: the raw channels ease from the follow-through numbers straight onto the authored rest
            # numbers, which keeps the end exact and the motion continuous (the weapon comes back in across the front)
            pa = tuple(lerp(a, b, u) for a, b in zip(hold_a, rest_rot))
            pg = tuple(lerp(a, b, u) for a, b in zip(hold_g, grot))
            A, G = part_rotation(*pa), part_rotation(*pg)
        if i % SUB == 0:
            arm_e.append(pa); grip_e.append(pg)
        elif i % SUB == SUB // 2 and seg != "recover":
            mids.append((int(t), A, G))
    # how far the renderer's mid-tick Euler lerp strays from the great-circle between the two keyframe blades
    for t, A, G in mids:
        a0, a1 = arm_e[t], arm_e[t + 1]; g0, g1 = grip_e[t], grip_e[t + 1]
        am = part_rotation(*[(x + y) / 2 for x, y in zip(a0, a1)]); gm = part_rotation(*[(x + y) / 2 for x, y in zip(g0, g1)])
        b0 = unit(part_rotation(*a0) @ HELD_ITEM @ part_rotation(*g0) @ YHAT); b1 = unit(part_rotation(*a1) @ HELD_ITEM @ part_rotation(*g1) @ YHAT)
        b_lerp = unit(am @ HELD_ITEM @ gm @ YHAT)
        worst_mid = max(worst_mid, math.degrees(math.acos(max(-1.0, min(1.0, float(np.dot(slerp(b0, b1, 0.5), b_lerp)))))))
    span_a = max(abs(a - b) for a, b in zip(hold_a, rest_rot)); span_g = max(abs(a - b) for a, b in zip(hold_g, grot))
    notes = []
    if worst_mid > 6: notes.append(f"mid-tick blade error {worst_mid:.1f} deg")
    if span_a > 3.0 or span_g > 3.0: notes.append(f"long recovery spin arm {span_a:.2f} grip {span_g:.2f} rad")
    for i, (e_, ref) in enumerate(((arm_e[-1], rest_rot), (grip_e[-1], grot))):
        if max(abs(a - b) for a, b in zip(e_, ref)) > 1e-3: notes.append(f"{'arm' if i == 0 else 'grip'} ends off rest {e_}")
    if notes: print(f"  ! {name}: " + "; ".join(notes))

    h = np.array([0.0, -2.0, 0.0])
    moves = []
    for t in range(0, w["length"] + 1):
        p, y, r = arm_e[t]; gp, gy, gr = grip_e[t]
        goff = G_rest @ h - part_rotation(gp, gy, gr) @ h   # palm point stays where the rest pose puts it
        byaw = phase_value(t, k, *sw["yaw"], 0.0)
        bpitch = phase_value(t, k, *sw["pitch"], 0.0)
        broll = phase_value(t, k, *sw["roll"], 0.0)
        sx = phase_value(t, k, sh[0][0], sh[1][0], sh[2][0], rest_sh[0])
        sy = phase_value(t, k, sh[0][1], sh[1][1], sh[2][1], rest_sh[1])
        sz = phase_value(t, k, sh[0][2], sh[1][2], sh[2][2], rest_sh[2])
        gate = 1 if k["I"] - 3 <= t <= k["I"] + 3 else 0
        move = {"tick": t, "easing": "LINEAR",
                "rightArm": {"x": r6(sx), "y": r6(sy), "z": r6(sz), "pitch": r6(p), "yaw": r6(y), "roll": r6(r), "scaleX": 1, "scaleY": 1, "scaleZ": 1},
                "rightItem": {"x": r6(grip["x"] + goff[0]), "y": r6(grip["y"] + goff[1]), "z": r6(grip["z"] + goff[2]), "pitch": r6(gp), "yaw": r6(gy), "roll": r6(gr), "scaleX": 1, "scaleY": 1, "scaleZ": 1},
                "body": {"x": 0.0, "y": 0.0, "z": 0.0, "pitch": r6(bpitch), "yaw": r6(byaw), "roll": r6(broll)},
                "torso": {"pitch": r6(bpitch * 0.3), "yaw": r6(byaw * 0.25), "roll": 0.0},
                # `body` turns the whole model about the hips, so the legs counter-turn to keep the feet planted
                "rightLeg": {"yaw": r6(-byaw * 0.7), "roll": r6(-broll * 0.5), "scaleY": 1},
                "leftLeg": {"yaw": r6(-byaw * 0.75), "roll": r6(-broll * 0.5), "scaleY": 1},
                "nightBatTrail": {"x": 0, "y": gate, "z": 0}}
        if w["two"]:
            # second hand rides the same hilt: parallel arm from the left shoulder, pulled across toward the right hand
            cross = phase_value(t, k, 2.6, 2.2, 2.4, 4.070605)
            move["leftArm"] = {"x": r6(cross), "y": r6(sy + 0.3), "z": r6(sz + 0.2), "pitch": r6(p), "yaw": r6(y), "roll": r6(r), "scaleX": 1, "scaleY": 1, "scaleZ": 1}
        else:
            # free arm counterbalances: swings opposite to the body turn
            move["leftArm"] = {"x": 5.0, "y": 2.0, "z": r6(-sz * 0.25), "pitch": r6(phase_value(t, k, -0.35, -0.75, -0.30, -0.15)),
                               "yaw": r6(-byaw * 0.5), "roll": r6(phase_value(t, k, 0.18, 0.30, 0.22, 0.06)), "scaleX": 1, "scaleY": 1, "scaleZ": 1}
        moves.append(move)
    # settle: identical rest frames up to the stop tick so the layer releases cleanly
    for t in range(w["length"] + 1, w["stop"] + 1):
        m = json.loads(json.dumps(moves[-1])); m["tick"] = t; moves.append(m)
    doc = {"version": 3, "name": name, "author": "Killer Ranking",
           "description": f"Reference-style {swing_name}: large wind-up behind the body, four-tick whip through the front with the hips and feet turning, held follow-through, then settle. Edge leads the cut, arm rides the blade frame, hilt fixed in the palm.",
           "emote": {"isLoop": "false", "returnTick": 0, "beginTick": 0, "endTick": w["length"], "stopTick": w["stop"], "degrees": False, "moves": moves}}
    return doc, k


def verify(doc, k, w, name):
    rows = []
    for m in doc["emote"]["moves"][: w["length"] + 1]:
        a = m["rightArm"]; g = m["rightItem"]; d = blade_dir((a["pitch"], a["yaw"], a["roll"]), (g["pitch"], g["yaw"], g["roll"]))
        rows.append((m["tick"], d, m["body"]["yaw"], m["nightBatTrail"]["y"]))
    I = k["I"]
    dI = [r for r in rows if r[0] == I][0][1]
    before = [r for r in rows if r[0] == I - 2][0][1]; after = [r for r in rows if r[0] == I + 2][0][1]
    print(f"{name:28s} I={I:2d} impact dir=({dI[0]:+.2f},{dI[1]:+.2f},{dI[2]:+.2f}) before=({before[0]:+.2f},{before[1]:+.2f},{before[2]:+.2f}) after=({after[0]:+.2f},{after[1]:+.2f},{after[2]:+.2f})")
    return rows


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    only = sys.argv[1:]
    for wname, w in WEAPONS.items():
        for clip, swing in w["clips"].items():
            if only and not any(clip.startswith(o) for o in only):
                continue
            doc, k = build_clip(clip, swing, w)
            (OUT / f"{clip}.json").write_text(json.dumps(doc, ensure_ascii=False), encoding="utf-8")
            verify(doc, k, w, clip)


if __name__ == "__main__":
    main()
