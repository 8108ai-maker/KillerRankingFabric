package dev.killerranking.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static dev.killerranking.client.PoisonSnakeModel.box;
import static dev.killerranking.client.PoisonSnakeModel.face;

/** Block-sized coils and a thin upper-neck flare form one ghostly snake silhouette. */
final class SalmosaSnakeModel {
    static final float HEAD_SCALE = .75f;
    private static final RenderType LAYER = RenderType.entityTranslucent(
        ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png"));
    private static final int BODY = 0x9E77C5, SCALE = 0xD4B4F1, BELLY = 0xBE9ADF;
    private static final int MOUTH = 0x382243, FANG = 0xF2E8FF;
    private static final float[][] SKULL = {
        {-.38f, -.53f, .30f}, {.38f, -.53f, .30f}, {.48f, -.30f, .35f},
        {.42f, .34f, .28f}, {.30f, .83f, .23f}, {-.30f, .83f, .23f},
        {-.42f, .34f, .28f}, {-.48f, -.30f, .35f}
    };
    // A narrow stem, long lateral flare, and short collar beneath the small skull.
    private static final float[][] HOOD = {
        {.16f, .29f, .27f}, {.35f, .33f, .24f}, {.54f, .52f, .18f},
        {.73f, .72f, .14f}, {.88f, .67f, .15f}, {1f, .32f, .25f}
    };
    private static final float[][] RING = {
        {-.84f,-1}, {.84f,-1}, {1,-.65f}, {1,.65f},
        {.84f,1}, {-.84f,1}, {-1,.65f}, {-1,-.65f}
    };

    static void render(WorldRenderContext context, Player player, float delta, float alpha) {
        var stack = context.matrixStack();
        var camera = context.camera().getPosition();
        var vertices = context.consumers().getBuffer(LAYER);
        float time = player.tickCount + delta, age = PoisonVfx.age(player.getUUID()) + delta;
        stack.pushPose();
        stack.translate(Mth.lerp(delta, player.xOld, player.getX()) - camera.x,
            Mth.lerp(delta, player.yOld, player.getY()) - camera.y,
            Mth.lerp(delta, player.zOld, player.getZ()) - camera.z);
        stack.mulPose(Axis.YP.rotationDegrees(-Mth.rotLerp(delta, player.yBodyRotO, player.yBodyRot)));
        var previous = coil(0, time);
        for (int i = 1; i <= 10; i++) {
            var next = coil(i / 10f, time);
            segment(stack, vertices, previous, next, .14f + .44f * Math.min(1, i / 3f), alpha * .86f);
            previous = next;
        }
        segment(stack, vertices, previous, neck(.16f, time, age), .57f, alpha * .86f);
        hood(stack, vertices, time, age, alpha * .88f);
        var origin = neck(1, time, age);
        stack.pushPose();
        stack.translate(origin.x, origin.y, origin.z);
        stack.mulPose(Axis.YP.rotationDegrees(headYaw(time)));
        stack.mulPose(Axis.XP.rotationDegrees(headPitch(time, age)));
        stack.scale(HEAD_SCALE, HEAD_SCALE, HEAD_SCALE);
        head(stack, vertices, alpha * .93f, age);
        stack.popPose();
        stack.popPose();
    }

    static Vector3f headPoint(Player player, float delta, float x, float y, float z) {
        float time = player.tickCount + delta, age = PoisonVfx.age(player.getUUID()) + delta;
        return new Vector3f(x, y, z).mul(HEAD_SCALE)
            .rotateX((float) Math.toRadians(headPitch(time, age)))
            .rotateY((float) Math.toRadians(headYaw(time)))
            .add(neck(1, time, age))
            .rotateY((float) Math.toRadians(-Mth.rotLerp(delta, player.yBodyRotO, player.yBodyRot)));
    }

    private static float headYaw(float time) { return -31 + (float) Math.sin(time * .035f) * 5; }
    private static float headPitch(float time, float age) { return 6 - 11 * threat(age); }
    private static float smooth(float x) { x = Mth.clamp(x, 0, 1); return x * x * (3 - 2 * x); }
    private static float threat(float age) {
        float t = age % 72;
        return smooth((t - 7) / 10) * smooth((47 - t) / 12);
    }

    private static Vector3f coil(float t, float time) {
        double angle = -Math.PI / 2 + t * Math.PI * 2.65;
        float radius = .94f + .27f * (float) Math.sin(t * Math.PI);
        return new Vector3f((float) Math.sin(angle) * radius,
            .24f + t * 1.10f + (float) Math.sin(angle * 2 + time * .045) * .018f,
            -1.85f + (float) Math.cos(angle) * .62f);
    }

    private static Vector3f neck(float t, float time, float age) {
        var base = coil(1, time);
        float sway = (float) Math.sin(time * .045) * .055f;
        return new Vector3f(base.x - .20f * (float) Math.sin(t * Math.PI) + (.12f + sway) * t,
            base.y + 1.48f * t, base.z + .30f * t + .05f * threat(age) * t);
    }

    private static Vector3f hoodPoint(float t, float x, float z, float time, float age) {
        int band = 0;
        while (band < HOOD.length - 2 && t > HOOD[band + 1][0]) band++;
        var low = HOOD[band]; var high = HOOD[band + 1];
        float u = Mth.clamp((t - low[0]) / (high[0] - low[0]), 0, 1);
        float width = Mth.lerp(u, low[1], high[1]) * (.95f + .05f * threat(age));
        float depth = Mth.lerp(u, low[2], high[2]);
        return new Vector3f(x * width, 0, z * depth - .28f * t)
            .rotateY((float) Math.toRadians(headYaw(time) * .65f)).add(neck(t, time, age));
    }

    private static void hood(PoseStack stack, VertexConsumer v, float time, float age, float alpha) {
        for (int band = 0; band < HOOD.length - 1; band++) {
            float low = HOOD[band][0], high = HOOD[band + 1][0];
            for (int edge = 0; edge < RING.length; edge++) {
                var a = RING[edge]; var b = RING[(edge + 1) % RING.length];
                var p = hoodPoint(low, a[0], a[1], time, age);
                var q = hoodPoint(low, b[0], b[1], time, age);
                var r = hoodPoint(high, b[0], b[1], time, age);
                var s = hoodPoint(high, a[0], a[1], time, age);
                var normal = new Vector3f(s).sub(p).cross(new Vector3f(q).sub(p)).normalize();
                quad(stack, v, edge == 4 ? BELLY : BODY, alpha, normal, p, s, r, q);
            }
        }
        // Small stepped scale edges sit on the surface; no wire diamonds or extra model chunks.
        for (int surface : new int[]{-1, 1}) {
            for (int row = 0; row < 8; row++) {
                float t = .25f + row * .087f;
                for (int col = -2; col <= 2; col++) {
                    float x = col * .31f + (row % 2) * .12f;
                    if (Math.abs(x) > .75f) continue;
                    hoodPixel(stack, v, t, t + .009f, x - .115f, x - .038f, surface, time, age, alpha * .65f);
                    hoodPixel(stack, v, t - .012f, t - .003f, x - .038f, x + .038f, surface, time, age, alpha * .65f);
                    hoodPixel(stack, v, t, t + .009f, x + .038f, x + .115f, surface, time, age, alpha * .65f);
                }
            }
        }
        // Broad belly ribs reinforce the rising neck instead of a flat circular shield.
        for (int row = 0; row < 9; row++) {
            float t = .20f + row * .08f;
            hoodPixel(stack, v, t, t + .014f, -.21f, .21f, 1, time, age, alpha * .7f);
        }
    }

    private static void hoodPixel(PoseStack stack, VertexConsumer v, float low, float high,
                                  float left, float right, int surface, float time, float age, float alpha) {
        float z = surface * 1.012f;
        var normal = new Vector3f(0, 0, surface).rotateY((float) Math.toRadians(headYaw(time) * .65f));
        var a = hoodPoint(low, left, z, time, age); var b = hoodPoint(low, right, z, time, age);
        var c = hoodPoint(high, right, z, time, age); var d = hoodPoint(high, left, z, time, age);
        if (surface > 0) quad(stack, v, SCALE, alpha, normal, a, b, c, d);
        else quad(stack, v, SCALE, alpha, normal, b, a, d, c);
    }

    private static void segment(PoseStack stack, VertexConsumer v, Vector3f a, Vector3f b,
                                float width, float alpha) {
        var direction = new Vector3f(b).sub(a);
        float length = direction.length() * .97f, half = width * .5f;
        stack.pushPose();
        stack.translate((a.x + b.x) * .5f, (a.y + b.y) * .5f, (a.z + b.z) * .5f);
        stack.mulPose(new Quaternionf().rotationTo(new Vector3f(0, 1, 0), direction.normalize()));
        box(stack, v, -half, -length * .5f, -half, width, length, width, BODY, alpha);
        for (int side = 0; side < 4; side++) {
            stack.pushPose(); stack.mulPose(Axis.YP.rotationDegrees(side * 90));
            for (int row = 0; row < 3; row++) {
                float y = (row - 1) * length * .29f;
                for (int col = -1; col <= 1; col++) {
                    float x = col * width * .27f;
                    pixel(stack, v, x - width * .10f, y, width * .07f, .013f, half + .002f, SCALE, alpha * .68f);
                    pixel(stack, v, x - width * .035f, y - .018f, width * .07f, .013f, half + .002f, SCALE, alpha * .68f);
                    pixel(stack, v, x + width * .03f, y, width * .07f, .013f, half + .002f, SCALE, alpha * .68f);
                }
            }
            stack.popPose();
        }
        stack.popPose();
    }

    private static void pixel(PoseStack stack, VertexConsumer v, float x, float y, float w, float h,
                              float z, int color, float alpha) {
        face(stack, v, color, alpha, 0, 0, 1, x, y, z, x + w, y, z,
            x + w, y + h, z, x, y + h, z);
    }

    private static void quad(PoseStack stack, VertexConsumer v, int color, float alpha, Vector3f normal,
                             Vector3f a, Vector3f b, Vector3f c, Vector3f d) {
        face(stack, v, color, alpha, normal.x, normal.y, normal.z,
            a.x, a.y, a.z, b.x, b.y, b.z, c.x, c.y, c.z, d.x, d.y, d.z);
    }

    private static void head(PoseStack stack, VertexConsumer v, float alpha, float age) {
        float hiss = threat(age);
        for (int i = 0; i < SKULL.length; i++) {
            var a = SKULL[i]; var b = SKULL[(i + 1) % SKULL.length];
            float dx = b[0] - a[0], dz = b[1] - a[1], length = (float) Math.hypot(dx, dz);
            face(stack, v, BODY, alpha, dz / length, 0, -dx / length,
                a[0], -.08f, a[1], b[0], -.08f, b[1], b[0], b[2], b[1], a[0], a[2], a[1]);
            face(stack, v, BELLY, alpha, 0, 1, 0, a[0], a[2], a[1], b[0], b[2], b[1],
                0, .33f, -.12f, 0, .33f, -.12f);
            face(stack, v, MOUTH, alpha, 0, -1, 0, b[0], -.082f, b[1], a[0], -.082f, a[1],
                0, -.082f, -.12f, 0, -.082f, -.12f);
        }
        for (int side : new int[]{-1, 1}) {
            // The brow is raised at the rear, so the flame sits on the cheek instead of above the skull.
            stack.pushPose();
            stack.translate(side * .36f, .20f, .23f);
            stack.mulPose(Axis.ZP.rotationDegrees(side * 8));
            box(stack, v, -.09f, -.012f, -.13f, .18f, .045f, .37f, BELLY, alpha);
            stack.popPose();
            box(stack, v, side * .16f - .020f, -.016f, .834f, .04f, .028f, .008f, MOUTH, alpha);
            stack.pushPose();
            stack.translate(side * .24f, -.087f, .55f);
            stack.mulPose(Axis.XP.rotationDegrees(76 * (1 - hiss)));
            box(stack, v, -.047f, -.21f, -.042f, .094f, .21f, .084f, FANG, alpha);
            box(stack, v, -.026f, -.44f, -.022f, .052f, .23f, .044f, FANG, alpha);
            box(stack, v, -.011f, -.58f, -.009f, .022f, .14f, .018f, 0xFFFFFF, alpha);
            stack.popPose();
        }
        stack.pushPose();
        stack.translate(0, -.09f, -.40f);
        stack.mulPose(Axis.XP.rotationDegrees(10 + 42 * hiss));
        box(stack, v, -.35f, -.12f, 0, .70f, .12f, .78f, BODY, alpha);
        box(stack, v, -.29f, -.11f, .71f, .58f, .11f, .47f, BELLY, alpha);
        box(stack, v, -.29f, .001f, .08f, .58f, .009f, 1.01f, MOUTH, alpha);
        float t = age % 72, flick = 0;
        for (int center : new int[]{20, 28, 36})
            flick = Math.max(flick, Math.max(0, 1 - Math.abs(t - center) / 3));
        flick = smooth(flick) * hiss;
        if (flick > .02f) {
            stack.pushPose();
            stack.translate(0, .02f, 1.02f);
            stack.mulPose(Axis.XP.rotationDegrees(-32 * hiss));
            float length = .14f + .36f * flick;
            box(stack, v, -.018f, 0, 0, .036f, .02f, length, 0xE866B0, alpha);
            for (int side : new int[]{-1, 1}) {
                stack.pushPose(); stack.translate(side * .009f, 0, length - .008f);
                stack.mulPose(Axis.YP.rotationDegrees(side * 25));
                box(stack, v, -.009f, 0, 0, .018f, .017f, .15f * flick, 0xFFC0E1, alpha);
                stack.popPose();
            }
            stack.popPose();
        }
        stack.popPose();
    }

    private SalmosaSnakeModel() { }
}
