package dev.killerranking.client;

import dev.killerranking.Trait;
import dev.killerranking.combat.CombatRules;
import dev.killerranking.combat.TraitLevels;
import dev.killerranking.network.CombatStatusPayload;

public final class ClientCombatStatus {
	private static CombatStatusPayload value;
	private static int elapsed;
	private ClientCombatStatus() { }
	public static void set(CombatStatusPayload payload) { value = payload; elapsed = 0; }
	public static void reset() { value = null; elapsed = 0; }
	public static void tick() { if (value != null && elapsed < Integer.MAX_VALUE) elapsed++; }
	public static boolean available() { return value != null; }
	public static Trait trait() { return value == null ? Trait.UNASSIGNED : Trait.fromId(value.traitId()); }
	public static int effectTicks() { return value == null ? 0 : Math.max(0, value.effectTicks() - elapsed); }
	public static int parryTicks() { return value == null ? 0 : Math.max(0, value.parryTicks() - elapsed); }
    public static int survivalTicks() { return value == null ? 0 : Math.max(0, value.survivalTicks() - elapsed); }
    public static int survivalCooldown() { return value == null ? 0 : Math.max(0, value.survivalCooldown() - elapsed); }
	public static boolean parryActive() { return value != null && value.parryWindow() > elapsed; }
	public static float parryProgress() { return value == null ? 0 : CombatRules.cooldownProgress(parryTicks(), value.parryTotal()); }
	public static int power() { return value == null ? 0 : value.powerPercent(); }
    public static double madnessBonus() { return trait()==Trait.MIXED_MADNESS?power()*(double)gauge():0; }
	public static int level() { return value == null ? 1 : TraitLevels.clamp(value.traitLevel()); }
	public static int effectTotalTicks() { return value == null ? 60 : Math.max(1, value.effectTotalTicks()); }
	public static int highHealthPercent() { return value == null ? 0 : value.highHealthPercent(); }
	public static int halfHealthPercent() { return value == null ? 0 : value.halfHealthPercent(); }
	public static int lowHealthPercent() { return value == null ? 0 : value.lowHealthPercent(); }
	public static boolean active() { return value != null && (trait() == Trait.KILLING_INTENT ? effectTicks() > 0 : value.active()); }
	public static float gauge() {
		return value == null ? 0 : trait() == Trait.KILLING_INTENT ? (float) effectTicks() / effectTotalTicks() : value.gauge();
	}
}
