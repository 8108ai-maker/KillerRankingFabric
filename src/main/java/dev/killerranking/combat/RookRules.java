package dev.killerranking.combat;

public final class RookRules {
    public static final int COST=1000,COOLDOWN=400,AIM_DURATION=200,CHOICE_DURATION=100;
    public static final double REACH=2;
    public static final int IDLE=0,AIM=1,CHOICE=2,THROW=3,PUNCH=4,MOUNT=5;
    public static final int MOUNT_HITS=6,MOUNT_FIRST_HIT=8,MOUNT_HIT_INTERVAL=15;
    public static final int THROW_RELEASE=8,THROW_DURATION=18,FLIGHT_DURATION=35,PUNCH_DURATION=28,
        MOUNT_DURATION=MOUNT_FIRST_HIT+(MOUNT_HITS-1)*MOUNT_HIT_INTERVAL+10;
    public static final float THROW_DAMAGE=8,WALL_DAMAGE=6,PUNCH_DAMAGE=4,MOUNT_DAMAGE=8;
    public static boolean strike(int phase,int age) {
        // Give the first punch a short side wind-up, then hit at full extension.
        return phase==PUNCH&&age>=3&&age<=27&&age%6==3
            ||phase==MOUNT&&age>=MOUNT_FIRST_HIT&&age<=MOUNT_FIRST_HIT+(MOUNT_HITS-1)*MOUNT_HIT_INTERVAL
                &&(age-MOUNT_FIRST_HIT)%MOUNT_HIT_INTERVAL==0;
    }
    public static int duration(int phase) { return switch(phase) {case AIM->AIM_DURATION;case CHOICE->CHOICE_DURATION;case THROW->THROW_DURATION;case PUNCH->PUNCH_DURATION;case MOUNT->MOUNT_DURATION;default->0;}; }
    public static boolean canUnlock(boolean rush,boolean checkmate,boolean rook) { return !rush&&!checkmate&&!rook; }
    private RookRules() { }
}
