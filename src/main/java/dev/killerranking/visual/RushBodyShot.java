package dev.killerranking.visual;

import net.minecraft.world.phys.Vec3;

/** Native-length forearm folds toward the front around the middle of the arm. */
public final class RushBodyShot {
    public static final double ELBOW=4;
    private static double angle(double y,double fold){
        double blend=Math.clamp((y-3.4)/1.2,0,1);
        return fold*blend*blend*(3-2*blend);
    }
    public static Vec3 point(double x,double y,double z,double fold){
        if(y<=3.4||fold==0)return new Vec3(x,y,z);
        double angle=angle(y,fold),cos=Math.cos(angle),sin=Math.sin(angle);
        return new Vec3(x,ELBOW+(y-ELBOW)*cos-z*sin,(y-ELBOW)*sin+z*cos);
    }
    public static Vec3 normal(double x,double y,double z,double atY,double fold){
        if(atY<=3.4||fold==0)return new Vec3(x,y,z);
        double angle=angle(atY,fold),cos=Math.cos(angle),sin=Math.sin(angle);
        return new Vec3(x,y*cos-z*sin,y*sin+z*cos);
    }
    private RushBodyShot(){ }
}
