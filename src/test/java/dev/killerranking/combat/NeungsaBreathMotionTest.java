package dev.killerranking.combat;

import dev.killerranking.visual.NeungsaBreathMotion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NeungsaBreathMotionTest {
    @Test void bothCurledStreamsConvergeAtTheMouthAndRemainBelowTheChin(){
        for(int side:new int[]{-1,1}){
            var end=NeungsaBreathMotion.point(1,side,18);
            assertEquals(0,end.side(),.00001);assertEquals(0,end.height(),.00001);assertEquals(0,end.forward(),.00001);
            assertTrue(NeungsaBreathMotion.point(0,side,18).height()>.15);
            assertTrue(NeungsaBreathMotion.point(.5f,side,18).height()<-.20);
        }
    }
    @Test void pairedStripsHaveNoLargeGapsOrInvertedWidthsAcrossTheCharge(){
        for(int age=8;age<=300;age+=4)for(int i=0;i<NeungsaBreathMotion.SEGMENTS;i++){
            float u=i/(float)NeungsaBreathMotion.SEGMENTS;
            var a=NeungsaBreathMotion.point(u,1,age);var b=NeungsaBreathMotion.point((i+1)/(float)NeungsaBreathMotion.SEGMENTS,1,age);
            var mirror=NeungsaBreathMotion.point(u,-1,age);
            assertEquals(-a.side(),mirror.side());assertEquals(a.height(),mirror.height());
            double gap=Math.sqrt(Math.pow(a.side()-b.side(),2)+Math.pow(a.height()-b.height(),2)+Math.pow(a.forward()-b.forward(),2));
            assertTrue(gap>0&&gap<.1,"Joined tessellation keeps a continuous ribbon");
            assertTrue(NeungsaBreathMotion.width(u)>.005);
        }
    }
    @Test void brightBandsTravelFromTheOuterCurlTowardTheMouth(){
        for(float u=.1f;u<.8f;u+=.1f)
            assertEquals(NeungsaBreathMotion.flow(u,20),NeungsaBreathMotion.flow(u+.05f,21),.00001);
    }
}
