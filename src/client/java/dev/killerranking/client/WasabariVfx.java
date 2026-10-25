package dev.killerranking.client;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.WasabariRules;
import dev.killerranking.network.WasabariVisualPayload;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.minecraftApi.*;
import java.util.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.model.geom.ModelPart;
import dev.killerranking.visual.WasabariMotion;
import org.joml.Vector3f;
import com.mojang.blaze3d.vertex.*;

public final class WasabariVfx {
    private static final ResourceLocation LAYER=KillerRankingMod.id("wasabari_layer"),ANIMATION=KillerRankingMod.id("salmosa_wasabari");
    private static final class State {UUID victim;int age,remaining,lastBlood=-1;float yaw;Vec3 bladeTip,bladeDirection;boolean tripped,animated;State(WasabariVisualPayload p){victim=p.victim();age=p.age();remaining=p.remaining();yaw=p.yaw();tripped=p.tripped();}}
    private static final Map<UUID,State> ACTIVE=new HashMap<>();private static long sweeps,stabs,prone;
    public record LegPoints(Vec3 ankle,Vec3 shin,Vec3 centre,Vec3 outward) { }
    private static final Map<UUID,LegPoints> LEGS=new HashMap<>();
    public static LegPoints legPoints(UUID victim){return LEGS.get(victim);}
    public static LegPoints heldLeg(UUID owner){var s=ACTIVE.get(owner);return s==null||!s.tripped?null:LEGS.get(s.victim);}
    public static Vec3 knifeTip(UUID owner){var s=ACTIVE.get(owner);return s==null?null:s.bladeTip;}
    public static Vec3 bladeDirection(UUID owner){var s=ACTIVE.get(owner);return s==null?null:s.bladeDirection;}
    public static void recordKnife(UUID owner,PoseStack stack){var s=ACTIVE.get(owner);if(s!=null){var matrix=new org.joml.Matrix4f(stack.last().pose());var camera=Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();s.bladeTip=world(matrix,0,(dev.killerranking.visual.SalmosaKnifeGeometry.TIP-8)/16,0,camera);s.bladeDirection=s.bladeTip.subtract(world(matrix,0,-3f/16,0,camera)).normalize();}}
    /** Read the rendered limb instead of estimating its position from a standing hitbox. */
    public static void recordLeg(LivingEntity victim,ModelPart leg,PoseStack stack){
        if(!captured(victim.getUUID()))return;
        var matrix=new org.joml.Matrix4f(stack.last().pose());
        var local=new PoseStack();leg.translateAndRotate(local);matrix.mul(local.last().pose());
        var camera=Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        var owner=captor(victim.getUUID());if(owner==null)return;
        var side=Vec3.directionFromRotation(0,owner.yBodyRot+90).scale(owner.getMainArm()==HumanoidArm.LEFT?-1:1);
        var axis=matrix.transformDirection(new Vector3f(1,0,0)).normalize();
        double sign=side.dot(new Vec3(axis.x,axis.y,axis.z))>=0?1:-1;
        var centre=world(matrix,0,5.5f/16,0,camera);
        var shin=world(matrix,(float)(sign*2/16),5.5f/16,0,camera);
        LEGS.put(victim.getUUID(),new LegPoints(world(matrix,0,10f/16,0,camera),shin,centre,shin.subtract(centre).normalize()));
    }
    /** Visit the real leg cube with all ancestor pivots and native cube dimensions applied. */
    public static void recordLeg(LivingEntity victim,net.minecraft.client.model.HierarchicalModel<?> tree,ModelPart leg,String name,PoseStack stack){
        if(!captured(victim.getUUID()))return;
        var owner=captor(victim.getUUID());if(owner==null)return;
        var camera=Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        var side=Vec3.directionFromRotation(0,owner.yBodyRot+90).scale(owner.getMainArm()==HumanoidArm.LEFT?-1:1);
        float[] longest={0};
        tree.root().visit(stack,(pose,path,index,cube)->{
            if(!(path.equals(name)||path.endsWith("/"+name)))return;
            float length=cube.maxY-cube.minY;if(length<=longest[0])return;longest[0]=length;
            var matrix=new org.joml.Matrix4f(pose.pose());
            var axis=matrix.transformDirection(new Vector3f(1,0,0)).normalize();
            float x=(cube.minX+cube.maxX)/2,z=(cube.minZ+cube.maxZ)/2;
            float edge=side.dot(new Vec3(axis.x,axis.y,axis.z))>=0?cube.maxX:cube.minX;
            float shinY=cube.minY+length*.46f,ankleY=cube.minY+length*.84f;
            var centre=world(matrix,x/16,shinY/16,z/16,camera);
            var shin=world(matrix,edge/16,shinY/16,z/16,camera);
            LEGS.put(victim.getUUID(),new LegPoints(world(matrix,x/16,ankleY/16,z/16,camera),shin,centre,shin.subtract(centre).normalize()));
        });
    }
    private static Vec3 world(org.joml.Matrix4f matrix,float x,float y,float z,Vec3 origin){var v=matrix.transformPosition(new Vector3f(x,y,z));return origin.add(v.x,v.y,v.z);}
    private static Vec3 nativePoint(PoseStack stack,Vec3 world){
        var at=world.subtract(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
        var local=new org.joml.Matrix4f(stack.last().pose()).invert().transformPosition(new Vector3f((float)at.x,(float)at.y,(float)at.z));
        return new Vec3(local.x*16,local.y*16,local.z*16);
    }
    public static Vec3 knifeDirection(LivingEntity e,PoseStack stack){
        var points=heldLeg(e.getUUID());
        return points==null?WasabariMotion.knifeDirection(e.getMainArm()==HumanoidArm.LEFT):nativePoint(stack,points.shin().add(incoming(points))).subtract(nativePoint(stack,points.shin())).normalize();
    }
    /** Ice-pick grip: a high downward plunge still enters the side of the lifted leg. */
    private static Vec3 incoming(LegPoints points){return points.outward().scale(-.48).add(0,-.88,0).normalize();}
    private static WasabariMotion.Pose basePose(LivingEntity e,float age){
        var c=Minecraft.getInstance();boolean left=e.getMainArm()==HumanoidArm.LEFT;
        return e==c.player&&c.options.getCameraType().isFirstPerson()?WasabariMotion.pose(age,left):WasabariMotion.thirdPersonPose(age,left);
    }
    public static WasabariMotion.Pose heldPose(LivingEntity e,PoseStack stack,float delta){
        float age=age(e.getUUID(),delta);boolean left=e.getMainArm()==HumanoidArm.LEFT;
        var base=basePose(e,age);var state=ACTIVE.get(e.getUUID());
        var points=state!=null&&state.tripped?LEGS.get(state.victim):null;
        return points==null?base:WasabariMotion.hold(base,age,left,nativePoint(stack,points.ankle()),nativePoint(stack,points.shin()),knifeDirection(e,stack),HeavyPunchPose.center(e));
    }
    public static void register(){
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((p,stack)->{var layer=new ModifierLayer<IAnimation>();layer.addModifierLast(new dev.kosmx.playerAnim.minecraftApi.layers.LeftHandedHelperModifier(p));stack.addAnimLayer(3500,layer);PlayerAnimationAccess.getPlayerAssociatedData(p).set(LAYER,layer);});
        WorldRenderEvents.AFTER_ENTITIES.register(WasabariVfx::render);
    }
    public static void accept(WasabariVisualPayload p){if(p.remaining()==0){remove(p.owner());return;}if(!p.tripped())LEGS.remove(p.victim());ACTIVE.compute(p.owner(),(id,s)->{if(s==null)return new State(p);s.victim=p.victim();s.age=p.age();s.remaining=p.remaining();s.yaw=p.yaw();s.tripped=p.tripped();return s;});}
    public static boolean active(UUID id){return ACTIVE.containsKey(id);}
    public static boolean captured(UUID id){return ACTIVE.values().stream().anyMatch(s->s.tripped&&id.equals(s.victim));}
    public static boolean immobilized(Entity e){return e.level().isClientSide&&(active(e.getUUID())||captured(e.getUUID()));}
    public static float age(UUID id,float delta){var s=ACTIVE.get(id);return s==null?-1:s.age+delta;}
    public static AbstractClientPlayer captor(UUID victim){var c=Minecraft.getInstance();if(c.level==null)return null;for(var entry:ACTIVE.entrySet())if(entry.getValue().tripped&&victim.equals(entry.getValue().victim)&&c.level.getPlayerByUUID(entry.getKey()) instanceof AbstractClientPlayer p)return p;return null;}
    public static float proneProgress(UUID victim,float delta){for(var s:ACTIVE.values())if(s.tripped&&victim.equals(s.victim)){prone++;return Math.clamp((s.age+delta-WasabariRules.BARI)/4f,0,1);}return 0;}
    public static long renderedSweeps(){return sweeps;}public static long renderedStabs(){return stabs;}public static long renderedProne(){return prone;}
    private static LivingEntity victim(State s){var c=Minecraft.getInstance();if(c.level==null)return null;for(var e:c.level.entitiesForRendering())if(s.victim.equals(e.getUUID())&&e instanceof LivingEntity living)return living;return null;}
    public static void tick(Minecraft c){if(c.isPaused()||c.level==null)return;for(var id:new ArrayList<>(ACTIVE.keySet())){
        var s=ACTIVE.get(id);var p=c.level.getPlayerByUUID(id);if(--s.remaining<=0||p==null||!p.isAlive()||p.isSpectator()){remove(id);continue;}
        if(!s.animated)s.animated=animate(p,s.age);p.yBodyRot=p.yBodyRotO=s.yaw;s.age++;
    }}
    @SuppressWarnings("unchecked") private static boolean animate(net.minecraft.world.entity.player.Player p,int age){
        if(!(p instanceof AbstractClientPlayer player))return false;var layer=PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER);if(!(layer instanceof ModifierLayer<?>))return false;
        IAnimation animation=null;if(age>=0){var data=PlayerAnimationRegistry.getAnimation(ANIMATION);if(data==null)return false;animation=(IAnimation)data.playAnimation();
            if(animation instanceof KeyframeAnimationPlayer key){for(int i=0;i<age;i++)key.tick();key.setFirstPersonMode(dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode.THIRD_PERSON_MODEL);key.setFirstPersonConfiguration(new dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration(true,true,false,false));}}
        ((ModifierLayer<IAnimation>)layer).setAnimation(animation);return true;
    }
    private static void remove(UUID id){var s=ACTIVE.remove(id);if(s!=null)LEGS.remove(s.victim);var c=Minecraft.getInstance();if(c.level!=null){var p=c.level.getPlayerByUUID(id);if(p!=null)animate(p,-1);}}
    public static void clear(){for(var id:new ArrayList<>(ACTIVE.keySet()))remove(id);LEGS.clear();}
    private static void render(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context){
        var c=Minecraft.getInstance();var pose=context.matrixStack();var buffers=context.consumers();if(c.level==null||pose==null||buffers==null)return;
        float delta=context.tickCounter().getGameTimeDeltaPartialTick(false);var camera=context.camera().getPosition();
        for(var entry:ACTIVE.entrySet()){
            var player=c.level.getPlayerByUUID(entry.getKey());if(player==null||player.distanceToSqr(camera)>4096)continue;var s=entry.getValue();float age=s.age+delta;
            var forward=net.minecraft.world.phys.Vec3.directionFromRotation(0,s.yaw);
            for(int beat:new int[]{WasabariRules.WA,WasabariRules.BARI})if(age>=beat-1&&age<beat+4){
                float time=age-beat+1;int frame=Math.min(3,(int)time);var motion=basePose(player,age);var foot=(player.getMainArm()==HumanoidArm.LEFT?motion.leftLeg():motion.rightLeg()).point(0,12,0);
                var at=player.getPosition(delta).add(new Vec3(forward.z,0,-forward.x).scale(foot.x/16)).add(forward.scale(-foot.z/16)).add(0,(24-foot.y)/16+.04,0);
                pose.pushPose();pose.translate(at.x-camera.x,at.y-camera.y,at.z-camera.z);pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-s.yaw+(beat==WasabariRules.BARI?70:-15)));pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
                quad(pose,buffers.getBuffer(RenderType.entityTranslucent(KillerRankingMod.id("textures/effect/wasabari_sweep.png"))),-.9f,-.7f,.9f,.7f,frame/4f,(frame+1)/4f,Math.min(1,(5-time)/2));pose.popPose();sweeps++;
            }
            var target=victim(s);if(!s.tripped||target==null)continue;
            for(int i=0;i<WasabariRules.STABS;i++){
                float time=age-(WasabariRules.FIRST_STAB+i*WasabariRules.STAB_INTERVAL);
                if(time<-.5f||time>=3)continue;int frame=Math.min(3,(int)Math.max(0,time));
                var points=LEGS.get(target.getUUID());
                var at=points==null?target.getPosition(delta).subtract(forward.scale(Math.min(1,target.getBbHeight()*.36))).add(0,.26,0):points.shin();
                if(time>=0&&s.lastBlood<i&&points!=null){
                    s.lastBlood=i;var dust=new net.minecraft.core.particles.DustParticleOptions(new Vector3f(.88f,.025f,.06f),.85f);
                    for(int fleck=0;fleck<8;fleck++){var random=c.level.random;var spray=points.outward().scale(.08+random.nextFloat()*.12).add((random.nextFloat()-.5)*.10,.04+random.nextFloat()*.10,(random.nextFloat()-.5)*.10);c.level.addParticle(dust,at.x,at.y,at.z,spray.x,spray.y,spray.z);}
                }
                pose.pushPose();pose.translate(at.x-camera.x,at.y-camera.y,at.z-camera.z);var direction=points==null?new Vec3(forward.z,-1,-forward.x).normalize():incoming(points);
                pose.mulPose(new org.joml.Quaternionf().rotationTo(new Vector3f(1,0,0),new Vector3f((float)direction.x,(float)direction.y,(float)direction.z)));
                quad(pose,buffers.getBuffer(RenderType.entityTranslucent(KillerRankingMod.id("textures/effect/wasabari_stab.png"))),-.40f,-.06f,.10f,.06f,frame/4f,(frame+1)/4f,Math.min(1,(3-time)/2));pose.popPose();stabs++;
            }
        }
    }
    private static void quad(PoseStack s,VertexConsumer v,float x0,float y0,float x1,float y1,float lo,float hi,float a){vertex(s,v,x0,y0,0,hi,a);vertex(s,v,x1,y0,1,hi,a);vertex(s,v,x1,y1,1,lo,a);vertex(s,v,x0,y1,0,lo,a);}
    private static void vertex(PoseStack s,VertexConsumer v,float x,float y,float u,float tex,float alpha){v.addVertex(s.last().pose(),x,y,0).setColor(1,1,1,alpha).setUv(u,tex).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(s.last(),0,0,1);}
    private WasabariVfx() { }
}
