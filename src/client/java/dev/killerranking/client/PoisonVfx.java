package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.network.PoisonVfxPayload;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.entity.player.Player;
import java.util.*;

public final class PoisonVfx {
    private static final Map<UUID, State> ACTIVE = new HashMap<>();
    private static SpriteSet sprites;
    private static SpriteSet flameSprites;
    private static final class State { int ticks, age; final boolean salmosa; State(int ticks, boolean salmosa) { this.ticks = ticks; this.salmosa = salmosa; } }
    public static void register() {
        ParticleFactoryRegistry.getInstance().register(KillerRankingMod.POISON_AURA, set -> {
            sprites = set;
            return (parameters, world, x, y, z, dx, dy, dz) -> null; // Owner-bound particles are spawned below.
        });
        ParticleFactoryRegistry.getInstance().register(KillerRankingMod.POISON_FLAME, set -> {
            flameSprites = set;
            return (parameters, world, x, y, z, dx, dy, dz) -> null;
        });
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            var client = Minecraft.getInstance();
            if (context.matrixStack() == null || context.consumers() == null || client.level == null) return;
            float partial = context.tickCounter().getGameTimeDeltaPartialTick(false);
            for (var player : visible(client)) {
                var state = ACTIVE.get(player.getUUID());
                float alpha = Math.min(1, (state.age + partial) / 8f) * Math.min(1, (state.ticks - partial) / 8f);
                PoisonSnakeModel.render(context, player, partial, Math.max(0, alpha));
            }
        });
    }
    public static void accept(PoisonVfxPayload payload) {
        if (payload.ticks() == 0) ACTIVE.remove(payload.player());
        else ACTIVE.compute(payload.player(), (id, previous) -> {
            if (previous == null || previous.salmosa != payload.salmosa()) return new State(payload.ticks(), payload.salmosa());
            previous.ticks = payload.ticks(); return previous;
        });
    }
    public static void clear() { ACTIVE.clear(); }
    public static boolean active(UUID id) { return ACTIVE.containsKey(id); }
    public static boolean salmosa(UUID id) { var state=ACTIVE.get(id);return state!=null&&state.salmosa; }
    static int age(UUID id) { var state=ACTIVE.get(id);return state==null?0:state.age; }
    private static List<Player> visible(Minecraft client) {
        var camera = client.getCameraEntity();
        if (camera == null || client.level == null) return List.of();
        return ACTIVE.keySet().stream().map(id -> client.level.getPlayerByUUID(id)).filter(Objects::nonNull)
            .filter(p -> !KillingIntentVfx.hiddenForCamera(p) && p.distanceToSqr(camera) <= 1024)
            .sorted(Comparator.comparingDouble(p -> p.distanceToSqr(camera))).limit(4).toList();
    }
    public static void tick(Minecraft client) {
        if (client.isPaused() || client.level == null) return;
        ACTIVE.values().forEach(s -> { s.ticks--; s.age++; });
        ACTIVE.values().removeIf(s -> s.ticks <= 0);
        var camera=client.getCameraEntity();
        if(camera!=null)for(var entry:ACTIVE.entrySet()){
            var player=client.level.getPlayerByUUID(entry.getKey());
            if(entry.getValue().age%72==14&&player!=null&&player.isAlive()&&!player.isInvisible()&&!player.isSpectator()&&player.distanceToSqr(camera)<=256){
                var head=PoisonSnakeModel.headPoint(player,0,0,0,.3f);
                client.level.playLocalSound(player.getX()+head.x,player.getY()+head.y,player.getZ()+head.z,
                    dev.killerranking.KillerRankingSounds.POISON_HISS,
                    net.minecraft.sounds.SoundSource.PLAYERS,.45f,1f,false);
            }
        }
        if (sprites == null) return;
        var setting = client.options.particles().get();
        int count = setting == ParticleStatus.MINIMAL ? 1 : setting == ParticleStatus.DECREASED ? 2 : 4;
        for (var player : visible(client)) {
            for (int i = 0; i < count; i++) client.particleEngine.add(new PoisonAuraParticle(client.level, player, sprites, player.getRandom().nextFloat() < .25f));
            if (flameSprites != null && (setting != ParticleStatus.MINIMAL || player.tickCount % 2 == 0)) {
                int eyeCount = setting == ParticleStatus.ALL ? 2 : 1;
                for (int i = 0; i < eyeCount; i++) {
                    boolean orange = (i + player.tickCount) % 3 == 0;
                    client.particleEngine.add(new PoisonFlameParticle(client.level, player, flameSprites, -1, orange));
                    client.particleEngine.add(new PoisonFlameParticle(client.level, player, flameSprites, 1, orange));
                }
            }
        }
    }
    private PoisonVfx() { }
}
