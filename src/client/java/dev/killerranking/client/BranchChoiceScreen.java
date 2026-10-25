package dev.killerranking.client;

import dev.killerranking.combat.SpiritBranch;
import dev.killerranking.network.BranchRequestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public final class BranchChoiceScreen extends PanelScreen {
    private boolean waiting;
    private net.minecraft.client.gui.components.AbstractButton first, second;
    public BranchChoiceScreen(Screen parent) { super("투기 분기 선택", parent); }
    @Override protected void init() {
        super.init();
        first = button(210, 100, 108, 18, "떡발 선택", () -> choose(SpiritBranch.TTEOKBAL));
        second = button(210, 188, 108, 18, "킹앤킬 선택", () -> choose(SpiritBranch.KING_AND_KILL));
        first.active = second.active = ClientDashboard.needsChoice() && !waiting;
    }
    private void choose(SpiritBranch branch) {
        if (waiting || !ClientDashboard.needsChoice() || !ClientPlayNetworking.canSend(BranchRequestPayload.TYPE)) return;
        waiting = true;
        ClientPlayNetworking.send(new BranchRequestPayload(branch.id()));
    }
    @Override public void tick() {
        first.active = second.active = ClientDashboard.needsChoice() && !waiting;
        if (waiting && !ClientDashboard.needsChoice()) onClose();
    }
    @Override protected void content(GuiGraphics g) {
        var d = ClientDashboard.get(); int level = d == null ? 1 : d.level();
        text(g, "현재 레벨 " + level + " 기준 · 이로운 효과는 레벨에 비례", 20, 43, MUTED);
        card(g, 60, SpiritBranch.TTEOKBAL, level);
        card(g, 148, SpiritBranch.KING_AND_KILL, level);
        String status = waiting ? "선택을 적용하는 중…" : ClientDashboard.needsChoice() ? "분기 선택 후 변경은 관리자 명령으로 가능합니다." : "선택 가능: 투기 특성을 가진 분기 미선택 플레이어";
        text(g, status, 20, 226, MUTED);
        if (ClientDashboard.branch() != SpiritBranch.NONE) text(g, "현재 선택: " + ClientDashboard.branch().label(), 20, 240, ACCENT);
    }
    private void card(GuiGraphics g, int y, SpiritBranch branch, int level) {
        g.fill(16, y - 3, 324, y + 64, 0xFF1B2636);
        text(g, branch.label(), 22, y, ACCENT);
        text(g, "공격 +" + branch.attack(level) + "% · 방어 +" + branch.defense(level) + "%", 22, y + 16, WHITE);
        text(g, branch == SpiritBranch.TTEOKBAL ? "공속 +" + branch.attackSpeed(level) + "% · 이속 +" + branch.movement(level, false) + "%"
            : "이속 −2% → 체크 메이트 보유 시 +" + branch.movement(level, true) + "%", 22, y + 30, WHITE);
        text(g, branch == SpiritBranch.TTEOKBAL ? "연타 계열" : "체크 메이트 계열", 22, y + 48, MUTED);
    }
}
