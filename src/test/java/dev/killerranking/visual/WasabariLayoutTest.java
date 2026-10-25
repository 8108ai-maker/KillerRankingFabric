package dev.killerranking.visual;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class WasabariLayoutTest {
    @Test void largeGroundedBodiesDoNotUseTheCappedHumanoidGap(){
        assertEquals(1.55,WasabariLayout.gap(.6,.6,2,false),.001);
        assertTrue(WasabariLayout.gap(.6,.9,2.9,false)>2);
        assertTrue(WasabariLayout.gap(.6,1.4,2.7,false)>1.9);
        assertTrue(WasabariLayout.gap(.6,1.95,2.2,true)>1.7);
        for(double width:new double[]{.6,.9,1.4,1.95,3})for(double height:new double[]{1,2,2.9,4})for(boolean flipped:new boolean[]{false,true})
            assertTrue(WasabariLayout.gap(.6,width,height,flipped)>(.6+width)*.5);
    }
}
