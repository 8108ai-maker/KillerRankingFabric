package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.CheckmateRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.world.phys.Vec3;

public record CheckmateVisualPayload(UUID player,int remaining,int age,Vec3 origin,List<Vec3> tiles,int selected,boolean aiming) implements CustomPacketPayload {
    public CheckmateVisualPayload {
        int duration=aiming?CheckmateRules.AIM_DURATION:CheckmateRules.DURATION;
        remaining=Math.clamp(remaining,0,duration);age=Math.clamp(age,0,duration);
        tiles=List.copyOf(tiles);
        if(tiles.size()>8||!finite(origin)||tiles.stream().anyMatch(v->!finite(v))) throw new IllegalArgumentException("Invalid checkmate board");
        if(remaining>0&&(tiles.isEmpty()||selected<(aiming?-1:0)||selected>=tiles.size())) throw new IllegalArgumentException("Invalid destination");
    }
    // Separate channel prevents old clients from decoding the extended board data as the old packet.
    public static final Type<CheckmateVisualPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"checkmate_visual_v3"));
    public static final StreamCodec<RegistryFriendlyByteBuf,CheckmateVisualPayload> CODEC=StreamCodec.of(
        (b,v)->{ b.writeUUID(v.player());b.writeVarInt(v.remaining());b.writeVarInt(v.age());writeVector(b,v.origin());
            b.writeVarInt(v.tiles().size());for(var tile:v.tiles()) writeVector(b,tile);b.writeVarInt(v.selected());b.writeBoolean(v.aiming()); },
        b->{ var id=b.readUUID();int remaining=b.readVarInt(),age=b.readVarInt();var origin=readVector(b);
            int count=b.readVarInt();if(count<0||count>8) throw new io.netty.handler.codec.DecoderException("Invalid board size");
            var tiles=new ArrayList<Vec3>();for(int i=0;i<count;i++) tiles.add(readVector(b));
            return new CheckmateVisualPayload(id,remaining,age,origin,tiles,b.readVarInt(),b.readBoolean()); });
    private static boolean finite(Vec3 v) { return Double.isFinite(v.x)&&Double.isFinite(v.y)&&Double.isFinite(v.z); }
    private static void writeVector(RegistryFriendlyByteBuf b,Vec3 v) { b.writeDouble(v.x);b.writeDouble(v.y);b.writeDouble(v.z); }
    private static Vec3 readVector(RegistryFriendlyByteBuf b) { return new Vec3(b.readDouble(),b.readDouble(),b.readDouble()); }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
