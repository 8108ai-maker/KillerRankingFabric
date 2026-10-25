package dev.killerranking.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.*;
import dev.killerranking.network.NeungsaEchoVisualPayload;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.firstPerson.*;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.api.layered.modifier.*;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.*;
import dev.kosmx.playerAnim.minecraftApi.layers.LeftHandedHelperModifier;
import java.util.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.joml.Quaternionf;

/** The original sheathes and waits; independent, short-lived skin echoes perform the middle cuts. */
public final class NeungsaEchoVfx {
    private static final ResourceLocation LAYER=KillerRankingMod.id("neungsa_echo_layer");
    private static final Map<UUID,State> ACTIVE=new HashMap<>();
    private static PlayerModel<AbstractClientPlayer> wide,slim;
    private static int created,rendered,segments;
    private static final class State {
        final UUID id;final Vec3 origin,direction;final float yaw;
        final Set<Integer> spawned=new HashSet<>();final List<Echo> echoes=new ArrayList<>();
        int age,remaining;boolean animated;
        State(NeungsaEchoVisualPayload p){id=p.player();origin=new Vec3(p.x(),p.y(),p.z());direction=new Vec3(p.dx(),p.dy(),p.dz());yaw=(float)Math.toDegrees(Math.atan2(-p.dx(),p.dz()));age=p.age();remaining=p.remaining();}
    }
    private static final class Echo {
        final int index;final ModifierLayer<IAnimation> animation=new ModifierLayer<>();int age;
        Echo(AbstractClientPlayer p,int index,int age){
            this.index=index;this.age=age;
            var clip=PlayerAnimationRegistry.getAnimation(KillerRankingMod.id("neungsa_katana_echo_"+(index%2==0?"right":"left")));
            animation.addModifierLast(new SpeedModifier(32));animation.addModifierLast(new MirrorModifier(p.getMainArm()==HumanoidArm.LEFT));
            if(clip!=null){animation.setAnimation((IAnimation)clip.playAnimation());for(int n=0;n<age;n++)animation.tick();}
        }
    }
    public static void register(){
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((p,stack)->{
            var layer=new ModifierLayer<IAnimation>();layer.addModifierLast(new SpeedModifier(32));layer.addModifierLast(new LeftHandedHelperModifier(p));
            stack.addAnimLayer(3300,layer);PlayerAnimationAccess.getPlayerAssociatedData(p).set(LAYER,layer);
        });
        WorldRenderEvents.AFTER_ENTITIES.register(NeungsaEchoVfx::render);
        ClientTickEvents.END_CLIENT_TICK.register(c->{
            if(c.level==null||c.isPaused())return;
            for(var s:new ArrayList<>(ACTIVE.values())){
                var raw=c.level.getPlayerByUUID(s.id);
                if(!(raw instanceof AbstractClientPlayer p)||!p.isAlive()||!p.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA)||s.remaining--<=0){remove(s.id);continue;}
                if(!s.animated)s.animated=animate(p,s);
                s.echoes.removeIf(e->{e.animation.tick();return ++e.age>=NeungsaEchoRules.ECHO_LIFE;});
                for(int i=0;i<NeungsaEchoRules.ECHO_COUNT;i++){
                    int age=s.age-NeungsaEchoRules.echoStart(i);
                    if(age>=0&&age<NeungsaEchoRules.ECHO_LIFE&&s.spawned.add(i)){s.echoes.add(new Echo(p,i,age));created++;}
                }
                s.age++;
            }
        });
    }
    public static void accept(NeungsaEchoVisualPayload p){
        if(p.remaining()==0){remove(p.player());return;}
        var old=ACTIVE.get(p.player());
        if(old==null||p.age()<old.age-5)ACTIVE.put(p.player(),new State(p));else old.remaining=p.remaining();
    }
    @SuppressWarnings("unchecked") private static boolean animate(AbstractClientPlayer p,State s){
        var layer=PlayerAnimationAccess.getPlayerAssociatedData(p).get(LAYER);if(!(layer instanceof ModifierLayer<?> raw))return false;
        NeungsaDrawController.interruptIaidoRecovery(p);NeungsaDrawController.iaido(p);
        var clip=PlayerAnimationRegistry.getAnimation(KillerRankingMod.id("neungsa_katana_echo_sequence"+(p.getSkin().model()==PlayerSkin.Model.SLIM?"_slim":"")));
        if(clip==null)return false;
        var k=(KeyframeAnimationPlayer)clip.playAnimation();for(int i=0;i<s.age*32;i++)k.tick();
        k.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);k.setFirstPersonConfiguration(new FirstPersonConfiguration(true,true,true,false));
        ((ModifierLayer<IAnimation>)raw).setAnimation(k);return true;
    }
    public static boolean active(UUID id){return ACTIVE.containsKey(id);}
    public static int age(UUID id){var s=ACTIVE.get(id);return s==null?-1:s.age;}
    public static int createdEchoes(){return created;}
    public static int renderedEchoes(){return rendered;}
    public static int segments(){return segments;}
    public static int echoCount(){return ACTIVE.values().stream().mapToInt(s->s.echoes.size()).sum();}
    private static void remove(UUID id){
        ACTIVE.remove(id);var c=Minecraft.getInstance();var p=c.level==null?null:c.level.getPlayerByUUID(id);
        if(p instanceof AbstractClientPlayer player){var raw=PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER);if(raw instanceof ModifierLayer<?> l)l.setAnimation(null);}
    }
    public static void clear(){for(var id:new ArrayList<>(ACTIVE.keySet()))remove(id);wide=slim=null;created=rendered=segments=0;}
    private static PlayerModel<AbstractClientPlayer> model(boolean narrow){
        var m=narrow?slim:wide;if(m==null){m=new PlayerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(narrow?ModelLayers.PLAYER_SLIM:ModelLayers.PLAYER),narrow);m.young=false;if(narrow)slim=m;else wide=m;}return m;
    }
    private static void joint(ModelPart part,String bone,Echo e,float delta){
        part.resetPose();var pos=e.animation.get3DTransform(bone,TransformType.POSITION,delta,new Vec3f(part.x,part.y,part.z));
        var rot=e.animation.get3DTransform(bone,TransformType.ROTATION,delta,Vec3f.ZERO);
        part.setPos(pos.getX(),pos.getY(),pos.getZ());part.setRotation(rot.getX(),rot.getY(),rot.getZ());part.xScale=part.yScale=part.zScale=1;
    }
    private static void render(WorldRenderContext context){
        segments=0;var c=Minecraft.getInstance();if(c.level==null||context.matrixStack()==null||context.consumers()==null)return;
        float partial=context.tickCounter().getGameTimeDeltaPartialTick(false);var camera=context.camera().getPosition();int count=0;
        for(var s:ACTIVE.values()){
            var raw=c.level.getPlayerByUUID(s.id);if(!(raw instanceof AbstractClientPlayer p)||p.isInvisible()||p.distanceToSqr(camera)>4096||++count>8)continue;
            for(var e:s.echoes)renderEcho(context,p,s,e,partial);
            var stack=context.matrixStack();stack.pushPose();stack.translate(s.origin.x-camera.x,s.origin.y-camera.y,s.origin.z-camera.z);
            // Orient the whole forward area to the cast's original aim, including vertical aim.
            stack.mulPose(new Quaternionf().rotationTo(0,0,1,(float)s.direction.x,(float)s.direction.y,(float)s.direction.z));
            var end=s.origin.add(s.direction.scale(NeungsaEchoRules.REACH));
            var wall=c.level.clip(new ClipContext(s.origin,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
            float reach=(float)(wall.getType()==HitResult.Type.BLOCK?s.origin.distanceTo(wall.getLocation()):NeungsaEchoRules.REACH);
            var vertices=context.consumers().getBuffer(WeaponTrailRenderType.LAYER);
            for(int i=-1;i<=NeungsaEchoRules.ECHO_COUNT;i++){
                int beat=i<0?NeungsaEchoRules.OPEN_HIT:i==NeungsaEchoRules.ECHO_COUNT?NeungsaEchoRules.FINAL_HIT:NeungsaEchoRules.echoHit(i);
                float t=s.age+partial-beat+1;if(t<0||t>=4||reach<.5)continue;
                boolean big=i<0||i==NeungsaEchoRules.ECHO_COUNT;
                double angle=big?.08:(i%2==0?1:-1)*(.18+(i%5)*.14);
                // Each echo's cut occupies a new depth/height, rather than a fixed stack of identical flashes.
                float depth=Math.min(reach-.15f,.9f+((i*7+21)%20)/20f*Math.max(0,reach-1.1f));
                var stroke=new BishopBarrageMotion.Stroke(0,big?0:((i%5)-2)*.19f,depth,(float)Math.cos(angle),(float)Math.sin(angle),big?2f:1.7f,.035f,0,0);
                segments+=BishopBarrageVfx.renderStroke(stack,new Tint(vertices,.85f,true),stroke,t*1.5f);
            }
            stack.popPose();
        }
    }
    private static void renderEcho(WorldRenderContext context,AbstractClientPlayer p,State s,Echo e,float partial){
        var c=Minecraft.getInstance();boolean first=p==c.player&&c.options.getCameraType().isFirstPerson();
        float age=e.age+partial,alpha=(first?.25f:.52f)*Math.min(1,(age+.5f)/.7f)*Math.clamp((5-age)/2,0,1);
        if(alpha<=0)return;e.animation.setupAnim(partial);
        var m=model(p.getSkin().model()==PlayerSkin.Model.SLIM);
        joint(m.body,"body",e,partial);joint(m.head,"head",e,partial);joint(m.rightArm,"rightArm",e,partial);joint(m.leftArm,"leftArm",e,partial);joint(m.rightLeg,"rightLeg",e,partial);joint(m.leftLeg,"leftLeg",e,partial);
        m.hat.copyFrom(m.head);m.jacket.copyFrom(m.body);m.rightSleeve.copyFrom(m.rightArm);m.leftSleeve.copyFrom(m.leftArm);m.rightPants.copyFrom(m.rightLeg);m.leftPants.copyFrom(m.leftLeg);
        var side=new Vec3(s.direction.z,0,-s.direction.x).normalize();
        var at=p.getPosition(partial).add(side.scale((e.index%2==0?1:-1)*(.32+(e.index%3)*.18))).add(s.direction.scale(.12+(e.index%4)*.12));
        var camera=context.camera().getPosition();var stack=context.matrixStack();stack.pushPose();stack.translate(at.x-camera.x,at.y-camera.y,at.z-camera.z);
        float scale=.9375f*p.getScale();stack.mulPose(Axis.YP.rotationDegrees(180-s.yaw));stack.scale(-scale,-scale,scale);stack.translate(0,-1.501,0);
        int color=((int)(alpha*255)<<24)|0xCFB5FF;var v=context.consumers().getBuffer(RenderType.entityTranslucentEmissive(p.getSkin().texture()));
        var parts=first?List.of(m.rightArm,m.leftArm,m.rightSleeve,m.leftSleeve):List.of(m.body,m.head,m.hat,m.rightArm,m.leftArm,m.rightLeg,m.leftLeg,m.jacket,m.rightSleeve,m.leftSleeve,m.rightPants,m.leftPants);
        for(var part:parts)part.render(stack,v,0xF000F0,OverlayTexture.NO_OVERLAY,color);
        var hand=p.getMainArm();stack.pushPose();m.translateToHand(hand,stack);stack.translate(hand==HumanoidArm.LEFT?1.0/16:-1.0/16,10.0/16,0);
        var rot=e.animation.get3DTransform(hand==HumanoidArm.LEFT?"leftItem":"rightItem",TransformType.ROTATION,partial,Vec3f.ZERO);
        stack.mulPose(new Quaternionf().rotationZYX(rot.getZ(),rot.getY(),rot.getX()));if(hand==HumanoidArm.LEFT)stack.mulPose(Axis.YP.rotationDegrees(180));stack.scale(.9f,.9f,.9f);
        var baked=((FabricBakedModelManager)c.getModelManager()).getModel(KillerRankingMod.id("item/neungsa_katana"));
        var vertices=context.consumers().getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));MultiBufferSource tinted=type->new Tint(vertices,alpha,true);
        c.getItemRenderer().render(new ItemStack(KillerRankingMod.NEUNGSA_KATANA),ItemDisplayContext.NONE,false,stack,tinted,0xF000F0,OverlayTexture.NO_OVERLAY,baked);
        stack.popPose();stack.popPose();rendered++;
    }
    private record Tint(VertexConsumer target,float alpha,boolean purple) implements VertexConsumer {
        public VertexConsumer addVertex(float x,float y,float z){target.addVertex(x,y,z);return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){target.setColor(purple?(int)(r*.84):r,purple?(int)(g*.62):g,b,(int)(a*alpha));return this;}
        public VertexConsumer setUv(float u,float v){target.setUv(u,v);return this;}
        public VertexConsumer setUv1(int u,int v){target.setUv1(u,v);return this;}
        public VertexConsumer setUv2(int u,int v){target.setUv2(u,v);return this;}
        public VertexConsumer setNormal(float x,float y,float z){target.setNormal(x,y,z);return this;}
    }
    private NeungsaEchoVfx(){ }
}
