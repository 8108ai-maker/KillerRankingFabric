package dev.killerranking.combat;

public final class RushRules {
    public static final int DURATION=100, HITS=30, COOLDOWN=400, COST=1000;
    public static final float DAMAGE=5;
    public static final double RANGE=3.2;
    /** Spread exactly thirty strikes over one hundred server ticks, including the last tick. */
    public static boolean strikesAt(int age) {
        return age>=0 && age<DURATION && (age+1)*HITS/DURATION > age*HITS/DURATION;
    }
    public static boolean inFront(double distanceSquared,double facingDot) {
        return distanceSquared<=RANGE*RANGE && facingDot>=.78;
    }
    private RushRules() { }
}
