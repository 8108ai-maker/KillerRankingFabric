"""Minimal joml-compatible rotation math (Matrix4f.rotateZ/Y/X order) for authoring player-animator clips."""
from __future__ import annotations
import math
import numpy as np

def Rx(a):
    c,s=math.cos(a),math.sin(a); return np.array([[1,0,0],[0,c,-s],[0,s,c]],dtype=float)
def Ry(a):
    c,s=math.cos(a),math.sin(a); return np.array([[c,0,s],[0,1,0],[-s,0,c]],dtype=float)
def Rz(a):
    c,s=math.cos(a),math.sin(a); return np.array([[c,-s,0],[s,c,0],[0,0,1]],dtype=float)

def part_rotation(pitch, yaw, roll):
    """joml: new Matrix4f().rotateZ(roll).rotateY(yaw).rotateX(pitch) == Rz @ Ry @ Rx."""
    return Rz(roll) @ Ry(yaw) @ Rx(pitch)

HELD_ITEM = Rx(-math.pi/2) @ Ry(math.pi)   # vanilla third-person held item frame inside the arm

def blade_dir(arm_rot, grip_rot):
    """Unit vector of the item's +Y (blade) axis in body space, like NightBatAnimationTest."""
    v = part_rotation(*arm_rot) @ HELD_ITEM @ part_rotation(*grip_rot) @ np.array([0.0,1.0,0.0])
    return v/np.linalg.norm(v)

def decompose_zyx(M):
    """Inverse of part_rotation: M = Rz(roll) Ry(yaw) Rx(pitch) -> (pitch, yaw, roll)."""
    # M = Rz Ry Rx. Standard: for R = Rz(c) Ry(b) Rx(a):
    # R[2,0] = -sin b ; R[2,1] = cos b sin a ; R[2,2] = cos b cos a ; R[1,0] = cos b sin c ; R[0,0] = cos b cos c
    sb = -M[2,0]; sb=max(-1.0,min(1.0,sb)); b=math.asin(sb); cb=math.cos(b)
    if abs(cb)>1e-6:
        a=math.atan2(M[2,1],M[2,2]); c=math.atan2(M[1,0],M[0,0])
    else:
        a=math.atan2(-M[1,2],M[1,1]); c=0.0
    return (a,b,c)

def align(v_from, v_to):
    """Minimal rotation matrix taking unit v_from onto unit v_to."""
    v_from=v_from/np.linalg.norm(v_from); v_to=v_to/np.linalg.norm(v_to)
    c=float(np.dot(v_from,v_to)); axis=np.cross(v_from,v_to); s=float(np.linalg.norm(axis))
    if s<1e-9:
        if c>0: return np.eye(3)
        # 180°: pick any perpendicular axis
        p=np.cross(v_from,[1,0,0]);
        if np.linalg.norm(p)<1e-6: p=np.cross(v_from,[0,1,0])
        p/=np.linalg.norm(p); return axis_angle(p,math.pi)
    axis/=s; return axis_angle(axis,math.atan2(s,c))

def axis_angle(axis,ang):
    axis=np.asarray(axis,dtype=float); x,y,z=axis; c,s=math.cos(ang),math.sin(ang); C=1-c
    return np.array([[c+x*x*C, x*y*C-z*s, x*z*C+y*s],[y*x*C+z*s, c+y*y*C, y*z*C-x*s],[z*x*C-y*s, z*y*C+x*s, c+z*z*C]])

def arm_for_blade(target_dir, grip_rot, twist=0.0):
    """Arm (pitch,yaw,roll) so that the blade points along target_dir, then twisted by `twist` about the blade."""
    v0 = HELD_ITEM @ part_rotation(*grip_rot) @ np.array([0.0,1.0,0.0]); v0/=np.linalg.norm(v0)
    t=np.asarray(target_dir,dtype=float); t/=np.linalg.norm(t)
    R = axis_angle(t,twist) @ align(v0,t)
    return decompose_zyx(R)

def unit(v):
    v=np.asarray(v,dtype=float); return v/np.linalg.norm(v)

def slerp(a,b,u):
    a=unit(a); b=unit(b); d=max(-1.0,min(1.0,float(np.dot(a,b)))); th=math.acos(d)
    if th<1e-6: return a
    return (math.sin((1-u)*th)*a+math.sin(u*th)*b)/math.sin(th)

def ease_out(u): return 1-(1-u)**3
def ease_in(u): return u**3
def ease_in_out(u): return 3*u*u-2*u*u*u
def smoothstep(u): u=max(0.0,min(1.0,u)); return u*u*(3-2*u)


def solve_arm(target_dir, grip_rot, roll, init=(-1.0,0.0)):
    """Arm (pitch,yaw) with a FIXED roll so the blade points along target_dir; continuous via `init`.
    Returns (pitch, yaw, roll, residual_deg)."""
    from scipy.optimize import minimize
    t=unit(target_dir)
    def cost(v):
        d=blade_dir((v[0],v[1],roll),grip_rot); return 1.0-float(np.dot(d,t))
    best=None
    for guess in (init,(init[0],init[1]+math.pi),(init[0]+math.pi,init[1]),(-1.2,0.0),(-1.2,1.2),(-1.2,-1.2),(0.5,0.0),(-2.5,0.0)):
        r=minimize(cost,np.array(guess,dtype=float),method='Nelder-Mead',options=dict(xatol=1e-6,fatol=1e-9,maxiter=600))
        # continuity preference: tiny penalty on distance from init
        score=r.fun+1e-4*((r.x[0]-init[0])**2+(r.x[1]-init[1])**2)
        if best is None or score<best[0]: best=(score,r)
    r=best[1]
    pch=wrap_near(float(r.x[0]),init[0]); yw=wrap_near(float(r.x[1]),init[1])
    res=math.degrees(math.acos(max(-1.0,min(1.0,1.0-float(r.fun)))))
    return pch,yw,roll,res

def wrap_near(a,ref):
    while a-ref>math.pi: a-=2*math.pi
    while a-ref<-math.pi: a+=2*math.pi
    return a


def arm_axis(pitch,yaw,roll):
    """Direction the arm box hangs along. Model space has +Y DOWN (the renderer flips Y), so the arm is +Y."""
    return part_rotation(pitch,yaw,roll) @ np.array([0.0,1.0,0.0])

def solve_arm_axis(target_axis, roll, init=(-1.0,0.0)):
    """Arm (pitch,yaw) with fixed roll so the ARM points along target_axis (continuous via init)."""
    from scipy.optimize import minimize
    t=unit(target_axis)
    def cost(v):
        return 1.0-float(np.dot(arm_axis(v[0],v[1],roll),t))
    best=None
    for guess in (init,(init[0],init[1]+math.pi),(init[0]+math.pi,init[1]),(-1.2,0.0),(-1.2,1.2),(-1.2,-1.2),(-2.4,0.0),(-0.3,0.0)):
        r=minimize(cost,np.array(guess,dtype=float),method='Nelder-Mead',options=dict(xatol=1e-7,fatol=1e-10,maxiter=800))
        px=wrap_near(float(r.x[0]),init[0]); yx=wrap_near(float(r.x[1]),init[1])
        score=r.fun+0.03*((px-init[0])**2+(yx-init[1])**2)
        if best is None or score<best[0]: best=(score,r,px,yx)
    return best[2], best[3], roll

def solve_grip(arm_rot, target_dir, prev=None, rest_grip=(0.0,0.0,0.0), edge=None, tangent=None, edge_weight=1.0):
    """Absolute item (pitch,yaw,roll) so the blade (+Y of the item) points along target_dir given the arm pose.
    Minimal rotation on top of the weapon's authored rest grip; when `edge` (item-space direction of the
    cutting edge / thin side of the blade) and `tangent` (body-space direction of travel) are given, the item is
    twisted about the blade so the edge leads the cut.  Decomposed Z/Y/X; unwrapped near prev."""
    base = part_rotation(*arm_rot) @ HELD_ITEM
    e = base.T @ unit(target_dir)
    R0 = part_rotation(*rest_grip)
    rest_blade = R0 @ np.array([0.0,1.0,0.0])
    Rg = align(rest_blade, e)
    if edge is not None and tangent is not None and edge_weight > 1e-6:
        g = base.T @ np.asarray(tangent, dtype=float)
        g = g - np.dot(g, e) * e
        if np.linalg.norm(g) > 1e-3:
            g = g / np.linalg.norm(g)
            ed = Rg @ R0 @ unit(edge)
            ed = ed - np.dot(ed, e) * e
            if np.linalg.norm(ed) > 1e-3:
                ed = ed / np.linalg.norm(ed)
                phi = math.atan2(float(np.dot(np.cross(ed, g), e)), float(np.dot(ed, g))) * edge_weight
                Rg = axis_angle(e, phi) @ Rg
    p,y,r = decompose_zyx(Rg @ R0)
    if prev is not None:
        p,y,r = wrap_near(p,prev[0]), wrap_near(y,prev[1]), wrap_near(r,prev[2])
    return p,y,r

def grip_offset(grip_rot, rest_grip=(0.0,0.0,0.0), palm=(0.0,-2.0,0.0)):
    """Item translation (added to the authored rest translation) that keeps the palm point where the
    authored rest pose puts it after rotating the item about its origin."""
    h=np.array(palm,dtype=float)
    return part_rotation(*rest_grip) @ h - part_rotation(*grip_rot) @ h

def tilt_toward(d, down, theta):
    """Rotate unit d toward `down` (unit) by theta radians, staying in their plane."""
    d=unit(d); down=unit(down)
    axis=np.cross(d,down); n=np.linalg.norm(axis)
    if n<1e-6: return d
    return axis_angle(axis/n,theta) @ d


def nearest_euler(M, prev):
    """(pitch,yaw,roll) of M (= Rz Ry Rx) on whichever Euler branch and 2*pi band lies closest to prev."""
    p,y,r = decompose_zyx(M)
    best=None
    for cand in ((p,y,r),(p+math.pi, math.pi-y, r+math.pi)):
        c = tuple(wrap_near(v, pv) for v,pv in zip(cand, prev))
        d = sum((a-b)**2 for a,b in zip(c, prev))
        if best is None or d < best[0]:
            best=(d,c)
    return best[1]

def rot_slerp(A, B, u):
    """Rotation matrix interpolated along the geodesic from A (u=0) to B (u=1)."""
    rel = B @ A.T
    ang = math.acos(max(-1.0, min(1.0, (np.trace(rel)-1.0)/2.0)))
    if ang < 1e-9:
        return A.copy()
    axis = np.array([rel[2,1]-rel[1,2], rel[0,2]-rel[2,0], rel[1,0]-rel[0,1]])
    n = np.linalg.norm(axis)
    if n < 1e-9:
        # 180-degree turn: pick any axis orthogonal via eigenvector of rel for eigenvalue 1
        w,v = np.linalg.eig(rel); axis = np.real(v[:, np.argmin(np.abs(w-1.0))]); n = np.linalg.norm(axis)
    axis = axis / n
    return axis_angle(axis, ang*u) @ A
