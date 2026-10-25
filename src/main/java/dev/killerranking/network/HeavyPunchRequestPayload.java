package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** No client target or damage is trusted. */
public record HeavyPunchRequestPayload() implements CustomPacketPayload {
    public static final Type<HeavyPunchRequestPayload> TYPE=new Type<>(KillerRankingMod.id("heavy_punch_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf,HeavyPunchRequestPayload> CODEC=StreamCodec.unit(new HeavyPunchRequestPayload());
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
