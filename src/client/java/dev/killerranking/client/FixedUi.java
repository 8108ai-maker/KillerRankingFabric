package dev.killerranking.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Mod coordinates use three physical pixels per unit, independent of vanilla GUI scale. */
public final class FixedUi {
    public static final int SCALE = 3;
    public static float factor() { return (float) (SCALE / Minecraft.getInstance().getWindow().getGuiScale()); }
    public static int width() { return (int) Math.ceil(Minecraft.getInstance().getWindow().getWidth() / (double) SCALE); }
    public static int height() { return (int) Math.ceil(Minecraft.getInstance().getWindow().getHeight() / (double) SCALE); }
    public static void begin(GuiGraphics graphics) {
        graphics.pose().pushPose();
        graphics.pose().scale(factor(), factor(), 1);
    }
    public static void end(GuiGraphics graphics) { graphics.pose().popPose(); }
    private FixedUi() { }
}
