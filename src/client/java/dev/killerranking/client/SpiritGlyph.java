package dev.killerranking.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.killerranking.combat.SpiritBranch;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/** Calligraphic fighting-spirit character, rasterized once from a brush typeface. */
final class SpiritGlyph {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("killerranking", "textures/vfx/tteokbal_kanji.png");
    private static final RenderType LAYER = RenderType.entityTranslucentEmissive(TEXTURE);
    private static final RenderType CORE = RenderType.entityTranslucent(TEXTURE);
    static void render(WorldRenderContext context, Player player, float delta, float alpha, SpiritBranch branch) {
        var pose = context.matrixStack();
        var camera = context.camera().getPosition();
        float time = player.tickCount + delta;
        float yaw = Mth.rotLerp(delta, player.yBodyRotO, player.yBodyRot);
        pose.pushPose();
        pose.translate(Mth.lerp(delta, player.xOld, player.getX()) - camera.x,
            Mth.lerp(delta, player.yOld, player.getY()) - camera.y,
            Mth.lerp(delta, player.zOld, player.getZ()) - camera.z);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.translate(Math.sin(time * .025) * .025, 1.67 + Math.sin(time * .04) * .035, -.95);
        double radians = Math.toRadians(yaw);
        boolean back = (camera.x - player.getX()) * -Math.sin(radians) + (camera.z - player.getZ()) * Math.cos(radians) < 0;
        if (back) pose.mulPose(Axis.YP.rotationDegrees(180));
        float pulse = alpha * (.92f + .08f * (float) Math.sin(time * .06)) * (back ? .85f : 1);
        var vertices = context.consumers().getBuffer(LAYER);
        boolean white = branch == SpiritBranch.KING_AND_KILL;
        quad(context, vertices, 3.40f, 3.88f, -.02f, white ? 0xC5C5C5 : 0x25CE4C, pulse * .12f);
        quad(context, vertices, 3.28f, 3.74f, -.01f, white ? 0xEBEBEB : 0x4BE666, pulse * .24f);
        // The main strokes write depth, so the denser smoke stays behind the character.
        quad(context, context.consumers().getBuffer(CORE), 3.20f, 3.65f, 0, white ? 0xFFFFFF : 0xBDFFAA, pulse * .94f);
        pose.popPose();
    }
    private static void quad(WorldRenderContext context, VertexConsumer v, float width, float height,
            float z, int color, float alpha) {
        var pose = context.matrixStack().last();
        float[] x = {-width/2, width/2, width/2, -width/2};
        float[] y = {-height/2, -height/2, height/2, height/2};
        for (int i=0; i<4; i++) v.addVertex(pose.pose(), x[i], y[i], z)
            .setColor((color >> 16 & 255)/255f, (color >> 8 & 255)/255f, (color & 255)/255f, alpha)
            .setUv(i == 0 || i == 3 ? 0 : 1, i < 2 ? 1 : 0)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0, 1, 0);
    }
    private SpiritGlyph() { }
}
