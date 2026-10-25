package dev.killerranking.combat;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.Trait;
import dev.killerranking.data.*;
import dev.killerranking.network.TraitVisualPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

/** One five-second introduction per activation, followed by a renewable outline lease. */
public final class TraitVisualService {
    private record Key(Trait trait, SpiritBranch branch, boolean enabled, boolean visible, boolean salmosa) { }
    private record State(Key key, long started) { }
    private final Map<UUID, State> states = new HashMap<>();
    private final Map<UUID, Long> lastToggle = new HashMap<>();
    private Key key(ServerPlayer p) {
        var trait = PlayerProfiles.get(p).trait();
        var branch = trait == Trait.FIGHTING_SPIRIT ? SpiritState.get(p).branch() : SpiritBranch.NONE;
        boolean enabled = TraitActivation.enabled(p);
        return new Key(trait, branch, enabled, enabled && p.isAlive() && !p.isSpectator() && trait != Trait.UNASSIGNED,
            trait == Trait.POISON_AURA && SalmosaState.get(p).unlocked());
    }
    private State update(ServerPlayer p) {
        var key = key(p);
        return states.compute(p.getUUID(), (id, previous) -> previous != null && previous.key().equals(key)
            ? previous : new State(key, p.server.overworld().getGameTime()));
    }
    public void toggle(ServerPlayer p) {
        if (!p.isAlive() || p.isSpectator() || KillerRankingMod.ROOK.captured(p) || PlayerProfiles.get(p).trait() == Trait.UNASSIGNED) return;
        long now = p.server.overworld().getGameTime();
        if (now - lastToggle.getOrDefault(p.getUUID(), now - 4) < 4) return;
        lastToggle.put(p.getUUID(), now);
        boolean enabled = !TraitActivation.enabled(p);
        TraitActivation.set(p, enabled);
        if (!enabled) { KillerRankingMod.COMBAT_STATUS.clearKillingIntent(p); KillerRankingMod.HUNTING_MARKS.clear(p); }
        KillerRankingMod.assignAndSync(p);
        if (enabled) {
            var trait = PlayerProfiles.get(p).trait();
            var stinger = switch (trait) {
                case KILLING_INTENT -> dev.killerranking.KillerRankingSounds.TRAIT_KILLING_INTENT;
                case MIXED_MADNESS -> dev.killerranking.KillerRankingSounds.TRAIT_MIXED_MADNESS;
                case FIGHTING_SPIRIT -> dev.killerranking.KillerRankingSounds.TRAIT_FIGHTING_SPIRIT;
                case POISON_AURA -> dev.killerranking.KillerRankingSounds.TRAIT_POISON_AURA;
                default -> null;
            };
            if (stinger != null) dev.killerranking.KillerRankingSounds.play(p, stinger, 1.0F, 1.0F);
        } else dev.killerranking.KillerRankingSounds.play(p, dev.killerranking.KillerRankingSounds.TRAIT_OFF, 0.8F, 1.0F);
        p.displayClientMessage(Component.literal(enabled ? "특성 켜짐 · VFX 5초 후 발광" : "특성 꺼짐 · 능력 효과와 경험치 획득 중지"), true);
    }
    public void tick(MinecraftServer server) {
        for (var p : server.getPlayerList().getPlayers()) {
            var previous = states.get(p.getUUID());
            var state = update(p);
            if (previous != state || server.getTickCount() % 20 == 0) sync(p);
        }
    }
    public void sync(ServerPlayer p) {
        update(p); send(p, p);
        for (var viewer : PlayerLookup.tracking(p)) send(p, viewer);
    }
    public void send(ServerPlayer p, ServerPlayer viewer) {
        var state = update(p); var key = state.key();
        int intro = key.visible() ? (int) Math.max(0, 100 - (p.server.overworld().getGameTime() - state.started())) : 0;
        if (ServerPlayNetworking.canSend(viewer, TraitVisualPayload.TYPE)) ServerPlayNetworking.send(viewer,
            new TraitVisualPayload(p.getUUID(), key.trait(), key.branch(), key.enabled(), intro, key.visible() ? 60 : 0, key.salmosa()));
    }
    public void forget(UUID id) { states.remove(id); lastToggle.remove(id); }
    public void clear() { states.clear(); lastToggle.clear(); }
}
