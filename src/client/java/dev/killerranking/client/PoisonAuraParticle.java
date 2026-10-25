package dev.killerranking.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.entity.player.Player;

/** A rising violet veil, anchored behind the body and rendered with vanilla smoke sprites. */
final class PoisonAuraParticle extends TextureSheetParticle {
    private final Player owner;
    private final SpriteSet sprites;
    private final boolean dark;
    private final boolean salmosa;
    private final float side, startHeight, phase, size, rear;
    PoisonAuraParticle(ClientLevel world, Player owner, SpriteSet sprites, boolean dark) {
        super(world, owner.getX(), owner.getY(), owner.getZ());
        this.owner = owner; this.sprites = sprites; this.dark = dark;
        salmosa = PoisonVfx.salmosa(owner.getUUID());
        hasPhysics = false;
        lifetime = 24 + random.nextInt(12);
        side = (random.nextFloat() - .5f) * (salmosa ? 3.2f : 2.8f);
        startHeight = random.nextFloat() * 2.8f - .10f;
        phase = random.nextFloat() * (float) (Math.PI * 2);
        rear = (salmosa ? 2.25f : .9f) + random.nextFloat() * .6f;
        size = dark ? .48f + random.nextFloat() * .22f : .55f + random.nextFloat() * .27f;
        if (dark) setColor(.10f, .015f, .19f);
        else if (salmosa) setColor(.49f + random.nextFloat() * .12f, .20f, .78f + random.nextFloat() * .12f);
        else setColor(.36f + random.nextFloat() * .12f, .025f, .68f + random.nextFloat() * .15f);
        alpha = 0;
        place(); xo = x; yo = y; zo = z;
        setSpriteFromAge(sprites);
    }
    private void place() {
        float progress = age / (float) lifetime;
        double yaw = Math.toRadians(owner.yBodyRot);
        double sweep = side * (1 + progress * .24) + Math.sin(phase + progress * 5) * .13;
        double back = rear + Math.cos(phase + progress * 4) * .10;
        setPos(owner.getX() + Math.cos(yaw) * sweep + Math.sin(yaw) * back,
            owner.getY() + startHeight + progress * 1.65,
            owner.getZ() + Math.sin(yaw) * sweep - Math.cos(yaw) * back);
    }
    @Override public void tick() {
        xo = x; yo = y; zo = z; oRoll = roll;
        if (owner.isRemoved() || !owner.isAlive() || owner.isInvisible() || owner.isSpectator()) { remove(); return; }
        if (!PoisonVfx.active(owner.getUUID())) age = Math.max(age, lifetime - 4);
        if (++age >= lifetime) { remove(); return; }
        float progress = age / (float) lifetime;
        alpha = Math.min(1, age / 3f) * Math.min(1, (lifetime - age) / 7f) * (dark ? .36f : .40f);
        if (salmosa) alpha *= .62f;
        quadSize = size * (1.05f - progress * .45f);
        roll = (float) Math.sin(phase + progress * 3) * .20f;
        place(); setSpriteFromAge(sprites);
        if ((x - xo) * (x - xo) + (z - zo) * (z - zo) > 16) { xo = x; yo = y; zo = z; }
    }
    @Override public void render(VertexConsumer vertices, Camera camera, float delta) {
        if (!KillingIntentVfx.hiddenForCamera(owner)) super.render(vertices, camera, delta);
    }
    @Override protected int getLightColor(float delta) { return 0xF000F0; }
    @Override public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
}
