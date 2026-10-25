package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Requests an action for the sending player; the server validates ownership and cost. */
public record RushUnlockPayload() implements CustomPacketPayload {
    public static final Type<RushUnlockPayload> TYPE = new Type<>(KillerRankingMod.id("rush_unlock"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RushUnlockPayload> CODEC = StreamCodec.unit(new RushUnlockPayload());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
