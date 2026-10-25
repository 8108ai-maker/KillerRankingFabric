package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.KnifePursuitRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

public record KnifePursuitVisualPayload(UUID player,UUID target,int phase,int age,float yaw,Vec3 origin,Vec3 wound,Vec3 destination,boolean embedded) implements CustomPacketPayload {
    public KnifePursuitVisualPayload {phase=Math.clamp(phase,0,3);age=Math.clamp(age,0,KnifePursuitRules.MAX_HOLD);if(!Float.isFinite(yaw))yaw=0;}
    public static final Type<KnifePursuitVisualPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"knife_pursuit_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf,KnifePursuitVisualPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeUUID(v.player());b.writeUUID(v.target());b.writeVarInt(v.phase());b.writeVarInt(v.age());b.writeFloat(v.yaw());write(b,v.origin());write(b,v.wound());write(b,v.destination());b.writeBoolean(v.embedded());},
        b->new KnifePursuitVisualPayload(b.readUUID(),b.readUUID(),b.readVarInt(),b.readVarInt(),b.readFloat(),read(b),read(b),read(b),b.readBoolean()));
    private static void write(RegistryFriendlyByteBuf b,Vec3 v){b.writeDouble(v.x);b.writeDouble(v.y);b.writeDouble(v.z);}
    private static Vec3 read(RegistryFriendlyByteBuf b){return new Vec3(b.readDouble(),b.readDouble(),b.readDouble());}
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
