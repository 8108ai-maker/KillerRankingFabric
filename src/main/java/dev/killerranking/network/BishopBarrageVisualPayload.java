package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.BishopBarrageRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;

public record BishopBarrageVisualPayload(UUID player,int remaining,double x,double y,double z,float yaw) implements CustomPacketPayload {
    public BishopBarrageVisualPayload {remaining=Math.clamp(remaining,0,BishopBarrageRules.DURATION);}
    public static final Type<BishopBarrageVisualPayload> TYPE=new Type<>(KillerRankingMod.id("bishop_barrage_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf,BishopBarrageVisualPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeUUID(v.player());b.writeVarInt(v.remaining());b.writeDouble(v.x());b.writeDouble(v.y());b.writeDouble(v.z());b.writeFloat(v.yaw());},
        b->new BishopBarrageVisualPayload(b.readUUID(),b.readVarInt(),b.readDouble(),b.readDouble(),b.readDouble(),b.readFloat()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
