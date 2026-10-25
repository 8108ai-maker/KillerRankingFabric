package dev.killerranking.combat;

import dev.killerranking.KillerRankingSounds;
import dev.killerranking.network.WeaponHitPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Impact sound and pixel VFX for ordinary melee contacts with a mod weapon.
 * Skills own their own contact feedback and are excluded through their damage flags
 * and the skill combat lock, so nothing here doubles a skill's sound.
 */
public final class WeaponHitService {
    private static final double VIEW_RANGE_SQ = 64 * 64;

    public static void afterDamage(LivingEntity victim, DamageSource source, float damageTaken, boolean blocked) {
        if (blocked || damageTaken <= 0) return;
        if (!(source.getEntity() instanceof ServerPlayer attacker) || source.getDirectEntity() != attacker) return;
        if (source.is(DamageTypeTags.IS_PROJECTILE) || attacker == victim) return;
        if (RushDamage.current(victim) || CheckmateDamage.current(victim) || BleedingDamage.current(victim)) return;
        if (SkillCombatLock.blocksAttack(attacker)) return;
        var family = WeaponFamily.of(attacker.getMainHandItem());
        if (family == null) return;

        boolean crit = attacker.fallDistance > 0 && !attacker.onGround() && !attacker.onClimbable()
            && !attacker.isInWater() && !attacker.isPassenger() && !attacker.isSprinting();
        boolean lethal = victim.isDeadOrDying() || victim.getHealth() <= 0;
        Vec3 contact = contactPoint(attacker, victim);

        var level = attacker.serverLevel();
        KillerRankingSounds.play(level, contact, family.hit, family.volume, KillerRankingSounds.vary(attacker, family.pitch, 0.06f));
        if (crit) KillerRankingSounds.play(level, contact, KillerRankingSounds.HIT_CRIT, 0.7f, KillerRankingSounds.vary(attacker, 1.0f, 0.05f));

        var payload = new WeaponHitPayload(attacker.getUUID(), family.ordinal(), contact.x, contact.y, contact.z, attacker.getYRot(), damageTaken, crit, lethal);
        for (ServerPlayer viewer : level.players()) {
            if (viewer.distanceToSqr(contact) <= VIEW_RANGE_SQ && ServerPlayNetworking.canSend(viewer, WeaponHitPayload.TYPE)) ServerPlayNetworking.send(viewer, payload);
        }
    }

    /** A point on the victim's silhouette facing the attacker, at mid-torso height. */
    static Vec3 contactPoint(LivingEntity attacker, LivingEntity victim) {
        Vec3 toAttacker = attacker.position().subtract(victim.position()).multiply(1, 0, 1);
        if (toAttacker.lengthSqr() < 1e-4) toAttacker = attacker.getLookAngle().multiply(-1, 0, -1);
        toAttacker = toAttacker.normalize().scale(victim.getBbWidth() * 0.45);
        double height = victim.getBbHeight() * (0.55 + attacker.getRandom().nextFloat() * 0.15);
        return victim.position().add(toAttacker).add(0, height, 0);
    }

    private WeaponHitService() { }
}
