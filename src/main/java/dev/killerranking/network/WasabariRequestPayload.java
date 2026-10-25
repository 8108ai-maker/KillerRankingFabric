package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
/** Selection, damage and timing are entirely server-authoritative. */
public record WasabariRequestPayload() implements CustomPacketPayload {
    public static final Type<WasabariRequestPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"wasabari_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf,WasabariRequestPayload> CODEC=StreamCodec.unit(new WasabariRequestPayload());
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
