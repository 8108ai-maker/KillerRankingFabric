package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Target IDs are only hints; range, sight, weapon and jump geometry are rechecked by the server. */
public record MadnessLeapRequestPayload(int operation,int target) implements CustomPacketPayload {
    public static final int START=0,RELEASE=1,CANCEL=2;
    public static final Type<MadnessLeapRequestPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"madness_leap_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf,MadnessLeapRequestPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeVarInt(v.operation());b.writeVarInt(v.target());},b->new MadnessLeapRequestPayload(b.readVarInt(),b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
