package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Request only; the server decides the trait, owner and 1,000P cost. */
public record SalmosaUnlockPayload() implements CustomPacketPayload {
    public static final Type<SalmosaUnlockPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"salmosa_unlock"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SalmosaUnlockPayload> CODEC=StreamCodec.unit(new SalmosaUnlockPayload());
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
