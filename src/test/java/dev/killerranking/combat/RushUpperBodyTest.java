package dev.killerranking.combat;

import dev.killerranking.visual.RushUpperBody;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RushUpperBodyTest {
    @Test void exactPlayerOriginIsRetainedAtAllFacingsSizesAndPositions(){
        for(var player:new Vec3[]{new Vec3(4,5,6),new Vec3(-70,-60,132)})for(float yaw:new float[]{0,45,90,180,270})for(float scale:new float[]{1,1.2f,.8f}){
            assertEquals(0,player.distanceTo(RushUpperBody.origin(player,yaw,scale)),1e-8);
            assertEquals(yaw,RushUpperBody.facing(yaw),1e-6);
        }
    }
    @Test void rendersNativeUpperBodyProportionsWithoutBabyOrLimbScaling(){
        var root=RushUpperBody.root(Vec3.ZERO,0,1,Vec3.ZERO,Vec3.ZERO);
        var head=root.transformPosition(new Vector3f(0,-.25f,0));
        var shoulders=root.transformPosition(new Vector3f(0,.125f,0));
        assertEquals(.375*.9375,head.distance(shoulders),1e-5);
        var waist=root.transformPosition(new Vector3f(0,.75f,0));
        assertEquals(.75*.9375,root.transformPosition(new Vector3f(0,0,0)).distance(waist),1e-5);
        for(var axis:new Vector3f[]{new Vector3f(1,0,0),new Vector3f(0,1,0),new Vector3f(0,0,1)})
            assertEquals(.9375,root.transformDirection(axis).length(),1e-5);
    }
    @Test void colocatedEchoFacesTheSameDirectionWithoutAnExtraRotation(){
        var root=RushUpperBody.root(Vec3.ZERO,0,1,Vec3.ZERO,Vec3.ZERO);
        var direction=root.transformDirection(new Vector3f(0,0,-1)).normalize();
        var origin=RushUpperBody.origin(Vec3.ZERO,0,1);
        var toTarget=new Vec3(0,origin.y,1.6).subtract(origin).normalize();
        assertEquals(toTarget.x,direction.x,1e-5);assertEquals(toTarget.z,direction.z,1e-5);
    }
}
