package dev.killerranking.network;
import java.util.UUID;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.NeungsaSlashRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Animate the wielder only. There is deliberately no visible travelling slash entity or beam. */
public record NeungsaSlashVisualPayload(UUID player,int age,int remaining) implements CustomPacketPayload {
    public NeungsaSlashVisualPayload {age=Math.clamp(age,0,NeungsaSlashRules.DURATION);remaining=Math.clamp(remaining,0,NeungsaSlashRules.DURATION);}
    public static final Type<NeungsaSlashVisualPayload> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"neungsa_slash_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf,NeungsaSlashVisualPayload> CODEC=StreamCodec.of(
        (b,v)->{b.writeUUID(v.player());b.writeVarInt(v.age());b.writeVarInt(v.remaining());},
        b->new NeungsaSlashVisualPayload(b.readUUID(),b.readVarInt(),b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
