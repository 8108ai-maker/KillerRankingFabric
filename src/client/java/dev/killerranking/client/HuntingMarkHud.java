package dev.killerranking.client;

import com.mojang.math.Axis;
import dev.killerranking.network.HuntingMarkPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import java.util.Locale;

public final class HuntingMarkHud {
    private static HuntingMarkPayload mark = HuntingMarkPayload.CLEAR;
    private static int remaining;
    public static void accept(HuntingMarkPayload value) { mark = value; remaining = Math.clamp(value.ticks(), 0, 180); }
    public static void clear() { accept(HuntingMarkPayload.CLEAR); }
    public static boolean active() { return mark.active() && remaining > 0; }
    public static HuntingMarkPayload get() { return mark; }
    public static void tick(Minecraft client) { if (!client.isPaused() && remaining > 0) remaining--; }
    public static void render(GuiGraphics g) {
        var client = Minecraft.getInstance();
        if (!active() || client.player == null || !client.player.isAlive() || client.player.isSpectator() || client.options.hideGui || client.screen != null) return;
        FixedUi.begin(g);
        int width = Math.min(250, FixedUi.width() - 12), x = (FixedUi.width() - width) / 2, y = 8;
        double dx = mark.x() - client.player.getX(), dy = mark.y() - client.player.getY(), dz = mark.z() - client.player.getZ();
        double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
        g.fill(x, y, x + width, y + 33, 0xCE151021);
        g.fill(x, y, x + 2, y + 33, 0xFFBF84F2);
        var font = client.font;
        String target = "사냥표식 · " + mark.name() + " · " + Math.round(distance) + "m";
        g.drawString(font, font.plainSubstrByWidth(target, width - 31), x + 26, y + 5, 0xFFE4C0FF, false);
        String detail = String.format(Locale.ROOT, "X %.0f Y %.0f Z %.0f · %.1fs", mark.x(), mark.y(), mark.z(), remaining / 20f);
        g.drawString(font, font.plainSubstrByWidth(detail, width - 31), x + 26, y + 19, 0xFFFFFFFF, false);
        float angle = Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(-dx, dz)) - client.player.getYRot());
        g.pose().pushPose(); g.pose().translate(x + 14, y + 16, 0); g.pose().mulPose(Axis.ZP.rotationDegrees(angle));
        g.fill(-1, -7, 2, 6, 0xFFDCAAFF);
        for (int i = 0; i < 5; i++) g.fill(-i, -7 + i, i + 1, -6 + i, 0xFFDCAAFF);
        g.pose().popPose();
        FixedUi.end(g);
    }
    private HuntingMarkHud() { }
}
