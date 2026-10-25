package dev.killerranking.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.AutomaticParryState;
import dev.killerranking.network.*;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.firstPerson.*;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.*;
import dev.kosmx.playerAnim.minecraftApi.layers.LeftHandedHelperModifier;
import net.fabricmc.fabric.api.client.rendering.v1.*;
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
import java.util.*;

/** Native-size upper-body echoes; rapid hits never restart the body's current parry. */
public final class AutoParryVfx {
    private static final ResourceLocation LAYER=KillerRankingMod.id("auto_parry_layer");
    private static final Map<UUID,Integer> HOLDS=new HashMap<>();
    private static final List<Echo> ECHOES=new ArrayList<>();
    private static PlayerModel<AbstractClientPlayer> wide,slim;
    private static long bodyMotions,createdEchoes,renderedEchoes;
    private static final class Echo {
        final UUID player;final float yaw;final HumanoidArm hand;final boolean bladeBraced;
        final ModifierLayer<IAnimation> animation;int age;
        Echo(AbstractClientPlayer p,AutoParryEffectPayload event,IAnimation motion,HumanoidArm authoredHand,boolean bladeBraced){
            boolean mirror=p.getMainArm()==HumanoidArm.LEFT;
            player=p.getUUID();yaw=event.yaw();hand=mirror?authoredHand.getOpposite():authoredHand;
            this.bladeBraced=bladeBraced;
            animation=new ModifierLayer<>();animation.addModifierLast(new MirrorModifier(mirror));animation.setAnimation(motion);
        }
    }
    public static void register(){
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((p,stack)->{
            var layer=new ModifierLayer<IAnimation>();layer.addModifierLast(new LeftHandedHelperModifier(p));
            stack.addAnimLayer(3100,layer);PlayerAnimationAccess.getPlayerAssociatedData(p).set(LAYER,layer);
        });
        WorldRenderEvents.AFTER_ENTITIES.register(AutoParryVfx::render);
    }
    public static boolean active(UUID player){return HOLDS.containsKey(player);}
    public static void accept(AutoParryStatusPayload event){
        if(!event.active()){stop(event.player());return;}
        boolean first=!HOLDS.containsKey(event.player());HOLDS.put(event.player(),25);
        if(first){var p=player(event.player());if(p!=null)idle(p);}
    }
    public static void accept(AutoParryEffectPayload event){
        var p=player(event.player());if(p==null||!p.isAlive()||p.isSpectator())return;
        HOLDS.put(event.player(),25);
        String name=event.echo()?(switch(Math.floorMod(event.sequence()-1,10)){
            case 0->"pawn6_katana_echo_horizontal";
            case 1->"pawn6_katana_echo_horizontal_reverse";
            case 2->"pawn6_katana_echo_down";
            case 3->"pawn6_katana_echo_down_reverse";
            case 4->"pawn6_katana_echo_upright";
            case 5->"pawn6_katana_echo_upright_reverse";
            case 6->"pawn6_katana_echo_overhead";
            case 7->"pawn6_katana_echo_overhead_reverse";
            case 8->"pawn6_katana_echo_cross_guard";
            default->"pawn6_katana_echo_cross_guard_reverse";
        })
            :(event.reversed()?"pawn6_katana_auto_parry_reverse":"pawn6_katana_auto_parry");
        IAnimation motion=motion(name,!event.echo());
        if(motion==null)return;
        if(!event.echo()){set(p,motion);bodyMotions++;}
        else{
            if(ECHOES.stream().filter(e->e.player.equals(event.player())).count()>=16){
                for(var iterator=ECHOES.iterator();iterator.hasNext();)if(iterator.next().player.equals(event.player())){iterator.remove();break;}
            }
            if(ECHOES.size()>=96)ECHOES.removeFirst();
            // The reference guard grips the raised hilt with the left hand and braces
            // the lower blade with the right. Its reverse uses the opposite hands.
            HumanoidArm swordHand=name.equals("pawn6_katana_echo_cross_guard")?HumanoidArm.LEFT:HumanoidArm.RIGHT;
            ECHOES.add(new Echo(p,event,motion,swordHand,name.startsWith("pawn6_katana_echo_cross_guard")));createdEchoes++;
        }
    }
    private static AbstractClientPlayer player(UUID id){var c=Minecraft.getInstance();var p=c.level==null?null:c.level.getPlayerByUUID(id);return p instanceof AbstractClientPlayer clientPlayer?clientPlayer:null;}
    private static IAnimation motion(String name,boolean firstPerson){
        var data=PlayerAnimationRegistry.getAnimation(KillerRankingMod.id(name));if(data==null)return null;
        var motion=(IAnimation)data.playAnimation();
        if(firstPerson&&motion instanceof KeyframeAnimationPlayer key){key.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);key.setFirstPersonConfiguration(new FirstPersonConfiguration(true,true,true,false));}
        return motion;
    }
    @SuppressWarnings("unchecked") private static void set(AbstractClientPlayer p,IAnimation motion){
        var layer=PlayerAnimationAccess.getPlayerAssociatedData(p).get(LAYER);if(layer instanceof ModifierLayer<?> l)((ModifierLayer<IAnimation>)l).setAnimation(motion);
    }
    private static void idle(AbstractClientPlayer p){set(p,motion("pawn6_katana_auto_guard",true));}
    public static void tick(Minecraft c){
        if(c.level==null||c.isPaused())return;
        for(var id:new ArrayList<>(HOLDS.keySet())){
            var p=player(id);int ttl=HOLDS.get(id)-1;
            if(ttl<=0||p==null||!p.isAlive()||p.isSpectator()||!p.getMainHandItem().is(KillerRankingMod.PAWN6_KATANA)){stop(id);continue;}
            HOLDS.put(id,ttl);var layer=PlayerAnimationAccess.getPlayerAssociatedData(p).get(LAYER);
            if(layer instanceof ModifierLayer<?> l&&(l.getAnimation()==null||!l.getAnimation().isActive()))idle(p);
        }
        ECHOES.removeIf(e->{e.age++;e.animation.tick();return e.age>=AutomaticParryState.MOTION_TICKS||!HOLDS.containsKey(e.player);});
    }
    public static void stop(UUID id){HOLDS.remove(id);ECHOES.removeIf(e->e.player.equals(id));var p=player(id);if(p!=null)set(p,null);}
    public static void clear(){for(var id:new ArrayList<>(HOLDS.keySet()))stop(id);ECHOES.clear();wide=slim=null;bodyMotions=createdEchoes=renderedEchoes=0;}
    public static int echoCount(){return ECHOES.size();}
    public static long bodyMotionCount(){return bodyMotions;}
    public static long createdEchoCount(){return createdEchoes;}
    public static long renderedEchoCount(){return renderedEchoes;}
    private static PlayerModel<AbstractClientPlayer> model(boolean narrow){
        var found=narrow?slim:wide;if(found!=null)return found;
        found=new PlayerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(narrow?ModelLayers.PLAYER_SLIM:ModelLayers.PLAYER),narrow);
        found.young=false;
        if(narrow)slim=found;else wide=found;return found;
    }
    private static void joint(ModelPart part,String name,Echo echo,float delta){
        part.resetPose();
        var pos=echo.animation.get3DTransform(name,TransformType.POSITION,delta,new Vec3f(part.x,part.y,part.z));
        var rot=echo.animation.get3DTransform(name,TransformType.ROTATION,delta,Vec3f.ZERO);
        part.setPos(pos.getX(),pos.getY(),pos.getZ());part.setRotation(rot.getX(),rot.getY(),rot.getZ());
        part.xScale=part.yScale=part.zScale=1;
    }
    private static void render(WorldRenderContext context){
        if(context.matrixStack()==null||context.consumers()==null)return;
        var c=Minecraft.getInstance();var camera=context.camera().getPosition();float delta=context.tickCounter().getGameTimeDeltaPartialTick(false);
        for(var echo:ECHOES){
            var p=player(echo.player);if(p==null||p.isInvisible())continue;
            // Follow the source player's interpolated origin, including movement during a chain.
            var at=p.getPosition(delta);if(at.distanceToSqr(camera)>1024)continue;
            boolean firstPerson=p==c.player&&c.options.getCameraType().isFirstPerson();
            float opacity=(firstPerson?.24f:.48f)*Math.min(1,(echo.age+delta+1)/2)*(1-(echo.age+delta)/AutomaticParryState.MOTION_TICKS);
            if(opacity<=.01f)continue;
            echo.animation.setupAnim(delta);var model=model(p.getSkin().model()==PlayerSkin.Model.SLIM);
            joint(model.body,"body",echo,delta);joint(model.rightArm,"rightArm",echo,delta);joint(model.leftArm,"leftArm",echo,delta);
            // Slim-skin held-item anchors shift each hand outward by half a pixel.
            // Bring the blade-support hand to the same side as the hilt anchor.
            if(echo.bladeBraced&&p.getSkin().model()==PlayerSkin.Model.SLIM){
                if(echo.hand==HumanoidArm.LEFT)model.rightArm.x-=1;
                else model.leftArm.x+=1;
            }
            model.jacket.copyFrom(model.body);model.rightSleeve.copyFrom(model.rightArm);model.leftSleeve.copyFrom(model.leftArm);
            var stack=context.matrixStack();stack.pushPose();stack.translate(at.x-camera.x,at.y-camera.y,at.z-camera.z);
            float nativeScale=.9375f*p.getScale();
            stack.mulPose(Axis.YP.rotationDegrees(180-echo.yaw));stack.scale(-nativeScale,-nativeScale,nativeScale);stack.translate(0,-1.501,0);
            int color=((int)(opacity*255)<<24)|0xDCEEFF;
            var skinType=firstPerson?RenderType.entityTranslucentCull(p.getSkin().texture()):RenderType.entityTranslucentEmissive(p.getSkin().texture());
            renderUpperBody(model,stack,context.consumers().getBuffer(skinType),color,!firstPerson);
            stack.pushPose();model.translateToHand(echo.hand,stack);stack.mulPose(Axis.XP.rotationDegrees(-90));stack.mulPose(Axis.YP.rotationDegrees(180));
            stack.translate(echo.hand==HumanoidArm.LEFT?-1f/16:1f/16,.125,-.625);
            String part=echo.hand==HumanoidArm.LEFT?"leftItem":"rightItem";
            var pos=echo.animation.get3DTransform(part,TransformType.POSITION,delta,Vec3f.ZERO);var rot=echo.animation.get3DTransform(part,TransformType.ROTATION,delta,Vec3f.ZERO);
            stack.translate(pos.getX()/16,pos.getY()/16,pos.getZ()/16);stack.mulPose(Axis.ZP.rotation(rot.getZ()));stack.mulPose(Axis.YP.rotation(rot.getY()));stack.mulPose(Axis.XP.rotation(rot.getX()));
            VertexConsumer vertices=context.consumers().getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
            MultiBufferSource tinted=type->new Tint(vertices,opacity);
            c.getItemRenderer().renderStatic(p,new ItemStack(KillerRankingMod.PAWN6_KATANA),echo.hand==HumanoidArm.LEFT?ItemDisplayContext.THIRD_PERSON_LEFT_HAND:ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                echo.hand==HumanoidArm.LEFT,stack,tinted,c.level,0xF000F0,OverlayTexture.NO_OVERLAY,p.getId());
            stack.popPose();stack.popPose();renderedEchoes++;
        }
    }
    private static void renderUpperBody(PlayerModel<?> model,PoseStack stack,VertexConsumer vertices,int color,boolean torso){
        // Render only these parts directly: no head/legs and no AgeableListModel baby-body scale.
        // The local first-person camera is inside this torso; keep its arms/sword visible.
        if(torso)model.body.render(stack,vertices,0xF000F0,OverlayTexture.NO_OVERLAY,color);
        model.rightArm.render(stack,vertices,0xF000F0,OverlayTexture.NO_OVERLAY,color);
        model.leftArm.render(stack,vertices,0xF000F0,OverlayTexture.NO_OVERLAY,color);
        if(torso)model.jacket.render(stack,vertices,0xF000F0,OverlayTexture.NO_OVERLAY,color);
        model.rightSleeve.render(stack,vertices,0xF000F0,OverlayTexture.NO_OVERLAY,color);
        model.leftSleeve.render(stack,vertices,0xF000F0,OverlayTexture.NO_OVERLAY,color);
    }
    private record Tint(VertexConsumer target,float alpha) implements VertexConsumer {
        public VertexConsumer addVertex(float x,float y,float z){target.addVertex(x,y,z);return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){target.setColor(r,g,b,(int)(a*alpha));return this;}
        public VertexConsumer setUv(float u,float v){target.setUv(u,v);return this;}
        public VertexConsumer setUv1(int u,int v){target.setUv1(u,v);return this;}
        public VertexConsumer setUv2(int u,int v){target.setUv2(u,v);return this;}
        public VertexConsumer setNormal(float x,float y,float z){target.setNormal(x,y,z);return this;}
    }
    private AutoParryVfx() { }
}
