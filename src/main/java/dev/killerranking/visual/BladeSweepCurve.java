package dev.killerranking.visual;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;

/** The front boundary is the actual straight blade, not a perpendicular curved cap. */
public final class BladeSweepCurve {
    public static List<BatTrailHistory.Point> smooth(List<BatTrailHistory.Point> raw) {
        if(raw.size()<3) return List.copyOf(raw);
        double travel=raw.getLast().distance()-raw.getFirst().distance();
        if(travel<.001) return List.copyOf(raw);
        Vec3 tipControl=control(raw,travel,true),innerControl=control(raw,travel,false);
        var points=new ArrayList<BatTrailHistory.Point>(17);
        Vec3 previous=null;double distance=0;
        for(int i=0;i<=16;i++) {
            double t=i/16d;
            Vec3 tip=curve(raw.getFirst().tip(),tipControl,raw.getLast().tip(),t);
            Vec3 inner=curve(raw.getFirst().inner(),innerControl,raw.getLast().inner(),t);
            if(previous!=null) distance+=tip.distanceTo(previous);
            points.add(new BatTrailHistory.Point(inner,tip,distance));previous=tip;
        }
        return List.copyOf(points);
    }
    private static Vec3 control(List<BatTrailHistory.Point> raw,double travel,boolean tip) {
        Vec3 start=tip?raw.getFirst().tip():raw.getFirst().inner();
        Vec3 end=tip?raw.getLast().tip():raw.getLast().inner();
        Vec3 chord=end.subtract(start);double length=chord.length();
        if(length<.04) return start.lerp(end,.5);
        Vec3 control=Vec3.ZERO;double weight=0;
        for(var point:raw) {
            double t=(point.distance()-raw.getFirst().distance())/travel,w=2*t*(1-t);
            Vec3 position=tip?point.tip():point.inner();
            control=control.add(position.subtract(start.scale((1-t)*(1-t))).subtract(end.scale(t*t)).scale(w));
            weight+=w*w;
        }
        control=weight>.00001?control.scale(1/weight):start.lerp(end,.5);
        Vec3 direction=chord.scale(1/length),relative=control.subtract(start);
        double along=Math.clamp(relative.dot(direction)/length,.15,.85);
        Vec3 bend=relative.subtract(direction.scale(relative.dot(direction)));
        if(bend.length()>length) bend=bend.normalize().scale(length);
        return start.add(chord.scale(along)).add(bend);
    }
    private static Vec3 curve(Vec3 start,Vec3 control,Vec3 end,double t) {
        // Keep both leading endpoints exact, even for a very short or wide cut.
        if(t==0) return start;
        if(t==1) return end;
        return start.scale((1-t)*(1-t)).add(control.scale(2*t*(1-t))).add(end.scale(t*t));
    }
    private BladeSweepCurve() { }
}
