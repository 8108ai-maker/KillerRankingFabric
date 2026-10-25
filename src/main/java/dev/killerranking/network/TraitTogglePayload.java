package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Requests a toggle for the sending player only. The server owns the resulting state. */
public record TraitTogglePayload() implements CustomPacketPayload {
    public static final Type<TraitTogglePayload> TYPE = new Type<>(KillerRankingMod.id("trait_toggle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TraitTogglePayload> CODEC = StreamCodec.unit(new TraitTogglePayload());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
