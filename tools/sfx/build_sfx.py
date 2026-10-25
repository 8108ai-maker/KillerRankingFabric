"""Builds every Killer Ranking sound as OGG and writes sounds.json + subtitle lang keys.

Run from the repo root:  python3 tools/sfx/build_sfx.py
Requires numpy, scipy, ffmpeg on PATH.
"""
from __future__ import annotations

import json
import os
import subprocess
import sys
import tempfile
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).parent))
from synth import *  # noqa: E402,F401,F403

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/client/resources/assets/killerranking/sounds"
SOUNDS_JSON = ROOT / "src/client/resources/assets/killerranking/sounds.json"
LANG_DIR = ROOT / "src/main/resources/assets/killerranking/lang"

# name -> (builder, variant count, subtitle_ko, subtitle_en, attenuation)
CATALOG: dict[str, tuple] = {}


def sound(name, variants=1, ko="", en="", dist=16):
    def deco(fn):
        CATALOG[name] = (fn, variants, ko, en, dist)
        return fn
    return deco


# ============================================================ weapon swings
@sound("swing.katana", 3, "카타나 휘두름", "Katana swing")
def swing_katana(v):
    d = 0.24
    body = whoosh(d, 1600, 5200, q=1.3, peak=0.42, sharp=1.8, seed=10 + v, gain=0.9)
    edge = whoosh(d, 4000, 9000, q=2.2, peak=0.5, sharp=2.6, seed=20 + v, gain=0.45)
    sh = shimmer(0.16, 5200 + 300 * v, 7600, gain=0.18, decay=16)
    return master(mix(body, edge, sh, offsets=[0, 0.01, 0.07]))


@sound("swing.saw", 3, "톱칼 휘두름", "Saw knife swing")
def swing_saw(v):
    d = 0.3
    body = whoosh(d, 900, 3200, q=1.1, peak=0.45, sharp=1.5, seed=30 + v)
    buzz = amod(whoosh(d, 1500, 4200, q=1.6, peak=0.45, sharp=1.6, seed=40 + v, gain=0.7), 85 + 6 * v, 0.9)
    grit = crackle(d, 900, lo=1800, hi=7000, seed=50 + v, gain=0.25) * env_hump(d, 0.45, 1.6)
    return master(mix(body, buzz, grit))


@sound("swing.dagger", 3, "단검 휘두름", "Dagger swing")
def swing_dagger(v):
    d = 0.15
    body = whoosh(d, 2400, 6500, q=1.5, peak=0.4, sharp=2.2, seed=60 + v)
    sh = shimmer(0.09, 6500 + 400 * v, 8800, gain=0.14, decay=28)
    return master(mix(body, sh, offsets=[0, 0.04]))


@sound("swing.hatchet", 3, "손도끼 휘두름", "Hatchet swing")
def swing_hatchet(v):
    d = 0.32
    body = whoosh(d, 380, 1900, q=1.1, peak=0.5, sharp=1.5, seed=70 + v)
    air = whoosh(d, 1200, 3600, q=1.4, peak=0.55, sharp=2.0, seed=80 + v, gain=0.5)
    return master(mix(body, air))


@sound("swing.bat", 3, "방망이 휘두름", "Bat swing")
def swing_bat(v):
    d = 0.36
    body = whoosh(d, 220, 1300, q=1.0, peak=0.52, sharp=1.4, seed=90 + v)
    air = whoosh(d, 800, 2600, q=1.3, peak=0.55, sharp=1.8, seed=100 + v, gain=0.45)
    wire = crackle(d, 300, lo=3000, hi=9000, seed=110 + v, gain=0.12) * env_hump(d, 0.55, 2)
    return master(mix(body, air, wire))


@sound("swing.fist", 3, "주먹 휘두름", "Fist swing")
def swing_fist(v):
    d = 0.16
    body = whoosh(d, 450, 2300, q=1.2, peak=0.45, sharp=2.0, seed=120 + v)
    puff = thump(0.08, 160, 90, decay=40, gain=0.25)
    return master(mix(body, puff, offsets=[0, 0.05]))


@sound("swing.knuckle", 3, "너클 휘두름", "Knuckle swing")
def swing_knuckle(v):
    d = 0.2
    body = whoosh(d, 320, 1800, q=1.1, peak=0.5, sharp=1.7, seed=130 + v)
    ring = metal(0.1, [2300, 3900], [60, 70], gain=0.07, seed=v)
    return master(mix(body, ring, offsets=[0, 0.06]))


# ============================================================ weapon hits
@sound("hit.blade", 3, "베임", "Blade hit")
def hit_blade(v):
    c = click(0.014, lo=2500, hi=10000, seed=200 + v, gain=1.0)
    cut = bandpass(noise(0.13, 210 + v), 1800, 6500) * env_exp(0.13, 32) * 0.9
    wet = bandpass(noise(0.2, 220 + v), 250, 1100) * env_exp(0.2, 22) * 0.7
    ring = metal(0.18, [3100, 5200], [40, 55], gain=0.12, seed=v)
    return master(mix(c, cut, wet, ring, offsets=[0, 0.002, 0.01, 0.004]))


@sound("hit.saw", 3, "찢김", "Saw hit")
def hit_saw(v):
    c = click(0.012, lo=1500, hi=8000, seed=230 + v)
    tear = amod(bandpass(noise(0.26, 240 + v), 500, 3200) * env_exp(0.26, 13), 62 + 5 * v, 0.85)
    grit = crackle(0.26, 1400, lo=1200, hi=7000, seed=250 + v, gain=0.35) * env_exp(0.26, 14)
    wet = bandpass(noise(0.2, 260 + v), 200, 900) * env_exp(0.2, 20) * 0.6
    return master(mix(c, tear, grit, wet))


@sound("hit.blunt", 3, "둔기 타격", "Blunt hit")
def hit_blunt(v):
    th = thump(0.26, 95 + 5 * v, 42, decay=15, gain=1.0, drive=2.2)
    knock = metal(0.09, [640, 1180], [70, 90], gain=0.35, seed=v)
    burst = bandpass(noise(0.07, 270 + v), 300, 2600) * env_exp(0.07, 60) * 0.8
    return master(mix(th, knock, burst))


@sound("hit.fist", 3, "주먹 타격", "Punch hit")
def hit_fist(v):
    th = thump(0.18, 125 + 6 * v, 58, decay=22, gain=1.0, drive=2.0)
    burst = bandpass(noise(0.05, 280 + v), 400, 3000) * env_exp(0.05, 80) * 0.7
    return master(mix(th, burst))


@sound("hit.hatchet", 3, "도끼 찍힘", "Hatchet chop")
def hit_hatchet(v):
    c = click(0.016, lo=1200, hi=7000, seed=290 + v, gain=1.0)
    th = thump(0.2, 150 + 8 * v, 65, decay=20, gain=0.9, drive=2.0)
    crunch = crackle(0.14, 2200, lo=700, hi=5000, seed=300 + v, gain=0.5) * env_exp(0.14, 28)
    wet = bandpass(noise(0.18, 310 + v), 250, 1000) * env_exp(0.18, 24) * 0.5
    return master(mix(c, th, crunch, wet))


@sound("hit.pierce", 2, "찔림", "Pierce hit")
def hit_pierce(v):
    c = click(0.01, lo=3000, hi=11000, seed=320 + v)
    sh = shimmer(0.12, 4200, 2600, gain=0.25, decay=24)
    wet = bandpass(noise(0.16, 330 + v), 300, 1400) * env_exp(0.16, 26) * 0.7
    return master(mix(c, sh, wet, offsets=[0, 0.004, 0.008]))


@sound("hit.crit", 2, "치명타", "Critical hit")
def hit_crit(v):
    c = click(0.014, lo=2000, hi=10000, seed=340 + v)
    th = thump(0.22, 110, 50, decay=16, gain=0.8, drive=2.4)
    ring = metal(0.3, [2600, 4100, 6800], [18, 24, 30], gain=0.25, seed=v)
    sh = shimmer(0.25, 3000, 9000, gain=0.2, decay=10)
    return master(mix(c, th, ring, sh))


# ============================================================ parry / guard
@sound("parry.success", 2, "패링 성공", "Parry")
def parry_success(v):
    c = click(0.012, lo=2500, hi=12000, seed=400 + v, gain=1.2)
    clang = metal(0.55, [2080, 3390, 5260, 7880, 10400], [9, 11, 14, 18, 22], gain=1.0, detune=0.01, seed=v)
    spark = crackle(0.3, 600, lo=4000, hi=12000, seed=410 + v, gain=0.35) * env_exp(0.3, 12)
    rise = shimmer(0.35, 2400, 6800, gain=0.22, decay=7)
    out = mix(c, clang, spark, rise, offsets=[0, 0.003, 0.01, 0.02])
    return master(reverb(out, 0.1, 0.12, seed=v))


@sound("parry.guard", 2, "가드", "Guard raise")
def parry_guard(v):
    slide = whoosh(0.14, 2500, 5500, q=1.8, peak=0.35, sharp=2.2, seed=420 + v, gain=0.7)
    tick = metal(0.12, [3600, 5900], [50, 60], gain=0.25, seed=v)
    return master(mix(slide, tick, offsets=[0, 0.06]))


@sound("parry.fail", 1, "패링 실패", "Parry miss")
def parry_fail(v):
    dull = thump(0.14, 240, 120, decay=30, gain=0.6, drive=1.5)
    air = whoosh(0.16, 600, 1500, q=1.2, peak=0.4, sharp=2.0, seed=430, gain=0.5)
    return master(mix(dull, air))


@sound("parry.auto", 2, "자동 패링", "Auto parry")
def parry_auto(v):
    clang = metal(0.4, [2500, 4200, 6400, 9100], [12, 15, 19, 24], gain=0.9, detune=0.008, seed=5 + v)
    sh = shimmer(0.3, 5000, 8500, gain=0.25, decay=10)
    echo = delay(clang * 0.5, 0.05, 0.4, 0.5, taps=2)
    return master(mix(clang, sh, echo, offsets=[0, 0.01, 0.0]))


# ============================================================ neungsa (katana skills)
@sound("neungsa.draw", 2, "발도", "Blade draw")
def neungsa_draw(v):
    slide = whoosh(0.3, 1800, 7000, q=1.6, peak=0.6, sharp=1.6, seed=500 + v, gain=0.8)
    sh = shimmer(0.4, 3800, 9500, gain=0.5, decay=7)
    ring = metal(0.5, [4100, 6600, 9900], [10, 13, 17], gain=0.35, seed=v)
    return master(mix(slide, sh, ring, offsets=[0, 0.12, 0.2]))


@sound("neungsa.sheathe", 1, "납도", "Sheathe")
def neungsa_sheathe(v):
    slide = whoosh(0.22, 5000, 1500, q=1.6, peak=0.45, sharp=1.6, seed=510, gain=0.7)
    clack = mix(click(0.01, lo=800, hi=4000, seed=511, gain=0.8), metal(0.12, [1500, 2300], [45, 60], gain=0.4))
    return master(mix(slide, clack, offsets=[0, 0.2]))


@sound("neungsa.charge", 1, "발도술 충전", "Iaido charge", dist=12)
def neungsa_charge(v):
    d = 1.0
    hum = sine(sweep(180, 520, d, curve=1.6), d) * env_adsr(d, 0.1, 0.2, 0.8, 0.15) * 0.5
    air = whoosh(d, 800, 5200, q=1.2, peak=0.9, sharp=1.2, seed=520, gain=0.6)
    tremor = amod(sine(sweep(60, 110, d), d), 14, 0.6) * env_adsr(d, 0.2, 0.2, 0.7, 0.1) * 0.3
    tick = shimmer(0.25, 6000, 9000, gain=0.25, decay=10)
    return master(mix(hum, air, tremor, tick, offsets=[0, 0, 0, 0.78]))


@sound("neungsa.iaido", 1, "발도술", "Iaido strike")
def neungsa_iaido(v):
    dash = whoosh(0.18, 600, 3000, q=1.1, peak=0.35, sharp=2.2, seed=530, gain=0.7)
    cut = whoosh(0.22, 2500, 9000, q=1.4, peak=0.3, sharp=2.4, seed=531, gain=1.0)
    sh = shimmer(0.5, 6500, 11000, gain=0.45, decay=6)
    ring = metal(0.6, [3200, 5100, 8200], [8, 10, 13], gain=0.3, seed=3)
    return master(reverb(mix(dash, cut, sh, ring, offsets=[0, 0.1, 0.12, 0.13]), 0.14, 0.15))


@sound("neungsa.echo_cut", 4, "잔상 베기", "Echo cut")
def neungsa_echo_cut(v):
    cut = whoosh(0.13, 2800 + 400 * v, 8500, q=1.6, peak=0.35, sharp=2.4, seed=540 + v, gain=0.9)
    sh = shimmer(0.1, 6000 + 500 * v, 9000, gain=0.2, decay=26)
    return master(mix(cut, sh, offsets=[0, 0.03]))


@sound("neungsa.big_cut", 1, "대참격", "Great cut")
def neungsa_big_cut(v):
    body = whoosh(0.4, 900, 6000, q=1.2, peak=0.4, sharp=1.6, seed=550, gain=1.0)
    sh = shimmer(0.5, 4500, 10500, gain=0.5, decay=6)
    boom = thump(0.3, 120, 55, decay=14, gain=0.5, drive=2)
    ring = metal(0.7, [2900, 4700, 7600], [7, 9, 12], gain=0.3, seed=4)
    return master(reverb(mix(body, sh, boom, ring, offsets=[0, 0.1, 0.13, 0.14]), 0.16, 0.16))


@sound("neungsa.slash_cast", 1, "무형참격", "Formless slash")
def neungsa_slash_cast(v):
    pre = whoosh(0.25, 300, 2500, q=1.2, peak=0.85, sharp=1.4, seed=560, gain=0.6)
    cuts = [whoosh(0.1, 3000 + 500 * i, 9000, q=1.6, peak=0.3, sharp=2.5, seed=570 + i, gain=0.7) for i in range(4)]
    sh = shimmer(0.4, 7000, 12000, gain=0.35, decay=8)
    return master(mix(pre, *cuts, sh, offsets=[0, 0.22, 0.29, 0.36, 0.43, 0.25]))


# ============================================================ tteokbal / rush
@sound("rush.start", 1, "연타 시작", "Rush start")
def rush_start(v):
    inhale = whoosh(0.3, 400, 2000, q=1.2, peak=0.8, sharp=1.3, seed=600, gain=0.7)
    stomp = thump(0.2, 90, 45, decay=18, gain=0.7, drive=2)
    return master(mix(inhale, stomp, offsets=[0, 0.22]))


@sound("rush.punch", 4, "연타", "Rush punch")
def rush_punch(v):
    air = whoosh(0.09, 500 + 80 * v, 2600, q=1.3, peak=0.4, sharp=2.4, seed=610 + v, gain=0.7)
    th = thump(0.12, 130 + 10 * v, 60, decay=30, gain=0.9, drive=2.2)
    return master(mix(air, th, offsets=[0, 0.03]))


@sound("rush.finisher", 1, "연타 마무리", "Rush finisher")
def rush_finisher(v):
    air = whoosh(0.16, 350, 2400, q=1.2, peak=0.4, sharp=2.0, seed=620, gain=0.8)
    th = thump(0.32, 110, 40, decay=11, gain=1.0, drive=2.6)
    burst = bandpass(noise(0.08, 621), 300, 2800) * env_exp(0.08, 50) * 0.8
    return master(reverb(mix(air, th, burst, offsets=[0, 0.05, 0.05]), 0.12, 0.14))


@sound("heavy_punch.charge", 1, "강타 충전", "Heavy punch charge", dist=12)
def heavy_punch_charge(v):
    d = 0.7
    hum = sine(sweep(70, 160, d, curve=1.3), d) * env_adsr(d, 0.08, 0.2, 0.8, 0.1) * 0.6
    crack = crackle(d, 160, lo=500, hi=3000, seed=630, gain=0.3) * env_adsr(d, 0.2, 0.2, 0.9, 0.1)
    air = whoosh(d, 300, 1800, q=1.2, peak=0.9, sharp=1.3, seed=631, gain=0.4)
    return master(mix(hum, crack, air))


@sound("heavy_punch.impact", 1, "강타", "Heavy punch")
def heavy_punch_impact(v):
    th = thump(0.45, 85, 32, decay=9, gain=1.0, drive=3)
    burst = bandpass(noise(0.1, 640), 200, 2500) * env_exp(0.1, 40) * 0.9
    knock = metal(0.15, [420, 760], [40, 55], gain=0.4, seed=2)
    return master(reverb(mix(th, burst, knock), 0.18, 0.2))


# ============================================================ knight / checkmate
@sound("checkmate.select", 1, "체크메이트 조준", "Checkmate aim", dist=8)
def checkmate_select(v):
    tick = metal(0.15, [1800, 2700], [40, 55], gain=0.6, seed=7)
    sh = shimmer(0.2, 2000, 4200, gain=0.2, decay=14)
    return master(mix(tick, sh))


@sound("checkmate.jump", 1, "체크메이트 도약", "Checkmate leap")
def checkmate_jump(v):
    air = whoosh(0.5, 300, 4000, q=1.1, peak=0.35, sharp=1.4, seed=700, gain=0.9)
    th = thump(0.2, 140, 60, decay=20, gain=0.6, drive=2)
    sh = shimmer(0.4, 1500, 4500, gain=0.2, decay=8)
    return master(mix(th, air, sh, offsets=[0, 0.05, 0.1]))


@sound("checkmate.slam", 1, "체크메이트 타격", "Checkmate slam")
def checkmate_slam(v):
    th = thump(0.5, 90, 30, decay=8, gain=1.0, drive=3)
    wood = metal(0.25, [540, 890, 1400], [22, 28, 35], gain=0.6, seed=8)
    burst = bandpass(noise(0.12, 710), 250, 3000) * env_exp(0.12, 35) * 0.9
    wire = crackle(0.2, 900, lo=3000, hi=9000, seed=711, gain=0.3) * env_exp(0.2, 18)
    return master(reverb(mix(th, wood, burst, wire), 0.2, 0.22))


# ============================================================ rook
@sound("rook.grab", 1, "잡기", "Grab")
def rook_grab(v):
    air = whoosh(0.14, 400, 2000, q=1.2, peak=0.4, sharp=2, seed=800, gain=0.7)
    clamp = mix(thump(0.1, 200, 110, decay=35, gain=0.7), click(0.01, lo=800, hi=5000, seed=801))
    return master(mix(air, clamp, offsets=[0, 0.08]))


@sound("rook.throw", 1, "던지기", "Throw")
def rook_throw(v):
    wind = whoosh(0.45, 250, 3500, q=1.1, peak=0.3, sharp=1.5, seed=810, gain=0.9)
    grunt = thump(0.15, 180, 90, decay=25, gain=0.5, drive=2)
    return master(mix(grunt, wind, offsets=[0, 0.04]))


@sound("rook.slam", 1, "내려찍기", "Slam")
def rook_slam(v):
    th = thump(0.45, 80, 30, decay=9, gain=1.0, drive=3)
    burst = bandpass(noise(0.12, 820), 200, 2500) * env_exp(0.12, 35) * 0.9
    debris = crackle(0.3, 500, lo=400, hi=4000, seed=821, gain=0.4) * env_exp(0.3, 12)
    return master(reverb(mix(th, burst, debris), 0.2, 0.2))


@sound("rook.punch", 3, "연속타", "Mounted punch")
def rook_punch(v):
    th = thump(0.16, 120 + 8 * v, 55, decay=24, gain=0.9, drive=2.3)
    burst = bandpass(noise(0.05, 830 + v), 400, 3000) * env_exp(0.05, 80) * 0.6
    return master(mix(th, burst))


# ============================================================ pawn6 / cleave
@sound("cleave.charge", 1, "일도양단 충전", "Cleave charge", dist=12)
def cleave_charge(v):
    d = 0.9
    hum = sine(sweep(220, 640, d, curve=1.5), d) * env_adsr(d, 0.1, 0.2, 0.8, 0.1) * 0.45
    sh = sine(sweep(2200, 4800, d, curve=1.5), d) * env_adsr(d, 0.2, 0.2, 0.7, 0.1) * 0.12
    air = whoosh(d, 900, 5000, q=1.3, peak=0.9, sharp=1.2, seed=900, gain=0.5)
    return master(mix(hum, sh, air))


@sound("cleave.dash", 1, "일도양단 돌진", "Cleave dash")
def cleave_dash(v):
    dash = whoosh(0.3, 500, 4000, q=1.1, peak=0.3, sharp=1.6, seed=910, gain=1.0)
    boom = thump(0.2, 110, 50, decay=18, gain=0.5, drive=2)
    return master(mix(boom, dash))


@sound("cleave.release", 1, "일도양단", "Cleave")
def cleave_release(v):
    cut = whoosh(0.35, 1500, 9000, q=1.3, peak=0.3, sharp=2.0, seed=920, gain=1.0)
    sh = shimmer(0.6, 5000, 12000, gain=0.5, decay=5)
    ring = metal(0.8, [2700, 4300, 6900, 9800], [6, 8, 10, 13], gain=0.35, seed=9)
    boom = thump(0.35, 100, 40, decay=12, gain=0.6, drive=2.4)
    return master(reverb(mix(cut, sh, ring, boom, offsets=[0, 0.08, 0.1, 0.12]), 0.18, 0.18))


# ============================================================ bishop
@sound("bishop.summon", 1, "신의 부름", "Bishop summon", dist=20)
def bishop_summon(v):
    d = 0.9
    choir = np.zeros(int(d * SR), dtype=np.float32)
    for i, f in enumerate([196, 247, 294, 392, 494]):
        choir += sine(f * (1 + 0.003 * np.sin(2 * np.pi * 5 * t(d))), d) * env_adsr(d, 0.15, 0.2, 0.7, 0.3) * (0.25 / (i + 1) ** 0.5)
    breath = whoosh(d, 1500, 4000, q=1.0, peak=0.5, sharp=1.1, seed=1000, gain=0.35)
    bell = metal(0.8, [1046, 1568, 2093, 2637], [5, 6, 7, 9], gain=0.35, seed=10)
    return master(reverb(mix(choir, breath, bell, offsets=[0, 0, 0.3]), 0.25, 0.3))


@sound("bishop.vanish", 2, "소멸", "Vanish")
def bishop_vanish(v):
    rev = whoosh(0.3, 5000, 900, q=1.3, peak=0.25, sharp=1.6, seed=1010 + v, gain=0.8)
    sh = shimmer(0.25, 8000, 3000, gain=0.3, decay=12)
    pop = thump(0.06, 400, 150, decay=60, gain=0.4)
    return master(mix(rev, sh, pop, offsets=[0, 0, 0.02]))


@sound("bishop.cut", 4, "형제의 베기", "Brother cut")
def bishop_cut(v):
    cut = whoosh(0.11, 2200 + 500 * v, 7500, q=1.6, peak=0.35, sharp=2.4, seed=1020 + v, gain=0.85)
    sh = shimmer(0.09, 5500 + 400 * v, 8000, gain=0.15, decay=26)
    return master(mix(cut, sh, offsets=[0, 0.03]))


@sound("bishop.barrage", 1, "형제들의 도움", "Brothers' barrage", dist=20)
def bishop_barrage(v):
    pre = bishop_summon(0)[: int(0.45 * SR)] * np.linspace(1, 0.3, int(0.45 * SR))
    boom = thump(0.4, 100, 38, decay=10, gain=0.8, drive=2.5)
    cuts = [whoosh(0.1, 2500 + 300 * i, 8000, q=1.6, peak=0.35, sharp=2.4, seed=1030 + i, gain=0.6) for i in range(6)]
    return master(reverb(mix(pre, boom, *cuts, offsets=[0, 0.35, 0.4, 0.47, 0.54, 0.61, 0.68, 0.75]), 0.2, 0.2))


# ============================================================ salmosa / knife skills
@sound("wasabari.trip", 1, "와사바리 넘어뜨림", "Wasabari trip")
def wasabari_trip(v):
    kick = whoosh(0.14, 300, 1800, q=1.2, peak=0.4, sharp=2, seed=1100, gain=0.7)
    fall = mix(thump(0.3, 120, 45, decay=12, gain=0.9, drive=2.2), bandpass(noise(0.15, 1101), 200, 2000) * env_exp(0.15, 25) * 0.6)
    return master(mix(kick, fall, offsets=[0, 0.12]))


@sound("wasabari.stab", 3, "와사바리 찌르기", "Wasabari stab")
def wasabari_stab(v):
    c = click(0.01, lo=3000, hi=11000, seed=1110 + v)
    air = whoosh(0.08, 2500, 7000, q=1.6, peak=0.4, sharp=2.5, seed=1120 + v, gain=0.6)
    wet = bandpass(noise(0.12, 1130 + v), 300, 1400) * env_exp(0.12, 30) * 0.7
    return master(mix(air, c, wet, offsets=[0, 0.05, 0.055]))


@sound("knife_pursuit.throw", 1, "칼날 추격 투척", "Knife throw")
def knife_pursuit_throw(v):
    air = whoosh(0.3, 1500, 6000, q=1.5, peak=0.25, sharp=1.8, seed=1200, gain=0.9)
    spin = amod(whoosh(0.3, 3000, 7000, q=2, peak=0.4, sharp=1.5, seed=1201, gain=0.5), 28, 0.7)
    return master(mix(air, spin))


@sound("knife_pursuit.embed", 1, "칼날 박힘", "Knife embed")
def knife_pursuit_embed(v):
    c = click(0.012, lo=2000, hi=10000, seed=1210, gain=1.1)
    th = thump(0.14, 200, 90, decay=28, gain=0.6, drive=2)
    ring = metal(0.35, [3400, 5500, 8100], [14, 18, 22], gain=0.3, seed=11)
    wet = bandpass(noise(0.15, 1211), 300, 1200) * env_exp(0.15, 26) * 0.6
    return master(mix(c, th, ring, wet))


@sound("shoulder_stab.stab", 1, "어깨 찌르기", "Shoulder stab")
def shoulder_stab_stab(v):
    air = whoosh(0.1, 2000, 6500, q=1.5, peak=0.35, sharp=2.4, seed=1300, gain=0.7)
    c = click(0.012, lo=2500, hi=10000, seed=1301, gain=1.0)
    th = thump(0.16, 170, 70, decay=24, gain=0.7, drive=2)
    wet = bandpass(noise(0.18, 1302), 250, 1200) * env_exp(0.18, 22) * 0.7
    return master(mix(air, c, th, wet, offsets=[0, 0.07, 0.07, 0.075]))


@sound("shoulder_stab.catch", 1, "공중 잡기", "Air catch")
def shoulder_stab_catch(v):
    sh = shimmer(0.25, 2500, 6500, gain=0.4, decay=10)
    tick = metal(0.2, [2900, 4600], [30, 40], gain=0.4, seed=12)
    air = whoosh(0.2, 800, 4000, q=1.3, peak=0.3, sharp=2, seed=1310, gain=0.5)
    return master(mix(air, sh, tick, offsets=[0, 0.05, 0.08]))


@sound("air_counter.drop", 1, "공중 내려찍기", "Air drop")
def air_counter_drop(v):
    fall = whoosh(0.35, 600, 2800, q=1.1, peak=0.7, sharp=1.4, seed=1320, gain=0.8)
    th = thump(0.3, 110, 40, decay=12, gain=1.0, drive=2.6)
    c = click(0.012, lo=2000, hi=9000, seed=1321)
    return master(reverb(mix(fall, th, c, offsets=[0, 0.3, 0.3]), 0.14, 0.16))


# ============================================================ madness / eunbal
@sound("madness_leap.jump", 1, "광기 도약", "Madness leap")
def madness_leap_jump(v):
    growl = amod(sine(sweep(90, 140, 0.3), 0.3), 22, 0.7) * env_adsr(0.3, 0.02, 0.1, 0.6, 0.1) * 0.5
    air = whoosh(0.45, 300, 3500, q=1.1, peak=0.35, sharp=1.4, seed=1400, gain=0.9)
    return master(mix(growl, air, offsets=[0, 0.08]))


@sound("madness_leap.land", 1, "광기 내려찍기", "Madness slam")
def madness_leap_land(v):
    th = thump(0.5, 85, 28, decay=8, gain=1.0, drive=3)
    chop = hit_hatchet(0)[: int(0.3 * SR)]
    debris = crackle(0.3, 600, lo=300, hi=4000, seed=1410, gain=0.4) * env_exp(0.3, 12)
    return master(reverb(mix(th, chop, debris), 0.2, 0.22))


@sound("madness.survive", 1, "반 칸 생존", "Half-heart survival", dist=12)
def madness_survive(v):
    d = 0.9
    heart = mix(thump(0.12, 80, 55, decay=30, gain=0.8, drive=2), thump(0.12, 70, 45, decay=30, gain=0.7, drive=2), offsets=[0, 0.14])
    heart2 = heart.copy()
    drone = sine(sweep(55, 50, d), d) * env_adsr(d, 0.05, 0.2, 0.8, 0.3) * 0.35
    air = whoosh(d, 2000, 300, q=1.0, peak=0.2, sharp=1.2, seed=1420, gain=0.5)
    return master(mix(drone, air, heart, heart2, offsets=[0, 0, 0.05, 0.5]))


# ============================================================ trait activation stingers
@sound("trait.killing_intent", 1, "살기 발동", "Killing intent", dist=20)
def trait_killing_intent(v):
    d = 0.8
    drone = amod(sine(sweep(70, 95, d, curve=0.8), d), 9, 0.5) * env_adsr(d, 0.03, 0.2, 0.7, 0.3) * 0.5
    hiss = whoosh(d, 500, 3500, q=1.0, peak=0.3, sharp=1.2, seed=1500, gain=0.5)
    stab = metal(0.5, [1850, 2960], [9, 12], gain=0.25, seed=13)
    return master(reverb(mix(drone, hiss, stab, offsets=[0, 0, 0.05]), 0.22, 0.25))


@sound("trait.mixed_madness", 1, "광기 발동", "Mixed madness", dist=20)
def trait_mixed_madness(v):
    d = 0.9
    drone = amod(sine(sweep(60, 110, d, curve=1.3), d), 15, 0.7) * env_adsr(d, 0.03, 0.2, 0.8, 0.3) * 0.55
    scream = sine(sweep(900, 2400, 0.5, curve=0.6), 0.5) * env_adsr(0.5, 0.02, 0.1, 0.6, 0.2) * 0.12
    scream = amod(scream, 31, 0.5)
    hiss = whoosh(d, 400, 4500, q=1.0, peak=0.4, sharp=1.2, seed=1510, gain=0.5)
    drip = crackle(d, 60, lo=1500, hi=5000, seed=1511, gain=0.3)
    return master(reverb(mix(drone, scream, hiss, drip, offsets=[0, 0.1, 0, 0.3]), 0.25, 0.28))


@sound("trait.fighting_spirit", 1, "투기 발동", "Fighting spirit", dist=20)
def trait_fighting_spirit(v):
    d = 0.8
    gong = metal(d, [196, 294, 392, 588, 784], [3, 4, 5, 6, 8], gain=0.8, detune=0.004, seed=14)
    flare = whoosh(0.6, 300, 3000, q=1.1, peak=0.25, sharp=1.3, seed=1520, gain=0.5)
    stomp = thump(0.25, 100, 45, decay=15, gain=0.6, drive=2)
    return master(reverb(mix(stomp, gong, flare, offsets=[0, 0.02, 0.02]), 0.25, 0.25))


@sound("trait.poison_aura", 1, "독기 발동", "Poison aura", dist=20)
def trait_poison_aura(v):
    d = 0.9
    hiss = amod(whoosh(d, 2000, 6000, q=1.2, peak=0.3, sharp=1.2, seed=1530, gain=0.7), 40, 0.4)
    bubble = crackle(d, 90, lo=300, hi=1500, seed=1531, gain=0.35)
    drone = sine(sweep(110, 90, d), d) * env_adsr(d, 0.05, 0.2, 0.7, 0.3) * 0.3
    return master(reverb(mix(hiss, bubble, drone), 0.2, 0.2))


@sound("trait.off", 1, "특성 해제", "Trait off", dist=8)
def trait_off(v):
    rev = whoosh(0.35, 3000, 400, q=1.2, peak=0.2, sharp=1.5, seed=1540, gain=0.7)
    tone = sine(sweep(600, 300, 0.3, curve=0.8), 0.3) * env_exp(0.3, 10) * 0.25
    return master(mix(rev, tone))


@sound("poison.hiss", 2, "뱀 울음", "Snake hiss", dist=16)
def poison_hiss(v):
    d = 0.6
    hiss = amod(whoosh(d, 2500, 7000, q=1.2, peak=0.3, sharp=1.3, seed=1560 + v, gain=0.8), 36 + 4 * v, 0.45)
    low = sine(sweep(140, 95, d), d) * env_adsr(d, 0.05, 0.15, 0.5, 0.3) * 0.2
    return master(mix(hiss, low))


@sound("knife_pursuit.blink", 1, "칼날 추격 점멸", "Blink", dist=24)
def knife_pursuit_blink(v):
    rev = whoosh(0.2, 600, 6000, q=1.3, peak=0.3, sharp=1.8, seed=1570, gain=0.8)
    sh = shimmer(0.3, 3000, 9000, gain=0.35, decay=10)
    pop = thump(0.05, 500, 200, decay=70, gain=0.5)
    return master(mix(pop, rev, sh, offsets=[0, 0.01, 0.03]))


@sound("hunt_mark.apply", 1, "사냥표식", "Hunting mark", dist=12)
def hunt_mark_apply(v):
    tick = metal(0.3, [1400, 2100, 3500], [16, 20, 26], gain=0.5, seed=15)
    hiss = whoosh(0.25, 3000, 7000, q=1.5, peak=0.3, sharp=2, seed=1550, gain=0.4)
    return master(mix(tick, hiss))


# ============================================================ UI
@sound("ui.open", 1, "휴대폰 열기", "Phone open", dist=4)
def ui_open(v):
    tone = mix(sine(880, 0.09) * env_exp(0.09, 30), sine(1318, 0.12) * env_exp(0.12, 24), offsets=[0, 0.05]) * 0.5
    c = click(0.006, lo=2000, hi=8000, seed=1600, gain=0.4)
    return master(mix(c, tone))


@sound("ui.close", 1, "휴대폰 닫기", "Phone close", dist=4)
def ui_close(v):
    tone = mix(sine(1318, 0.08) * env_exp(0.08, 30), sine(880, 0.12) * env_exp(0.12, 24), offsets=[0, 0.05]) * 0.5
    c = click(0.006, lo=2000, hi=8000, seed=1610, gain=0.4)
    return master(mix(c, tone))


@sound("ui.click", 2, "클릭", "Click", dist=4)
def ui_click(v):
    tone = sine(1760 + 200 * v, 0.05) * env_exp(0.05, 60) * 0.35
    c = click(0.008, lo=1500, hi=7000, seed=1620 + v, gain=0.6)
    return master(mix(c, tone))


@sound("ui.deny", 1, "불가", "Denied", dist=4)
def ui_deny(v):
    tone = mix(sine(330, 0.1) * env_exp(0.1, 20), sine(311, 0.14) * env_exp(0.14, 18), offsets=[0, 0.09]) * 0.5
    return master(tone)


@sound("ui.purchase", 1, "구매", "Purchase", dist=6)
def ui_purchase(v):
    notes = [(784, 0), (988, 0.07), (1175, 0.14), (1568, 0.22)]
    parts = [sine(f, 0.25) * env_exp(0.25, 12) * 0.4 for f, _ in notes]
    coin = metal(0.4, [2700, 4100, 6200], [10, 13, 16], gain=0.35, seed=16)
    return master(mix(*parts, coin, offsets=[o for _, o in notes] + [0.22]))


@sound("mission.complete", 1, "미션 완료", "Mission complete", dist=6)
def mission_complete(v):
    notes = [(659, 0), (784, 0.09), (1046, 0.18), (1318, 0.3)]
    parts = [sine(f, 0.45) * env_exp(0.45, 7) * 0.4 for f, _ in notes]
    bell = metal(0.7, [2093, 3136, 4186], [5, 7, 9], gain=0.3, seed=17)
    return master(reverb(mix(*parts, bell, offsets=[o for _, o in notes] + [0.3]), 0.2, 0.2))


@sound("level.up", 1, "특성 레벨업", "Trait level up", dist=8)
def level_up(v):
    d = 0.8
    rise = sine(sweep(440, 1760, 0.5, curve=1.4), 0.5) * env_adsr(0.5, 0.02, 0.1, 0.8, 0.1) * 0.35
    chord = np.zeros(int(d * SR), dtype=np.float32)
    for f in [880, 1108, 1318, 1760]:
        chord += sine(f, d) * env_adsr(d, 0.01, 0.2, 0.6, 0.3) * 0.18
    bell = metal(0.8, [3520, 5274, 7040], [5, 6, 8], gain=0.3, seed=18)
    return master(reverb(mix(rise, chord, bell, offsets=[0, 0.45, 0.45]), 0.25, 0.22))


# ============================================================ writer
def write_ogg(data: np.ndarray, path: Path):
    path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
        wav = tmp.name
    pcm = np.clip(data, -1, 1)
    pcm16 = (pcm * 32767).astype("<i2")
    import wave
    with wave.open(wav, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(pcm16.tobytes())
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav, "-c:a", "libvorbis", "-q:a", "5", str(path)], check=True)
    os.unlink(wav)


def main():
    only = sys.argv[1:]  # optional name filters
    events = {}
    subtitles_ko, subtitles_en = {}, {}
    total = 0
    for name, (fn, variants, ko, en, dist) in CATALOG.items():
        if only and not any(name.startswith(o) for o in only):
            continue
        file_base = name.replace(".", "_")
        entries = []
        for v in range(variants):
            data = fn(v)
            fname = file_base if variants == 1 else f"{file_base}_{v + 1}"
            write_ogg(data, OUT / f"{fname}.ogg")
            entries.append({"name": f"killerranking:{fname}", "attenuation_distance": dist})
            total += 1
        events[name] = {"subtitle": f"subtitles.killerranking.{name}", "sounds": entries}
        subtitles_ko[f"subtitles.killerranking.{name}"] = ko
        subtitles_en[f"subtitles.killerranking.{name}"] = en
        print(f"  {name:28s} x{variants}  {len(data) / SR:.2f}s")
    if only:
        print(f"wrote {total} files (filtered; sounds.json untouched)")
        return
    SOUNDS_JSON.write_text(json.dumps(events, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    for lang, subs in (("ko_kr", subtitles_ko), ("en_us", subtitles_en)):
        p = LANG_DIR / f"{lang}.json"
        d = json.loads(p.read_text(encoding="utf-8"))
        d = {k: v for k, v in d.items() if not k.startswith("subtitles.killerranking.")}
        d.update(subs)
        p.write_text(json.dumps(d, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"wrote {total} ogg files, {len(events)} events")


if __name__ == "__main__":
    main()
