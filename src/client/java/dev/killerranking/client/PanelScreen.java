package dev.killerranking.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class PanelScreen extends Screen {
    protected static final int W = 340, H = 286, WHITE = 0xFFF1F4FA, MUTED = 0xFF9FACBF, ACCENT = 0xFF81D6C5;
    protected final Screen parent;
    protected int left, top;
    protected float scale;
    protected PanelScreen(String title, Screen parent) { super(Component.literal(title)); this.parent = parent; if (parent == null) UiSounds.open(); }
    @Override protected void init() {
        scale = FixedUi.factor();
        left = Math.round((width - W * scale) / 2); top = Math.round((height - H * scale) / 2);
        button(303, 11, 22, 18, "×", this::onClose);
    }
    protected AbstractButton button(int x, int y, int w, int h, String label, Runnable action) {
        return addRenderableWidget(new AbstractButton(left + Math.round(x * scale), top + Math.round(y * scale), Math.round(w * scale), Math.round(h * scale), Component.literal(label)) {
            @Override public void onPress() { action.run(); }
            @Override protected void renderWidget(GuiGraphics g, int mx, int my, float delta) {
                // Keep widgets above the separately batched panel background.
                g.pose().pushPose(); g.pose().translate(0, 0, 10);
                g.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), !active ? 0xFF202C3D : isHoveredOrFocused() ? 0xFF456478 : 0xFF2E425A);
                if (isFocused()) g.renderOutline(getX(), getY(), getWidth(), getHeight(), WHITE);
                g.pose().pushPose(); g.pose().translate(getX() + getWidth() / 2f, getY() + (getHeight() - 8 * scale) / 2f, 0); g.pose().scale(scale, scale, 1);
                g.drawCenteredString(font, getMessage(), 0, 0, active ? WHITE : MUTED); g.pose().popPose();
                g.pose().popPose();
            }
            @Override protected void updateWidgetNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
            @Override public void playDownSound(net.minecraft.client.sounds.SoundManager manager) { UiSounds.click(manager); }
        });
    }
    @Override public void renderBackground(GuiGraphics g, int mx, int my, float delta) {
        super.renderBackground(g, mx, my, delta);
        g.pose().pushPose(); g.pose().translate(left, top, 0); g.pose().scale(scale, scale, 1);
        g.fill(0, 0, W, H, 0xFF101723); g.renderOutline(0, 0, W, H, 0xFF456478);
        g.drawCenteredString(font, title, W / 2, 16, ACCENT);
        g.fill(16, 34, W - 16, 35, 0xFF344053);
        content(g);
        g.drawCenteredString(font, "ESC로 닫기", W / 2, 266, MUTED);
        g.pose().popPose();
        g.flush();
    }
    protected abstract void content(GuiGraphics g);
    protected void text(GuiGraphics g, String value, int x, int y, int color) { g.drawString(font, value, x, y, color, false); }
    @Override public void onClose() { if (parent == null) UiSounds.close(); minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
