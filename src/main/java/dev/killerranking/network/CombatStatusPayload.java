package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server-owned state. No client message can set gauges or cooldowns. */
public record CombatStatusPayload(String traitId, float gauge, boolean active, int effectTicks,
		int powerPercent, int parryTicks, int parryTotal, int parryWindow,
		int traitLevel, int effectTotalTicks, int highHealthPercent, int halfHealthPercent, int lowHealthPercent,
        int survivalTicks, int survivalCooldown) implements CustomPacketPayload {
	public static final Type<CombatStatusPayload> TYPE = new Type<>(KillerRankingMod.id("combat_status_v2"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CombatStatusPayload> CODEC = StreamCodec.of(
		(buffer, value) -> {
			buffer.writeUtf(value.traitId());
			buffer.writeFloat(value.gauge());
			buffer.writeBoolean(value.active());
			buffer.writeVarInt(value.effectTicks());
			buffer.writeVarInt(value.powerPercent());
			buffer.writeVarInt(value.parryTicks());
			buffer.writeVarInt(value.parryTotal());
			buffer.writeVarInt(value.parryWindow());
			buffer.writeVarInt(value.traitLevel());
			buffer.writeVarInt(value.effectTotalTicks());
			buffer.writeVarInt(value.highHealthPercent());
			buffer.writeVarInt(value.halfHealthPercent());
			buffer.writeVarInt(value.lowHealthPercent());
            buffer.writeVarInt(value.survivalTicks());
            buffer.writeVarInt(value.survivalCooldown());
		}, buffer -> new CombatStatusPayload(buffer.readUtf(), buffer.readFloat(), buffer.readBoolean(),
			buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
			buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
            buffer.readVarInt(), buffer.readVarInt()));
	@Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
