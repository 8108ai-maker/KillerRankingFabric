package dev.killerranking.combat;

/** Level scaling is linear in the BONUS, never in the player's whole stat. */
public final class TraitLevels {
	public static final int MAX = 3;
	private TraitLevels() { }
	public static int clamp(int level) { return Math.clamp(level, 1, MAX); }
	public static int bonusPercent(int basePercent, int level) {
		return Math.clamp(basePercent, 0, 100) * clamp(level);
	}
	public static int defensePercent(int basePercent, int level) {
		return Math.min(90, bonusPercent(basePercent, level));
	}
	public static int poisonTicks(int level) { return 60 * clamp(level); }
	public static int parryWindow(int level) { return 5 + 2 * clamp(level); }
}
