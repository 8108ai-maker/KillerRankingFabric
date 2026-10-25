package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.MadnessLeapRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MadnessLeapStatusPayload(boolean available,int phase,int age,int cooldown) implements CustomPacketPayload {
    public MadnessLeapStatusPayload {phase=Math.clamp(phase,0,2);age=Math.clamp(age,0,MadnessLeapRules.MAX_HOLD);cooldown=Math.clamp(cooldown,0,MadnessLeapRules.COOLDOWN);}
    public static final Type<MadnessLeapStatusPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"madness_leap_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf,MadnessLeapStatusPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeBoolean(v.available());b.writeVarInt(v.phase());b.writeVarInt(v.age());b.writeVarInt(v.cooldown());},
        b->new MadnessLeapStatusPayload(b.readBoolean(),b.readVarInt(),b.readVarInt(),b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
