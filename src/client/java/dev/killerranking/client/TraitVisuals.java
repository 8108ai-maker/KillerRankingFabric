package dev.killerranking.client;

import dev.killerranking.Trait;
import dev.killerranking.combat.SpiritBranch;
import dev.killerranking.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import java.util.*;

public final class TraitVisuals {
    private static final Map<UUID, State> ACTIVE = new HashMap<>();
    private static boolean ownEnabled = true;
    private static final class State {
        final Trait trait; final SpiritBranch branch; final boolean salmosa;
        int intro, lease;
        State(TraitVisualPayload v) { trait=v.trait(); branch=v.branch(); salmosa=v.salmosa(); intro=v.introTicks(); lease=v.leaseTicks(); }
    }
    public static void accept(TraitVisualPayload value) {
        var client = Minecraft.getInstance();
        if (client.player != null && client.player.getUUID().equals(value.player())) ownEnabled = value.enabled();
        if (value.leaseTicks() == 0) { ACTIVE.remove(value.player()); feed(value.player(), null); }
        else {
            var previous = ACTIVE.get(value.player());
            var state = new State(value);
            if (previous != null && previous.trait == state.trait && previous.branch == state.branch && previous.salmosa == state.salmosa) state.intro = Math.min(previous.intro, state.intro);
            ACTIVE.put(value.player(), state); feed(value.player(), state);
        }
    }
    private static void feed(UUID id, State state) {
        int intro = state == null ? 0 : state.intro;
        Trait trait = state == null ? Trait.UNASSIGNED : state.trait;
        SpiritVfx.accept(new SpiritVfxPayload(id, trait == Trait.FIGHTING_SPIRIT ? Math.min(60, intro) : 0, state == null ? SpiritBranch.NONE : state.branch));
        PoisonVfx.accept(new PoisonVfxPayload(id, trait == Trait.POISON_AURA ? intro : 0, state != null && state.salmosa));
        KillingIntentVfx.accept(new KillingIntentVfxPayload(id, trait == Trait.KILLING_INTENT ? Math.min(60, intro) : 0));
    }
    public static void tick(Minecraft client) {
        if (client.isPaused() || client.level == null) return;
        var iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry=iterator.next(); var state=entry.getValue();
            state.intro=Math.max(0,state.intro-1);
            if (--state.lease <= 0) { iterator.remove(); feed(entry.getKey(), null); }
            else feed(entry.getKey(), state);
        }
    }
    public static boolean enabled() { return ownEnabled; }
    public static int intro(UUID id) { var state=ACTIVE.get(id); return state==null?0:state.intro; }
    public static Trait trait(UUID id) { var state=ACTIVE.get(id); return state==null?Trait.UNASSIGNED:state.trait; }
    public static boolean glows(Entity entity) {
        var state=ACTIVE.get(entity.getUUID());
        return state != null && state.intro == 0 && entity instanceof Player player && !KillingIntentVfx.hiddenForCamera(player);
    }
    public static int color(Trait trait, SpiritBranch branch) {
        return switch(trait) {
            case KILLING_INTENT -> 0xF02030;
            case MIXED_MADNESS -> 0x850D20;
            case FIGHTING_SPIRIT -> branch == SpiritBranch.KING_AND_KILL ? 0xFFFFFF : 0x4FED63;
            case POISON_AURA -> 0xAB61ED;
            default -> 0xFFFFFF;
        };
    }
    public static int color(UUID id) {
        var state=ACTIVE.get(id); return state==null?0xFFFFFF:color(state.trait,state.branch);
    }
    public static void clear() { ACTIVE.clear(); ownEnabled=true; }
    private TraitVisuals() { }
}
