package dev.killerranking.combat;
import dev.killerranking.visual.RushArmShape;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RushArmShapeTest {
    @Test void compressionUsesNativeGeometryAndEveryCenterlineStaysStraight() {
        for(double center:new double[]{-1,-.5,.5,1})for(double y=-2;y<=10;y+=.25) {
            var original=new Vec3(center-2,y,2);assertEquals(original,RushArmShape.deform(original.x,y,2,center,.46));
            for(double extension:new double[]{.46,1,1.8})assertEquals(new Vec3(center,y,0),RushArmShape.deform(center,y,0,center,extension));
        }
    }
    @Test void stretchFlareEmphasizesFistWithoutRotatingOrLengtheningMesh() {
        assertTrue(RushArmShape.width(6,1.8)<1,"Narrow wrist smear");assertTrue(RushArmShape.width(10,1.8)>1.3,"Flared fist");
        for(double y=-2;y<=10;y+=.1) {
            var a=RushArmShape.deform(-3,y,2,-1,1.8);assertEquals(y,a.y);assertTrue(Math.abs(a.x+1)<2.8&&Math.abs(a.z)<2.8);
        }
    }
    @Test void taperIsContinuousAndTheShoulderStaysAttachedForWideAndSlimArms() {
        for(double center:new double[]{-1,-.5,.5,1}) {
            var shoulder=RushArmShape.deform(center-2,-2,2,center,1.8);assertTrue(shoulder.distanceTo(new Vec3(center-2,-2,2))<.00001);
            for(double y=-2;y<10;y+=.05) {
                var a=RushArmShape.deform(center-2,y,2,center,1.8);var c=RushArmShape.deform(center-2,y+.001,2,center,1.8);
                assertTrue(a.distanceTo(c)<.005,"No broken strips or elbow hinge");
            }
        }
    }
}
