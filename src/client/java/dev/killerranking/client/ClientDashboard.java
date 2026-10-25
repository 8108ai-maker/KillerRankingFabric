package dev.killerranking.client;

import dev.killerranking.combat.SpiritBranch;
import dev.killerranking.network.DashboardData;

public final class ClientDashboard {
    private static DashboardData data;
    public static DashboardData get() { return data; }
    public static void set(DashboardData value) { data = value; }
    public static void reset() { data = null; }
    public static SpiritBranch branch() { return data == null ? SpiritBranch.NONE : SpiritBranch.fromId(data.branch()); }
    public static boolean needsChoice() { return data != null && data.trait().equals("fighting_spirit") && branch() == SpiritBranch.NONE; }
    public static String remaining() {
        if (data == null) return "동기화 중";
        long hours = (data.remainingTicks() + 999) / 1000;
        return (hours / 24) + "일 " + (hours % 24) + "시간 후 교체 (게임 시간)";
    }
    private ClientDashboard() { }
}
