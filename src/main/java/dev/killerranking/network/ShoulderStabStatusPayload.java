package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.ShoulderStabRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ShoulderStabStatusPayload(boolean available,int phase,int age,int cooldown) implements CustomPacketPayload {
    public ShoulderStabStatusPayload {phase=Math.clamp(phase,0,3);age=Math.clamp(age,0,ShoulderStabRules.MAX_HOLD);cooldown=Math.clamp(cooldown,0,ShoulderStabRules.COOLDOWN);}
    public static final Type<ShoulderStabStatusPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"shoulder_stab_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ShoulderStabStatusPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeBoolean(v.available());b.writeVarInt(v.phase());b.writeVarInt(v.age());b.writeVarInt(v.cooldown());},
        b->new ShoulderStabStatusPayload(b.readBoolean(),b.readVarInt(),b.readVarInt(),b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
