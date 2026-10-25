package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.CheckmateRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CheckmateStatusPayload(boolean branch,boolean unlocked,boolean rushOwned,int activeTicks,int cooldownTicks) implements CustomPacketPayload {
    public CheckmateStatusPayload { activeTicks=Math.clamp(activeTicks,0,CheckmateRules.DURATION);cooldownTicks=Math.clamp(cooldownTicks,0,CheckmateRules.COOLDOWN); }
    public static final Type<CheckmateStatusPayload> TYPE=new Type<>(KillerRankingMod.id("checkmate_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf,CheckmateStatusPayload> CODEC=StreamCodec.of(
        (b,v)->{ b.writeBoolean(v.branch());b.writeBoolean(v.unlocked());b.writeBoolean(v.rushOwned());b.writeVarInt(v.activeTicks());b.writeVarInt(v.cooldownTicks()); },
        b->new CheckmateStatusPayload(b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readVarInt(),b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
