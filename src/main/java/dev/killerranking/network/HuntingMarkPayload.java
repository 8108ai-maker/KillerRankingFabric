package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Sent exclusively to the player who applied the mark. */
public record HuntingMarkPayload(boolean active, String name, double x, double y, double z, int ticks, int bleedingDamage) implements CustomPacketPayload {
    public static final HuntingMarkPayload CLEAR = new HuntingMarkPayload(false, "", 0, 0, 0, 0, 0);
    public static final Type<HuntingMarkPayload> TYPE = new Type<>(KillerRankingMod.id("hunting_mark"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HuntingMarkPayload> CODEC = StreamCodec.of(
        (b, v) -> { b.writeBoolean(v.active()); b.writeUtf(v.name(), 128); b.writeDouble(v.x()); b.writeDouble(v.y()); b.writeDouble(v.z()); b.writeVarInt(v.ticks()); b.writeVarInt(v.bleedingDamage()); },
        b -> new HuntingMarkPayload(b.readBoolean(), b.readUtf(128), b.readDouble(), b.readDouble(), b.readDouble(), b.readVarInt(), b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
