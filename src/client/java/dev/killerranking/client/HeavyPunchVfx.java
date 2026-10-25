package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.HeavyPunchRules;
import dev.killerranking.network.HeavyPunchVisualPayload;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.minecraftApi.*;
import java.util.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import com.mojang.blaze3d.vertex.*;

/** A single falling drop and a forward pixel trail for the self-braced rotational punch. */
public final class HeavyPunchVfx {
    private static final ResourceLocation LAYER=KillerRankingMod.id("heavy_punch_layer"),ANIMATION=KillerRankingMod.id("tteokbal_heavy_punch");
    private static final Map<UUID,State> ACTIVE=new HashMap<>();
    private static final class State {int remaining,age,target;float yaw,height=1.55f,reach=1.57f;boolean animated;
        State(HeavyPunchVisualPayload p){remaining=p.remaining();age=p.age();target=p.target();yaw=p.yaw();}}
    private static long drops,impacts;
    public static void register(){
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((p,stack)->{
            var layer=new ModifierLayer<IAnimation>();
            layer.addModifierLast(new dev.kosmx.playerAnim.minecraftApi.layers.LeftHandedHelperModifier(p));
            stack.addAnimLayer(2600,layer);PlayerAnimationAccess.getPlayerAssociatedData(p).set(LAYER,layer);
        });
        WorldRenderEvents.AFTER_ENTITIES.register(HeavyPunchVfx::render);
    }
    public static void accept(HeavyPunchVisualPayload p){
        if(p.remaining()<=0){remove(p.player());return;}
        ACTIVE.compute(p.player(),(id,s)->{if(s==null)return new State(p);s.remaining=p.remaining();s.age=p.age();s.target=p.target();s.yaw=p.yaw();return s;});
    }
    public static boolean active(UUID id){return ACTIVE.containsKey(id);}
    public static boolean captured(LivingEntity entity){return false;}
    public static net.minecraft.world.entity.player.Player captor(LivingEntity target){return null;}
    public static LivingEntity target(UUID id){var s=ACTIVE.get(id);var c=Minecraft.getInstance();
        return s!=null&&s.target>=0&&c.level!=null&&c.level.getEntity(s.target) instanceof LivingEntity e?e:null;}
    public static float age(UUID id,float delta){var s=ACTIVE.get(id);return s==null?-1:s.age+delta;}
    public static float facing(LivingEntity entity,float fallback){return yaw(entity.getUUID(),fallback);}
    public static boolean locked(LivingEntity e){return e.level().isClientSide&&active(e.getUUID());}
    public static float yaw(UUID id,float fallback){var s=ACTIVE.get(id);return s==null?fallback:s.yaw;}
    public static net.minecraft.world.phys.Vec3 contactPoint(UUID id,net.minecraft.world.phys.Vec3 origin,net.minecraft.world.phys.Vec3 forward){
        var s=ACTIVE.get(id);return s==null?origin.add(forward.scale(1.57)).add(0,1.55,0):origin.add(forward.scale(s.reach)).add(0,s.height,0);
    }
    /** Offset the native punching block for the target's actual head height. */
    public static float headOffset(UUID id){var s=ACTIVE.get(id);if(s==null)return 0;
        float weight=Math.clamp((s.age-38)/3f,0,1)*Math.clamp((49-s.age)/5f,0,1);
        return -(s.height-1.65f)*16*weight;}
    public static long renderedDrops(){return drops;}
    public static long renderedImpacts(){return impacts;}
    public static void tick(Minecraft client){
        if(client.isPaused()||client.level==null)return;
        for(var id:new ArrayList<>(ACTIVE.keySet())){
            var s=ACTIVE.get(id);var e=client.level.getPlayerByUUID(id);
            if(--s.remaining<=0||e==null||!e.isAlive()||e.isSpectator()){remove(id);continue;}
            if(!s.animated)s.animated=animate(e,s.age);
            if(s.age<HeavyPunchRules.IMPACT&&s.target>=0&&client.level.getEntity(s.target) instanceof LivingEntity target){
                s.height=(float)Math.clamp(target.getEyeY()-.08-e.getY(),.55,2.65);
                var forward=net.minecraft.world.phys.Vec3.directionFromRotation(0,s.yaw);
                s.reach=(float)Math.clamp(target.position().subtract(e.position()).dot(forward)-.18,.65,2.22);
            }
            // Lock the body to the opponent, while allowing mouse look to remain free.
            e.yBodyRot=e.yBodyRotO=s.yaw;s.age++;
        }
    }
    @SuppressWarnings("unchecked")
    private static boolean animate(net.minecraft.world.entity.player.Player e,int age){
        if(!(e instanceof AbstractClientPlayer p))return false;
        var layer=PlayerAnimationAccess.getPlayerAssociatedData(p).get(LAYER);if(!(layer instanceof ModifierLayer<?>))return false;
        IAnimation animation=null;
        if(age>=0){var data=PlayerAnimationRegistry.getAnimation(ANIMATION);if(data==null)return false;
            animation=(IAnimation)data.playAnimation();if(animation instanceof KeyframeAnimationPlayer key){
                for(int i=0;i<age;i++)key.tick();
                key.setFirstPersonMode(dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode.THIRD_PERSON_MODEL);
                key.setFirstPersonConfiguration(new dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration(true,true,false,false));
            }
        }
        ((ModifierLayer<IAnimation>)layer).setAnimation(animation);return true;
    }
    private static void remove(UUID id){ACTIVE.remove(id);var c=Minecraft.getInstance();if(c.level!=null){var p=c.level.getPlayerByUUID(id);if(p!=null)animate(p,-1);}}
    public static void clear(){for(var id:new ArrayList<>(ACTIVE.keySet()))remove(id);}
    private static void render(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context){
        var c=Minecraft.getInstance();var stack=context.matrixStack();var buffers=context.consumers();
        if(c.level==null||stack==null||buffers==null)return;
        float delta=context.tickCounter().getGameTimeDeltaPartialTick(false);var camera=context.camera().getPosition();
        for(var entry:ACTIVE.entrySet()){
            var p=c.level.getPlayerByUUID(entry.getKey());if(p==null||p.isInvisible()||p.distanceToSqr(camera)>4096)continue;
            var s=entry.getValue();float time=s.age+delta;
            var dir=net.minecraft.world.phys.Vec3.directionFromRotation(0,s.yaw);
            var origin=p.getPosition(delta).add(dir.scale(.55));
            if(time>=HeavyPunchRules.DROP&&time<HeavyPunchRules.SPLASH){
                float progress=(time-HeavyPunchRules.DROP)/(HeavyPunchRules.SPLASH-HeavyPunchRules.DROP);
                stack.pushPose();stack.translate(origin.x-camera.x,origin.y+.15+1.4*(1-progress*progress)-camera.y,origin.z-camera.z);
                stack.mulPose(context.camera().rotation());
                quad(stack,buffers.getBuffer(RenderType.entityTranslucent(KillerRankingMod.id("textures/effect/heavy_drop.png"))),-.055f,-.10f,.055f,.10f,0,0,1,1,1);
                stack.popPose();drops++;
            }
            if(time>=HeavyPunchRules.SPLASH&&time<HeavyPunchRules.SPLASH+3){
                float r=.08f+(time-HeavyPunchRules.SPLASH)*.10f;
                stack.pushPose();stack.translate(origin.x-camera.x,origin.y+.03-camera.y,origin.z-camera.z);
                stack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
                quad(stack,buffers.getBuffer(RenderType.entityTranslucent(KillerRankingMod.id("textures/effect/heavy_ripple.png"))),-r,-r,r,r,0,0,1,1,1);
                stack.popPose();
            }
            if(time>=HeavyPunchRules.IMPACT-2&&time<HeavyPunchRules.IMPACT+6){
                float elapsed=time-(HeavyPunchRules.IMPACT-2);int frame=Math.min(3,(int)elapsed/2);
                stack.pushPose();stack.translate(p.getX()-camera.x,p.getY()+s.height-camera.y,p.getZ()-camera.z);
                stack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-s.yaw));
                var v=buffers.getBuffer(RenderType.entityTranslucent(KillerRankingMod.id("textures/effect/heavy_impact.png")));
                float alpha=Math.min(1,(8-elapsed)/3);float lo=frame/4f,hi=(frame+1)/4f;
                // The trail follows the forward head-height strike, without an upward flare.
                stack.translate(0,0,.45);stack.pushPose();stack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
                stack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-5));
                quad(stack,v,-1.40f,-.19f,-.06f,.19f,0,lo,1,hi,alpha);stack.popPose();
                stack.translate(0,0,s.reach-.45);quad(stack,v,-.38f,-.38f,.38f,.38f,0,lo,1,hi,alpha);
                stack.popPose();impacts++;
            }
        }
    }
    private static void quad(PoseStack s,VertexConsumer v,float x0,float y0,float x1,float y1,float u0,float v0,float u1,float v1,float a){
        vertex(s,v,x0,y0,u0,v1,a);vertex(s,v,x1,y0,u1,v1,a);vertex(s,v,x1,y1,u1,v0,a);vertex(s,v,x0,y1,u0,v0,a);
    }
    private static void vertex(PoseStack s,VertexConsumer v,float x,float y,float u,float tex,float alpha){v.addVertex(s.last().pose(),x,y,0).setColor(1,1,1,alpha).setUv(u,tex).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(s.last(),0,0,1);}
    private HeavyPunchVfx(){}
}
