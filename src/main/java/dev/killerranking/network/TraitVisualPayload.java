package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.Trait;
import dev.killerranking.combat.SpiritBranch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;

public record TraitVisualPayload(UUID player, Trait trait, SpiritBranch branch, boolean enabled, int introTicks, int leaseTicks, boolean salmosa) implements CustomPacketPayload {
    public TraitVisualPayload(UUID player, Trait trait, SpiritBranch branch, boolean enabled, int introTicks, int leaseTicks) {
        this(player, trait, branch, enabled, introTicks, leaseTicks, false);
    }
    public TraitVisualPayload {
        introTicks = Math.clamp(introTicks, 0, 100);
        leaseTicks = Math.clamp(leaseTicks, 0, 60);
        if (!enabled || trait == Trait.UNASSIGNED || leaseTicks == 0) { introTicks = 0; leaseTicks = 0; }
        if (trait != Trait.FIGHTING_SPIRIT) branch = SpiritBranch.NONE;
        if (trait != Trait.POISON_AURA) salmosa = false;
    }
    public static final Type<TraitVisualPayload> TYPE = new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID,"trait_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TraitVisualPayload> CODEC = StreamCodec.of(
        (b, v) -> { b.writeUUID(v.player()); b.writeUtf(v.trait().id(), 32); b.writeUtf(v.branch().id(), 32); b.writeBoolean(v.enabled()); b.writeVarInt(v.introTicks()); b.writeVarInt(v.leaseTicks()); b.writeBoolean(v.salmosa()); },
        b -> new TraitVisualPayload(b.readUUID(), Trait.fromId(b.readUtf(32)), SpiritBranch.fromId(b.readUtf(32)), b.readBoolean(), b.readVarInt(), b.readVarInt(), b.readBoolean()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
