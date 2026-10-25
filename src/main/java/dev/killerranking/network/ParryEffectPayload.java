package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record ParryEffectPayload(UUID playerId, boolean reversed) implements CustomPacketPayload {
	public ParryEffectPayload(UUID playerId) { this(playerId, false); }
	public static final Type<ParryEffectPayload> TYPE = new Type<>(KillerRankingMod.id("parry_effect"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ParryEffectPayload> CODEC = StreamCodec.of(
			(buffer, payload) -> { buffer.writeUUID(payload.playerId()); buffer.writeBoolean(payload.reversed()); },
			buffer -> new ParryEffectPayload(buffer.readUUID(), buffer.readBoolean()));

	@Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
