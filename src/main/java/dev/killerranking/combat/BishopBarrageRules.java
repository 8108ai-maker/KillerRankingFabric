package dev.killerranking.combat;

public final class BishopBarrageRules {
    public static final int DURATION=100, INTERVAL=2, ECHO_TICKS=4;
    public static final float DAMAGE=2;
    public static final double RANGE=6, HALF_WIDTH=2;
    public static boolean contact(int age){return age>0&&age<=DURATION&&age%INTERVAL==0;}
    public static boolean inZone(double forward,double side,double height,double radius){
        return Double.isFinite(forward+side+height+radius)&&radius>=0
            &&forward+radius>=.4&&forward-radius<=RANGE&&Math.abs(side)<=HALF_WIDTH+radius
            &&height>=-.5&&height<=2.75;
    }
    private BishopBarrageRules() { }
}
