package dev.killerranking.visual;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RookMountLayoutTest {
    @Test void pelvisSitsOnVictimInsteadOfAddingTheStandingHipHeight() {
        for(double scale:new double[]{1,2.17/1.8,2})for(boolean inverted:new boolean[]{true,false}) {
            double root=RookMountLayout.rootOffset(1.4,.9,inverted,scale);
            assertEquals(RookMountLayout.surface(1.4,.9,inverted),root+.75*scale,1e-9);
            assertTrue(root<(inverted?1.4*.55:.12));
        }
    }
    @Test void detachedFeetStayAtGroundHeightForBipedsAndAnimalsOfDifferentSizes() {
        for(double scale:new double[]{1,2.17/1.8,2})for(boolean inverted:new boolean[]{true,false})for(double height:new double[]{.8,1.4,1.8}) {
            double root=RookMountLayout.rootOffset(height,.6,inverted,scale);
            double lift=RookMountLayout.legLift(height,.6,inverted,scale);
            assertEquals(.03,root+lift*scale/16,1e-9);
        }
    }
    @Test void spacingClearsTorsoAndMatchesCapturedBodyWithoutTheOldWideGap() {
        double scale=2.17/1.8;
        for(double width:new double[]{.6,.9,1.2}) {
            double spread=RookMountLayout.spread(width,scale);
            assertTrue(1.9+spread-2>4);
            double inner=(1.9+spread-2)*scale/16;
            assertTrue(inner>=width/2&&inner-width/2<.04);
            assertTrue(spread<width*8);
        }
    }
}
