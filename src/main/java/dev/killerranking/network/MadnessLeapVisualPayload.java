package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.MadnessLeapRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;

public record MadnessLeapVisualPayload(UUID player,int phase,int age,float yaw) implements CustomPacketPayload {
    public MadnessLeapVisualPayload {phase=Math.clamp(phase,0,2);age=Math.clamp(age,0,MadnessLeapRules.MAX_HOLD);if(!Float.isFinite(yaw))yaw=0;}
    public static final Type<MadnessLeapVisualPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"madness_leap_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf,MadnessLeapVisualPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeUUID(v.player());b.writeVarInt(v.phase());b.writeVarInt(v.age());b.writeFloat(v.yaw());},
        b->new MadnessLeapVisualPayload(b.readUUID(),b.readVarInt(),b.readVarInt(),b.readFloat()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
