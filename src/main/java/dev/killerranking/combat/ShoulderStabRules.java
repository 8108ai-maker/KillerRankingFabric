package dev.killerranking.combat;
import net.minecraft.world.phys.Vec3;

/** A short windup, one instantaneous approach and one shoulder contact. */
public final class ShoulderStabRules {
    public static final int COUNTER=3, COUNTER_CATCH=6, COUNTER_DROP=10, COUNTER_IMPACT=12, COUNTER_DURATION=26;
    public static Vec3 counterPosition(Vec3 from,Vec3 landing,Vec3 catchKnife,double age){
        double across=ease(age/4),rise=ease(age/COUNTER_CATCH),drop=ease((age-COUNTER_DROP)/(COUNTER_IMPACT-COUNTER_DROP));
        double peak=Math.max(landing.y+.6,catchKnife.y-1.85);
        return new Vec3(from.x+(landing.x-from.x)*across,(from.y+(peak-from.y)*rise)*(1-drop)+landing.y*drop,from.z+(landing.z-from.z)*across);
    }
    public static Vec3 counterKnife(Vec3 from,Vec3 catchKnife,double age){return from.lerp(catchKnife,ease(age/COUNTER_CATCH));}
    private static double ease(double t){t=Math.clamp(t,0,1);return t*t*(3-2*t);}
    public static final int AIM=1,CAST=2,MAX_HOLD=200,BLINK=4,IMPACT=8,WITHDRAW=14,DURATION=24,COOLDOWN=400;
    public static final double REACH=12;
    public static final float DAMAGE=20;
    public static boolean inRange(double squared){return Double.isFinite(squared)&&squared>=0&&squared<=REACH*REACH;}
    public static Vec3 position(Vec3 origin,Vec3 destination,double age){return age<BLINK?origin:destination;}
    public static Vec3 shoulder(Vec3 position,double width,double height,float yaw,boolean left){
        var forward=Vec3.directionFromRotation(0,yaw);var side=new Vec3(forward.z,0,-forward.x);
        return position.add(side.scale((left?1:-1)*width*.30)).subtract(forward.scale(width*.32)).add(0,height*.82,0);
    }
    public static Vec3 incoming(float yaw){return Vec3.directionFromRotation(0,yaw).scale(.28).add(0,-.96,0).normalize();}
    private ShoulderStabRules(){ }
}
