package dev.killerranking.combat;
import com.mojang.serialization.JsonOps;
import dev.killerranking.data.SalmosaState;
import dev.killerranking.Trait;
import java.util.UUID;
import dev.killerranking.network.*;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SalmosaPurchaseTest {
    @BeforeAll static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
    @Test void purchasedOwnershipPersistsIndependentlyFromLegacyCooldownData(){
        assertFalse(SalmosaState.LOCKED.unlocked());
        for(var state:new SalmosaState[]{SalmosaState.LOCKED,SalmosaState.OWNED}){
            var json=SalmosaState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();
            assertEquals(state,SalmosaState.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow());
        }
    }
    @Test void ownershipSyncKeepsTraitEligibilitySeparateFromPurchase(){
        for(boolean branch:new boolean[]{false,true})for(boolean owned:new boolean[]{false,true}){
            var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
            try{
                var value=new SalmosaStatusPayload(branch,owned);SalmosaStatusPayload.CODEC.encode(b,value);
                assertEquals(value,SalmosaStatusPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
                var request=new SalmosaUnlockPayload();SalmosaUnlockPayload.CODEC.encode(b,request);
                assertEquals(request,SalmosaUnlockPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
            }finally{b.release();}
        }
    }
    @Test void snakeOwnershipTravelsWithEachPlayersVisualLease(){
        var id=UUID.randomUUID();
        for(boolean owned:new boolean[]{false,true})for(boolean enabled:new boolean[]{false,true}){
            var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
            try{
                var value=new TraitVisualPayload(id,Trait.POISON_AURA,SpiritBranch.NONE,enabled,100,60,owned);
                TraitVisualPayload.CODEC.encode(b,value);
                assertEquals(value,TraitVisualPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
                assertEquals(owned,value.salmosa());assertEquals(enabled?100:0,value.introTicks());
                var effect=new PoisonVfxPayload(id,100,owned);PoisonVfxPayload.CODEC.encode(b,effect);
                assertEquals(effect,PoisonVfxPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
            }finally{b.release();}
        }
        assertFalse(new TraitVisualPayload(id,Trait.FIGHTING_SPIRIT,SpiritBranch.KING_AND_KILL,true,100,60,true).salmosa());
        assertFalse(new TraitVisualPayload(id,Trait.POISON_AURA,SpiritBranch.NONE,true,100,60).salmosa());
    }
}
