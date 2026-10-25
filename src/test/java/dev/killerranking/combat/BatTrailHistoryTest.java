package dev.killerranking.combat;

import dev.killerranking.visual.BatTrailHistory;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BatTrailHistoryTest {
    @Test void aLiveBladeSweepDoesNotExpireWhileTheBladeIsStillCutting() {
        var h=new BatTrailHistory(true);
        for(int tick=0;tick<=20;tick++) {
            double angle=tick*.12;
            var tip=new Vec3(2*Math.cos(angle),1,2*Math.sin(angle));
            h.sample(tick,tip.scale(.5),tip,Vec3.ZERO,true);
            if(tick>0) {
                var ribbons=h.visible(tick);assertEquals(1,ribbons.size());
                assertEquals(tick,ribbons.getFirst().born(),.0001,"Fade starts at the latest blade sample");
                assertEquals(tip,ribbons.getFirst().points().getLast().tip(),"Leading end follows the blade");
            }
        }
        h.resetStroke();assertFalse(h.visible(26.99).isEmpty());assertTrue(h.visible(27).isEmpty());
    }
    @Test void aLiveSweepDropsOldTailPointsAndDoesNotBridgeTheNextAttack() {
        var h=new BatTrailHistory(true);
        for(int tick=0;tick<=20;tick++)h.sample(tick,new Vec3(tick*.2,1,0),new Vec3(tick*.2,2,0),Vec3.ZERO,true);
        var points=h.visible(20).getFirst().points();
        assertTrue(points.getFirst().tip().x>=2.6,"Old trail tail is bounded to the trailing window");
        h.resetStroke();h.sample(20.5,new Vec3(-3,1,0),new Vec3(-3,2,0),Vec3.ZERO,true);
        assertEquals(1,h.visible(20.5).size());
        h.sample(21,new Vec3(-2.8,1,0),new Vec3(-2.8,2,0),Vec3.ZERO,true);
        assertEquals(2,h.visible(21).size());
        assertEquals(-3,h.visible(21).getLast().points().getFirst().tip().x,.001);
    }
    @Test void liveSweepClockResetAndTeleportStillClearSampleContinuity() {
        var h=new BatTrailHistory(true);
        h.sample(10,Vec3.ZERO,new Vec3(0,1,0),Vec3.ZERO,true);
        h.sample(11,new Vec3(1,0,0),new Vec3(1,1,0),Vec3.ZERO,true);
        h.sample(12,new Vec3(30,0,0),new Vec3(30,1,0),new Vec3(29,0,0),true);
        assertEquals(1,h.visible(12).size(),"Teleport must not append a long ribbon");
        h.sample(0,Vec3.ZERO,new Vec3(0,1,0),Vec3.ZERO,true);
        assertTrue(h.visible(0).isEmpty());
    }
    @Test void aBroadSkillCutSurvivesSkippedFramesWithoutBridgingTheNextCut(){
        var h=new BatTrailHistory();
        h.sampleStroke(10,new Vec3(-1.5,1,1),new Vec3(-1.5,1,2),Vec3.ZERO,true,1);
        h.sampleStroke(12,new Vec3(1.5,1,1),new Vec3(1.5,1,2),Vec3.ZERO,true,1);
        assertEquals(1,h.visible(12).size(),"A fast three-metre sweep still appears at low FPS");
        h.sampleStroke(14,new Vec3(-1.5,1,1),new Vec3(-1.5,1,2),Vec3.ZERO,true,2);
        assertEquals(1,h.visible(14).size(),"First sample of a different cut never bridges the old ribbon");
        h.sampleStroke(16,new Vec3(1.5,1,1),new Vec3(1.5,1,2),Vec3.ZERO,true,2);
        assertEquals(2,h.visible(16).size());
        h.sampleStroke(17,new Vec3(21.5,1,1),new Vec3(21.5,1,2),new Vec3(20,0,0),true,2);
        assertEquals(2,h.visible(17).size(),"Actual teleport is still rejected");
    }
    @Test void reversalCreatesSeparateOverlappingRibbonsWithoutAConnectingZigzag(){
        var h=new BatTrailHistory();
        h.sampleStroke(0,new Vec3(-1,1,1),new Vec3(-1,1,2),Vec3.ZERO,true,1);
        h.sampleStroke(.5,new Vec3(1,1,1),new Vec3(1,1,2),Vec3.ZERO,true,1);
        h.sampleStroke(1,new Vec3(1,1,1),new Vec3(1,1,2),Vec3.ZERO,true,2);
        h.sampleStroke(1.5,new Vec3(-1,1,1),new Vec3(-1,1,2),Vec3.ZERO,true,2);
        var ribbons=h.visible(1.5);assertEquals(2,ribbons.size());
        assertEquals(1,ribbons.getFirst().points().getLast().tip().x,.001);
        assertEquals(1,ribbons.getLast().points().getFirst().tip().x,.001);
        assertEquals(-1,ribbons.getLast().points().getLast().tip().x,.001);
        assertEquals(2,ribbons.getLast().points().size(),"Separate strokes never interpolate across the reversal");
    }
    @Test void ribbonFollowsBothActualBladeEndpointsAndExpires() {
        var h=new BatTrailHistory();
        h.sample(10,new Vec3(0,1,1),new Vec3(0,1,2),Vec3.ZERO,true);
        assertTrue(h.empty());assertFalse(h.canDiscard(10));
        h.sample(10.5,new Vec3(.6,1,1),new Vec3(.6,1,2),Vec3.ZERO,true);
        var ribbons=h.visible(10.5);assertEquals(1,ribbons.size());
        var points=ribbons.getFirst().points();assertEquals(2,points.size());
        assertEquals(new Vec3(0,1,1),points.getFirst().inner());
        assertEquals(new Vec3(0,1,2),points.getFirst().tip());
        assertEquals(new Vec3(.6,1,1),points.getLast().inner());
        assertEquals(new Vec3(.6,1,2),points.getLast().tip());
        assertEquals(.6,points.getLast().distance(),.0001);
        assertTrue(h.visible(17.5).isEmpty());
    }
    @Test void idleWalkingTeleportAndNewAttackCannotDrawConnectingLines() {
        var h=new BatTrailHistory();var a=new Vec3(0,1,1);var b=new Vec3(0,1,2);
        h.sample(1,a,b,Vec3.ZERO,true);
        h.sample(2,a.add(1,0,0),b.add(1,0,0),new Vec3(1,0,0),true);
        assertTrue(h.empty(),"Walking alone has no trail");
        h.sample(3,a.add(20,0,0),b.add(20,0,0),new Vec3(20,0,0),true);
        assertTrue(h.empty(),"Teleport has no trail");
        h.sample(4,a,b,Vec3.ZERO,false);
        h.sample(5,a.add(1,0,0),b.add(1,0,0),Vec3.ZERO,true);
        assertTrue(h.empty(),"New attack starts with a fresh sample");
    }
    @Test void eightTextureFramesProgressAndNeverWrapAfterExpiry() {
        for(int frame=0;frame<8;frame++) assertEquals(frame,BatTrailHistory.frameAt((frame+.5)*BatTrailHistory.LIFETIME/8));
        assertEquals(-1,BatTrailHistory.frameAt(-.01));
        assertEquals(-1,BatTrailHistory.frameAt(BatTrailHistory.LIFETIME));
        assertEquals(-1,BatTrailHistory.frameAt(Double.NaN));
    }
    @Test void strokesStaySeparateDuringCombosAndMemoryIsBounded() {
        var h=new BatTrailHistory();
        for(int i=0;i<1000;i++) {
            double t=i*.13,x=Math.sin(t)*1.5;
            h.sample(t,new Vec3(x,1,1),new Vec3(x,1,2),Vec3.ZERO,true);
            var ribbons=h.visible(t);assertTrue(ribbons.size()<=BatTrailHistory.MAX_STROKES);
            for(var r:ribbons) assertTrue(r.points().size()<=BatTrailHistory.MAX_POINTS);
        }
        h.sample(0,Vec3.ZERO,new Vec3(0,1,0),Vec3.ZERO,true);
        assertTrue(h.empty(),"Clock/world reset clears previous geometry");
        h.sample(.5,new Vec3(1,0,0),new Vec3(1,1,0),Vec3.ZERO,true);
        h.sample(1,Vec3.ZERO,Vec3.ZERO,Vec3.ZERO,false);
        h.sample(1.5,new Vec3(2,0,0),new Vec3(2,1,0),Vec3.ZERO,true);
        h.sample(2,new Vec3(3,0,0),new Vec3(3,1,0),Vec3.ZERO,true);
        var ribbons=h.visible(2);assertEquals(2,ribbons.size());
        assertEquals(new Vec3(2,1,0),ribbons.getLast().points().getFirst().tip());
    }
}
