package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;
public record AutoParryStatusPayload(UUID player,boolean active) implements CustomPacketPayload {
    public static final Type<AutoParryStatusPayload> TYPE=new Type<>(KillerRankingMod.id("auto_parry_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf,AutoParryStatusPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeUUID(v.player());b.writeBoolean(v.active());},b->new AutoParryStatusPayload(b.readUUID(),b.readBoolean()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
