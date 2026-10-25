package dev.killerranking.combat;

/** Raise the foot, sweep on two separate beats, then stab only after a successful trip. */
public final class WasabariRules {
    public static final int WA=12,BARI=20,PRONE_TICKS=40,RELEASE=BARI+PRONE_TICKS,DURATION=RELEASE+6;
    public static final int FIRST_STAB=26,STAB_INTERVAL=6,STABS=6,COOLDOWN=400;
    public static final int EFFECT_TICKS=100,BLEED_INTERVAL=10,SLOWNESS_AMPLIFIER=1;
    public static final float STAB_DAMAGE=2,BLEED_DAMAGE=2;
    public static final double REACH=2;
    public static boolean stab(int age){return age>=FIRST_STAB&&age<=FIRST_STAB+(STABS-1)*STAB_INTERVAL&&(age-FIRST_STAB)%STAB_INTERVAL==0;}
    public static boolean bleedPulse(int remaining){return remaining>0&&remaining%BLEED_INTERVAL==0;}
    public static int cooldown(long ready,long now){return (int)Math.clamp(ready-now,0,COOLDOWN);}
    private WasabariRules() { }
}
