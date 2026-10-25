package dev.killerranking.data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Fixed-point currency: one internal unit is one won; 1 P = 10,000,000 won. */
public final class KillerPoints {
    public static final long UNITS_PER_POINT = 10_000_000L;
    public static final long MAX_UNITS = Integer.MAX_VALUE * UNITS_PER_POINT;
    public static long whole(int points) { return points * UNITS_PER_POINT; }
    public static long fromDecimal(double points) {
        if (!Double.isFinite(points)) throw new IllegalArgumentException("Non-finite points");
        return BigDecimal.valueOf(points).movePointRight(7).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
    /** Catalog weights are scaled by 0.00001 P: a weight of 100 pays 0.001 P. */
    public static long missionReward(int weight) { return Math.max(0L, weight) * 100L; }
    public static long add(long balance, long change) {
        balance = Math.clamp(balance, 0, MAX_UNITS);
        return balance + Math.clamp(change, -balance, MAX_UNITS - balance);
    }
    public static String format(long units) { return BigDecimal.valueOf(units, 7).stripTrailingZeros().toPlainString(); }
    private KillerPoints() { }
}
