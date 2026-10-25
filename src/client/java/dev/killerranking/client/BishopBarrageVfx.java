package dev.killerranking.client;
import dev.killerranking.combat.BishopBarrageRules;
import dev.killerranking.combat.BishopBarrageMotion;
import dev.killerranking.network.BishopBarrageVisualPayload;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Fast traveling cuts fill the forward space only after the four brief helpers vanish. */
public final class BishopBarrageVfx {
    private static final Map<UUID,State> ACTIVE=new HashMap<>();
    private static int renderedSegments;
    private static final class State {
        final Vec3 origin;final float yaw;final long seed=ThreadLocalRandom.current().nextLong();int age,ttl=25;boolean departed;
        State(BishopBarrageVisualPayload p){origin=new Vec3(p.x(),p.y(),p.z());yaw=p.yaw();age=BishopBarrageRules.DURATION-p.remaining();departed=age>=BishopBarrageRules.ECHO_TICKS;if(!departed)flash(this);}
    }
    private static final class Blink extends TextureSheetParticle {
        Blink(ClientLevel level,Vec3 at){
            super(level,at.x,at.y,at.z);lifetime=4;hasPhysics=false;quadSize=.8f;setColor(1,.92f,1);
            var atlas=(TextureAtlas)Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_PARTICLES);
            setSprite(atlas.getSprite(ResourceLocation.withDefaultNamespace("flash")));
        }
        @Override public float getQuadSize(float partial){return quadSize*(float)Math.sin(Math.PI*Math.clamp((age+partial)/lifetime,0,1));}
        @Override public int getLightColor(float partial){return 0xF000F0;}
        @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    }
    public static void register(){WorldRenderEvents.AFTER_ENTITIES.register(BishopBarrageVfx::render);}
    public static void accept(BishopBarrageVisualPayload p){
        if(p.remaining()==0){ACTIVE.remove(p.player());return;}
        ACTIVE.compute(p.player(),(id,old)->{
            if(old==null||old.origin.distanceToSqr(new Vec3(p.x(),p.y(),p.z()))>.001||old.age>BishopBarrageRules.DURATION-p.remaining()+5)return new State(p);
            old.age=BishopBarrageRules.DURATION-p.remaining();old.ttl=25;return old;
        });
    }
    public static void tick(Minecraft c){
        if(c.level==null||c.isPaused())return;
        ACTIVE.values().removeIf(s->{
            ++s.age;
            if(!s.departed&&s.age>=BishopBarrageRules.ECHO_TICKS){s.departed=true;flash(s);}
            return s.age>=BishopBarrageRules.DURATION||--s.ttl<=0;
        });
    }
    private static void flash(State s){
        var c=Minecraft.getInstance();if(c.level==null)return;
        double angle=Math.toRadians(s.yaw);var forward=new Vec3(-Math.sin(angle),0,Math.cos(angle));var side=new Vec3(forward.z,0,-forward.x);
        for(int helper=0;helper<4;helper++){
            var at=s.origin.add(side.scale(helper<2?1.4:-1.4)).add(forward.scale(helper%2==0?.65:-.65)).add(0,1.25,0);
            c.particleEngine.add(new Blink(c.level,at));
        }
    }
    public static boolean active(UUID player){return ACTIVE.containsKey(player);}
    public static void clear(){ACTIVE.clear();renderedSegments=0;}
    public static int segments(){return renderedSegments;}
    private static void render(WorldRenderContext context){
        renderedSegments=0;if(context.matrixStack()==null||context.consumers()==null)return;
        var camera=context.camera().getPosition();float partial=context.tickCounter().getGameTimeDeltaPartialTick(false);int zones=0;
        for(var state:ACTIVE.values()){
            if(state.origin.distanceToSqr(camera)>4096||++zones>8)continue;
            var stack=context.matrixStack();stack.pushPose();stack.translate(state.origin.x-camera.x,state.origin.y-camera.y,state.origin.z-camera.z);stack.mulPose(Axis.YP.rotationDegrees(-state.yaw));
            var vertices=context.consumers().getBuffer(WeaponTrailRenderType.LAYER);float age=state.age+partial;
            blink(stack,vertices,age);
            float time=age-BishopBarrageMotion.START;
            if(time<0){stack.popPose();continue;}
            int latest=(int)(time/BishopBarrageMotion.SPACING);
            for(int pulse=Math.max(0,latest-2);pulse<=latest;pulse++){
                float elapsed=time-pulse*BishopBarrageMotion.SPACING;if(elapsed<0||elapsed>=BishopBarrageMotion.LIFE)continue;
                for(int slash=0;slash<BishopBarrageMotion.STRIKES;slash++){
                    renderedSegments+=renderStroke(stack,vertices,BishopBarrageMotion.stroke(state.seed,pulse,slash),elapsed);
                }
            }
            stack.popPose();
        }
    }
    /** Shared by victim cuts: the actual Bishop texture frames, moving head/tail, thickness and colors. */
    static int renderStroke(PoseStack stack,VertexConsumer vertices,BishopBarrageMotion.Stroke stroke,float elapsed){
        if(elapsed<0||elapsed>=BishopBarrageMotion.LIFE)return 0;
        float tail=BishopBarrageMotion.tail(elapsed),head=BishopBarrageMotion.head(elapsed);if(head-tail<.001f)return 0;
        int frame=Math.min(7,(int)(elapsed*8/BishopBarrageMotion.LIFE));float alpha=.95f*Math.min(1,(BishopBarrageMotion.LIFE-elapsed)/2);
        for(int segment=0;segment<12;segment++){
            float u0=segment/12f,u1=(segment+1)/12f;
            var a=stroke.point(tail+(head-tail)*u0);var b=stroke.point(tail+(head-tail)*u1);
            float vx=b.x()-a.x(),vy=b.y()-a.y(),length=(float)Math.sqrt(vx*vx+vy*vy);
            float nx=-vy/length,ny=vx/length;
            for(int edge=0;edge<2;edge++){
                float width=edge==0?.115f:.085f;
                float w0=width*(float)Math.sin(Math.PI*u0),w1=width*(float)Math.sin(Math.PI*u1);
                float depth=edge==0?0:-.003f;
                quad(stack,vertices,a.x()-nx*w0,a.y()-ny*w0,a.z()+depth,a.x()+nx*w0,a.y()+ny*w0,a.z()+depth,b.x()+nx*w1,b.y()+ny*w1,b.z()+depth,b.x()-nx*w1,b.y()-ny*w1,b.z()+depth,u0,u1,frame,alpha,edge==0?.045f:1,edge==0?.012f:.94f,edge==0?.03f:.97f);
            }
        }
        return 12;
    }
    private static void blink(PoseStack stack,VertexConsumer vertices,float age){
        float phase=age<BishopBarrageRules.ECHO_TICKS?age:age-BishopBarrageRules.ECHO_TICKS;
        float life=age<BishopBarrageRules.ECHO_TICKS?BishopBarrageRules.ECHO_TICKS:3;
        if(phase>=life)return;
        float alpha=.95f*(1-phase/life),width=.22f*(1-phase/life);
        for(int helper=0;helper<4;helper++){
            float x=helper<2?1.4f:-1.4f,z=(helper%2==0?.65f:-.65f)+.4f;
            // A brief vertical glint and a forward smear frame the appearance/disappearance, not a long smoke cloud.
            quad(stack,vertices,x-width,.05f,z,x+width,.05f,z,x+width,2.65f,z,x-width,2.65f,z,0,1,2,alpha,1,.9f,1);
            quad(stack,vertices,x-.8f,2.08f-width,z,x-.8f,2.08f+width,z,x+.8f,2.08f+width,z,x+.8f,2.08f-width,z,0,1,2,alpha,1,.9f,1);
            if(age>=BishopBarrageRules.ECHO_TICKS){
                float end=z+1.4f+phase*.7f;
                quad(stack,vertices,x,1.05f,z,x,1.28f,z,x,1.28f,end,x,1.05f,end,0,1,2,alpha,1,.9f,1);
            }
        }
    }
    private static void quad(PoseStack stack,VertexConsumer vertices,float ax,float ay,float az,float bx,float by,float bz,float cx,float cy,float cz,float dx,float dy,float dz,float u0,float u1,int frame,float alpha,float red,float green,float blue){
        float low=(frame+.015625f)/8,high=(frame+.984375f)/8;
        vertex(stack,vertices,ax,ay,az,u0,high,alpha,red,green,blue);vertex(stack,vertices,bx,by,bz,u0,low,alpha,red,green,blue);
        vertex(stack,vertices,cx,cy,cz,u1,low,alpha,red,green,blue);vertex(stack,vertices,dx,dy,dz,u1,high,alpha,red,green,blue);
    }
    private static void vertex(PoseStack s,VertexConsumer v,float x,float y,float z,float u,float texY,float alpha,float red,float green,float blue){
        v.addVertex(s.last().pose(),x,y,z).setColor(red,green,blue,alpha).setUv(u,texY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(s.last(),0,0,1);
    }
    private BishopBarrageVfx() { }
}
