package dev.killerranking.combat;
import dev.killerranking.network.*;
import dev.killerranking.visual.*;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class KnifeAirCounterTest {
    @Test void deflectedKnifeRisesThenFallsAndOnlyTheDefinedTimingCanChain(){
        var at=new Vec3(3,2,8);assertEquals(at,KnifePursuitRules.deflected(at,0));
        assertEquals(at.y+3,KnifePursuitRules.deflected(at,15).y,.000001);
        assertTrue(KnifePursuitRules.deflected(at,24).y<KnifePursuitRules.deflected(at,15).y);
        for(double age:new double[]{0,3.99,18.01,24,Double.NaN,Double.POSITIVE_INFINITY})assertFalse(KnifePursuitRules.counterWindow(age));
        assertTrue(KnifePursuitRules.counterWindow(4));assertTrue(KnifePursuitRules.counterWindow(18));
    }
    @Test void casterJumpsToTheAirCatchHoldsThenDropsQuicklyOntoTheLanding(){
        var start=new Vec3(0,0,0);var landing=new Vec3(.2,0,7);var knife=new Vec3(0,3.2,8);
        assertEquals(start,ShoulderStabRules.counterPosition(start,landing,knife,0));
        var caught=ShoulderStabRules.counterPosition(start,landing,knife,6);
        assertEquals(landing.x,caught.x);assertEquals(landing.z,caught.z);assertTrue(caught.y>1);
        assertEquals(caught,ShoulderStabRules.counterPosition(start,landing,knife,10));
        assertEquals(landing,ShoulderStabRules.counterPosition(start,landing,knife,12));
        assertEquals(landing,ShoulderStabRules.counterPosition(start,landing,knife,26));
        assertEquals(knife,ShoulderStabRules.counterKnife(knife.add(0,-.5,0),knife,6));
    }
    @Test void sameRigidHandCatchesTheAirHandleThenDrivesTheActualTipIntoTheVictim(){
        var wound=new Vec3(2,-4,-14);var direction=new Vec3(.1,.96,-.28).normalize();var air=new Vec3(4,-20,-12);
        for(boolean left:new boolean[]{false,true})for(boolean fp:new boolean[]{false,true})for(float centre:new float[]{.5f,1}){
            for(float age:new float[]{6,8,10}){var pose=ShoulderStabMotion.counterContact(age,left,wound,direction,air,centre,fp);var hand=left?pose.leftArm():pose.rightArm();
                assertEquals(0,hand.point(left?centre:-centre,10,0).distanceTo(air),.00001);
                assertEquals(10,hand.point(0,10,0).distanceTo(hand.pivot()),.00001);
            }
            var pose=ShoulderStabMotion.counterContact(12,left,wound,direction,air,centre,fp);var hand=left?pose.leftArm():pose.rightArm();
            assertEquals(0,hand.point(left?centre:-centre,10,0).add(direction.scale(SalmosaKnifeGeometry.SKILL_REACH)).distanceTo(wound),.00001);
        }
    }
    @Test void packetsPreserveAirPhaseCatchPointAndCounterOperation(){
        var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);try{
            var request=new ShoulderStabRequestPayload(ShoulderStabRequestPayload.COUNTER,-1);ShoulderStabRequestPayload.CODEC.encode(b,request);assertEquals(request,ShoulderStabRequestPayload.CODEC.decode(b));
            var v=new ShoulderStabVisualPayload(UUID.randomUUID(),UUID.randomUUID(),3,6,90,Vec3.ZERO,new Vec3(0,1.5,8),new Vec3(0,0,7),new Vec3(0,2,8),new Vec3(0,3,8));
            ShoulderStabVisualPayload.CODEC.encode(b,v);assertEquals(v,ShoulderStabVisualPayload.CODEC.decode(b));
            var x=new KnifePursuitStatusPayload(true,3,4,390);KnifePursuitStatusPayload.CODEC.encode(b,x);assertEquals(x,KnifePursuitStatusPayload.CODEC.decode(b));
            var c=new ShoulderStabStatusPayload(true,3,12,390);ShoulderStabStatusPayload.CODEC.encode(b,c);assertEquals(c,ShoulderStabStatusPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
        }finally{b.release();}
    }
}
