package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.CleaveRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;

public record CleaveVisualPayload(UUID player,int phase,int age,boolean full,float yaw) implements CustomPacketPayload {
    public CleaveVisualPayload {phase=Math.clamp(phase,0,2);age=Math.clamp(age,0,CleaveRules.MAX_HOLD);}
    public static final Type<CleaveVisualPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"cleave_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf,CleaveVisualPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeUUID(v.player());b.writeVarInt(v.phase());b.writeVarInt(v.age());b.writeBoolean(v.full());b.writeFloat(v.yaw());},
        b->new CleaveVisualPayload(b.readUUID(),b.readVarInt(),b.readVarInt(),b.readBoolean(),b.readFloat()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
