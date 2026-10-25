package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SalmosaStatusPayload(boolean branch,boolean unlocked) implements CustomPacketPayload {
    public static final Type<SalmosaStatusPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"salmosa_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SalmosaStatusPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeBoolean(v.branch());b.writeBoolean(v.unlocked());},b->new SalmosaStatusPayload(b.readBoolean(),b.readBoolean()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
