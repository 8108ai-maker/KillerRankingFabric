package dev.killerranking.combat;

import dev.killerranking.visual.BatTrailHistory;
import dev.killerranking.visual.WeaponTrailCurve;
import java.util.ArrayList;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WeaponTrailCurveTest {
    private static ArrayList<BatTrailHistory.Point> noisyArc(boolean mirror) {
        var points=new ArrayList<BatTrailHistory.Point>();double distance=0;Vec3 previous=null;
        for(int i=0;i<33;i++) {
            double theta=-1.2+i*2.4/32,noise=i%2==0?.06:-.06,sign=mirror?-1:1;
            var tip=new Vec3(sign*(1+noise)*Math.sin(theta),1+(1+noise)*Math.cos(theta),i%2==0?.035:-.035);
            var span=new Vec3(sign*.55*Math.sin(theta),.55*Math.cos(theta),i%3==0?.12:-.12);
            if(previous!=null) distance+=tip.distanceTo(previous);
            points.add(new BatTrailHistory.Point(tip.subtract(span),tip,distance));previous=tip;
        }
        return points;
    }
    @Test void removesAlternatingWobbleWithoutMovingTheBladeTipEndpoints() {
        for(boolean mirror:new boolean[]{false,true}) {
            var raw=noisyArc(mirror);var smooth=WeaponTrailCurve.smooth(raw);
            assertEquals(raw.getFirst().tip(),smooth.getFirst().tip());
            assertEquals(raw.getLast().tip(),smooth.getLast().tip());
            double direction=mirror?-1:1,width=smooth.getFirst().tip().distanceTo(smooth.getFirst().inner());
            for(int i=1;i<smooth.size();i++) {
                var a=smooth.get(i-1);var b=smooth.get(i);
                assertTrue(direction*(b.tip().x-a.tip().x)>0,"The trail never reverses into a fold");
                assertEquals(width,b.tip().distanceTo(b.inner()),.000001,"Wrist twist must not pulse the strip width");
                assertTrue(Math.abs(b.tip().z-a.tip().z)<.07*.25,"Adjacent depth jitter is reduced by at least 75% without flattening the swing to a world axis");
                assertTrue(b.distance()>a.distance());
            }
            for(int i=1;i<smooth.size()-1;i++) {
                var a=smooth.get(i-1).tip();var c=smooth.get(i).tip();var b=smooth.get(i+1).tip();
                assertTrue(c.y-a.y>=b.y-c.y-.000001,"A single arc has no alternating bends");
            }
        }
    }
    @Test void shortSharpArcCannotTurnTheInnerEdgeInsideOut() {
        var raw=new ArrayList<BatTrailHistory.Point>();
        raw.add(new BatTrailHistory.Point(new Vec3(0,0,0),new Vec3(0,1,0),0));
        raw.add(new BatTrailHistory.Point(new Vec3(.1,.1,0),new Vec3(.1,1.8,0),.8));
        raw.add(new BatTrailHistory.Point(new Vec3(.2,0,0),new Vec3(.2,1,0),1.6));
        var smooth=WeaponTrailCurve.smooth(raw);
        for(int i=1;i<smooth.size();i++) {
            assertTrue(smooth.get(i).inner().x>smooth.get(i-1).inner().x,"Inner boundary cannot fold backward");
            assertTrue(smooth.get(i).tip().distanceTo(smooth.get(i).inner())<.1,"Width respects the bend radius");
        }
    }
    @Test void straightThrustAndTwoSamplesRemainFiniteAndDoNotAcquireABend() {
        var raw=new ArrayList<BatTrailHistory.Point>();
        for(int i=0;i<6;i++) raw.add(new BatTrailHistory.Point(new Vec3(i*.2,0,0),new Vec3(i*.2+.5,0,0),i*.2));
        for(var p:WeaponTrailCurve.smooth(raw)) {
            assertTrue(Double.isFinite(p.inner().x+p.tip().x));assertEquals(0,p.tip().y);assertEquals(0,p.tip().z);
        }
        assertEquals(raw.subList(0,2),WeaponTrailCurve.smooth(raw.subList(0,2)));
    }
}
