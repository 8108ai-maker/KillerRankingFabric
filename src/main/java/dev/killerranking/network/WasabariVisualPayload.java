package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.WasabariRules;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public record WasabariVisualPayload(UUID owner,UUID victim,int age,int remaining,float yaw,boolean tripped) implements CustomPacketPayload {
    public WasabariVisualPayload {age=Math.clamp(age,0,WasabariRules.DURATION);remaining=Math.clamp(remaining,0,WasabariRules.DURATION);yaw=Float.isFinite(yaw)?yaw:0;}
    public static final Type<WasabariVisualPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"wasabari_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf,WasabariVisualPayload> CODEC=StreamCodec.of((b,v)->{b.writeUUID(v.owner());b.writeUUID(v.victim());b.writeVarInt(v.age());b.writeVarInt(v.remaining());b.writeFloat(v.yaw());b.writeBoolean(v.tripped());},b->new WasabariVisualPayload(b.readUUID(),b.readUUID(),b.readVarInt(),b.readVarInt(),b.readFloat(),b.readBoolean()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
