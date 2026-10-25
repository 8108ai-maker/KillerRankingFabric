package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** -1 cancels; the server validates every requested index against its own preview. */
public record CheckmateConfirmPayload(int selected) implements CustomPacketPayload {
    public static final Type<CheckmateConfirmPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"checkmate_confirm"));
    public static final StreamCodec<RegistryFriendlyByteBuf,CheckmateConfirmPayload> CODEC=StreamCodec.of(
        (b,v)->b.writeVarInt(v.selected()),b->new CheckmateConfirmPayload(b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
