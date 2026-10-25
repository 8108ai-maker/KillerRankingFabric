package dev.killerranking.combat;

import net.minecraft.world.phys.Vec3;

/** Fast throw, a single teleport and a level withdrawal have separate contact beats. */
public final class KnifePursuitRules {
    public static final int AIM=1, CAST=2, MAX_HOLD=200, THROW=6, IMPACT=8;
    public static final int DEFLECTED=3, AIR_DURATION=24, COUNTER_OPEN=4, COUNTER_CLOSE=18;
    public static final int BLINK=12, GRIP=14, EXTRACT=18, RETURN=22, DURATION=34, COOLDOWN=400;
    public static final double REACH=12;
    public static final float THROW_DAMAGE=6, EXTRACT_DAMAGE=10;
    public static boolean inRange(double squared){return Double.isFinite(squared)&&squared>=0&&squared<=REACH*REACH;}
    public static Vec3 blink(Vec3 from,Vec3 to,double age){return age<BLINK?from:to;}
    public static Vec3 flight(Vec3 from,Vec3 to,double age){return from.lerp(to,Math.clamp((age-THROW)/(IMPACT-THROW),0,1));}
    public static boolean away(double age){return age>=THROW&&age<RETURN;}
    public static boolean counterWindow(double age){return Double.isFinite(age)&&age>=COUNTER_OPEN&&age<=COUNTER_CLOSE;}
    /** The parried blade rises quickly, hangs near its apex, then falls. */
    public static Vec3 deflected(Vec3 origin,double age){double t=Math.clamp(age,0,30)/15;return origin.add(0,3*t*(2-t),0);}
    private KnifePursuitRules(){ }
}
