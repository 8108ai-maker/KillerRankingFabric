package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** No client-supplied charge time, damage, victim or position. */
public record CleaveRequestPayload(int operation) implements CustomPacketPayload {
    public static final int START=0,RELEASE=1,CANCEL=2,UNLOCK=3;
    public static final Type<CleaveRequestPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"cleave_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf,CleaveRequestPayload> CODEC=StreamCodec.of(
        (b,v)->b.writeVarInt(v.operation()),b->new CleaveRequestPayload(b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
