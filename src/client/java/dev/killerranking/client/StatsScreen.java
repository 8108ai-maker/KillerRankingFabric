package dev.killerranking.client;

import dev.killerranking.combat.SpiritBranch;
import net.minecraft.client.gui.GuiGraphics;
import java.util.Locale;

public final class StatsScreen extends PanelScreen {
    public StatsScreen() { this(null); }
    public StatsScreen(net.minecraft.client.gui.screens.Screen parent) { super("플레이어 스탯", parent); }
    @Override protected void content(GuiGraphics g) {
        var d = ClientDashboard.get();
        if (d == null) { text(g, "서버에서 스탯을 불러오는 중…", 20, 55, MUTED); return; }
        String name = ClientDashboard.branch() == SpiritBranch.NONE ? dev.killerranking.Trait.fromId(d.trait()).koreanName() : ClientDashboard.branch().label();
        text(g, name + "  Lv." + d.level() + " / 3", 20, 44, ACCENT);
        row(g, 64, "체력", fmt(d.health()) + " / " + fmt(d.maxHealth()), "");
        row(g, 82, "공격력", fmt(d.attack()), bonus(d.attackBonus()) + (d.madnessBonus() > 0 ? (d.attackBonus() > 0 ? " · " : "") + "광기 +" + String.format(Locale.ROOT,"%.1f",d.madnessBonus()) + "%" : ""));
        row(g, 100, "공격 속도", fmt(d.attackSpeed()) + " /초", bonus(d.speedBonus()));
        row(g, 118, "이동 속도", fmt(d.movement() * 1000) + "%", bonus(d.movementBonus()));
        row(g, 136, "방어력 / 강도", fmt(d.armor()) + " / " + fmt(d.toughness()), "장비 포함");
        row(g, 154, "투기 피해 감소", d.defense() + "%", "전투 피해");
        row(g, 172, "넉백저항", fmt(d.knockbackResistance() * 100) + "%", "장비·특성 포함");
        row(g, 190, "사기", String.format(Locale.ROOT, "×%.3f", d.morale()), "피해 배율 · 0.75~1.25");
        if (d.knightBatBonus()) text(g, "체크 메이트: 방망이 피해 ×2 · 신속Ⅰ · 점프 강화Ⅰ", 20, 210, ACCENT);
        text(g, "장비·물약·특성·사기 반영 (0.5초 간격)", 20, 228, MUTED);
        text(g, "공격력: 완충 공격 기준, 치명타·상대 방어 제외", 20, 241, MUTED);
    }
    private void row(GuiGraphics g, int y, String label, String value, String extra) {
        g.fill(16, y - 3, 324, y + 12, 0xFF1B2636);
        text(g, label, 22, y, MUTED); text(g, value, 120, y, WHITE);
        g.drawString(font, extra, 317 - font.width(extra), y, ACCENT, false);
    }
    private static String fmt(double value) { return String.format(Locale.ROOT, "%.2f", value); }
    private static String bonus(int value) { return value == 0 ? "" : "특성 " + (value > 0 ? "+" : "") + value + "%"; }
}
