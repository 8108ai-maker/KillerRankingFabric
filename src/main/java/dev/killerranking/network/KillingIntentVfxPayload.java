package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;

/** Visual state only. Damage and activation remain owned by the server. */
public record KillingIntentVfxPayload(UUID player, int ticks) implements CustomPacketPayload {
    public KillingIntentVfxPayload { ticks = Math.clamp(ticks, 0, 60); }
    public static final Type<KillingIntentVfxPayload> TYPE = new Type<>(KillerRankingMod.id("killing_intent_vfx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, KillingIntentVfxPayload> CODEC = StreamCodec.of(
        (buffer, value) -> { buffer.writeUUID(value.player()); buffer.writeVarInt(value.ticks()); },
        buffer -> new KillingIntentVfxPayload(buffer.readUUID(), buffer.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
