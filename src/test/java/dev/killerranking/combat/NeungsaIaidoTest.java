package dev.killerranking.combat;
import dev.killerranking.data.NeungsaSkillState;
import dev.killerranking.network.*;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.phys.*;
import java.util.UUID;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
class NeungsaIaidoTest {
    @BeforeAll static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
    @Test void oldPurchasedProfilesGetIaidoWithoutAnotherPurchaseAndCooldownsStayIndependent(){
        var old=com.google.gson.JsonParser.parseString("{\"unlocked\":true,\"readyAt\":500}");
        var state=NeungsaSkillState.CODEC.parse(JsonOps.INSTANCE,old).getOrThrow();
        assertTrue(state.unlocked());assertEquals(0,state.iaidoCooldown(100));
        var x=state.iaido(100);assertEquals(500,x.readyAt());assertEquals(400,x.iaidoReadyAt());
        assertEquals(x,x.iaido(120));assertEquals(x,x.owned());
        assertEquals(NeungsaSkillState.LOCKED,NeungsaSkillState.LOCKED.iaido(0));
        var encoded=NeungsaSkillState.CODEC.encodeStart(JsonOps.INSTANCE,x).getOrThrow();
        assertEquals(x,NeungsaSkillState.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow());
        var z=x.cast(500);assertEquals(740,z.readyAt());assertEquals(400,z.iaidoReadyAt());
    }
    @Test void defenseExpiresExactlyAfterTwentyTicksAndAreaHasFrontWidthButNoExtraRange(){
        assertEquals(20,NeungsaIaidoRules.remaining(100,100));assertEquals(1,NeungsaIaidoRules.remaining(100,119));assertEquals(0,NeungsaIaidoRules.remaining(100,120));
        var start=new Vec3(0,1,0);var forward=new Vec3(0,0,1);
        var flank=new AABB(1.8,0,3,2.4,2,3.6);
        assertTrue(NeungsaSlashRules.entry(start,forward,flank,5,2,1).isPresent());
        for(var outside:new AABB[]{flank.move(1,0,0),flank.move(0,0,4),flank.move(0,0,-5),flank.move(0,4,0)})
            assertTrue(NeungsaSlashRules.entry(start,forward,outside,5,2,1).isEmpty());
    }
    @Test void extendedStatusCastAndVictimPacketsRoundTrip(){
        var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try{
            var status=new NeungsaStatusPayload(true,true,0,220,19,299);NeungsaStatusPayload.CODEC.encode(b,status);assertEquals(status,NeungsaStatusPayload.CODEC.decode(b));
            var visual=new NeungsaIaidoVisualPayload(UUID.randomUUID(),3,17);NeungsaIaidoVisualPayload.CODEC.encode(b,visual);assertEquals(visual,NeungsaIaidoVisualPayload.CODEC.decode(b));
            var victim=new NeungsaCutEffectPayload(91,UUID.randomUUID());NeungsaCutEffectPayload.CODEC.encode(b,victim);assertEquals(victim,NeungsaCutEffectPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
            var charge=new NeungsaStatusPayload(true,true,0,0,0,0,NeungsaIaidoRules.CHARGING,29);NeungsaStatusPayload.CODEC.encode(b,charge);assertEquals(charge,NeungsaStatusPayload.CODEC.decode(b));
            var dash=new NeungsaIaidoVisualPayload(UUID.randomUUID(),4,16,NeungsaIaidoRules.SLASHING,true,1,-60,8,.6f,0,.8f);NeungsaIaidoVisualPayload.CODEC.encode(b,dash);assertEquals(dash,NeungsaIaidoVisualPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
        }finally{b.release();}
    }
    @Test void fullChargeHasAnExactServerTickBoundaryAndANarrowEightMetreCut(){
        assertFalse(NeungsaIaidoRules.full(29));assertTrue(NeungsaIaidoRules.full(30));assertTrue(NeungsaIaidoRules.full(90));
        assertEquals(12,NeungsaIaidoRules.damage(false));assertEquals(25,NeungsaIaidoRules.damage(true));
        var origin=new Vec3(0,1,0);var aim=new Vec3(0,0,1);
        for(double z:new double[]{1,4,7.8})assertTrue(NeungsaSlashRules.entry(origin,aim,new AABB(-.3,0,z-.3,.3,2,z+.3),8,.75,1).isPresent());
        for(var excluded:new AABB[]{new AABB(1.2,0,3,1.8,2,3.6),new AABB(-.3,0,8.4,.3,2,9),new AABB(-.3,0,-1,.3,2,-.4)})assertTrue(NeungsaSlashRules.entry(origin,aim,excluded,8,.75,1).isEmpty());
    }
}
