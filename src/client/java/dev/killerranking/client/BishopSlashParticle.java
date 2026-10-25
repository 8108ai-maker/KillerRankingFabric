package dev.killerranking.client;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/** Six authored pixel frames flash briefly at the dagger's impact. */
public final class BishopSlashParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private BishopSlashParticle(ClientLevel level,double x,double y,double z,SpriteSet sprites){
        super(level,x,y,z);this.sprites=sprites;lifetime=6;quadSize=.4f;hasPhysics=false;setSpriteFromAge(sprites);
    }
    @Override public void tick(){super.tick();setSpriteFromAge(sprites);alpha=1-age/(float)lifetime;}
    @Override protected int getLightColor(float delta){return 0xF000F0;}
    @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    public static final class Provider implements ParticleProvider<SimpleParticleType>{
        private final SpriteSet sprites;public Provider(SpriteSet sprites){this.sprites=sprites;}
        @Override public Particle createParticle(SimpleParticleType type,ClientLevel level,double x,double y,double z,double dx,double dy,double dz){return new BishopSlashParticle(level,x,y,z,sprites);}
    }
}
