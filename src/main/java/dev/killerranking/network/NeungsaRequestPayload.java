package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** The client sends intent only; ownership, aim, blocks and damage are decided on the server. */
public record NeungsaRequestPayload(int operation) implements CustomPacketPayload {
    public static final int START=0,UNLOCK=1,IAIDO=2,IAIDO_RELEASE=3,IAIDO_CANCEL=4,ECHO=5;
    public static final Type<NeungsaRequestPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"neungsa_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf,NeungsaRequestPayload> CODEC=StreamCodec.of(
        (b,v)->b.writeVarInt(v.operation()),b->new NeungsaRequestPayload(b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
