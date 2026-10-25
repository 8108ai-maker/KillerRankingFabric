package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Requests an action for the sending player; the server validates ownership and cost. */
public record CheckmateRequestPayload() implements CustomPacketPayload {
    public static final Type<CheckmateRequestPayload> TYPE = new Type<>(KillerRankingMod.id("checkmate_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CheckmateRequestPayload> CODEC = StreamCodec.unit(new CheckmateRequestPayload());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
