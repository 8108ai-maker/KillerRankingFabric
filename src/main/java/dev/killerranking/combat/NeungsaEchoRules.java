package dev.killerranking.combat;

/** One opening cut, twenty independent echoes while sheathed, then one finishing cut. */
public final class NeungsaEchoRules {
    public static final int OPEN_HIT=6, SHEATHED=19, ECHO_START=20, ECHO_COUNT=20,
        ECHO_SPACING=2, ECHO_CONTACT=2, ECHO_LIFE=5, FINAL_START=64, FINAL_HIT=70,
        DURATION=94, COOLDOWN=400;
    public static final double REACH=12, HALF_WIDTH=2, HALF_HEIGHT=1.5;
    public static final float DAMAGE=4;
    public static int echoStart(int index){return ECHO_START+index*ECHO_SPACING;}
    public static int echoHit(int index){return echoStart(index)+ECHO_CONTACT;}
    public static boolean hit(int age){
        if(age==OPEN_HIT||age==FINAL_HIT)return true;
        int offset=age-echoHit(0);
        return offset>=0&&offset%ECHO_SPACING==0&&offset/ECHO_SPACING<ECHO_COUNT;
    }
    public static int remaining(long start,long now){return (int)Math.clamp(DURATION-(now-start),0,DURATION);}
    private NeungsaEchoRules(){ }
}
