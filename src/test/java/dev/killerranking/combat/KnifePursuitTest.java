package dev.killerranking.combat;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.killerranking.data.KnifePursuitState;
import dev.killerranking.network.*;
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

class KnifePursuitTest {
    @BeforeAll static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
    @Test void targetingRejectsInvalidDistancesAndIncludesTwelveMetres(){
        assertTrue(KnifePursuitRules.inRange(0));assertTrue(KnifePursuitRules.inRange(144));
        for(double value:new double[]{144.001,-1,Double.NaN,Double.POSITIVE_INFINITY})assertFalse(KnifePursuitRules.inRange(value));
    }
    @Test void flightEmbedsInTwoTicksBeforeSingleBlinkAndWithdrawal(){
        var from=new Vec3(1,3,4);var to=new Vec3(-3,3,10);
        assertEquals(from,KnifePursuitRules.flight(from,to,KnifePursuitRules.THROW));
        assertEquals(to,KnifePursuitRules.flight(from,to,KnifePursuitRules.IMPACT));
        assertEquals(from,KnifePursuitRules.blink(from,to,KnifePursuitRules.BLINK-.001));
        assertEquals(to,KnifePursuitRules.blink(from,to,KnifePursuitRules.BLINK));
        assertEquals(to,KnifePursuitRules.blink(from,to,KnifePursuitRules.BLINK+.001));
        assertEquals(2,KnifePursuitRules.IMPACT-KnifePursuitRules.THROW);
        assertTrue(KnifePursuitRules.IMPACT<KnifePursuitRules.BLINK&&KnifePursuitRules.BLINK<KnifePursuitRules.EXTRACT);
        assertFalse(KnifePursuitRules.away(5));assertTrue(KnifePursuitRules.away(6));assertTrue(KnifePursuitRules.away(21.99));assertFalse(KnifePursuitRules.away(22));
    }
    @Test void cooldownSurvivesSavingAndCannotBeResetByAnotherCast(){
        var ready=KnifePursuitState.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("{}")).getOrThrow();
        var used=ready.cast(500);assertEquals(400,used.cooldown(500));assertEquals(used,used.cast(501));
        assertEquals(1,used.cooldown(899));assertEquals(0,used.cooldown(900));assertEquals(1300,used.cast(900).readyAt());
        assertEquals(used,KnifePursuitState.CODEC.parse(JsonOps.INSTANCE,KnifePursuitState.CODEC.encodeStart(JsonOps.INSTANCE,used).getOrThrow()).getOrThrow());
    }
    @Test void networkPreservesTargetOriginWoundAndConsumesExactlyItsBytes(){
        var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try{for(int phase=0;phase<=2;phase++){
            var visual=new KnifePursuitVisualPayload(UUID.randomUUID(),UUID.randomUUID(),phase,25,170,new Vec3(4,3,2),new Vec3(6,3,5),new Vec3(6,2,4),true);
            KnifePursuitVisualPayload.CODEC.encode(buffer,visual);assertEquals(visual,KnifePursuitVisualPayload.CODEC.decode(buffer));assertEquals(0,buffer.readableBytes());
            var status=new KnifePursuitStatusPayload(true,phase,25,400);KnifePursuitStatusPayload.CODEC.encode(buffer,status);assertEquals(status,KnifePursuitStatusPayload.CODEC.decode(buffer));
            var request=new KnifePursuitRequestPayload(phase,Integer.MAX_VALUE);KnifePursuitRequestPayload.CODEC.encode(buffer,request);assertEquals(request,KnifePursuitRequestPayload.CODEC.decode(buffer));assertEquals(0,buffer.readableBytes());
        }}finally{buffer.release();}
    }
    @Test void animationKeepsRigidNormalSizeLimbsAndDoesNotOverrideLook()throws Exception {
        try(var reader=Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/salmosa_knife_pursuit.json"))){
            var animation=new KeyframeAnimationPlayer(AnimationSerializing.deserializeAnimation(reader).getFirst());
            var head=new Vec3f(.12f,.63f,-.08f);
            for(int tick=0;tick<=KnifePursuitRules.DURATION;tick++){
                animation.setupAnim(0);
                for(String part:new String[]{"leftArm","rightArm","leftLeg","rightLeg"}){
                    var scale=animation.get3DTransform(part,TransformType.SCALE,0,new Vec3f(1,1,1));assertEquals(1,scale.getX());assertEquals(1,scale.getY());assertEquals(1,scale.getZ());
                }
                var look=animation.get3DTransform("head",TransformType.ROTATION,0,head);assertEquals(head.getX(),look.getX());assertEquals(head.getY(),look.getY());
                var root=animation.get3DTransform("body",TransformType.ROTATION,0,Vec3f.ZERO);
                var shift=animation.get3DTransform("body",TransformType.POSITION,0,Vec3f.ZERO);
                assertEquals(0,root.getX());assertEquals(0,root.getY());assertEquals(0,root.getZ());
                assertEquals(0,shift.getX());assertEquals(0,shift.getY());assertEquals(0,shift.getZ());
                animation.tick();
            }
            assertFalse(animation.isActive());
        }
    }
}
