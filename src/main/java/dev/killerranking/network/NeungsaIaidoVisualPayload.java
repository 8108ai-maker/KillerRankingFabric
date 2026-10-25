package dev.killerranking.network;
import java.util.UUID;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.NeungsaIaidoRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public record NeungsaIaidoVisualPayload(UUID player,int age,int remaining,int phase,boolean full,double x,double y,double z,float dx,float dy,float dz) implements CustomPacketPayload {
    public NeungsaIaidoVisualPayload {age=Math.clamp(age,0,NeungsaIaidoRules.MAX_HOLD);remaining=Math.clamp(remaining,0,NeungsaIaidoRules.MAX_HOLD);phase=Math.clamp(phase,0,2);}
    public NeungsaIaidoVisualPayload(UUID player,int age,int remaining){this(player,age,remaining,remaining>0?NeungsaIaidoRules.SLASHING:0,false,0,0,0,0,0,1);}
    public static final Type<NeungsaIaidoVisualPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"neungsa_iaido_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf,NeungsaIaidoVisualPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeUUID(v.player());b.writeVarInt(v.age());b.writeVarInt(v.remaining());b.writeVarInt(v.phase());b.writeBoolean(v.full());b.writeDouble(v.x());b.writeDouble(v.y());b.writeDouble(v.z());b.writeFloat(v.dx());b.writeFloat(v.dy());b.writeFloat(v.dz());},
        b->new NeungsaIaidoVisualPayload(b.readUUID(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readBoolean(),b.readDouble(),b.readDouble(),b.readDouble(),b.readFloat(),b.readFloat(),b.readFloat()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
