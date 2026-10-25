package dev.killerranking.network;
import java.util.UUID;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.NeungsaEchoRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record NeungsaEchoVisualPayload(UUID player,int age,int remaining,double x,double y,double z,float dx,float dy,float dz) implements CustomPacketPayload {
    public NeungsaEchoVisualPayload {age=Math.clamp(age,0,NeungsaEchoRules.DURATION);remaining=Math.clamp(remaining,0,NeungsaEchoRules.DURATION);}
    public static final Type<NeungsaEchoVisualPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"neungsa_echo_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf,NeungsaEchoVisualPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeUUID(v.player());b.writeVarInt(v.age());b.writeVarInt(v.remaining());b.writeDouble(v.x());b.writeDouble(v.y());b.writeDouble(v.z());b.writeFloat(v.dx());b.writeFloat(v.dy());b.writeFloat(v.dz());},
        b->new NeungsaEchoVisualPayload(b.readUUID(),b.readVarInt(),b.readVarInt(),b.readDouble(),b.readDouble(),b.readDouble(),b.readFloat(),b.readFloat(),b.readFloat()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
