package dev.killerranking.combat;

/** Release before 1.5 seconds for the short draw; a complete charge cuts along an eight-metre dash. */
public final class NeungsaIaidoRules {
    public static final int DURATION=20,HIT=3,ANIMATION=8,COOLDOWN=300;
    public static final int CHARGE=30,MAX_HOLD=300,DASH_TICKS=5;
    public static final int IDLE=0,CHARGING=1,SLASHING=2;
    public static final double DASH=8,DASH_HALF_WIDTH=.75;
    public static final float FULL_DAMAGE=25;
    public static final double REACH=5,HALF_WIDTH=2,HALF_HEIGHT=1;
    public static final float DAMAGE=12;
    public static int remaining(long started,long now){return (int)Math.clamp(DURATION-(now-started),0,DURATION);}
    public static boolean full(long ticks){return ticks>=CHARGE;}
    public static float damage(boolean full){return full?FULL_DAMAGE:DAMAGE;}
    private NeungsaIaidoRules(){ }
}
