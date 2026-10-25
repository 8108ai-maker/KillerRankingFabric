package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;

public record PoisonVfxPayload(UUID player, int ticks, boolean salmosa) implements CustomPacketPayload {
    public PoisonVfxPayload(UUID player, int ticks) { this(player, ticks, false); }
    public PoisonVfxPayload { ticks = Math.clamp(ticks, 0, 180); }
    public static final Type<PoisonVfxPayload> TYPE = new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"poison_vfx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PoisonVfxPayload> CODEC = StreamCodec.of(
        (buffer, value) -> { buffer.writeUUID(value.player()); buffer.writeVarInt(value.ticks()); buffer.writeBoolean(value.salmosa()); },
        buffer -> new PoisonVfxPayload(buffer.readUUID(), buffer.readVarInt(), buffer.readBoolean()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
