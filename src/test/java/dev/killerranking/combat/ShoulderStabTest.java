package dev.killerranking.combat;
import com.mojang.serialization.JsonOps;
import dev.killerranking.data.ShoulderStabState;
import dev.killerranking.network.*;
import dev.killerranking.visual.*;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ShoulderStabTest {
    @Test void blinkChangesPositionOnOneBeatWithoutIntermediateTravel(){
        var from=new Vec3(1,2,3);var to=new Vec3(2,2,14);
        for(float age:new float[]{0,2,3.9f,4,4.01f,8,23})assertEquals(age<4?from:to,ShoulderStabRules.position(from,to,age));
        assertTrue(ShoulderStabRules.BLINK<ShoulderStabRules.IMPACT);
        assertFalse(ShoulderStabRules.inRange(144.001));assertFalse(ShoulderStabRules.inRange(Double.NaN));assertFalse(ShoulderStabRules.inRange(Double.POSITIVE_INFINITY));assertFalse(ShoulderStabRules.inRange(-1));assertTrue(ShoulderStabRules.inRange(144));
    }
    @Test void reverseGripPlacesActualBladeTipOnShoulderForBothHandsAndPlayerModels(){
        for(boolean left:new boolean[]{false,true})for(boolean firstPerson:new boolean[]{false,true})for(float centre:new float[]{1,.5f})for(var wound:new Vec3[]{new Vec3(-3,-2,-12),new Vec3(4,-15,-16)}){
            var direction=new Vec3(.1,.96,-.28).normalize();var pose=ShoulderStabMotion.contact(8,left,wound,direction,centre,firstPerson);
            var limb=left?pose.leftArm():pose.rightArm();var palm=limb.point(left?centre:-centre,10,0);
            assertEquals(0,palm.add(direction.scale(SalmosaKnifeGeometry.SKILL_REACH)).distanceTo(wound),.00001);
            assertEquals(10,limb.point(0,10,0).distanceTo(limb.point(0,0,0)),.00001);
            var loaded=ShoulderStabMotion.contact(4,left,wound,direction,centre,firstPerson);var loadedArm=left?loaded.leftArm():loaded.rightArm();
            assertTrue(loadedArm.point(left?centre:-centre,10,0).y<palm.y-5);
        }
    }
    @Test void shoulderAnchorTracksTargetHeightWidthYawAndHand(){
        var origin=new Vec3(3,4,5);var right=ShoulderStabRules.shoulder(origin,.6,1.8,0,false);var left=ShoulderStabRules.shoulder(origin,.6,1.8,0,true);
        assertEquals(origin.y+1.8*.82,right.y,.000001);assertEquals(left.y,right.y);assertEquals(.36,left.x-right.x,.000001);
        var rotated=ShoulderStabRules.shoulder(origin,.6,1.8,90,false);
        assertEquals(right.subtract(origin).length(),rotated.subtract(origin).length(),.000001);
        assertTrue(ShoulderStabRules.incoming(0).y<-.9);
    }
    @Test void independentCooldownPersistsAndCannotBeRestartedDuringCooldown(){
        var state=ShoulderStabState.READY.cast(100);assertEquals(400,state.cooldown(100));assertEquals(state,state.cast(200));assertEquals(0,state.cooldown(500));
        var json=ShoulderStabState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();assertEquals(state,ShoulderStabState.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow());
        assertEquals(0,ShoulderStabState.CODEC.parse(JsonOps.INSTANCE,new com.google.gson.JsonObject()).getOrThrow().cooldown(0));
    }
    @Test void authoritativePacketsRoundTripTargetAndVisualContactWithoutRegistrySideEffects(){
        var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);try{
            var request=new ShoulderStabRequestPayload(ShoulderStabRequestPayload.RELEASE,17);ShoulderStabRequestPayload.CODEC.encode(b,request);assertEquals(request,ShoulderStabRequestPayload.CODEC.decode(b));
            var status=new ShoulderStabStatusPayload(true,2,8,399);ShoulderStabStatusPayload.CODEC.encode(b,status);assertEquals(status,ShoulderStabStatusPayload.CODEC.decode(b));
            var visual=new ShoulderStabVisualPayload(UUID.randomUUID(),UUID.randomUUID(),2,8,15,Vec3.ZERO,new Vec3(.2,1.5,8),new Vec3(0,0,7));
            ShoulderStabVisualPayload.CODEC.encode(b,visual);assertEquals(visual,ShoulderStabVisualPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
        }finally{b.release();}
    }
}
