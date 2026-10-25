package dev.killerranking.client;
import dev.killerranking.data.KillerPoints;

import dev.killerranking.KillerRankingMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** A resizable phone with keyboard-accessible navigation and live server data. */
public final class PhoneScreen extends Screen {
	private static final int W = 224, H = 270;
	private static final int WHITE = 0xFFF1F4FA, MUTED = 0xFF9FACBF, RED = 0xFFF06470;
	private int page;
	private int left, top;
	private float scale;
    private PhoneButton branchButton;
	public PhoneScreen() { super(Component.translatable("item.killerranking.phone")); UiSounds.open(); }
	@Override public void onClose() { UiSounds.close(); super.onClose(); }

	@Override protected void init() {
		scale = FixedUi.factor();
		left = Math.round((width - W * scale) / 2);
		top = Math.round((height - H * scale) / 2);
		for (int index = 0; index < 3; index++) {
			final int selected = index;
			addRenderableWidget(new PhoneButton(14 + index * 66, 230, 64, 18,
				Component.translatable("phone.killerranking.tab." + index), () -> { if (selected == 2) minecraft.setScreen(new MissionsScreen(this)); else page = selected; }, index));
		}
		addRenderableWidget(new PhoneButton(136, 17, 48, 16, Component.literal("스탯"), () -> minecraft.setScreen(new StatsScreen(this)), -1));
        addRenderableWidget(new PhoneButton(82, 17, 48, 16, Component.literal("상점"), () -> minecraft.setScreen(new PointsShopScreen(this)), -1));
        addRenderableWidget(new PhoneButton(191, 12, 18, 18, Component.literal("×"), this::onClose, -1));
        branchButton = addRenderableWidget(new PhoneButton(25, 135, 173, 17, Component.literal("투기 분기 선택"),
            () -> minecraft.setScreen(new BranchChoiceScreen(this)), -1));
        updateBranchButton();
	}
    private void updateBranchButton() {
        branchButton.visible = page == 1 && ClientDashboard.needsChoice();
        branchButton.active = branchButton.visible;
    }

	@Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
        updateBranchButton();
		super.renderBackground(g, mouseX, mouseY, delta);
		g.pose().pushPose();
		g.pose().translate(left, top, 0);
		g.pose().scale(scale, scale, 1);
		g.fill(2, 2, W - 2, H - 2, 0xFF080B11);
		g.fill(0, 10, W, H - 10, 0xFF080B11);
		g.fill(4, 6, W - 4, H - 6, 0xFF374355);
		g.fill(7, 9, W - 7, H - 9, 0xFF101723);
		g.fill(93, 10, 131, 13, 0xFF06080D);
		g.fill(136, 10, 139, 13, RED);
		text(g, "K / R", 17, 19, RED);
		text(g, "KILLER RANKING  ·  " + KillerRankingClient.traitKeyLabel().getString() + " " + (TraitVisuals.enabled() ? "ON" : "OFF"), 17, 36, WHITE);
		g.fill(17, 49, W - 17, 50, 0xFF344053);
		if (!ClientCombatStatus.available()) {
			g.drawCenteredString(font, tr("hud.killerranking.waiting"), W / 2, 112, MUTED);
		} else if (page == 0) renderHome(g);
		else if (page == 1) renderCombat(g);
		else renderMission(g);
		g.drawCenteredString(font, tr("phone.killerranking.close"), W / 2, 251, MUTED);
		g.pose().popPose();
	}

	private void renderHome(GuiGraphics g) {
		var profile = ClientProfile.get();
		card(g, 58, 43, RED);
		text(g, tr("phone.killerranking.points"), 25, 65, MUTED);
		g.pose().pushPose();
		g.pose().translate(25, 80, 0);
        String balance = KillerPoints.format(profile.pointUnits()) + " P";
        float balanceScale = Math.min(1.25F, 148F / Math.max(1, font.width(balance)));
		g.pose().scale(balanceScale, balanceScale, 1);
		text(g, balance, 0, 0, WHITE);
		g.pose().popPose();
		g.renderItem(KillerRankingMod.PHONE.getDefaultInstance(), 179, 70);
		card(g, 108, 57, CombatHud.traitColor());
		text(g, CombatHud.traitName(), 25, 116, CombatHud.traitColor());
		text(g, CombatHud.traitStatus(), 25, 134, WHITE);
		CombatHud.bar(g, 25, 151, 173, 5, ClientCombatStatus.gauge(), CombatHud.traitColor());
		card(g, 172, 49, 0xFF81D6C5);
		var data = ClientDashboard.get();
        long complete = data == null ? 0 : data.missions().stream().filter(m -> m.complete()).count();
        text(g, "랜덤 미션 · " + complete + " / 10 완료", 25, 179, MUTED);
        text(g, ClientDashboard.remaining(), 25, 194, WHITE);
        CombatHud.bar(g, 25, 208, 173, 4, complete / 10f, 0xFF81D6C5);
	}

	private void renderCombat(GuiGraphics g) {
		card(g, 58, 119, CombatHud.traitColor());
		text(g, CombatHud.traitName(), 25, 66, CombatHud.traitColor());
		g.drawWordWrap(font, CombatHud.traitDescription(), 25, 84, 173, MUTED);
        if (!ClientDashboard.needsChoice()) text(g, ClientCombatStatus.trait() == dev.killerranking.Trait.MIXED_MADNESS
            ? Component.literal("미친 고씨 · ").append(CombatHud.survivalStatus()) : CombatHud.traitStatus(), 25, 137, WHITE);
		CombatHud.bar(g, 25, 152, 173, 4, ClientCombatStatus.gauge(), CombatHud.traitColor());
        var progress = ClientDashboard.get();
        if (progress != null) text(g, progress.level() >= 3 ? "특성 경험치 · MAX" : "특성 경험치 " + progress.traitXp() + " / " + progress.traitXpRequired(), 25, 164, WHITE);
		card(g, 178, 43, 0xFF81D6C5);
		text(g, Component.translatable("phone.killerranking.parry_key", KillerRankingClient.parryKeyLabel()), 25, 186, WHITE);
		Component status = CombatHud.parryStatus();
		g.drawString(font, status, 199 - font.width(status), 186, 0xFF81D6C5, false);
		CombatHud.bar(g, 25, 205, 173, 5, ClientCombatStatus.parryProgress(), 0xFF81D6C5);
	}

	private void renderMission(GuiGraphics g) {
        text(g, "미션 탭을 눌러 10개 목록 보기", 25, 80, WHITE);
    }

	private static Component tr(String key) { return Component.translatable(key); }
	private void text(GuiGraphics g, String value, int x, int y, int color) { g.drawString(font, value, x, y, color, false); }
	private void text(GuiGraphics g, Component value, int x, int y, int color) { g.drawString(font, value, x, y, color, false); }
	private void card(GuiGraphics g, int y, int height, int accent) {
		g.fill(17, y, 207, y + height, 0xFF1B2636);
		g.fill(17, y, 19, y + height, accent);
	}
	@Override public boolean isPauseScreen() { return false; }

	private final class PhoneButton extends AbstractButton {
		private final Runnable action;
		private final int tab;
		PhoneButton(int x, int y, int w, int h, Component label, Runnable action, int tab) {
			super(left + Math.round(x * scale), top + Math.round(y * scale), Math.round(w * scale), Math.round(h * scale), label);
			this.action = action;
			this.tab = tab;
		}
		@Override public void onPress() { action.run(); }
		@Override protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
			boolean selected = tab >= 0 && page == tab;
			g.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), selected ? 0xFF963E4A : isHoveredOrFocused() ? 0xFF35445A : 0xFF202C3D);
			if (isFocused()) g.renderOutline(getX(), getY(), getWidth(), getHeight(), WHITE);
			g.pose().pushPose();
			g.pose().translate(getX() + getWidth() / 2.0F, getY() + (getHeight() - 8 * scale) / 2, 0);
			g.pose().scale(scale, scale, 1);
			g.drawCenteredString(Minecraft.getInstance().font, getMessage(), 0, 0, WHITE);
			g.pose().popPose();
		}
		@Override protected void updateWidgetNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
		@Override public void playDownSound(net.minecraft.client.sounds.SoundManager manager) { UiSounds.click(manager); }
	}
}
