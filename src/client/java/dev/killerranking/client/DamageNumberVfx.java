package dev.killerranking.client;

import dev.killerranking.combat.WeaponFamily;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

/** Floating pixel damage numbers at each weapon contact: rise, hang, then fade. Crits pop bigger and gold, kills red. */
public final class DamageNumberVfx {
    private record Number(Vec3 position, String text, int color, float scale, long born, float drift) { }
    private static final List<Number> NUMBERS = new ArrayList<>();
    private static final float LIFE = 22f;
    private static ClientLevel world;

    public static void register() { WorldRenderEvents.AFTER_ENTITIES.register(DamageNumberVfx::render); }

    public static void add(Vec3 at, float damage, WeaponFamily family, boolean crit, boolean lethal) {
        var c = Minecraft.getInstance();
        if (c.level == null || damage <= 0) return;
        if (world != c.level) { clear(); world = c.level; }
        if (NUMBERS.size() >= 48) NUMBERS.removeFirst();
        String text = damage >= 10 ? Integer.toString(Math.round(damage)) : String.format(java.util.Locale.ROOT, "%.1f", damage);
        int color = lethal ? 0xFF5A4A : crit ? 0xFFD54A : switch (family) {
            case SAW -> 0xFF7A6A;
            case HATCHET -> 0xFFC37A;
            case FIST, KNUCKLE -> 0xDDFFD2;
            case PIERCE -> 0xEADFFF;
            default -> 0xFFFFFF;
        };
        float scale = (crit || lethal ? 1.45f : 1f) * (damage >= 20 ? 1.2f : 1f);
        float drift = (c.level.random.nextFloat() - 0.5f) * 0.5f;
        NUMBERS.add(new Number(at.add(0, 0.25, 0), text, color, scale, c.level.getGameTime(), drift));
    }

    public static void tick(Minecraft c) {
        if (c.level != world) { clear(); world = c.level; return; }
        if (world == null || c.isPaused()) return;
        NUMBERS.removeIf(n -> world.getGameTime() - n.born >= LIFE + 1);
    }

    public static void clear() { NUMBERS.clear(); world = null; }

    private static void render(WorldRenderContext ctx) {
        if (ctx.world() != world || ctx.matrixStack() == null || ctx.consumers() == null || NUMBERS.isEmpty()) return;
        var camera = ctx.camera().getPosition();
        var font = Minecraft.getInstance().font;
        float delta = ctx.tickCounter().getGameTimeDeltaPartialTick(false);
        for (var n : NUMBERS) {
            if (n.position.distanceToSqr(camera) > 1024) continue;
            float age = world.getGameTime() - n.born + delta;
            if (age < 0 || age >= LIFE) continue;
            // quick pop, slow rise, late fade
            float pop = age < 2 ? 1.35f - 0.175f * age : 1f;
            float rise = (float) (1 - Math.exp(-age / 5f)) * 0.65f;
            float alpha = age < LIFE - 6 ? 1f : (LIFE - age) / 6f;
            if (alpha < 0.04f) continue; // Font treats near-zero alpha as opaque; skip the last sliver instead.
            float size = 0.028f * n.scale * pop;
            var stack = ctx.matrixStack();
            stack.pushPose();
            stack.translate(n.position.x - camera.x + n.drift * rise, n.position.y - camera.y + rise, n.position.z - camera.z);
            stack.mulPose(ctx.camera().rotation());
            stack.scale(-size, -size, size);
            float x = -font.width(n.text) / 2f;
            int a = (int) (alpha * 255) << 24;
            var m = stack.last().pose();
            // thick pixel outline: four offset shadow passes, then the coloured number
            for (int[] o : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}})
                font.drawInBatch(n.text, x + o[0], o[1], a | 0x1A1A1A, false, m, ctx.consumers(), Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);
            font.drawInBatch(n.text, x, 0, a | n.color, false, m, ctx.consumers(), Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);
            stack.popPose();
        }
    }

    private DamageNumberVfx() { }
}
