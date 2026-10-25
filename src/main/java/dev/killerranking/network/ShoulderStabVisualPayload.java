package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.ShoulderStabRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

public record ShoulderStabVisualPayload(UUID player,UUID target,int phase,int age,float yaw,Vec3 origin,Vec3 wound,Vec3 destination,Vec3 airStart,Vec3 airCatch) implements CustomPacketPayload {
    public ShoulderStabVisualPayload(UUID player,UUID target,int phase,int age,float yaw,Vec3 origin,Vec3 wound,Vec3 destination){this(player,target,phase,age,yaw,origin,wound,destination,Vec3.ZERO,Vec3.ZERO);}
    public ShoulderStabVisualPayload {phase=Math.clamp(phase,0,3);age=Math.clamp(age,0,ShoulderStabRules.MAX_HOLD);if(!Float.isFinite(yaw))yaw=0;}
    public static final Type<ShoulderStabVisualPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"shoulder_stab_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ShoulderStabVisualPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeUUID(v.player());b.writeUUID(v.target());b.writeVarInt(v.phase());b.writeVarInt(v.age());b.writeFloat(v.yaw());write(b,v.origin());write(b,v.wound());write(b,v.destination());write(b,v.airStart());write(b,v.airCatch());},
        b->new ShoulderStabVisualPayload(b.readUUID(),b.readUUID(),b.readVarInt(),b.readVarInt(),b.readFloat(),read(b),read(b),read(b),read(b),read(b)));
    private static void write(RegistryFriendlyByteBuf b,Vec3 v){b.writeDouble(v.x);b.writeDouble(v.y);b.writeDouble(v.z);}
    private static Vec3 read(RegistryFriendlyByteBuf b){return new Vec3(b.readDouble(),b.readDouble(),b.readDouble());}
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
