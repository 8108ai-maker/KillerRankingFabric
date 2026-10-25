package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenPhonePayload() implements CustomPacketPayload {
	public static final Type<OpenPhonePayload> TYPE = new Type<>(KillerRankingMod.id("open_phone"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenPhonePayload> CODEC = StreamCodec.unit(new OpenPhonePayload());
	@Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
