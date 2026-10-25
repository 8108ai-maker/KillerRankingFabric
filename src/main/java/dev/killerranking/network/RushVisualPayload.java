package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.RushRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;

public record RushVisualPayload(UUID player,int remaining,int age) implements CustomPacketPayload {
    public RushVisualPayload { remaining=Math.clamp(remaining,0,RushRules.DURATION);age=Math.clamp(age,0,RushRules.DURATION); }
    public static final Type<RushVisualPayload> TYPE=new Type<>(KillerRankingMod.id("rush_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf,RushVisualPayload> CODEC=StreamCodec.of(
        (b,v)->{ b.writeUUID(v.player());b.writeVarInt(v.remaining());b.writeVarInt(v.age()); },
        b->new RushVisualPayload(b.readUUID(),b.readVarInt(),b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
