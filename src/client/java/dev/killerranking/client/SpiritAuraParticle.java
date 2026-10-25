package dev.killerranking.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.killerranking.combat.SpiritBranch;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.entity.player.Player;

/** Branch-colored smoke and small light motes rise around the large character. */
final class SpiritAuraParticle extends TextureSheetParticle {
    private final Player owner;
    private final SpriteSet sprites;
    private final boolean spark;
    private final SpiritBranch branch;
    private final float side, height, phase, size, rear;
    SpiritAuraParticle(ClientLevel world, Player owner, SpriteSet sprites, boolean spark, SpiritBranch branch) {
        super(world, owner.getX(), owner.getY(), owner.getZ());
        this.owner = owner; this.sprites = sprites; this.spark = spark; this.branch = branch;
        hasPhysics = false;
        lifetime = spark ? 18 + random.nextInt(9) : 27 + random.nextInt(10);
        side = (random.nextFloat() - .5f) * 3.45f;
        height = random.nextFloat() * 3.0f - .05f;
        phase = random.nextFloat() * 6.283f;
        rear = .98f + random.nextFloat() * .25f;
        size = spark ? .06f + random.nextFloat() * .045f : .40f + random.nextFloat() * .24f;
        if (branch == SpiritBranch.KING_AND_KILL) {
            float shade = spark ? 1 : random.nextFloat() < .20f ? .32f + random.nextFloat() * .12f : .76f + random.nextFloat() * .24f;
            setColor(shade, shade, shade);
        }
        else if (spark) setColor(.68f, 1, .42f);
        else if (random.nextFloat() < .20f) setColor(.035f, .22f, .055f);
        else setColor(.10f + random.nextFloat() * .15f, .70f + random.nextFloat() * .25f, .16f);
        alpha = 0;
        place(); xo = x; yo = y; zo = z;
        setSpriteFromAge(sprites);
    }
    private void place() {
        float progress = age / (float) lifetime;
        double yaw = Math.toRadians(owner.yBodyRot);
        double sweep = side * (1 + progress * .12) + Math.sin(phase + progress * 4) * .10;
        double back = rear + Math.cos(phase + progress * 3) * .08;
        setPos(owner.getX() + Math.cos(yaw) * sweep + Math.sin(yaw) * back,
            owner.getY() + height + progress * (spark ? 1.75 : 1.55),
            owner.getZ() + Math.sin(yaw) * sweep - Math.cos(yaw) * back);
    }
    @Override public void tick() {
        xo = x; yo = y; zo = z; oRoll = roll;
        if (owner.isRemoved() || !owner.isAlive() || owner.isInvisible() || owner.isSpectator()) { remove(); return; }
        var currentBranch = SpiritVfx.branch(owner.getUUID());
        if (currentBranch != SpiritBranch.NONE && currentBranch != branch) { remove(); return; }
        if (currentBranch == SpiritBranch.NONE) age = Math.max(age, lifetime - 4);
        if (++age >= lifetime) { remove(); return; }
        float progress = age / (float) lifetime;
        alpha = Math.min(1, age / 4f) * Math.min(1, (lifetime - age) / 8f) * (spark ? .85f : .43f);
        quadSize = size * (1.05f - progress * .45f);
        roll = (float) Math.sin(phase + progress * 3) * .16f;
        place(); setSpriteFromAge(sprites);
        if ((x-xo)*(x-xo) + (z-zo)*(z-zo) > 16) { xo = x; yo = y; zo = z; }
    }
    @Override public void render(VertexConsumer vertices, Camera camera, float delta) {
        var currentBranch = SpiritVfx.branch(owner.getUUID());
        if (!KillingIntentVfx.hiddenForCamera(owner) && (currentBranch == branch || currentBranch == SpiritBranch.NONE))
            super.render(vertices, camera, delta);
    }
    @Override protected int getLightColor(float delta) { return 0xF000F0; }
    @Override public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
}
