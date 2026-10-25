package dev.killerranking.combat;

import dev.killerranking.KillerRankingMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

/** Normal attacks cannot overlap skill preparation or execution. Cooldowns and passive buffs do not lock attacks. */
public final class SkillCombatLock {
    public static boolean blocksAttack(ServerPlayer player) {
        return KillerRankingMod.NEUNGSA_SKILL.active(player)
            || KillerRankingMod.SHOULDER_STAB.active(player)
            || KillerRankingMod.KNIFE_PURSUIT.active(player)
            || KillerRankingMod.HEAVY_PUNCH.locked(player)
            || KillerRankingMod.BISHOP.casting(player)
            || KillerRankingMod.BISHOP_BARRAGE.active(player)
            || KillerRankingMod.RUSH.active(player)
            || KillerRankingMod.CHECKMATE.active(player)
            || KillerRankingMod.ROOK.active(player)
            || KillerRankingMod.ROOK.locked(player)
            || KillerRankingMod.MADNESS_LEAP.active(player)
            || KillerRankingMod.CLEAVE.active(player)
            || KillerRankingMod.PARRY.window(player) > 0;
    }

    /** Preserve scoped skill strikes and delayed effects, while rejecting melee damage from alternate attack paths. */
    public static boolean allowsDamage(LivingEntity victim, DamageSource source) {
        if (RushDamage.current(victim) || CheckmateDamage.current(victim)) return true;
        if (source.getEntity() instanceof LivingEntity attacker && KillerRankingMod.ROOK.locked(attacker)) return false;
        return !(source.getEntity() instanceof ServerPlayer player && blocksAttack(player)
            && source.is(DamageTypes.PLAYER_ATTACK) && !BleedingDamage.current(victim));
    }

    private SkillCombatLock() { }
}
