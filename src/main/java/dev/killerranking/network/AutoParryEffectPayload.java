package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;
/** Server-confirmed strike: the client never supplies attackers, success or the echo sequence. */
public record AutoParryEffectPayload(UUID player,boolean reversed,boolean echo,int sequence,double x,double y,double z,float yaw,float pitch) implements CustomPacketPayload {
    public AutoParryEffectPayload {sequence=Math.clamp(sequence,0,4095);}
    public static final Type<AutoParryEffectPayload> TYPE=new Type<>(KillerRankingMod.id("auto_parry_effect"));
    public static final StreamCodec<RegistryFriendlyByteBuf,AutoParryEffectPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeUUID(v.player());b.writeBoolean(v.reversed());b.writeBoolean(v.echo());b.writeVarInt(v.sequence());b.writeDouble(v.x());b.writeDouble(v.y());b.writeDouble(v.z());b.writeFloat(v.yaw());b.writeFloat(v.pitch());},
        b->new AutoParryEffectPayload(b.readUUID(),b.readBoolean(),b.readBoolean(),b.readVarInt(),b.readDouble(),b.readDouble(),b.readDouble(),b.readFloat(),b.readFloat()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
