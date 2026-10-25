package dev.killerranking.combat;
import dev.killerranking.data.CleaveState;
import dev.killerranking.network.*;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class CleaveTest {
    @BeforeAll static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
    @Test void threePointEightSecondChargeBoundary(){assertEquals(3.8,CleaveRules.CHARGE/20.0,.000001);assertFalse(CleaveRules.full(75));assertTrue(CleaveRules.full(76));assertTrue(CleaveRules.full(77));}
    @Test void rearAndFarEnemiesAreOutsideBothCuts(){
        for(boolean full:new boolean[]{false,true}){assertFalse(CleaveRules.inSlash(-1,0,0,full));assertFalse(CleaveRules.inSlash(6.1,0,0,full));assertFalse(CleaveRules.inSlash(1,4,0,full));assertFalse(CleaveRules.inSlash(1,0,2.3,full));}
    }
    @Test void fullCutReachesSixMetresWithoutExpandingPartialCut(){assertTrue(CleaveRules.inSlash(6,0,0,true));assertFalse(CleaveRules.inSlash(6,0,0,false));assertTrue(CleaveRules.inSlash(2,1,0,false));assertFalse(CleaveRules.inSlash(5.9,1.5,0,true));}
    @Test void cooldownStartsOnlyWhenUnlockedAndReady(){
        assertEquals(CleaveState.LOCKED,CleaveState.LOCKED.cast(200));
        var cast=new CleaveState(true,0).cast(200);assertEquals(600,cast.cooldown(200));assertEquals(1,cast.cooldown(799));assertEquals(0,cast.cooldown(800));assertEquals(cast,cast.cast(201));
    }
    @Test void changingOwnershipDoesNotResetCooldown(){var s=new CleaveState(true,1000);assertEquals(1000,s.owned(false).readyAt());assertEquals(1000,s.owned(false).owned(true).readyAt());}
    @Test void oldAndSavedAttachmentDataDecodeSafely(){
        assertEquals(CleaveState.LOCKED,CleaveState.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("{}")).getOrThrow());
        var s=new CleaveState(true,98765);var json=CleaveState.CODEC.encodeStart(JsonOps.INSTANCE,s).getOrThrow();assertEquals(s,CleaveState.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow());
    }
    @Test void chargeSlashCancellationAndAllRequestsRoundTripWithoutTrailingBytes(){
        var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try{
            for(int phase=0;phase<3;phase++){
                var visual=new CleaveVisualPayload(UUID.randomUUID(),phase,phase==1?100:7,phase>0,137.5f);
                CleaveVisualPayload.CODEC.encode(b,visual);assertEquals(visual,CleaveVisualPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
                var status=new CleaveStatusPayload(true,true,false,phase,phase==1?100:7,600);
                CleaveStatusPayload.CODEC.encode(b,status);assertEquals(status,CleaveStatusPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
            }
            for(int op=0;op<=3;op++){var r=new CleaveRequestPayload(op);CleaveRequestPayload.CODEC.encode(b,r);assertEquals(r,CleaveRequestPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());}
        }finally{b.release();}
    }
}
