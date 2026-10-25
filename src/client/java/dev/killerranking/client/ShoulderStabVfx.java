package dev.killerranking.client;
import com.mojang.blaze3d.vertex.*;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.ShoulderStabRules;
import dev.killerranking.network.ShoulderStabVisualPayload;
import dev.killerranking.visual.*;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.firstPerson.*;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.*;

public final class ShoulderStabVfx {
    private static final ResourceLocation LAYER=KillerRankingMod.id("shoulder_stab_layer");
    private static final class State {
        ShoulderStabVisualPayload value;int age,ttl;boolean animated,arrived;Vec3 tip;
        State(ShoulderStabVisualPayload p){update(p);}
        void update(ShoulderStabVisualPayload p){value=p;age=p.age();ttl=25;}
    }
    private static final Map<UUID,State> ACTIVE=new HashMap<>();private static long knives,ribbons,snaps;
    public static void register(){
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((p,stack)->{var layer=new ModifierLayer<IAnimation>();stack.addAnimLayer(3550,layer);PlayerAnimationAccess.getPlayerAssociatedData(p).set(LAYER,layer);});
        WorldRenderEvents.AFTER_ENTITIES.register(ShoulderStabVfx::render);
    }
    public static void accept(ShoulderStabVisualPayload p){
        if(p.phase()==0){remove(p.player());return;}
        ACTIVE.compute(p.player(),(id,s)->{if(s==null||s.value.phase()!=p.phase()||p.age()<s.age-3)return new State(p);s.update(p);return s;});
    }
    public static boolean active(UUID id){return ACTIVE.containsKey(id);}
    public static boolean casting(UUID id){var s=ACTIVE.get(id);return s!=null&&s.value.phase()>=ShoulderStabRules.CAST;}
    public static boolean counter(UUID id){var s=ACTIVE.get(id);return s!=null&&s.value.phase()==ShoulderStabRules.COUNTER;}
    public static float age(UUID id,float delta){var s=ACTIVE.get(id);return s==null?-1:s.age+delta;}
    public static long renderedKnives(){return knives;}public static long renderedRibbons(){return ribbons;}public static long snappedArrivals(){return snaps;}
    public static Vec3 knifeTip(UUID id){var s=ACTIVE.get(id);return s==null?null:s.tip;}
    public static Vec3 airCatch(UUID id){var s=ACTIVE.get(id);return s==null?null:s.value.airCatch();}
    public static void recordKnife(UUID id,PoseStack stack){var s=ACTIVE.get(id);if(s==null)return;var v=stack.last().pose().transformPosition(new Vector3f(0,(SalmosaKnifeGeometry.TIP-8)/16,0));s.tip=Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().add(v.x,v.y,v.z);knives++;}
    public static Vec3 wound(UUID id,float delta){var s=ACTIVE.get(id);if(s==null)return null;var c=Minecraft.getInstance();var p=c.level==null?null:c.level.getPlayerByUUID(id);var target=target(s);
        return target==null||p==null?s.value.wound():ShoulderStabRules.shoulder(target.getPosition(delta),target.getBbWidth(),target.getBbHeight(),s.value.yaw(),p.getMainArm()==HumanoidArm.LEFT);}
    private static LivingEntity target(State s){var c=Minecraft.getInstance();if(c.level==null)return null;for(var e:c.level.entitiesForRendering())if(e.getUUID().equals(s.value.target())&&e instanceof LivingEntity living)return living;return null;}
    private static Vec3 nativePoint(PoseStack stack,Vec3 world){var at=world.subtract(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());var local=new Matrix4f(stack.last().pose()).invert().transformPosition(new Vector3f((float)at.x,(float)at.y,(float)at.z));return new Vec3(local.x*16,local.y*16,local.z*16);}
    public static Vec3 knifeDirection(LivingEntity e,PoseStack stack){var s=ACTIVE.get(e.getUUID());var at=wound(e.getUUID(),0);if(s==null||at==null)return new Vec3(0,.96,-.28).normalize();return nativePoint(stack,at.add(ShoulderStabRules.incoming(s.value.yaw()))).subtract(nativePoint(stack,at)).normalize();}
    public static WasabariMotion.Pose pose(LivingEntity e,PoseStack stack,float delta){
        var state=ACTIVE.get(e.getUUID());float age=age(e.getUUID(),delta);boolean left=e.getMainArm()==HumanoidArm.LEFT;var at=wound(e.getUUID(),delta);
        // Interpolated animation time may reach the blink beat before its server packet.
        // Keep the raised hand with the caster until the confirmed arrival is applied.
        boolean firstPerson=e==Minecraft.getInstance().player&&FirstPersonMode.isFirstPersonPass();
        if(state!=null&&state.value.phase()==ShoulderStabRules.COUNTER&&at!=null)return ShoulderStabMotion.counterContact(age,left,nativePoint(stack,at),knifeDirection(e,stack),nativePoint(stack,state.value.airCatch()),HeavyPunchPose.center(e),firstPerson);
        return state==null||!state.arrived||age<ShoulderStabRules.BLINK||at==null?ShoulderStabMotion.pose(age,left):ShoulderStabMotion.contact(age,left,nativePoint(stack,at),knifeDirection(e,stack),HeavyPunchPose.center(e),firstPerson);
    }
    public static void tick(Minecraft c){if(c.level==null||c.isPaused())return;for(var id:new ArrayList<>(ACTIVE.keySet())){
        var s=ACTIVE.get(id);var p=c.level.getPlayerByUUID(id);
        boolean counter=s.value.phase()==ShoulderStabRules.COUNTER;
        if(--s.ttl<=0||p==null||!p.isAlive()||p.isSpectator()||s.value.phase()>=ShoulderStabRules.CAST&&s.age>=(counter?ShoulderStabRules.COUNTER_DURATION:ShoulderStabRules.DURATION)){remove(id);continue;}
        if(s.value.phase()>=ShoulderStabRules.CAST){
            if(counter){var at=ShoulderStabRules.counterPosition(s.value.origin(),s.value.destination(),s.value.airCatch(),s.age);p.lerpTo(at.x,at.y,at.z,p.getYRot(),p.getXRot(),1);p.setPos(at);s.arrived=s.age>=4;}
            else if(s.value.age()>=ShoulderStabRules.BLINK&&!s.arrived){var at=s.value.destination();p.lerpTo(at.x,at.y,at.z,p.getYRot(),p.getXRot(),1);p.setPos(at);p.xo=p.xOld=at.x;p.yo=p.yOld=at.y;p.zo=p.zOld=at.z;s.arrived=true;snaps++;}
            p.yBodyRot=p.yBodyRotO=s.value.yaw();if(!s.animated&&p instanceof AbstractClientPlayer skin)s.animated=animate(skin,s);
        }
        s.age++;
    }}
    @SuppressWarnings("unchecked") private static boolean animate(AbstractClientPlayer p,State s){var layer=PlayerAnimationAccess.getPlayerAssociatedData(p).get(LAYER);if(!(layer instanceof ModifierLayer<?> mods))return false;((ModifierLayer<IAnimation>)mods).setAnimation(new Motion(p,s));return true;}
    private static final class Motion implements IAnimation {
        final AbstractClientPlayer player;final State state;Motion(AbstractClientPlayer p,State s){player=p;state=s;}
        public boolean isActive(){return ACTIVE.get(player.getUUID())==state&&state.value.phase()>=ShoulderStabRules.CAST;}
        public void setupAnim(float delta){ }
        public FirstPersonMode getFirstPersonMode(float delta){return FirstPersonMode.THIRD_PERSON_MODEL;}
        public FirstPersonConfiguration getFirstPersonConfiguration(float delta){return new FirstPersonConfiguration(true,true,false,false);}
        public Vec3f get3DTransform(String name,TransformType type,float delta,Vec3f baseline){
            var pose=state.value.phase()==ShoulderStabRules.COUNTER?ShoulderStabMotion.counterPose(state.age+delta,player.getMainArm()==HumanoidArm.LEFT):ShoulderStabMotion.pose(state.age+delta,player.getMainArm()==HumanoidArm.LEFT);
            var limb=switch(name){case "torso"->pose.torso();case "head"->pose.head();case "rightArm"->pose.rightArm();case "leftArm"->pose.leftArm();case "rightLeg"->pose.rightLeg();case "leftLeg"->pose.leftLeg();default->null;};
            if(limb==null)return name.equals("body")&&type!=TransformType.SCALE?Vec3f.ZERO:baseline;
            if(type==TransformType.SCALE)return new Vec3f(1,1,1);if(type==TransformType.BEND)return Vec3f.ZERO;
            var value=type==TransformType.POSITION?limb.pivot():limb.rotation();return new Vec3f((float)value.x,(float)value.y,(float)value.z);
        }
    }
    private static void remove(UUID id){ACTIVE.remove(id);var c=Minecraft.getInstance();if(c.level!=null&&c.level.getPlayerByUUID(id) instanceof AbstractClientPlayer p){var l=PlayerAnimationAccess.getPlayerAssociatedData(p).get(LAYER);if(l instanceof ModifierLayer<?> layer)layer.setAnimation(null);}}
    public static void clear(){for(var id:new ArrayList<>(ACTIVE.keySet()))remove(id);}
    private static void render(WorldRenderContext context){
        if(context.matrixStack()==null||context.consumers()==null)return;float delta=context.tickCounter().getGameTimeDeltaPartialTick(false);
        for(var entry:ACTIVE.entrySet()){var s=entry.getValue();if(s.value.phase()<ShoulderStabRules.CAST)continue;float age=s.age+delta;var direction=ShoulderStabRules.incoming(s.value.yaw());
            boolean counter=s.value.phase()==ShoulderStabRules.COUNTER;
            if(counter&&age<ShoulderStabRules.COUNTER_CATCH){var p=Minecraft.getInstance().level.getPlayerByUUID(entry.getKey());if(p!=null)KnifePursuitVfx.airKnife(context,p,ShoulderStabRules.counterKnife(s.value.airStart(),s.value.airCatch(),age),s.value.yaw());}
            if(!counter&&s.arrived&&age>=ShoulderStabRules.BLINK&&age<ShoulderStabRules.BLINK+3){ribbon(context,s.value.origin().add(0,.9,0),Vec3.directionFromRotation(0,s.value.yaw()),.9f,.3f,age-ShoulderStabRules.BLINK,.65f);ribbon(context,s.value.destination().add(0,.9,0),direction,.8f,.3f,age-ShoulderStabRules.BLINK,.8f);}
            int impact=counter?ShoulderStabRules.COUNTER_IMPACT:ShoulderStabRules.IMPACT;
            if(s.arrived&&age>=impact-1&&age<impact+3){var at=wound(entry.getKey(),delta);if(at!=null){ribbon(context,at,direction,counter?1.25f:.65f,counter?.2f:.16f,age-impact+1,1);ribbons++;}}
        }
    }
    private static void ribbon(WorldRenderContext context,Vec3 at,Vec3 direction,float length,float width,float time,float alpha){
        var pose=context.matrixStack();var camera=context.camera().getPosition();int frame=Math.clamp((int)time,0,3);
        pose.pushPose();pose.translate(at.x-camera.x,at.y-camera.y,at.z-camera.z);pose.mulPose(new Quaternionf().rotationTo(new Vector3f(1,0,0),new Vector3f((float)direction.x,(float)direction.y,(float)direction.z)));
        var v=context.consumers().getBuffer(RenderType.entityTranslucent(KillerRankingMod.id("textures/effect/wasabari_stab.png")));float lo=frame/4f,hi=(frame+1)/4f;
        vertex(pose,v,-length,-width,0,hi,alpha);vertex(pose,v,0,-width,1,hi,alpha);vertex(pose,v,0,width,1,lo,alpha);vertex(pose,v,-length,width,0,lo,alpha);pose.popPose();
    }
    private static void vertex(PoseStack p,VertexConsumer v,float x,float y,float u,float tex,float alpha){v.addVertex(p.last().pose(),x,y,0).setColor(.88f,.68f,1,alpha).setUv(u,tex).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(p.last(),0,0,1);}
    private ShoulderStabVfx(){ }
}
