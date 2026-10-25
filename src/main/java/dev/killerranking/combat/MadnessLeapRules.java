package dev.killerranking.combat;

import net.minecraft.world.phys.Vec3;

/** All timings are server ticks. The axe connects exactly when the jump lands. */
public final class MadnessLeapRules {
    public static final int AIM=1, CAST=2, MAX_HOLD=200, HOP=4, HIT=22, DURATION=30, COOLDOWN=400;
    public static final int SPINS=3, SPIN_END=18;
    public static final double MIN_RANGE=2, MAX_RANGE=10, HEIGHT=2.6;
    public static final float DAMAGE=25;
    public static boolean inRange(double squaredDistance) {
        return Double.isFinite(squaredDistance)&&squaredDistance>=MIN_RANGE*MIN_RANGE&&squaredDistance<=MAX_RANGE*MAX_RANGE;
    }
    public static Vec3 position(Vec3 origin,Vec3 destination,double age) {
        double t=Math.clamp((age-HOP)/(HIT-HOP),0,1);
        double horizontal=t*t*(3-2*t);
        return origin.lerp(destination,horizontal).add(0,4*HEIGHT*t*(1-t),0);
    }
    private MadnessLeapRules() { }
}
