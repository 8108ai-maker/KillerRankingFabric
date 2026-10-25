package dev.killerranking.combat;

/** Combat momentum scales outgoing damage, after each completed hit. */
public final class MoraleRules {
    public static final double MIN = .75, MAX = 1.25, DEFAULT = 1;
    public static double clamp(double value) { return Double.isFinite(value) ? Math.clamp(value, MIN, MAX) : DEFAULT; }
    public static double dealt(double current, double damage) {
        return clamp(clamp(current) + (Double.isFinite(damage) && damage > 0 ? damage * .01 : 0));
    }
    public static double received(double current) { return clamp(clamp(current) - .01); }
    public static double actualDamage(float healthBefore, float absorptionBefore, float healthAfter, float absorptionAfter) {
        return Math.max(0, healthBefore - healthAfter) + Math.max(0, absorptionBefore - absorptionAfter);
    }
    private MoraleRules() { }
}
