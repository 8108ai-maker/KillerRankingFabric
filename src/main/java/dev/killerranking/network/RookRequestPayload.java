package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Only requests: range, aim, funds, ownership and every phase are validated on the server. */
public record RookRequestPayload(int operation,int target) implements CustomPacketPayload {
    public static final int START=0,CONFIRM=1,CANCEL=2,THROW=3,PUNCH=4,MOUNT=5,UNLOCK=6;
    public RookRequestPayload { if(operation<0||operation>6||target< -1)throw new IllegalArgumentException("Invalid rook request"); }
    public static final Type<RookRequestPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"rook_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf,RookRequestPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeVarInt(v.operation());b.writeVarInt(v.target());},b->new RookRequestPayload(b.readVarInt(),b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
