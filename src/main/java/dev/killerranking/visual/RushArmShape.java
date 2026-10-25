package dev.killerranking.visual;

import net.minecraft.world.phys.Vec3;

/** Straight anime smear: the wrist narrows and the fist flares; no elbow rotation. */
public final class RushArmShape {
    public static double width(double y,double extension) {
        double strength=Math.clamp((extension-.6)/1.2,0,1);
        double t=Math.clamp((y+2)/12,0,1);
        double wrist=Math.exp(-Math.pow((t-.64)/.18,2));
        double tip=Math.clamp((t-.75)/.25,0,1);tip=tip*tip*(3-2*tip);
        return 1+strength*(.38*tip-.20*wrist);
    }
    public static Vec3 deform(double x,double y,double z,double center,double extension) {
        double w=width(y,extension);
        return new Vec3(center+(x-center)*w,y,z*w);
    }
    private RushArmShape() { }
}
