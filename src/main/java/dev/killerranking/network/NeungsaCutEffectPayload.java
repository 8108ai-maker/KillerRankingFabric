package dev.killerranking.network;
import java.util.UUID;
import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
/** Only successfully damaged victims receive their own attached slash flash. */
public record NeungsaCutEffectPayload(int entity,UUID uuid) implements CustomPacketPayload {
    public static final Type<NeungsaCutEffectPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"neungsa_cut_effect"));
    public static final StreamCodec<RegistryFriendlyByteBuf,NeungsaCutEffectPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeVarInt(v.entity());b.writeUUID(v.uuid());},b->new NeungsaCutEffectPayload(b.readVarInt(),b.readUUID()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
