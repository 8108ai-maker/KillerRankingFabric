package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.NeungsaSlashRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record NeungsaStatusPayload(boolean branch,boolean unlocked,int remaining,int cooldown,int iaidoRemaining,int iaidoCooldown,int iaidoPhase,int iaidoAge,int echoRemaining,int echoCooldown) implements CustomPacketPayload {
    public NeungsaStatusPayload {echoRemaining=Math.clamp(echoRemaining,0,dev.killerranking.combat.NeungsaEchoRules.DURATION);echoCooldown=Math.clamp(echoCooldown,0,dev.killerranking.combat.NeungsaEchoRules.COOLDOWN);remaining=Math.clamp(remaining,0,NeungsaSlashRules.DURATION);cooldown=Math.clamp(cooldown,0,NeungsaSlashRules.COOLDOWN);iaidoRemaining=Math.clamp(iaidoRemaining,0,20);iaidoCooldown=Math.clamp(iaidoCooldown,0,300);iaidoPhase=Math.clamp(iaidoPhase,0,2);iaidoAge=Math.clamp(iaidoAge,0,dev.killerranking.combat.NeungsaIaidoRules.MAX_HOLD);}
    public NeungsaStatusPayload(boolean branch,boolean unlocked,int remaining,int cooldown,int iaidoRemaining,int iaidoCooldown,int iaidoPhase,int iaidoAge){this(branch,unlocked,remaining,cooldown,iaidoRemaining,iaidoCooldown,iaidoPhase,iaidoAge,0,0);}
    public NeungsaStatusPayload(boolean branch,boolean unlocked,int remaining,int cooldown,int iaidoRemaining,int iaidoCooldown){this(branch,unlocked,remaining,cooldown,iaidoRemaining,iaidoCooldown,iaidoRemaining>0?2:0,0);}
    public NeungsaStatusPayload(boolean branch,boolean unlocked,int remaining,int cooldown){this(branch,unlocked,remaining,cooldown,0,0);}
    public static final Type<NeungsaStatusPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"neungsa_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf,NeungsaStatusPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeBoolean(v.branch());b.writeBoolean(v.unlocked());b.writeVarInt(v.remaining());b.writeVarInt(v.cooldown());b.writeVarInt(v.iaidoRemaining());b.writeVarInt(v.iaidoCooldown());b.writeVarInt(v.iaidoPhase());b.writeVarInt(v.iaidoAge());b.writeVarInt(v.echoRemaining());b.writeVarInt(v.echoCooldown());},
        b->new NeungsaStatusPayload(b.readBoolean(),b.readBoolean(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
