package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.network.SpiritVfxPayload;
import dev.killerranking.combat.SpiritBranch;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.entity.player.Player;
import java.util.*;

public final class SpiritVfx {
    private static final Map<UUID, State> ACTIVE = new HashMap<>();
    private static SpriteSet sprites;
    private static final class State {
        int ticks, age;
        final SpiritBranch branch;
        State(int ticks, SpiritBranch branch) { this.ticks = ticks; this.branch = branch; }
    }
    public static void register() {
        ParticleFactoryRegistry.getInstance().register(KillerRankingMod.SPIRIT_AURA, set -> {
            sprites = set;
            return (parameters, world, x, y, z, dx, dy, dz) -> null;
        });
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            var client = Minecraft.getInstance();
            if (client.level == null || context.matrixStack() == null || context.consumers() == null) return;
            float delta = context.tickCounter().getGameTimeDeltaPartialTick(false);
            for (var player : visible(client)) {
                if (CleaveVfx.full(player.getUUID())) continue;
                var state = ACTIVE.get(player.getUUID());
                float alpha = Math.min(1, (state.age + delta) / 10f) * Math.min(1, (state.ticks - delta) / 10f);
                SpiritGlyph.render(context, player, delta, Math.max(0, alpha), state.branch);
            }
        });
    }
    public static void accept(SpiritVfxPayload payload) {
        if (payload.ticks() == 0) ACTIVE.remove(payload.player());
        else ACTIVE.compute(payload.player(), (id, state) -> {
            if (state == null || state.branch != payload.branch()) return new State(payload.ticks(), payload.branch());
            state.ticks = payload.ticks();
            return state;
        });
    }
    public static void clear() { ACTIVE.clear(); }
    public static boolean active(UUID id) { return ACTIVE.containsKey(id); }
    public static SpiritBranch branch(UUID id) {
        if (CleaveVfx.full(id)) return SpiritBranch.KING_AND_KILL;
        var state = ACTIVE.get(id);
        return state == null ? SpiritBranch.NONE : state.branch;
    }
    private static List<Player> visible(Minecraft client) {
        var camera = client.getCameraEntity();
        if (camera == null || client.level == null) return List.of();
        return ACTIVE.keySet().stream().map(client.level::getPlayerByUUID).filter(Objects::nonNull)
            .filter(p -> !KillingIntentVfx.hiddenForCamera(p) && p.distanceToSqr(camera) <= 1024)
            .sorted(Comparator.comparingDouble(p -> p.distanceToSqr(camera))).limit(4).toList();
    }
    public static void tick(Minecraft client) {
        if (client.isPaused() || client.level == null) return;
        ACTIVE.values().forEach(s -> { s.ticks--; s.age++; });
        ACTIVE.values().removeIf(s -> s.ticks <= 0);
        if (sprites == null) return;
        var setting = client.options.particles().get();
        int count = setting == ParticleStatus.MINIMAL ? 2 : setting == ParticleStatus.DECREASED ? 5 : 10;
        for (var player : visible(client)) {
            if (CleaveVfx.full(player.getUUID())) continue;
            var branch = branch(player.getUUID());
            for (int i = 0; i < count; i++) client.particleEngine.add(new SpiritAuraParticle(client.level, player, sprites, false, branch));
            if (setting != ParticleStatus.MINIMAL) {
                int sparks = setting == ParticleStatus.ALL ? 2 : 1;
                for (int i = 0; i < sparks; i++) client.particleEngine.add(new SpiritAuraParticle(client.level, player, sprites, true, branch));
            }
        }
    }
    private SpiritVfx() { }
    static void emitWhite(Player player,int count) {
        var client=Minecraft.getInstance();if(client.level==null||sprites==null)return;
        for(int i=0;i<count;i++)client.particleEngine.add(new SpiritAuraParticle(client.level,player,sprites,i%6==0,SpiritBranch.KING_AND_KILL));
    }
}
