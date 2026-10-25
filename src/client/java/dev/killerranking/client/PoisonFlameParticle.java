package dev.killerranking.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.entity.player.Player;

/** The same blocky smoke sprites as Killing Aura, clustered into red/orange flame-shaped eyes. */
final class PoisonFlameParticle extends TextureSheetParticle {
    private static final float[][] OUTER = {
        {0,0}, {.23f,.03f}, {.56f,.04f}, {.88f,.04f}, {.95f,.10f}, {.90f,.17f},
        {1,.27f}, {.98f,.39f}, {.90f,.48f}, {.98f,.56f}, {1.02f,.70f}, {.95f,.77f},
        {.94f,1}, {.73f,.91f}, {.62f,.78f}, {.52f,.58f}, {.39f,.39f}, {.24f,.25f}, {.12f,.16f}
    };
    private static final float[][] INNER = {
        {.025f,.065f}, {.24f,.12f}, {.46f,.13f}, {.65f,.18f}, {.76f,.25f}, {.77f,.44f},
        {.73f,.49f}, {.65f,.48f}, {.56f,.45f}, {.46f,.38f}, {.34f,.29f}, {.20f,.23f}
    };
    private final Player owner;
    private final SpriteSet sprites;
    private final int side;
    private final boolean orange;
    private final float px, py, size, phase;
    PoisonFlameParticle(ClientLevel world, Player owner, SpriteSet sprites, int side, boolean orange) {
        super(world, owner.getX(), owner.getY(), owner.getZ());
        this.owner = owner; this.sprites = sprites; this.side = side; this.orange = orange;
        hasPhysics = false;
        lifetime = 20 + random.nextInt(7);
        phase = random.nextFloat() * 6.283f;
        size = (orange ? .110f + random.nextFloat() * .035f : .130f + random.nextFloat() * .040f)
            * (PoisonVfx.salmosa(owner.getUUID()) ? SalmosaSnakeModel.HEAD_SCALE : 1);
        float sx, sy;
        do {
            sx = random.nextFloat() * 1.02f;
            sy = random.nextFloat();
        } while (!inside(orange ? INNER : OUTER, sx, sy) || (!orange && inside(INNER, sx, sy)));
        px = sx; py = sy;
        if (orange) setColor(1, .32f + random.nextFloat() * .12f, .015f);
        else setColor(.93f + random.nextFloat() * .07f, .012f, .025f);
        alpha = 0; quadSize = size;
        place(); xo = x; yo = y; zo = z;
        setSpriteFromAge(sprites);
    }
    private static boolean inside(float[][] polygon, float x, float y) {
        boolean hit = false;
        for (int i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
            var a = polygon[i]; var b = polygon[j];
            if ((a[1] > y) != (b[1] > y) && x < (b[0]-a[0]) * (y-a[1]) / (b[1]-a[1]) + a[0]) hit = !hit;
        }
        return hit;
    }
    private void place() {
        float progress = age / (float) lifetime;
        float flickerX = (float) Math.sin(phase + progress * 4) * .018f;
        float flickerY = (float) Math.sin(phase + progress * 3) * .022f;
        boolean salmosa = PoisonVfx.salmosa(owner.getUUID());
        float localX = side * ((salmosa ? .26f : .12f) + (px + flickerX) * (salmosa ? .23f : .32f));
        float localY = (salmosa ? .10f : .12f) + (py + flickerY) * (salmosa ? .12f : .23f);
        float localZ = (salmosa ? .65f : .57f) - px * (salmosa ? .39f : .23f) + (orange ? .013f : 0);
        var origin = PoisonSnakeModel.headPoint(owner, 0, localX, localY, localZ);
        // New particles start in the eye silhouette; older ones rise and disperse like the aura.
        double yaw = Math.toRadians(owner.yBodyRot);
        double drift = Math.sin(phase + progress * 4) * .04 * progress;
        setPos(owner.getX() + origin.x + Math.cos(yaw) * drift,
            owner.getY() + origin.y + progress * (salmosa ? .28 * SalmosaSnakeModel.HEAD_SCALE : .65),
            owner.getZ() + origin.z + Math.sin(yaw) * drift);
    }
    @Override public void tick() {
        xo = x; yo = y; zo = z; oRoll = roll;
        if (owner.isRemoved() || !owner.isAlive() || owner.isInvisible() || owner.isSpectator()) { remove(); return; }
        if (!PoisonVfx.active(owner.getUUID())) age = Math.max(age, lifetime - 4);
        if (++age >= lifetime) { remove(); return; }
        float progress = age / (float) lifetime;
        alpha = Math.min(1, age / 3f) * Math.min(1, (lifetime - age) / 7f) * .82f;
        quadSize = size * (1.05f - progress * .45f);
        roll = (float) Math.sin(phase + progress * 3) * .12f;
        place(); setSpriteFromAge(sprites);
        if ((x-xo)*(x-xo) + (z-zo)*(z-zo) > 16) { xo = x; yo = y; zo = z; }
    }
    @Override public void render(VertexConsumer vertices, Camera camera, float delta) {
        if (!KillingIntentVfx.hiddenForCamera(owner)) super.render(vertices, camera, delta);
    }
    @Override protected int getLightColor(float delta) { return 0xF000F0; }
    @Override public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
}
