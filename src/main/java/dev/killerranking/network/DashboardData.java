package dev.killerranking.network;

import com.google.gson.Gson;
import java.util.List;

/** Server-owned values: the client only displays this snapshot. */
public record DashboardData(String trait, int level, String branch, boolean checkmate, long pointUnits,
    double health, double maxHealth, double attack, double attackSpeed, double movement,
    double armor, double toughness, int defense, int attackBonus, int speedBonus, int movementBonus,
    double madnessBonus, long remainingTicks, List<Mission> missions, int traitXp, int traitXpRequired, double knockbackResistance,
    double morale, boolean knightBatBonus, int madnessSpeedPercent) {
    public static final Gson JSON = new Gson();
    public record Mission(String title, int progress, int goal, long rewardUnits, boolean complete, int divisor, String unit) { }
}
