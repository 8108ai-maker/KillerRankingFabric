package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.KnifePursuitRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record KnifePursuitStatusPayload(boolean available,int phase,int age,int cooldown) implements CustomPacketPayload {
    public KnifePursuitStatusPayload {phase=Math.clamp(phase,0,3);age=Math.clamp(age,0,KnifePursuitRules.MAX_HOLD);cooldown=Math.clamp(cooldown,0,KnifePursuitRules.COOLDOWN);}
    public static final Type<KnifePursuitStatusPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"knife_pursuit_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf,KnifePursuitStatusPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeBoolean(v.available());b.writeVarInt(v.phase());b.writeVarInt(v.age());b.writeVarInt(v.cooldown());},
        b->new KnifePursuitStatusPayload(b.readBoolean(),b.readVarInt(),b.readVarInt(),b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
