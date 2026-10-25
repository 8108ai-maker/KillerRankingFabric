package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.UUID;

/** Only visible sword types are synchronized; the rest of the inventory remains private. */
public record KatanaHolstersPayload(UUID player, ResourceLocation waist, ResourceLocation back) implements CustomPacketPayload {
    public static final Type<KatanaHolstersPayload> TYPE = new Type<>(KillerRankingMod.id("katana_holsters"));
    public static final StreamCodec<RegistryFriendlyByteBuf,KatanaHolstersPayload> CODEC = StreamCodec.of(
        (b,v) -> { b.writeUUID(v.player); b.writeResourceLocation(v.waist); b.writeResourceLocation(v.back); },
        b -> new KatanaHolstersPayload(b.readUUID(),b.readResourceLocation(),b.readResourceLocation()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
