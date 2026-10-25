package dev.killerranking.client;

import com.mojang.blaze3d.vertex.*;
import dev.killerranking.network.ParryImpactPayload;
import java.util.*;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

/** A white pixel impact, short crossed glint and separated sparks at the contact point. */
public final class ParryImpactVfx {
    private record Impact(UUID owner,Vec3 position,long born) { }
    private static final List<Impact> IMPACTS=new ArrayList<>();
    private static ClientLevel world;
    private static long received,rendered;
    public static void register(){
        WorldRenderEvents.AFTER_ENTITIES.register(ParryImpactVfx::render);
        HudRenderCallback.EVENT.register((g,t)->{
            var c=Minecraft.getInstance();if(c.player==null||c.level!=world)return;
            float delta=t.getGameTimeDeltaPartialTick(false),opacity=0;
            for(var i:IMPACTS)if(i.owner.equals(c.player.getUUID())){
                float age=c.level.getGameTime()-i.born+delta;
                if(age<2)opacity=Math.max(opacity,.13f*(1-age/2));
            }
            if(opacity>.004)g.fill(0,0,g.guiWidth(),g.guiHeight(),((int)(opacity*255)<<24)|0xEAF9FF);
        });
    }
    public static void accept(ParryImpactPayload p){
        var c=Minecraft.getInstance();if(c.level==null)return;
        if(world!=c.level){clear();world=c.level;}
        if(!Double.isFinite(p.x())||!Double.isFinite(p.y())||!Double.isFinite(p.z()))return;
        if(IMPACTS.size()>=64)IMPACTS.removeFirst();
        CombatCameraFeedback.parry(p.player());
        IMPACTS.add(new Impact(p.player(),new Vec3(p.x(),p.y(),p.z()),world.getGameTime()));received++;
    }
    public static void tick(Minecraft c){
        if(c.level!=world){clear();world=c.level;return;}
        if(world==null||c.isPaused())return;
        IMPACTS.removeIf(i->world.getGameTime()-i.born>=8);
    }
    public static int activeCount(){return IMPACTS.size();}
    public static long receivedCount(){return received;}
    public static long renderedCount(){return rendered;}
    public static void clear(){IMPACTS.clear();world=null;received=rendered=0;}
    private static void render(WorldRenderContext ctx){
        if(ctx.world()!=world||ctx.matrixStack()==null||ctx.consumers()==null)return;
        var camera=ctx.camera().getPosition();var vertices=ctx.consumers().getBuffer(WeaponTrailRenderType.BREATH);
        float delta=ctx.tickCounter().getGameTimeDeltaPartialTick(false);
        for(var i:IMPACTS){
            if(i.position.distanceToSqr(camera)>4096)continue;
            float age=world.getGameTime()-i.born+delta;if(age<0||age>=8)continue;
            var stack=ctx.matrixStack();stack.pushPose();
            stack.translate(i.position.x-camera.x,i.position.y-camera.y,i.position.z-camera.z);
            stack.mulPose(ctx.camera().rotation());
            float core=Math.max(0,1-age/3),length=.16f+.055f*age;
            if(core>0){
                // Flat pixel caps: no hooked crescents, no disconnected blade trail.
                rect(stack,vertices,0,0,length,.025f,0,core,0xF7FDFF);
                rect(stack,vertices,0,0,.025f,length,0,core,0xF7FDFF);
                rect(stack,vertices,0,0,.065f,.065f,.002f,core,0xFFFFFF);
            }
            for(int spark=0;spark<10;spark++){
                double a=spark*Math.PI*.2+(i.born%7)*.17;
                float speed=.035f+(spark%3)*.012f,r=.08f+age*speed;
                float fade=(1-age/8)*(spark%2==0?1:.7f);
                stack.pushPose();stack.mulPose(com.mojang.math.Axis.ZP.rotation((float)a));
                float streak=.018f+(spark%3)*.008f;
                int color=spark%3==0?0x9CE9FF:0xF2FAFF;
                rect(stack,vertices,r,0,streak,.008f,-.002f,fade,color);
                rect(stack,vertices,r,0,.008f,streak,-.002f,fade,color);
                stack.popPose();
            }
            stack.popPose();rendered++;
        }
    }
    private static void rect(PoseStack stack,VertexConsumer v,float x,float y,float halfX,float halfY,float z,float opacity,int rgb){
        int color=((int)(Math.clamp(opacity,0,1)*255)<<24)|rgb;var p=stack.last();
        float[][] corners={{x-halfX,y-halfY},{x+halfX,y-halfY},{x+halfX,y+halfY},{x-halfX,y+halfY}};
        for(int k=0;k<4;k++)v.addVertex(p.pose(),corners[k][0],corners[k][1],z).setColor(color)
            .setUv(k==0||k==3?0:1,k<2?0:1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(p,0,0,1);
    }
    private ParryImpactVfx(){ }
}
