package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DashboardPayload(String json) implements CustomPacketPayload {
    public static final Type<DashboardPayload> TYPE = new Type<>(KillerRankingMod.id("dashboard_v2"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DashboardPayload> CODEC = new StreamCodec<>() {
        public DashboardPayload decode(RegistryFriendlyByteBuf b) { return new DashboardPayload(b.readUtf(32767)); }
        public void encode(RegistryFriendlyByteBuf b, DashboardPayload p) { b.writeUtf(p.json, 32767); }
    };
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
