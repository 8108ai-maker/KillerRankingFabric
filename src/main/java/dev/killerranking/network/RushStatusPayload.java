package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.RushRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RushStatusPayload(boolean branch,boolean unlocked,int activeTicks,int cooldownTicks) implements CustomPacketPayload {
    public RushStatusPayload { activeTicks=Math.clamp(activeTicks,0,RushRules.DURATION);cooldownTicks=Math.clamp(cooldownTicks,0,RushRules.COOLDOWN); }
    public static final Type<RushStatusPayload> TYPE=new Type<>(KillerRankingMod.id("rush_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf,RushStatusPayload> CODEC=StreamCodec.of(
        (b,v)->{ b.writeBoolean(v.branch());b.writeBoolean(v.unlocked());b.writeVarInt(v.activeTicks());b.writeVarInt(v.cooldownTicks()); },
        b->new RushStatusPayload(b.readBoolean(),b.readBoolean(),b.readVarInt(),b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
