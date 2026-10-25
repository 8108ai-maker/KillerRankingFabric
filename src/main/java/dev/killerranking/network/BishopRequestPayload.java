package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Commands carry no client-selected victim, spawn position, damage or cooldown. */
public record BishopRequestPayload(int operation) implements CustomPacketPayload {
    public static final int SUMMON=0,BARRAGE=1,DESIGNATE=2,UNLOCK=3;
    public static final Type<BishopRequestPayload> TYPE=new Type<>(KillerRankingMod.id("bishop_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf,BishopRequestPayload> CODEC=StreamCodec.of((b,v)->b.writeVarInt(v.operation()),b->new BishopRequestPayload(b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
