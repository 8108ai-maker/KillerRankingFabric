package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Requests an action for the sending player; the server validates ownership and cost. */
public record CheckmateUnlockPayload() implements CustomPacketPayload {
    public static final Type<CheckmateUnlockPayload> TYPE = new Type<>(KillerRankingMod.id("checkmate_unlock"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CheckmateUnlockPayload> CODEC = StreamCodec.unit(new CheckmateUnlockPayload());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
