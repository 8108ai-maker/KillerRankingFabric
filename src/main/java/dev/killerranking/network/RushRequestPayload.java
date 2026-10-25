package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Requests an action for the sending player; the server validates ownership and cost. */
public record RushRequestPayload() implements CustomPacketPayload {
    public static final Type<RushRequestPayload> TYPE = new Type<>(KillerRankingMod.id("rush_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RushRequestPayload> CODEC = StreamCodec.unit(new RushRequestPayload());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
