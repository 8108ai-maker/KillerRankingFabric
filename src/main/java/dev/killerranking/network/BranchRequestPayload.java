package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record BranchRequestPayload(String branch) implements CustomPacketPayload {
    public static final Type<BranchRequestPayload> TYPE = new Type<>(KillerRankingMod.id("branch_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BranchRequestPayload> CODEC = new StreamCodec<>() {
        public BranchRequestPayload decode(RegistryFriendlyByteBuf b) { return new BranchRequestPayload(b.readUtf(32)); }
        public void encode(RegistryFriendlyByteBuf b, BranchRequestPayload p) { b.writeUtf(p.branch, 32); }
    };
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
