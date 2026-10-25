package dev.killerranking.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.WeaponFamily;
import dev.killerranking.network.WeaponHitPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * Camera-facing pixel sprite at every ordinary weapon contact. Each weapon family has its
 * own 8-frame sheet (4x2 grid); slashes lean with the swing and alternate direction so
 * left and right cuts read differently, bursts stay upright.
 */
public final class WeaponHitVfx {
    private static final int FRAMES = 8, COLS = 4, ROWS = 2;
    private static final float LIFE_TICKS = 7f;

    private record Impact(UUID attacker, WeaponFamily family, Vec3 position, float roll, float size, boolean lethal, long born, float seed) { }

    private static final List<Impact> IMPACTS = new ArrayList<>();
    private static final Map<UUID, Boolean> SIDE = new HashMap<>();
    private static final Map<WeaponFamily, ResourceLocation> SHEETS = new HashMap<>();
    private static final ResourceLocation LETHAL = KillerRankingMod.id("textures/vfx/hit_lethal.png");
    private static ClientLevel world;

    static {
        SHEETS.put(WeaponFamily.BLADE, KillerRankingMod.id("textures/vfx/hit_blade.png"));
        SHEETS.put(WeaponFamily.SAW, KillerRankingMod.id("textures/vfx/hit_saw.png"));
        SHEETS.put(WeaponFamily.BLUNT, KillerRankingMod.id("textures/vfx/hit_blunt.png"));
        SHEETS.put(WeaponFamily.FIST, KillerRankingMod.id("textures/vfx/hit_fist.png"));
        SHEETS.put(WeaponFamily.HATCHET, KillerRankingMod.id("textures/vfx/hit_hatchet.png"));
        SHEETS.put(WeaponFamily.PIERCE, KillerRankingMod.id("textures/vfx/hit_pierce.png"));
        SHEETS.put(WeaponFamily.KNUCKLE, KillerRankingMod.id("textures/vfx/hit_knuckle.png"));
    }

    public static void register() { WorldRenderEvents.AFTER_ENTITIES.register(WeaponHitVfx::render); }

    public static void accept(WeaponHitPayload p) {
        var c = Minecraft.getInstance();
        if (c.level == null) return;
        if (world != c.level) { clear(); world = c.level; }
        if (!Double.isFinite(p.x()) || !Double.isFinite(p.y()) || !Double.isFinite(p.z())) return;
        var family = WeaponFamily.byOrdinal(p.family());
        if (family == null) return;
        if (IMPACTS.size() >= 64) IMPACTS.removeFirst();
        boolean slash = family == WeaponFamily.BLADE || family == WeaponFamily.SAW || family == WeaponFamily.HATCHET;
        boolean left = !SIDE.getOrDefault(p.attacker(), false);
        SIDE.put(p.attacker(), left);
        float seed = (float) ((p.x() * 31 + p.z() * 17) % 1.0);
        // Slashes lean ±25° around their diagonal and mirror per swing; bursts only jitter.
        float roll = slash ? (left ? 1 : -1) * (20f + seed * 25f) + (left ? 0 : 90) : seed * 360f;
        float size = switch (family) {
            case BLUNT, HATCHET -> 1.7f;
            case KNUCKLE -> 1.5f;
            case PIERCE, FIST -> 1.15f;
            default -> 1.45f;
        } * (p.crit() ? 1.3f : 1f);
        var at = new Vec3(p.x(), p.y(), p.z());
        IMPACTS.add(new Impact(p.attacker(), family, at, roll, size, p.lethal(), world.getGameTime(), seed));
        // Shards burst away from the attacker; slashes throw them along the cut, bursts all around.
        var style = TrailStyle.of(family);
        var attacker = c.level.getPlayerByUUID(p.attacker());
        Vec3 away = attacker == null ? new Vec3(0, 0.3, 0) : at.subtract(attacker.position().add(0, 1.2, 0)).normalize().scale(0.6).add(0, 0.35, 0);
        int count = switch (family) { case BLUNT, HATCHET -> 18; case SAW -> 22; case PIERCE -> 10; default -> 14; } + (p.crit() ? 8 : 0) + (p.lethal() ? 14 : 0);
        SlashShardParticle.burst(at, away, count, style.shardColor, 0.32f, 0.42f, 0.09f, 18);
        if (p.lethal()) SlashShardParticle.burst(at, new Vec3(0, 0.5, 0), 12, 0xFF3A3A, 0.25f, 0.5f, 0.11f, 26);
        DamageNumberVfx.add(at, p.damage(), family, p.crit(), p.lethal());
        if (p.attacker().equals(c.player == null ? null : c.player.getUUID())) CombatCameraFeedback.hit(family, p.crit());
    }

    public static void tick(Minecraft c) {
        if (c.level != world) { clear(); world = c.level; return; }
        if (world == null || c.isPaused()) return;
        IMPACTS.removeIf(i -> world.getGameTime() - i.born >= LIFE_TICKS + 1);
    }

    public static int activeCount() { return IMPACTS.size(); }
    public static void clear() { IMPACTS.clear(); SIDE.clear(); world = null; }

    private static void render(WorldRenderContext ctx) {
        if (ctx.world() != world || ctx.matrixStack() == null || ctx.consumers() == null || IMPACTS.isEmpty()) return;
        var camera = ctx.camera().getPosition();
        float delta = ctx.tickCounter().getGameTimeDeltaPartialTick(false);
        for (var i : IMPACTS) {
            if (i.position.distanceToSqr(camera) > 4096) continue;
            float age = world.getGameTime() - i.born + delta;
            if (age < 0 || age >= LIFE_TICKS) continue;
            int frame = Math.min(FRAMES - 1, (int) (age * FRAMES / LIFE_TICKS));
            var stack = ctx.matrixStack();
            stack.pushPose();
            stack.translate(i.position.x - camera.x, i.position.y - camera.y, i.position.z - camera.z);
            stack.mulPose(ctx.camera().rotation());
            stack.mulPose(Axis.ZP.rotationDegrees(i.roll));
            float half = 0.55f * i.size * (1f + 0.15f * Math.min(1, age / 2f));
            sprite(stack, ctx.consumers().getBuffer(WeaponTrailRenderType.sprite(SHEETS.get(i.family))), half, frame, 1f);
            if (i.lethal) {
                stack.mulPose(Axis.ZP.rotationDegrees(-i.roll));
                sprite(stack, ctx.consumers().getBuffer(WeaponTrailRenderType.sprite(LETHAL)), half * 1.6f, frame, 0.9f);
            }
            stack.popPose();
        }
    }

    private static void sprite(PoseStack stack, VertexConsumer v, float half, int frame, float alpha) {
        var p = stack.last();
        float u0 = (frame % COLS) / (float) COLS, u1 = u0 + 1f / COLS;
        float v0 = (frame / COLS) / (float) ROWS, v1 = v0 + 1f / ROWS;
        int color = ((int) (Math.clamp(alpha, 0, 1) * 255) << 24) | 0xFFFFFF;
        // Two faces so the quad never culls away when the camera roll flips it.
        float[][] corners = {{-half, -half, u0, v1}, {half, -half, u1, v1}, {half, half, u1, v0}, {-half, half, u0, v0}};
        for (int k = 0; k < 4; k++) v.addVertex(p.pose(), corners[k][0], corners[k][1], 0.01f).setColor(color)
            .setUv(corners[k][2], corners[k][3]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(p, 0, 0, 1);
    }

    private WeaponHitVfx() { }
}
