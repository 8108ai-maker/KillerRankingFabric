package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.network.KillingIntentVfxPayload;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.entity.player.Player;
import java.util.*;

public final class KillingIntentVfx {
    private static final Map<UUID, Integer> ACTIVE = new HashMap<>();
    private static SpriteSet sprites;
    public static void register() {
        ParticleFactoryRegistry.getInstance().register(KillerRankingMod.KILLING_AURA, set -> {
            sprites = set;
            return (parameters, world, x, y, z, dx, dy, dz) -> null;
        });
    }
    static SpriteSet sprites() { return sprites; }
    public static void accept(KillingIntentVfxPayload payload) {
        if (payload.ticks() == 0) ACTIVE.remove(payload.player());
        else ACTIVE.put(payload.player(), payload.ticks());
    }
    public static void clear() { ACTIVE.clear(); }
    public static boolean active(UUID id) { return ACTIVE.getOrDefault(id, 0) > 0; }
    public static boolean hiddenForCamera(Player player) {
        var client = Minecraft.getInstance();
        return player.isInvisible() || player.isSpectator() || !player.isAlive()
            || (player == client.getCameraEntity() && client.options.getCameraType().isFirstPerson());
    }
    public static void tick(Minecraft client) {
        if (client.isPaused() || client.level == null) return;
        ACTIVE.replaceAll((id, ticks) -> ticks - 1);
        ACTIVE.entrySet().removeIf(e -> e.getValue() <= 0);
        if (sprites == null || client.getCameraEntity() == null) return;
        var camera = client.getCameraEntity();
        var players = ACTIVE.keySet().stream().map(id -> client.level.getPlayerByUUID(id)).filter(Objects::nonNull)
            .filter(p -> !hiddenForCamera(p) && p.distanceToSqr(camera) <= 1024)
            .sorted(Comparator.comparingDouble(p -> p.distanceToSqr(camera))).limit(6).toList();
        var setting = client.options.particles().get();
        int budget = setting == ParticleStatus.MINIMAL ? 4 : setting == ParticleStatus.DECREASED ? 12 : 24;
        for (var player : players) {
            int count = setting == ParticleStatus.MINIMAL ? 1 : setting == ParticleStatus.DECREASED || player.distanceToSqr(camera) > 256 ? 2 : 4;
            for (int i = 0; i < count && budget > 0; i++, budget--) {
                boolean dark = player.getRandom().nextFloat() < .28f;
                client.particleEngine.add(new KillingAuraParticle(client.level, player, sprites, dark));
            }
        }
    }
    private KillingIntentVfx() { }
}
