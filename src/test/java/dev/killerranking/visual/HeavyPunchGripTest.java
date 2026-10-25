package dev.killerranking.visual;
import dev.killerranking.combat.HeavyPunchRules;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HeavyPunchGripTest {
    @Test void holdIncludesTheHeadContactAndEndsBeforeRecovery(){
        assertTrue(HeavyPunchRules.holding(HeavyPunchRules.IMPACT));
        assertTrue(HeavyPunchRules.holding(HeavyPunchRules.RELEASE-1));
        assertFalse(HeavyPunchRules.holding(HeavyPunchRules.RELEASE));
        assertFalse(HeavyPunchRules.holding(-1));
        assertEquals(1,HeavyPunchGrip.gripWeight(HeavyPunchRules.IMPACT));
        assertEquals(1,HeavyPunchGrip.strike(HeavyPunchRules.IMPACT));
        assertEquals(0,HeavyPunchGrip.gripWeight(HeavyPunchRules.RELEASE));
    }
    @Test void ownWristGripMirrorsThroughTheEntirePreparationAndStrike(){
        for(float centre:new float[]{.5f,1})for(boolean fp:new boolean[]{false,true})for(boolean left:new boolean[]{false,true})for(int age=6;age<=46;age++){
            var pose=HeavyPunchGrip.pose(age,left,fp,centre);
            var wrist=pose.punch().point(left?centre:-centre,8.2,0);
            var offset=HeavyPunchGrip.braceOffset();if(left)offset=new Vec3(-offset.x,offset.y,offset.z);
            var hand=pose.brace().point(left?-centre:centre,10,0);
            assertEquals(0,hand.distanceTo(wrist.add(offset)),1e-4);
            var mirror=HeavyPunchGrip.pose(age,!left,fp,centre).punch().point(left?-centre:centre,10,0);
            var tip=pose.punch().point(left?centre:-centre,10,0);
            assertEquals(-tip.x,mirror.x,1e-4);assertEquals(tip.y,mirror.y,1e-4);assertEquals(tip.z,mirror.z,1e-4);
        }
    }
    @Test void torsoBowsAndTurnsWithItsHipAndNeckStillJoined(){
        var prep=HeavyPunchGrip.pose(30,false,false,1);var hit=HeavyPunchGrip.pose(41,false,false,1);
        assertTrue(prep.torso().rotation().x>.6&&prep.torso().rotation().y>1.9);
        assertTrue(hit.torso().rotation().x>.65&&hit.torso().rotation().y<0);
        assertTrue(hit.head().rotation().x>.85);
        for(int age=0;age<=54;age++){
            var pose=HeavyPunchGrip.pose(age,false,false,1);
            assertEquals(0,pose.torso().point(0,12,0).distanceTo(new Vec3(0,12,0)),1e-4);
            assertEquals(pose.torso().pivot(),pose.head().pivot());
        }
    }
    @Test void raisedArmStaysNearOwnShoulderAndOtherArmSupportsItFromBelow(){
        for(int age=6;age<=46;age++){
            var pose=HeavyPunchGrip.pose(age,false,false,1);
            assertTrue(pose.punch().point(-1,0,0).distanceTo(pose.torso().point(-5,2,0))<7);
            assertTrue(pose.brace().point(1,0,0).distanceTo(pose.torso().point(5,2,0))<9);
        }
        var pose=HeavyPunchGrip.pose(41,false,false,1);
        assertTrue(pose.punch().point(-1,10,0).y<pose.head().pivot().y);
        assertTrue(pose.brace().direction().y<-.2);
        assertTrue(Math.abs(pose.punch().direction().y)<.2);
        assertEquals(pose.punch(),HeavyPunchGrip.pose(44,false,false,1).punch());
        var rightFoot=pose.rightLeg().point(0,12,0);var leftFoot=pose.leftLeg().point(0,12,0);
        assertTrue(leftFoot.x-rightFoot.x>9,"Feet must spread outward at contact");
        assertTrue(leftFoot.z-rightFoot.z>10,"A forward and rear foot must brace the strike");
        assertEquals(HeavyPunchGrip.pose(38,false,false,1).rightLeg(),pose.rightLeg());
        assertEquals(pose.leftLeg(),HeavyPunchGrip.pose(44,false,false,1).leftLeg());
    }
}
