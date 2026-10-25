package dev.killerranking.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;

/**
 * Small full-bright pixel shards in the weapon's color. They fly off the blade during a swing
 * and burst from the contact point on a hit, then drift, spin and fade like petals.
 */
public final class SlashShardParticle extends TextureSheetParticle {
    private static SpriteSet sprites;
    private final float spin;
    private final float baseSize;

    private SlashShardParticle(ClientLevel level, double x, double y, double z, Vec3 velocity, int rgb, float size, int life, SpriteSet set) {
        super(level, x, y, z, velocity.x, velocity.y, velocity.z);
        xd = velocity.x; yd = velocity.y; zd = velocity.z;
        hasPhysics = false;
        gravity = 0.045f + random.nextFloat() * 0.03f;
        friction = 0.90f;
        lifetime = life;
        baseSize = size;
        quadSize = size;
        spin = (random.nextFloat() - 0.5f) * 0.9f;
        roll = random.nextFloat() * 6.283f; oRoll = roll;
        float brighten = 0.85f + random.nextFloat() * 0.15f;
        setColor(((rgb >> 16) & 255) / 255f * brighten, ((rgb >> 8) & 255) / 255f * brighten, (rgb & 255) / 255f * brighten);
        setSprite(set.get(random));
        alpha = 1;
    }

    @Override public void tick() {
        xo = x; yo = y; zo = z; oRoll = roll;
        if (age++ >= lifetime) { remove(); return; }
        yd -= gravity * 0.04;
        move(xd, yd, zd);
        xd *= friction; yd *= 0.96; zd *= friction;
        roll += spin * Math.max(0.25f, 1 - age / (float) lifetime);
        float tail = (lifetime - age) / (float) Math.max(1, lifetime * 0.4f);
        alpha = Math.min(1, tail);
        quadSize = baseSize * (0.7f + 0.3f * Math.min(1, tail));
    }

    @Override protected int getLightColor(float delta) { return 0xF000F0; }
    @Override public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }

    /** Spawn `count` shards at `origin`, pushed along `direction` with spread; honours the particle setting. */
    public static void burst(Vec3 origin, Vec3 direction, int count, int rgb, float speed, float spread, float size, int life) {
        var c = Minecraft.getInstance();
        if (c.level == null || sprites == null) return;
        var setting = c.options.particles().get();
        if (setting == ParticleStatus.MINIMAL) count = Math.max(1, count / 4);
        else if (setting == ParticleStatus.DECREASED) count = Math.max(1, count / 2);
        var rng = c.level.random;
        for (int i = 0; i < count; i++) {
            Vec3 v = direction.scale(speed * (0.5 + rng.nextFloat()))
                .add((rng.nextFloat() - 0.5) * spread, (rng.nextFloat() - 0.3) * spread, (rng.nextFloat() - 0.5) * spread);
            float s = size * (0.6f + rng.nextFloat() * 0.8f);
            int l = life + rng.nextInt(Math.max(1, life / 2));
            c.particleEngine.add(new SlashShardParticle(c.level, origin.x + (rng.nextFloat() - 0.5) * 0.1, origin.y + (rng.nextFloat() - 0.5) * 0.1,
                origin.z + (rng.nextFloat() - 0.5) * 0.1, v, rgb, s, l, sprites));
        }
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet set) { sprites = set; }
        @Override public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new SlashShardParticle(level, x, y, z, new Vec3(dx, dy, dz), 0xFFFFFF, 0.08f, 24, sprites);
        }
    }
}
