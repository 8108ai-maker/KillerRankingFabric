package dev.killerranking.combat;

import dev.killerranking.visual.CombatCameraMotion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CombatCameraMotionTest {
    @Test void restsBeforeContactAndReturnsExactlyAfterImpact(){
        for(float age:new float[]{-20,-.1f,0,8,30})assertEquals(CombatCameraMotion.Pose.REST,CombatCameraMotion.sample(age,1,1,true));
        assertEquals(CombatCameraMotion.Pose.REST,CombatCameraMotion.sample(7,1,1,false));
    }
    @Test void focusHasOneFastPeakAndAnOutwardRebound(){
        float early=CombatCameraMotion.sample(.1f,1,1,true).fov();
        float peak=CombatCameraMotion.sample(.65f,1,1,true).fov();
        float recoil=CombatCameraMotion.sample(3.2f,1,1,true).fov();
        assertTrue(peak<early);assertTrue(peak<.92f);assertTrue(recoil>1&&recoil<1.025f);
    }
    @Test void parryIsStrongerThanAnOrdinarySwing(){
        assertTrue(CombatCameraMotion.sample(.65f,1,1,true).fov()<CombatCameraMotion.sample(.65f,1,1,false).fov());
    }
    @Test void mirroredStrikesKeepFocusAndPitchButReverseLateralMotion(){
        var r=CombatCameraMotion.sample(1.2f,.8f,1,false);var l=CombatCameraMotion.sample(1.2f,.8f,-1,false);
        assertEquals(r.fov(),l.fov());assertEquals(r.pitch(),l.pitch());assertEquals(r.y(),l.y());
        assertEquals(-r.yaw(),l.yaw());assertEquals(-r.roll(),l.roll());assertEquals(-r.x(),l.x());
    }
    @Test void fineShakeChangesDirectionWithinTheShortImpact(){
        var a=CombatCameraMotion.sample(.3f,1,1,true);var b=CombatCameraMotion.sample(.7f,1,1,true);
        assertTrue(a.x()>0&&b.x()<0);assertTrue(Math.abs(a.x())<.006&&Math.abs(b.x())<.006);
    }
    @Test void resamplingDoesNotAccumulateAnglesOrZoom(){
        var expected=CombatCameraMotion.sample(1.25f,1,1,true);
        for(int i=0;i<100;i++)assertEquals(expected,CombatCameraMotion.sample(1.25f,1,1,true));
    }
    @Test void curveIsContinuousBoundedAndRejectsInvalidInputs(){
        assertEquals(CombatCameraMotion.Pose.REST,CombatCameraMotion.sample(Float.NaN,1,1,true));
        assertEquals(CombatCameraMotion.Pose.REST,CombatCameraMotion.sample(1,Float.POSITIVE_INFINITY,1,true));
        var previous=CombatCameraMotion.Pose.REST;
        for(int i=1;i<=800;i++){
            var v=CombatCameraMotion.sample(i/100f,1,1,true);
            assertTrue(Math.abs(v.fov()-previous.fov())<.003);
            assertTrue(Math.abs(v.pitch()-previous.pitch())<.08);
            assertTrue(v.fov()>=.90&&v.fov()<=1.025);assertTrue(Math.abs(v.roll())<.9);
            previous=v;
        }
    }
}
