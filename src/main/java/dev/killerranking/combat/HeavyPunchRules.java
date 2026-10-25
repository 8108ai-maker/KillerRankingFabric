package dev.killerranking.combat;

/** One chambered punch; the server and animation share the same contact tick. */
public final class HeavyPunchRules {
    public static final int DURATION=54, DROP=30, SPLASH=38, IMPACT=41, RELEASE=46, COOLDOWN=500;
    public static final float DAMAGE=30;
    public static final double REACH=2.4;
    public static int cooldown(long readyAt,long now){return (int)Math.clamp(readyAt-now,0,COOLDOWN);}
    public static boolean contact(int age){return age==IMPACT;}
    public static boolean holding(int age){return age>=0&&age<RELEASE;}
    private HeavyPunchRules(){}
}
