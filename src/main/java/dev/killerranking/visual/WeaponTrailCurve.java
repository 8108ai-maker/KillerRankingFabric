package dev.killerranking.visual;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;

/** Fits one restrained arc instead of reproducing every wrist wobble in the effect. */
public final class WeaponTrailCurve {
    private static final int STEPS=16;

    public static List<BatTrailHistory.Point> smooth(List<BatTrailHistory.Point> raw) {
        if(raw.size()<3) return List.copyOf(raw);
        Vec3 start=raw.getFirst().tip(),end=raw.getLast().tip(),chord=end.subtract(start);
        double length=chord.length(),travel=raw.getLast().distance()-raw.getFirst().distance();
        if(length<.04||travel<.001) return List.of(new BatTrailHistory.Point(raw.getFirst().inner(),start,0),
            new BatTrailHistory.Point(raw.getLast().inner(),end,length));
        Vec3 control=Vec3.ZERO,span=Vec3.ZERO;double weight=0,width=0;
        for(var p:raw) {
            double t=(p.distance()-raw.getFirst().distance())/travel;
            double w=2*t*(1-t);
            control=control.add(p.tip().subtract(start.scale((1-t)*(1-t))).subtract(end.scale(t*t)).scale(w));
            weight+=w*w;
            Vec3 blade=p.tip().subtract(p.inner());span=span.add(blade);width+=blade.length();
        }
        control=weight>.00001?control.scale(1/weight):start.lerp(end,.5);
        span=span.scale(1d/raw.size());width/=raw.size();
        Vec3 direction=chord.scale(1/length),relative=control.subtract(start);
        // A monotonic chord coordinate prevents a small reversal from folding
        // the ribbon back on itself. Bound the bend rather than amplify noise.
        double along=Math.clamp(relative.dot(direction)/length,.15,.85);
        Vec3 bend=relative.subtract(direction.scale(relative.dot(direction)));
        if(bend.length()>length) bend=bend.normalize().scale(length);
        control=start.add(chord.scale(along)).add(bend);
        Vec3 normal=chord.cross(bend);
        if(normal.lengthSqr()<.000001) normal=chord.cross(span);
        boolean planar=normal.lengthSqr()>.000001;
        if(planar) {
            normal=normal.normalize();
            if(normal.cross(tangent(start,control,end,.5)).dot(span)<0) normal=normal.scale(-1);
            // Keep the inner edge outside the curve's cusp radius: a wide
            // weapon on a very short arc must not turn its strip inside out.
            Vec3 acceleration=end.subtract(control.scale(2)).add(start).scale(2);
            double radius=Double.POSITIVE_INFINITY;
            for(int i=0;i<=STEPS;i++) {
                Vec3 velocity=tangent(start,control,end,(double)i/STEPS);
                double cross=velocity.cross(acceleration).length();
                if(cross>.000001) radius=Math.min(radius,Math.pow(velocity.length(),3)/cross);
            }
            width=Math.min(width,radius*.75);
        }
        Vec3 fallback=span.lengthSqr()>.000001?span.normalize():raw.getLast().tip().subtract(raw.getLast().inner()).normalize();
        var result=new ArrayList<BatTrailHistory.Point>(STEPS+1);
        Vec3 previous=null;double distance=0;
        for(int i=0;i<=STEPS;i++) {
            double t=(double)i/STEPS;
            Vec3 tip=start.scale((1-t)*(1-t)).add(control.scale(2*t*(1-t))).add(end.scale(t*t));
            Vec3 side=planar?normal.cross(tangent(start,control,end,t)).normalize():fallback;
            if(previous!=null) distance+=tip.distanceTo(previous);
            result.add(new BatTrailHistory.Point(tip.subtract(side.scale(width)),tip,distance));previous=tip;
        }
        return List.copyOf(result);
    }
    private static Vec3 tangent(Vec3 a,Vec3 c,Vec3 b,double t) {
        return c.subtract(a).scale(2*(1-t)).add(b.subtract(c).scale(2*t));
    }
    private WeaponTrailCurve() { }
}
