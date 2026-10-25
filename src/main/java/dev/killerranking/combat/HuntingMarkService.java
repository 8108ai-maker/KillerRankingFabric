package dev.killerranking.combat;

import dev.killerranking.Trait;
import dev.killerranking.data.PlayerProfiles;
import dev.killerranking.network.HuntingMarkPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HuntingMarkService {
    private final HuntingMarks marks = new HuntingMarks();
    private final Map<UUID, Long> lastBleed = new HashMap<>();
    private static boolean eligible(ServerPlayer p) { return p != null && dev.killerranking.data.TraitActivation.enabled(p) && p.isAlive() && !p.isSpectator() && PlayerProfiles.get(p).trait() == Trait.POISON_AURA; }
    public void onHit(ServerPlayer hunter, LivingEntity target) {
        if (!eligible(hunter) || target == hunter || !target.isAlive()) return;
        int level = PlayerProfiles.get(hunter).traitLevel();
        marks.set(hunter.getUUID(), target.getUUID(), target.level().dimension().location().toString(), hunter.server.overworld().getGameTime(),
            TraitLevels.poisonTicks(level));
        dev.killerranking.KillerRankingSounds.play(target, dev.killerranking.KillerRankingSounds.HUNT_MARK_APPLY, 0.7F, 1.0F);
        sync(hunter);
    }
    public void tick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        lastBleed.entrySet().removeIf(e -> now - e.getValue() > 200);
        for (var owner : marks.owners()) {
            var hunter = server.getPlayerList().getPlayer(owner);
            if (hunter == null) { marks.remove(owner); continue; }
            var due = marks.due(owner, now);
            if (eligible(hunter) && due != null && due.dimension().equals(hunter.level().dimension().location().toString())) {
                var entity = hunter.serverLevel().getEntity(due.target());
                if (entity instanceof LivingEntity victim && victim.isAlive() && !victim.isRemoved()
                    && now - lastBleed.getOrDefault(due.target(), now - 20) >= 20) {
                    lastBleed.put(due.target(), now);
                    if (BleedingDamage.hit(hunter, victim)) hunter.serverLevel().sendParticles(
                        new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.8f,.025f,.07f), .85f),
                        victim.getX(), victim.getY()+victim.getBbHeight()*.5, victim.getZ(), 5, .15,.15,.15, .01);
                }
                marks.advance(owner, now);
            }
            var target = target(hunter);
            if (target == null) clear(hunter);
            else if (server.getTickCount() % 5 == 0) sync(hunter);
        }
    }
    private LivingEntity target(ServerPlayer hunter) {
        if (!eligible(hunter)) return null;
        var mark = marks.get(hunter.getUUID(), hunter.server.overworld().getGameTime());
        if (mark == null || !mark.dimension().equals(hunter.level().dimension().location().toString())) return null;
        var entity = hunter.serverLevel().getEntity(mark.target());
        return entity instanceof LivingEntity living && living.isAlive() && !living.isRemoved() ? living : null;
    }
    private void sync(ServerPlayer hunter) {
        var target = target(hunter);
        if (target == null) { clear(hunter); return; }
        var mark = marks.get(hunter.getUUID(), hunter.server.overworld().getGameTime());
        String name = target.getName().getString();
        if (name.length() > 80) name = name.substring(0, 80);
        send(hunter, new HuntingMarkPayload(true, name, target.getX(), target.getY(), target.getZ(),
            (int) (mark.expires() - hunter.server.overworld().getGameTime()), 1));
    }
    private void send(ServerPlayer hunter, HuntingMarkPayload payload) {
        if (ServerPlayNetworking.canSend(hunter, HuntingMarkPayload.TYPE)) ServerPlayNetworking.send(hunter, payload);
    }
    public void clear(ServerPlayer hunter) { marks.remove(hunter.getUUID()); send(hunter, HuntingMarkPayload.CLEAR); }
    public void clear() { marks.clear(); lastBleed.clear(); }
}
