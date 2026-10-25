package dev.killerranking.combat;

import dev.killerranking.config.KillerRankingConfig;

/** Shared by damage calculation and the HUD, so their thresholds cannot drift. */
public final class CombatRules {
	public static final int KILLING_INTENT_TICKS = 60;
	private CombatRules() { }
	public static float healthRatio(float health, float maxHealth) {
		return maxHealth <= 0 ? 1 : Math.clamp(health / maxHealth, 0, 1);
	}
	public static float spiritReduction(float healthRatio) {
		return healthRatio <= 0.25F ? 0.18F : healthRatio <= 0.50F ? 0.10F : healthRatio <= 0.75F ? 0.05F : 0;
	}
	public static boolean madnessActive(float healthRatio) { return healthRatio < 0.30F; }
	public static int spiritPercent(float healthRatio, int level, KillerRankingConfig config) {
		int base = healthRatio <= 0.25F ? config.spiritLowHealthBasePercent : healthRatio <= 0.50F
			? config.spiritHalfHealthBasePercent : healthRatio <= 0.75F ? config.spiritHighHealthBasePercent : 0;
		return TraitLevels.defensePercent(base, level);
	}
	public static int maxSpiritPercent(int level, KillerRankingConfig config) {
		return TraitLevels.defensePercent(Math.max(config.spiritLowHealthBasePercent,
			Math.max(config.spiritHalfHealthBasePercent, config.spiritHighHealthBasePercent)), level);
	}
	public static float madnessProgress(float healthRatio) { return Math.clamp((1 - healthRatio) / 0.70F, 0, 1); }
    /** Missing health grants damage continuously; the full bonus is reached at 30% health. */
    public static double madnessDamagePercent(float healthRatio,int basePercent,int level) {
        return TraitLevels.bonusPercent(basePercent,level)*(double)madnessProgress(healthRatio);
    }
	public static float cooldownProgress(int remaining, int total) {
		return total <= 0 ? 1 : Math.clamp(1 - (float) remaining / total, 0, 1);
	}
}
