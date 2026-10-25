package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ParryRequestPayload() implements CustomPacketPayload {
	public static final Type<ParryRequestPayload> TYPE = new Type<>(KillerRankingMod.id("parry_request"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ParryRequestPayload> CODEC = StreamCodec.unit(new ParryRequestPayload());
	@Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
