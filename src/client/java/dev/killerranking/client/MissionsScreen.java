package dev.killerranking.client;
import dev.killerranking.data.KillerPoints;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public final class MissionsScreen extends PanelScreen {
    private int page;
    public MissionsScreen(Screen parent) { super("랜덤 미션 · 10개", parent); }
    @Override protected void init() {
        super.init();
        button(20, 234, 84, 18, "이전 5개", () -> page = 0);
        button(236, 234, 84, 18, "다음 5개", () -> page = 1);
    }
    @Override protected void content(GuiGraphics g) {
        var data = ClientDashboard.get();
        text(g, ClientDashboard.remaining(), 20, 43, ACCENT);
        if (data == null || data.missions().isEmpty()) { text(g, "미션을 불러오는 중…", 20, 70, MUTED); return; }
        for (int i = 0; i < 5; i++) {
            int index = page * 5 + i;
            if (index >= data.missions().size()) break;
            var m = data.missions().get(index); int y = 62 + i * 32;
            g.fill(16, y, 324, y + 29, 0xFF1B2636);
            text(g, (index + 1) + ". " + m.title(), 22, y + 4, m.complete() ? ACCENT : WHITE);
            String progress = (m.progress() / m.divisor()) + " / " + (m.goal() / m.divisor()) + " " + m.unit();
            g.drawString(font, progress, 317 - font.width(progress), y + 4, WHITE, false);
            String reward = (m.complete() ? "지급 완료 " : "보상 ") + KillerPoints.format(m.rewardUnits()) + " P";
            CombatHud.bar(g, 23, y + 19, Math.min(182, 284 - font.width(reward)), 5, m.progress() / (float) m.goal(), ACCENT);
            g.drawString(font, reward, 317 - font.width(reward), y + 17, m.complete() ? ACCENT : 0xFFF1CF7A, false);
        }
        text(g, "완료 즉시 지급 · " + (page + 1) + " / 2", 118, 239, MUTED);
    }
}
