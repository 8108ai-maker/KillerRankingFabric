package dev.killerranking.client;
import dev.killerranking.network.NeungsaCutEffectPayload;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Pixel frames stay on each struck victim, including movement during their brief lifetime. */
public final class NeungsaCutParticle extends TextureSheetParticle {
    private static SpriteSet loaded;
    private static final Set<NeungsaCutParticle> LIVE=new HashSet<>();
    private final SpriteSet sprites;
    private final LivingEntity target;
    private NeungsaCutParticle(ClientLevel level,double x,double y,double z,SpriteSet sprites,LivingEntity target){
        super(level,x,y,z);this.sprites=sprites;this.target=target;lifetime=8;hasPhysics=false;
        quadSize=target==null?.6f:Math.clamp(target.getBbHeight()*.42f,.45f,1.6f);
        roll=(random.nextBoolean()?1:-1)*.16f;oRoll=roll;setSpriteFromAge(sprites);
        if(target!=null){follow();xo=this.x;yo=this.y;zo=this.z;LIVE.add(this);}
    }
    private void follow(){
        var center=target.position().add(0,target.getBbHeight()*.57,0);
        var facing=Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().subtract(center).normalize();
        var p=center.add(facing.scale(target.getBbWidth()*.55+.08));setPos(p.x,p.y,p.z);
    }
    @Override public void tick(){
        super.tick();if(target!=null){if(target.isRemoved()){remove();return;}follow();}
        setSpriteFromAge(sprites);alpha=Math.min(1,(lifetime-age)/3f);
    }
    @Override public void remove(){super.remove();LIVE.remove(this);}
    @Override protected int getLightColor(float delta){return 0xF000F0;}
    @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    public static void emit(NeungsaCutEffectPayload p){
        NeungsaHitCutVfx.emit(p);
    }
    public static boolean active(UUID target){return NeungsaHitCutVfx.active(target);}
    public static void clear(){for(var p:new ArrayList<>(LIVE))p.remove();LIVE.clear();NeungsaHitCutVfx.clear();}
    public static final class Provider implements ParticleProvider<SimpleParticleType>{
        private final SpriteSet sprites;public Provider(SpriteSet sprites){this.sprites=sprites;loaded=sprites;}
        @Override public Particle createParticle(SimpleParticleType type,ClientLevel level,double x,double y,double z,double dx,double dy,double dz){return new NeungsaCutParticle(level,x,y,z,sprites,null);}
    }
}
