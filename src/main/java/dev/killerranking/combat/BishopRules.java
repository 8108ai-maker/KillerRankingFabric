package dev.killerranking.combat;

public final class BishopRules {
    public static final int COST=1000, CAST=12, DURATION=600, COOLDOWN=300, MAX_UNITS=30;
    public static final double LEAP_REACH=16, LEAP_SPEED=2.0, LEAP_UP=.32, PERSONAL_DAMAGE=.75;
    public static final double RUN_SPEED=2.15;
    public static final int LEAP_PREPARE=3, ATTACK_LENGTH=18;
    public static final int STAB=1, CUT=2, COMBO=3, STAB_COMBO=4;
    public static final int COMBO_HITS=6;
    public static final double COMBO_DAMAGE_SCALE=.5;
    public static int hitTick(int style){return switch(style){case STAB->8;case COMBO->4;case STAB_COMBO->5;default->10;};}
    public static boolean repeated(int style){return style==COMBO||style==STAB_COMBO;}
    public static boolean isHitTick(int style,int tick){return style==COMBO?(tick>=4&&tick<=24&&tick%4==0):style==STAB_COMBO?(tick>=5&&tick<=30&&tick%5==0):tick==hitTick(style);}
    public static int attackLength(int style){return style==COMBO?28:style==STAB_COMBO?34:ATTACK_LENGTH;}
    public static double reach(int style,float width){return (style==STAB_COMBO?2.15:1.7)+width*.5;}
    public static int count(int level){return 2*Math.clamp(level,1,3);}
    public static int cooldown(int level){return switch(Math.clamp(level,1,3)){case 2->200;case 3->140;default->300;};}
    public static double damage(int level){return 3+Math.clamp(level,1,3);}
    public static boolean canLeap(double distance){return Double.isFinite(distance)&&distance>=3&&distance<=LEAP_REACH;}
    private BishopRules() { }
}
