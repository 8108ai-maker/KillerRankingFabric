package dev.killerranking.combat;

/** Tick-based charge and a finite forward cutting area. All authority stays on the server. */
public final class CleaveRules {
    public static final int CHARGE=76, MAX_HOLD=300, COOLDOWN=600, COST=1000;
    public static final int CAST=20, DASH_TICKS=8, FULL_HIT=7;
    public static final double DASH=4, FULL_RANGE=6, DASH_REACH=2.25;
    public static final float DASH_DAMAGE=21, FULL_DAMAGE=45;
    public static final int IDLE=0, CHARGING=1, SLASHING=2;
    public static boolean full(int ticks) {return ticks>=CHARGE;}
    public static float damage(boolean full) {return full?FULL_DAMAGE:DASH_DAMAGE;}
    public static boolean inSlash(double forward,double side,double vertical,boolean full) {
        double range=full?FULL_RANGE:DASH_REACH;
        return forward>=-.25&&forward<=range&&forward*forward+side*side<=range*range
            &&Math.abs(side)<=.75+Math.max(0,forward)*.5&&Math.abs(vertical)<=2.25;
    }
    private CleaveRules() { }
}
