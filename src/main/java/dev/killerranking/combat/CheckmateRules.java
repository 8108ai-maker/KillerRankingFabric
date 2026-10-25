package dev.killerranking.combat;

public final class CheckmateRules {
    public static final int COST=1000, COOLDOWN=400, DURATION=42, HOP=6, DIVE=22, HIT=26;
    public static final int AIM_DURATION=200;
    /** A successful hit leaves half a heart; absorption is consumed before health. */
    public static float damageToHalfHeart(float health,float absorption) {
        return health<=1?0:health-1+Math.max(0,absorption);
    }
    private CheckmateRules() { }
}
