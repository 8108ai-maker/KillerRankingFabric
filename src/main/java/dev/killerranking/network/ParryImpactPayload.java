package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Sent only after the server has accepted a parried hit, separately from the guard pose. */
public record ParryImpactPayload(UUID player,double x,double y,double z) implements CustomPacketPayload {
    public static final Type<ParryImpactPayload> TYPE=new Type<>(KillerRankingMod.id("parry_impact"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ParryImpactPayload> CODEC=StreamCodec.of(
        (b,p)->{b.writeUUID(p.player());b.writeDouble(p.x());b.writeDouble(p.y());b.writeDouble(p.z());},
        b->new ParryImpactPayload(b.readUUID(),b.readDouble(),b.readDouble(),b.readDouble()));
    public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
