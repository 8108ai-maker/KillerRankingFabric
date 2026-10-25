package dev.killerranking.combat;

import dev.killerranking.visual.BatTrailHistory;
import dev.killerranking.visual.BladeSweepCurve;
import java.util.ArrayList;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BladeSweepCurveTest {
    private static ArrayList<BatTrailHistory.Point> cut(boolean mirror) {
        var points=new ArrayList<BatTrailHistory.Point>();double distance=0;Vec3 previous=null;
        for(int i=0;i<=12;i++) {
            double t=i/12d,sign=mirror?-1:1;
            var tip=new Vec3(sign*t*2,1+Math.sin(t*Math.PI)*.6,i%2==0?.02:-.02);
            var inner=new Vec3(sign*t*.5,.3,0);
            if(previous!=null) distance+=tip.distanceTo(previous);
            points.add(new BatTrailHistory.Point(inner,tip,distance));previous=tip;
        }
        return points;
    }
    @Test void leadingBoundaryKeepsBothRealBladeEndpointsAndItsFullWidth() {
        var raw=cut(false);var smooth=BladeSweepCurve.smooth(raw);
        assertEquals(raw.getFirst().inner(),smooth.getFirst().inner());
        assertEquals(raw.getFirst().tip(),smooth.getFirst().tip());
        assertEquals(raw.getLast().inner(),smooth.getLast().inner());
        assertEquals(raw.getLast().tip(),smooth.getLast().tip());
        assertEquals(raw.getLast().tip().distanceTo(raw.getLast().inner()),smooth.getLast().tip().distanceTo(smooth.getLast().inner()),.000001);
    }
    @Test void aShortWideCutNeverCollapsesToACurledNarrowCap() {
        var raw=new ArrayList<BatTrailHistory.Point>();
        raw.add(new BatTrailHistory.Point(Vec3.ZERO,new Vec3(0,1,0),0));
        raw.add(new BatTrailHistory.Point(new Vec3(.1,0,0),new Vec3(.1,1.8,0),.8));
        raw.add(new BatTrailHistory.Point(new Vec3(.2,0,0),new Vec3(.2,1,0),1.6));
        for(var point:BladeSweepCurve.smooth(raw)) assertTrue(point.tip().distanceTo(point.inner())>=1);
    }
    @Test void mirroredHandUsesTheSameSweepWithNoEndReversal() {
        var right=BladeSweepCurve.smooth(cut(false));var left=BladeSweepCurve.smooth(cut(true));
        for(int i=0;i<right.size();i++) {
            assertEquals(-right.get(i).tip().x,left.get(i).tip().x,.000001);
            assertEquals(-right.get(i).inner().x,left.get(i).inner().x,.000001);
            assertEquals(right.get(i).tip().y,left.get(i).tip().y,.000001);
        }
    }
    @Test void tipAndHiltContoursDoNotHookBackTowardTheTrailingEdge() {
        var points=BladeSweepCurve.smooth(cut(false));
        for(int i=1;i<points.size();i++) {
            assertTrue(points.get(i).tip().x>points.get(i-1).tip().x);
            assertTrue(points.get(i).inner().x>points.get(i-1).inner().x);
            assertTrue(points.get(i).distance()>points.get(i-1).distance());
        }
    }
}
