package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.HeavyPunchRules;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record HeavyPunchVisualPayload(UUID player,int target,int remaining,int age,float yaw) implements CustomPacketPayload {
    public HeavyPunchVisualPayload {remaining=Math.clamp(remaining,0,HeavyPunchRules.DURATION);age=Math.clamp(age,0,HeavyPunchRules.DURATION);yaw=Float.isFinite(yaw)?yaw:0;}
    public static final Type<HeavyPunchVisualPayload> TYPE=new Type<>(KillerRankingMod.id("heavy_punch_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf,HeavyPunchVisualPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeUUID(v.player());b.writeVarInt(v.target());b.writeVarInt(v.remaining());b.writeVarInt(v.age());b.writeFloat(v.yaw());},
        b->new HeavyPunchVisualPayload(b.readUUID(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readFloat()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
