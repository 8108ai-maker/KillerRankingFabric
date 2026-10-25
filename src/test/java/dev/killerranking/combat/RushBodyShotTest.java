package dev.killerranking.combat;

import dev.killerranking.visual.RushBodyShot;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RushBodyShotTest {
    @Test void upperArmAndElbowStayInPlace(){
        for(double bend:new double[]{-.8,-.5,-.12})for(double y=-2;y<=3.4;y+=.2){
            var p=new Vec3(-1,y,1);assertEquals(p,RushBodyShot.point(p.x,p.y,p.z,bend));
        }
        assertEquals(new Vec3(-1,4,0),RushBodyShot.point(-1,4,0,-.8));
    }
    @Test void forearmFoldsForwardWithoutGrowingOrMovingSideways(){
        for(double bend=-.8;bend<=-.12;bend+=.02){
            var tip=RushBodyShot.point(-1,10,0,bend);
            assertEquals(-1,tip.x);assertTrue(tip.z<0);assertEquals(6,tip.distanceTo(new Vec3(-1,4,0)),1e-6);
        }
    }
    @Test void skinRemainsConnectedAtTheElbow(){
        for(double bend:new double[]{-.8,-.5,-.12})for(double y=3.2;y<4.8;y+=.01){
            var a=RushBodyShot.point(-3,y,2,bend);var b=RushBodyShot.point(-3,y+.001,2,bend);
            assertTrue(a.distanceTo(b)<.005);
        }
    }
}
