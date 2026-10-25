package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public record AutoParryRequestPayload(boolean held) implements CustomPacketPayload {
    public static final Type<AutoParryRequestPayload> TYPE=new Type<>(KillerRankingMod.id("auto_parry_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf,AutoParryRequestPayload> CODEC=StreamCodec.of(
        (b,v)->b.writeBoolean(v.held()),b->new AutoParryRequestPayload(b.readBoolean()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
