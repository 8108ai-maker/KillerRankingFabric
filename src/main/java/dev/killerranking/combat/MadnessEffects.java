package dev.killerranking.combat;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.Trait;
import dev.killerranking.data.PlayerProfiles;
import dev.killerranking.data.TraitActivation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class MadnessEffects {
    public static final ResourceLocation SPEED = KillerRankingMod.id("madness_attack_speed");
    public static boolean active(ServerPlayer p) {
        return p.isAlive() && !p.isSpectator() && TraitActivation.enabled(p)
            && PlayerProfiles.get(p).trait() == Trait.MIXED_MADNESS
            && CombatRules.madnessActive(CombatRules.healthRatio(p.getHealth(), p.getMaxHealth()));
    }
    public static int speed(ServerPlayer p) {
        return active(p) ? TraitLevels.bonusPercent(KillerRankingMod.CONFIG.mixedMadnessAttackSpeedPercent, PlayerProfiles.get(p).traitLevel()) : 0;
    }
    public static double damageBonus(ServerPlayer p) {
        return p.isAlive()&&!p.isSpectator()&&TraitActivation.enabled(p)&&PlayerProfiles.get(p).trait()==Trait.MIXED_MADNESS
            ? CombatRules.madnessDamagePercent(CombatRules.healthRatio(p.getHealth(),p.getMaxHealth()),
                KillerRankingMod.CONFIG.mixedMadnessBasePercent,PlayerProfiles.get(p).traitLevel()) : 0;
    }
    public static void refresh(ServerPlayer p) {
        PoisonEffects.applyPercent(p.getAttribute(Attributes.ATTACK_SPEED), SPEED, speed(p));
    }
    private MadnessEffects() { }
}
