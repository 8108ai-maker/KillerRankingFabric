package dev.killerranking.network;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.WasabariRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public record WasabariStatusPayload(boolean available,int remaining,int cooldown) implements CustomPacketPayload {
    public WasabariStatusPayload {remaining=Math.clamp(remaining,0,WasabariRules.DURATION);cooldown=Math.clamp(cooldown,0,WasabariRules.COOLDOWN);}
    public static final Type<WasabariStatusPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"wasabari_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf,WasabariStatusPayload> CODEC=StreamCodec.of((b,v)->{b.writeBoolean(v.available());b.writeVarInt(v.remaining());b.writeVarInt(v.cooldown());},b->new WasabariStatusPayload(b.readBoolean(),b.readVarInt(),b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
