package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.KnifePursuitRules;
import dev.killerranking.combat.ShoulderStabRules;
import dev.killerranking.network.KnifePursuitVisualPayload;
import dev.killerranking.visual.SalmosaKnifeGeometry;
import dev.kosmx.playerAnim.api.firstPerson.*;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.minecraftApi.*;
import dev.kosmx.playerAnim.minecraftApi.layers.LeftHandedHelperModifier;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.*;

/** The actual dagger model travels, stays embedded on a moving target, then returns to the animated hand. */
public final class KnifePursuitVfx {
    private static final ResourceLocation LAYER=KillerRankingMod.id("knife_pursuit_layer"),ANIMATION=KillerRankingMod.id("salmosa_knife_pursuit");
    private static final Map<UUID,State> ACTIVE=new HashMap<>();
    private static long flyingFrames,embeddedFrames,extractFrames,deflectedFrames;
    private static final class State {
        KnifePursuitVisualPayload value;int age,ttl;boolean animated,arrived;
        State(KnifePursuitVisualPayload p){update(p);}
        void update(KnifePursuitVisualPayload p){value=p;age=p.age();ttl=25;}
    }
    public static void register(){
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((p,stack)->{
            var layer=new ModifierLayer<IAnimation>();layer.addModifierLast(new LeftHandedHelperModifier(p));
            stack.addAnimLayer(3450,layer);PlayerAnimationAccess.getPlayerAssociatedData(p).set(LAYER,layer);
        });
        WorldRenderEvents.AFTER_ENTITIES.register(KnifePursuitVfx::render);
    }
    public static void accept(KnifePursuitVisualPayload p){
        if(p.phase()==0){remove(p.player());return;}
        var previous=ACTIVE.get(p.player());if(previous!=null&&previous.value.phase()!=p.phase())remove(p.player());
        ACTIVE.compute(p.player(),(id,s)->{if(s==null||s.value.phase()!=p.phase()||p.age()<s.age-3)return new State(p);s.update(p);return s;});
    }
    public static boolean active(UUID id){return ACTIVE.containsKey(id);}
    public static boolean casting(UUID id){var s=ACTIVE.get(id);return s!=null&&s.value.phase()==KnifePursuitRules.CAST;}
    public static boolean hideItem(LivingEntity e,ItemStack item,ItemDisplayContext display,boolean left){
        var s=ACTIVE.get(e.getUUID());return s!=null&&(s.value.phase()==KnifePursuitRules.DEFLECTED||s.value.phase()==KnifePursuitRules.CAST&&KnifePursuitRules.away(s.age))
            &&display!=ItemDisplayContext.NONE&&item.is(KillerRankingMod.SALMOSA_MID_DAGGER)&&left==(e.getMainArm()==HumanoidArm.LEFT);
    }
    public static long flyingFrames(){return flyingFrames;}public static long embeddedFrames(){return embeddedFrames;}public static long extractFrames(){return extractFrames;}
    public static long deflectedFrames(){return deflectedFrames;}
    public static void clear(){for(var id:new ArrayList<>(ACTIVE.keySet()))remove(id);}
    private static void remove(UUID id){ACTIVE.remove(id);var c=Minecraft.getInstance();if(c.level==null)return;
        if(c.level.getPlayerByUUID(id) instanceof AbstractClientPlayer p){var l=PlayerAnimationAccess.getPlayerAssociatedData(p).get(LAYER);if(l instanceof ModifierLayer<?> layer)layer.setAnimation(null);}}
    @SuppressWarnings("unchecked") private static boolean animate(AbstractClientPlayer p,State s){
        var l=PlayerAnimationAccess.getPlayerAssociatedData(p).get(LAYER);var data=PlayerAnimationRegistry.getAnimation(ANIMATION);
        if(!(l instanceof ModifierLayer<?> layer)||data==null)return false;
        var animation=(IAnimation)data.playAnimation();if(animation instanceof KeyframeAnimationPlayer k){
            for(int i=0;i<Math.min(s.age,KnifePursuitRules.DURATION);i++)k.tick();
            k.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);k.setFirstPersonConfiguration(new FirstPersonConfiguration(true,true,true,true));
        }
        ((ModifierLayer<IAnimation>)layer).setAnimation(animation);return true;
    }
    public static void tick(Minecraft c){if(c.level==null||c.isPaused())return;
        for(var id:new ArrayList<>(ACTIVE.keySet())){var s=ACTIVE.get(id);var p=c.level.getPlayerByUUID(id);
            if(--s.ttl<=0||p==null||!p.isAlive()||p.isSpectator()||s.value.phase()==KnifePursuitRules.CAST&&s.age>=KnifePursuitRules.DURATION||s.value.phase()==KnifePursuitRules.DEFLECTED&&s.age>=KnifePursuitRules.AIR_DURATION){remove(id);continue;}
            if(s.value.phase()==KnifePursuitRules.CAST){
                float facing=s.value.yaw();
                if(s.value.age()>=KnifePursuitRules.BLINK){
                    var t=target(s);if(t!=null){var d=t.position().subtract(s.value.destination());facing=(float)Math.toDegrees(Math.atan2(-d.x,d.z));}
                    if(!s.arrived){
                        // Observers also snap on the arrival packet, rather than lerping through the path.
                        var at=s.value.destination();p.lerpTo(at.x,at.y,at.z,facing,p.getXRot(),1);
                        p.setPos(at);p.xo=p.xOld=at.x;p.yo=p.yOld=at.y;p.zo=p.zOld=at.z;s.arrived=true;
                    }
                }
                p.yBodyRot=p.yBodyRotO=facing;if(!s.animated&&p instanceof AbstractClientPlayer a)s.animated=animate(a,s);
            }
            s.age++;
        }
    }
    private static LivingEntity target(State s){var c=Minecraft.getInstance();for(var e:c.level.entitiesForRendering())if(s.value.target().equals(e.getUUID())&&e instanceof LivingEntity living)return living;return null;}
    private static void render(WorldRenderContext context){
        var c=Minecraft.getInstance();var pose=context.matrixStack();var buffers=context.consumers();
        if(c.level==null||pose==null||buffers==null)return;
        float delta=context.tickCounter().getGameTimeDeltaPartialTick(false);var camera=context.camera().getPosition();
        for(var entry:ACTIVE.entrySet()){
            var s=entry.getValue();
            var player=c.level.getPlayerByUUID(entry.getKey());if(player==null||player.distanceToSqr(camera)>4096)continue;
            if(s.value.phase()==KnifePursuitRules.DEFLECTED){
                float time=s.age+delta;var at=KnifePursuitRules.deflected(s.value.origin(),time);
                airKnife(context,player,at,s.value.yaw());deflectedFrames++;
                if(time<5)ribbon(context,at,new Vec3(0,1,0),.75f,.1f,time%4,.75f);
                continue;
            }
            if(s.value.phase()!=KnifePursuitRules.CAST)continue;
            float age=s.age+delta;var target=target(s);var forward=Vec3.directionFromRotation(0,s.value.yaw());
            var wound=target==null?s.value.wound():target.getPosition(delta).add(0,target.getBbHeight()*.62,0).subtract(forward.scale(Math.max(.08,target.getBbWidth()*.5-.07)));
            var direction=wound.subtract(s.value.origin()).normalize();
            if(age>=KnifePursuitRules.THROW&&age<KnifePursuitRules.RETURN){
                boolean pulling=age>=KnifePursuitRules.EXTRACT;
                float pull=(float)Math.clamp((age-KnifePursuitRules.EXTRACT)/(KnifePursuitRules.RETURN-KnifePursuitRules.EXTRACT),0,1);
                var at=age<KnifePursuitRules.IMPACT?KnifePursuitRules.flight(s.value.origin(),wound,age):wound.subtract(forward.scale(.65*pull));
                if(age>=KnifePursuitRules.IMPACT&&!s.value.embedded()&&!pulling)continue;
                var bladeDirection=age>=KnifePursuitRules.IMPACT?forward:direction;
                pose.pushPose();pose.translate(at.x-camera.x,at.y-camera.y,at.z-camera.z);
                pose.mulPose(new Quaternionf().rotationTo(new Vector3f(0,1,0),new Vector3f((float)bladeDirection.x,(float)bladeDirection.y,(float)bladeDirection.z)));
                // Roll from upright flight to a horizontal blade before gripping and withdrawal.
                float roll=90*(1-(float)Math.clamp((age-KnifePursuitRules.BLINK)/(KnifePursuitRules.GRIP-KnifePursuitRules.BLINK),0,1));
                pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(roll));
                float scale=.8f;pose.translate(0,(8-SalmosaKnifeGeometry.TIP)*scale/16,0);pose.scale(scale,scale,scale);
                recordKnife(entry.getKey(),pose,pulling);
                c.getItemRenderer().renderStatic(player,new ItemStack(KillerRankingMod.SALMOSA_MID_DAGGER),ItemDisplayContext.NONE,false,pose,buffers,c.level,0xF000F0,OverlayTexture.NO_OVERLAY,player.getId());pose.popPose();
                if(age<KnifePursuitRules.IMPACT)flyingFrames++;else embeddedFrames++;
                if(age<KnifePursuitRules.IMPACT)ribbon(context,at,direction,1.15f,.075f,age-KnifePursuitRules.THROW,.8f);
            }
            if(age>=KnifePursuitRules.BLINK&&age<KnifePursuitRules.BLINK+3){
                var at=s.value.destination().add(0,.85,0);ribbon(context,at,forward,.8f,.3f,age-KnifePursuitRules.BLINK,.7f);
                ribbon(context,s.value.origin().add(0,-.55,0),forward,.8f,.3f,age-KnifePursuitRules.BLINK,.5f);
            }
            if(age>=KnifePursuitRules.EXTRACT&&age<KnifePursuitRules.EXTRACT+4){ribbon(context,wound,direction.scale(-1),.65f,.13f,age-KnifePursuitRules.EXTRACT,1);extractFrames++;}
        }
    }
    /** Palm anchor is shared with the catching hand, so the same blade passes into its grip. */
    public static void airKnife(WorldRenderContext context,LivingEntity player,Vec3 palm,float yaw){
        var pose=context.matrixStack();var camera=context.camera().getPosition();var direction=ShoulderStabRules.incoming(yaw);
        pose.pushPose();pose.translate(palm.x-camera.x,palm.y-camera.y,palm.z-camera.z);
        pose.mulPose(new Quaternionf().rotationTo(new Vector3f(0,1,0),new Vector3f((float)direction.x,(float)direction.y,(float)direction.z)));
        float scale=SalmosaKnifeGeometry.SKILL_SCALE;pose.translate(0,(8-SalmosaKnifeGeometry.PALM)*scale/16,0);pose.scale(scale,scale,scale);
        var c=Minecraft.getInstance();c.getItemRenderer().renderStatic(player,new ItemStack(KillerRankingMod.SALMOSA_MID_DAGGER),ItemDisplayContext.NONE,false,pose,context.consumers(),c.level,0xF000F0,OverlayTexture.NO_OVERLAY,player.getId());pose.popPose();
    }
    /** Native render hook used by capture verification without altering the production model. */
    private static void recordKnife(UUID player,com.mojang.blaze3d.vertex.PoseStack pose,boolean pulling){ }
    private static void ribbon(WorldRenderContext context,Vec3 at,Vec3 direction,float length,float width,float time,float alpha){
        var pose=context.matrixStack();var camera=context.camera().getPosition();int frame=Math.clamp((int)time,0,3);
        pose.pushPose();pose.translate(at.x-camera.x,at.y-camera.y,at.z-camera.z);
        pose.mulPose(new Quaternionf().rotationTo(new Vector3f(1,0,0),new Vector3f((float)direction.x,(float)direction.y,(float)direction.z)));
        var consumer=context.consumers().getBuffer(RenderType.entityTranslucent(KillerRankingMod.id("textures/effect/wasabari_stab.png")));
        float lo=frame/4f,hi=(frame+1)/4f;
        vertex(pose,consumer,-length,-width,0,hi,alpha);vertex(pose,consumer,0,-width,1,hi,alpha);vertex(pose,consumer,0,width,1,lo,alpha);vertex(pose,consumer,-length,width,0,lo,alpha);pose.popPose();
    }
    private static void vertex(com.mojang.blaze3d.vertex.PoseStack p,com.mojang.blaze3d.vertex.VertexConsumer v,float x,float y,float u,float tex,float alpha){v.addVertex(p.last().pose(),x,y,0).setColor(.8f,.6f,1,alpha).setUv(u,tex).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(p.last(),0,0,1);}
    private KnifePursuitVfx(){ }
}
