package dev.killerranking.combat;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BishopTacticsTest {
    @Test void aFleeingOrDistantTargetCallsForStraightThrusts(){
        for(int formation=0;formation<6;formation++)for(int cycle=0;cycle<6;cycle++){
            assertEquals(BishopRules.STAB_COMBO,BishopTactics.attack(cycle,formation,2.1,0));
            assertEquals(BishopRules.STAB_COMBO,BishopTactics.attack(cycle,formation,1.6,.2));
        }
    }
    @Test void pursuitLeadsHorizontalMotionWithoutPredictingUnboundedTeleports(){
        var at=new Vec3(2,3,4);assertEquals(new Vec3(2.4,3,4),BishopTactics.lead(at,new Vec3(.2,2,0),2));
        assertEquals(.8,BishopTactics.lead(at,new Vec3(20,-10,10),3).distanceTo(at),.0001);
        assertEquals(at,BishopTactics.lead(at,new Vec3(Double.NaN,0,0),3));
    }
    @Test void meleeStepsAndEntryLanesStayForwardAndBounded(){
        assertEquals(0,BishopTactics.step(.8,true));assertEquals(0,BishopTactics.step(Double.NaN,true));
        assertTrue(BishopTactics.step(2.4,true)>.3);assertTrue(BishopTactics.step(20,true)<=.46);
        var self=Vec3.ZERO;var target=new Vec3(0,0,8);
        var a=BishopTactics.approach(self,target,0);var c=BishopTactics.approach(self,target,1);
        assertTrue(a.z>0&&a.z<target.z);assertEquals(a.z,c.z);assertEquals(a.x,-c.x,.0001);
        assertEquals(target,BishopTactics.approach(target,target,0));
    }
}
