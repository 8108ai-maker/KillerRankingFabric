package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.BishopRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;

public record BishopStatusPayload(UUID player,boolean branch,boolean unlocked,boolean otherOwned,boolean enabled,
    int count,int remaining,int cooldown,int castTicks,int barrageTicks,int barrageCooldown,String targetName) implements CustomPacketPayload {
    public BishopStatusPayload {count=Math.clamp(count,0,BishopRules.MAX_UNITS);remaining=Math.clamp(remaining,0,BishopRules.DURATION);cooldown=Math.clamp(cooldown,0,BishopRules.COOLDOWN);castTicks=Math.clamp(castTicks,0,BishopRules.CAST);barrageTicks=Math.clamp(barrageTicks,0,dev.killerranking.combat.BishopBarrageRules.DURATION);barrageCooldown=Math.clamp(barrageCooldown,0,BishopRules.COOLDOWN);targetName=targetName.substring(0,Math.min(80,targetName.length()));}
    public static final Type<BishopStatusPayload> TYPE=new Type<>(KillerRankingMod.id("bishop_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf,BishopStatusPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeUUID(v.player());b.writeBoolean(v.branch());b.writeBoolean(v.unlocked());b.writeBoolean(v.otherOwned());b.writeBoolean(v.enabled());b.writeVarInt(v.count());b.writeVarInt(v.remaining());b.writeVarInt(v.cooldown());b.writeVarInt(v.castTicks());b.writeVarInt(v.barrageTicks());b.writeVarInt(v.barrageCooldown());b.writeUtf(v.targetName(),80);},
        b->new BishopStatusPayload(b.readUUID(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readUtf(80)));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
