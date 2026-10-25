package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.SpiritBranch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;

/** Short-lived client lease, refreshed while the passive branch remains equipped. */
public record SpiritVfxPayload(UUID player, int ticks, SpiritBranch branch) implements CustomPacketPayload {
    public SpiritVfxPayload {
        ticks = Math.clamp(ticks, 0, 60);
        if (branch == null || branch == SpiritBranch.NONE || ticks == 0) { branch = SpiritBranch.NONE; ticks = 0; }
    }
    public static final Type<SpiritVfxPayload> TYPE = new Type<>(KillerRankingMod.id("spirit_vfx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpiritVfxPayload> CODEC = StreamCodec.of(
        (buffer, value) -> { buffer.writeUUID(value.player()); buffer.writeVarInt(value.ticks()); buffer.writeUtf(value.branch().id(), 32); },
        buffer -> new SpiritVfxPayload(buffer.readUUID(), buffer.readVarInt(), SpiritBranch.fromId(buffer.readUtf(32))));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
