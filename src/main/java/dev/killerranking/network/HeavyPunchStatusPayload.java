package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.HeavyPunchRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record HeavyPunchStatusPayload(boolean available,int remaining,int cooldown) implements CustomPacketPayload {
    public HeavyPunchStatusPayload {remaining=Math.clamp(remaining,0,HeavyPunchRules.DURATION);cooldown=Math.clamp(cooldown,0,HeavyPunchRules.COOLDOWN);}
    public static final Type<HeavyPunchStatusPayload> TYPE=new Type<>(KillerRankingMod.id("heavy_punch_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf,HeavyPunchStatusPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeBoolean(v.available());b.writeVarInt(v.remaining());b.writeVarInt(v.cooldown());},
        b->new HeavyPunchStatusPayload(b.readBoolean(),b.readVarInt(),b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
