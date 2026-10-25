package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RookVisualPayload(UUID owner,UUID victim,int phase,int age,int remaining) implements CustomPacketPayload {
    public RookVisualPayload { phase=Math.clamp(phase,0,5);age=Math.clamp(age,0,200);remaining=Math.clamp(remaining,0,200); }
    public static final Type<RookVisualPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"rook_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf,RookVisualPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeUUID(v.owner());b.writeBoolean(v.victim()!=null);if(v.victim()!=null)b.writeUUID(v.victim());b.writeVarInt(v.phase());b.writeVarInt(v.age());b.writeVarInt(v.remaining());},
        b->{var owner=b.readUUID();var victim=b.readBoolean()?b.readUUID():null;return new RookVisualPayload(owner,victim,b.readVarInt(),b.readVarInt(),b.readVarInt());});
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
