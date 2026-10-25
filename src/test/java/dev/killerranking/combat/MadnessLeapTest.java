package dev.killerranking.combat;

import dev.killerranking.data.MadnessLeapState;
import dev.killerranking.network.*;
import dev.killerranking.visual.FirstPersonWeaponPose;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import dev.kosmx.playerAnim.core.util.Vec3f;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class MadnessLeapTest {
    @BeforeAll static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
    @Test void inclusiveTwoToTenMetresAndInvalidDistances(){
        assertFalse(MadnessLeapRules.inRange(3.999));assertTrue(MadnessLeapRules.inRange(4));
        assertTrue(MadnessLeapRules.inRange(100));assertFalse(MadnessLeapRules.inRange(100.001));
        assertFalse(MadnessLeapRules.inRange(Double.NaN));assertFalse(MadnessLeapRules.inRange(Double.POSITIVE_INFINITY));
    }
    @Test void jumpHasWindupSinglePeakAndExactLanding(){
        var from=new Vec3(2,4,8);var to=new Vec3(-3,4,1);
        assertEquals(from,MadnessLeapRules.position(from,to,0));assertEquals(from,MadnessLeapRules.position(from,to,MadnessLeapRules.HOP));
        var peak=MadnessLeapRules.position(from,to,(MadnessLeapRules.HOP+MadnessLeapRules.HIT)/2.0);
        assertEquals(4+MadnessLeapRules.HEIGHT,peak.y,1e-9);
        assertEquals(to,MadnessLeapRules.position(from,to,MadnessLeapRules.HIT));
        assertEquals(to,MadnessLeapRules.position(from,to,MadnessLeapRules.DURATION));
        var previous=from;
        for(int i=0;i<=MadnessLeapRules.HIT;i++){
            var next=MadnessLeapRules.position(from,to,i);assertTrue(next.distanceToSqr(previous)<2);previous=next;
        }
    }
    @Test void twentySecondCooldownCannotBeRestartedAndSafeOldSaveDefaults(){
        var s=MadnessLeapState.READY.cast(100);
        assertEquals(400,s.cooldown(100));assertEquals(s,s.cast(101));assertEquals(1,s.cooldown(499));assertEquals(0,s.cooldown(500));
        assertEquals(900,s.cast(500).readyAt());assertEquals(0,new MadnessLeapState(-1).readyAt());
        assertEquals(MadnessLeapState.READY,MadnessLeapState.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("{}")).getOrThrow());
        var saved=MadnessLeapState.CODEC.encodeStart(JsonOps.INSTANCE,s).getOrThrow();assertEquals(s,MadnessLeapState.CODEC.parse(JsonOps.INSTANCE,saved).getOrThrow());
    }
    @Test void allPhasesAndRequestsConsumeExactlyTheirBytes(){
        var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try{
            for(int phase=0;phase<=2;phase++){
                var status=new MadnessLeapStatusPayload(true,phase,12,400);
                MadnessLeapStatusPayload.CODEC.encode(b,status);assertEquals(status,MadnessLeapStatusPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
                var visual=new MadnessLeapVisualPayload(UUID.randomUUID(),phase,12,45);
                MadnessLeapVisualPayload.CODEC.encode(b,visual);assertEquals(visual,MadnessLeapVisualPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
                var request=new MadnessLeapRequestPayload(phase,2147483647);
                MadnessLeapRequestPayload.CODEC.encode(b,request);assertEquals(request,MadnessLeapRequestPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
            }
            assertEquals(0,new MadnessLeapStatusPayload(false,-1,-5,-3).phase());
            assertEquals(400,new MadnessLeapStatusPayload(true,100,1000,10000).cooldown());
            assertEquals(0,new MadnessLeapVisualPayload(UUID.randomUUID(),1,0,Float.NaN).yaw());
        }finally{b.release();}
    }
    private KeyframeAnimationPlayer animation()throws Exception {
        try(var reader=Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/madness_leap.json"))){
            return new KeyframeAnimationPlayer(AnimationSerializing.deserializeAnimation(reader).getFirst());
        }
    }
    @Test void threeUprightTurnsWithNativeLimbsAndFreeHead()throws Exception {
        var a=animation();var head=new Vec3f(.1f,.2f,.3f);float previous=0,total=0;
        for(int tick=0;tick<=30;tick++){
            a.setupAnim(0);
            var body=a.get3DTransform("body",TransformType.ROTATION,0,Vec3f.ZERO);
            assertTrue(Math.abs(body.getX())<.3,"No somersault pitch");assertEquals(0,body.getZ(),1e-5);
            if(tick<=MadnessLeapRules.SPIN_END){float d=body.getY()-previous;while(d<-Math.PI)d+=2*(float)Math.PI;while(d>Math.PI)d-=2*(float)Math.PI;total+=d;previous=body.getY();}
            for(String part:new String[]{"rightArm","leftArm","rightLeg","leftLeg"}){
                var scale=a.get3DTransform(part,TransformType.SCALE,0,new Vec3f(1,1,1));
                assertEquals(1,scale.getX(),1e-5);assertEquals(1,scale.getY(),1e-5);assertEquals(1,scale.getZ(),1e-5);
            }
            var h=a.get3DTransform("head",TransformType.ROTATION,0,head);assertEquals(head.getX(),h.getX(),1e-5);assertEquals(head.getY(),h.getY(),1e-5);
            if(tick==MadnessLeapRules.HIT){
                assertEquals(1,a.get3DTransform("nightBatTrail",TransformType.POSITION,0,Vec3f.ZERO).getY(),1e-5);
                assertEquals(-.7,a.get3DTransform("rightArm",TransformType.ROTATION,0,Vec3f.ZERO).getX(),1e-5);
            }
            a.tick();
        }
        assertEquals(MadnessLeapRules.SPINS*2*Math.PI,total,1e-4);assertFalse(a.isActive());
    }
    @Test void firstPersonCancelsBodyRotationWithoutChangingWristOrArmSize()throws Exception {
        var a=animation();var name=a.getData().extraData.get("name");assertTrue(FirstPersonWeaponPose.supports(name));
        var wrist=new Vec3f(.2f,.3f,.4f);
        assertEquals(0,FirstPersonWeaponPose.adjust(name,"body",TransformType.ROTATION,new Vec3f(3,0,0),11,28,30).getX());
        assertSame(wrist,FirstPersonWeaponPose.adjust(name,"rightItem",TransformType.ROTATION,wrist,22,28,30));
        assertSame(wrist,FirstPersonWeaponPose.adjust(name,"rightArm",TransformType.SCALE,wrist,22,28,30));
    }
}
