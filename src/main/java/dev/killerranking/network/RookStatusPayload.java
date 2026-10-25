package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RookStatusPayload(boolean branch,boolean unlocked,boolean otherOwned,int phase,int target,int remaining,int cooldown,boolean captured) implements CustomPacketPayload {
    public RookStatusPayload { phase=Math.clamp(phase,0,5);target=Math.max(-1,target);remaining=Math.clamp(remaining,0,200);cooldown=Math.clamp(cooldown,0,400); }
    public static final Type<RookStatusPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"rook_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf,RookStatusPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeBoolean(v.branch());b.writeBoolean(v.unlocked());b.writeBoolean(v.otherOwned());b.writeVarInt(v.phase());b.writeVarInt(v.target());b.writeVarInt(v.remaining());b.writeVarInt(v.cooldown());b.writeBoolean(v.captured());},
        b->new RookStatusPayload(b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readBoolean()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
