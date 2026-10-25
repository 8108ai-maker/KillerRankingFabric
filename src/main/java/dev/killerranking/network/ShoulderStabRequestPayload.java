package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Target IDs are only hints; range, sight, weapon and approach geometry are rechecked by the server. */
public record ShoulderStabRequestPayload(int operation,int target) implements CustomPacketPayload {
    public static final int START=0,RELEASE=1,CANCEL=2,COUNTER=3;
    public static final Type<ShoulderStabRequestPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"shoulder_stab_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ShoulderStabRequestPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeVarInt(v.operation());b.writeVarInt(v.target());},b->new ShoulderStabRequestPayload(b.readVarInt(),b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
