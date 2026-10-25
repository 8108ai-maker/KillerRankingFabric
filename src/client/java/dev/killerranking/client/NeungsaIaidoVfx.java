package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.NeungsaIaidoRules;
import dev.killerranking.network.NeungsaIaidoVisualPayload;
import dev.killerranking.visual.NeungsaBreathMotion;
import dev.killerranking.visual.NeungsaIaidoDrawMotion;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.kosmx.playerAnim.api.firstPerson.*;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.minecraftApi.*;
import dev.kosmx.playerAnim.minecraftApi.layers.LeftHandedHelperModifier;
import java.util.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

/** Compact fixed-grip Iaido with two connected purple streams inhaled below the chin. */
public final class NeungsaIaidoVfx {
    private static final ResourceLocation LAYER=KillerRankingMod.id("neungsa_iaido_layer");
    private static final Map<UUID,State> ACTIVE=new HashMap<>();
    private static int breaths,breathSegments;
    private static final class State {int age,remaining,phase;boolean full,animated;State(NeungsaIaidoVisualPayload p){age=p.age();phase=p.phase();full=p.full();remaining=full&&phase==NeungsaIaidoRules.SLASHING?NeungsaIaidoDrawMotion.VISUAL_TICKS-age:p.remaining();}}
    public static void register(){
        WorldRenderEvents.AFTER_ENTITIES.register(NeungsaIaidoVfx::renderBreath);
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((p,stack)->{
            var layer=new ModifierLayer<IAnimation>();layer.addModifierLast(new SpeedModifier(32));
            layer.addModifierLast(new LeftHandedHelperModifier(p));stack.addAnimLayer(3200,layer);
            PlayerAnimationAccess.getPlayerAssociatedData(p).set(LAYER,layer);
        });
        ClientTickEvents.END_CLIENT_TICK.register(c->{
            if(c.level==null||c.isPaused())return;
            for(var id:new ArrayList<>(ACTIVE.keySet())){
                var s=ACTIVE.get(id);var p=c.level.getPlayerByUUID(id);
                if(!(p instanceof AbstractClientPlayer player)||!player.isAlive()||!player.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA)||s.remaining--<=0){remove(id);continue;}
                if(!s.animated)s.animated=animate(player,s);
                s.age++;
            }
        });
    }
    private static void renderBreath(WorldRenderContext context){
        breathSegments=0;
        var c=Minecraft.getInstance();
        if(c.level==null||context.matrixStack()==null||context.consumers()==null)return;
        var camera=context.camera().getPosition();float partial=context.tickCounter().getGameTimeDeltaPartialTick(false);int players=0;
        for(var entry:ACTIVE.entrySet()){
            var state=entry.getValue();if(state.phase!=NeungsaIaidoRules.CHARGING||state.age<8)continue;
            var raw=c.level.getPlayerByUUID(entry.getKey());
            if(!(raw instanceof AbstractClientPlayer p)||p.distanceToSqr(camera)>1024||++players>8)continue;
            boolean first=p==c.getCameraEntity()&&c.options.getCameraType().isFirstPerson();
            var look=p.getLookAngle();var side=new Vec3(look.z,0,-look.x).normalize();
            if(side.lengthSqr()<.01)side=new Vec3(1,0,0);
            var up=look.cross(side).normalize();
            var position=p.getPosition(partial).add(0,p.getEyeHeight()-(first?.10:.34),0).add(look.scale(first?.30:.24));
            float scale=first?.62f:1,age=state.age+partial;
            float fade=Math.clamp((age-8)/6,0,1);
            var stack=context.matrixStack();stack.pushPose();stack.translate(position.x-camera.x,position.y-camera.y,position.z-camera.z);
            var vertices=context.consumers().getBuffer(WeaponTrailRenderType.BREATH);
            for(int hand:new int[]{-1,1})for(int i=0;i<NeungsaBreathMotion.SEGMENTS;i++){
                float u=i/(float)NeungsaBreathMotion.SEGMENTS,v=(i+1)/(float)NeungsaBreathMotion.SEGMENTS;
                var a=NeungsaBreathMotion.point(u,hand,age);var b=NeungsaBreathMotion.point(v,hand,age);
                float dx=b.side()-a.side(),dy=b.height()-a.height();float length=(float)Math.sqrt(dx*dx+dy*dy);
                var normal=side.scale(-dy/length).add(up.scale(dx/length));
                var start=side.scale(a.side()*scale).add(up.scale(a.height()*scale)).add(look.scale(a.forward()*scale));
                var end=side.scale(b.side()*scale).add(up.scale(b.height()*scale)).add(look.scale(b.forward()*scale));
                for(int pass=0;pass<3;pass++){
                    float width=(pass==0?2.4f:pass==1?1:.32f)*scale;
                    float wa=NeungsaBreathMotion.width(u)*width,wb=NeungsaBreathMotion.width(v)*width;
                    float alpha=fade*(pass==0?.16f:pass==1?.68f:.92f);
                    float aa=alpha*NeungsaBreathMotion.flow(u,age)*(float)Math.sin(Math.PI*u);
                    float ab=alpha*NeungsaBreathMotion.flow(v,age)*(float)Math.sin(Math.PI*v);
                    float r=pass==2?.94f:.58f,g=pass==2?.78f:.24f;
                    vertex(stack,vertices,start.add(normal.scale(-wa)),0,1,aa,r,g);
                    vertex(stack,vertices,start.add(normal.scale(wa)),0,0,aa,r,g);
                    vertex(stack,vertices,end.add(normal.scale(wb)),1,0,ab,r,g);
                    vertex(stack,vertices,end.add(normal.scale(-wb)),1,1,ab,r,g);
                }
                breathSegments++;breaths++;
            }
            stack.popPose();
        }
    }
    private static void vertex(PoseStack stack,VertexConsumer vertices,Vec3 at,float u,float v,float alpha,float r,float g){
        vertices.addVertex(stack.last().pose(),(float)at.x,(float)at.y,(float)at.z).setColor(r,g,1,alpha)
            .setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(stack.last(),0,0,1);
    }
    public static void accept(NeungsaIaidoVisualPayload p){
        var old=ACTIVE.get(p.player());
        if(p.remaining()==0||p.phase()==0){
            // Finish the cut on the server after one second, while the drawn
            // blade remains visible for its separate two-second follow-through.
            // Early cancellation still removes the presentation immediately.
            if(old!=null&&old.full&&old.phase==NeungsaIaidoRules.SLASHING&&p.full()&&p.age()>=NeungsaIaidoRules.DURATION)return;
            remove(p.player());return;
        }
        if(old==null||old.phase!=p.phase()||old.full!=p.full()||p.age()<old.age-3)ACTIVE.put(p.player(),new State(p));
        else if(!old.full||old.phase!=NeungsaIaidoRules.SLASHING)old.remaining=p.remaining();
    }
    @SuppressWarnings("unchecked") private static boolean animate(AbstractClientPlayer p,State s){
        var raw=PlayerAnimationAccess.getPlayerAssociatedData(p).get(LAYER);if(!(raw instanceof ModifierLayer<?> layer))return false;
        String name="neungsa_katana_iaido";
        if(s.phase==NeungsaIaidoRules.CHARGING||s.full)name+="_"+(s.phase==NeungsaIaidoRules.CHARGING?"charge":"dash")+(p.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM?"_slim":"");
        var clip=PlayerAnimationRegistry.getAnimation(KillerRankingMod.id(name));if(clip==null)return false;
        NeungsaDrawController.iaido(p);
        var k=(KeyframeAnimationPlayer)clip.playAnimation();
        for(int i=0;i<s.age*32;i++)k.tick();
        k.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);k.setFirstPersonConfiguration(new FirstPersonConfiguration(true,true,true,false));
        ((ModifierLayer<IAnimation>)layer).setAnimation(k);return true;
    }
    public static boolean active(UUID id){return ACTIVE.containsKey(id);}
    public static boolean blocksAttack(UUID id){var s=ACTIVE.get(id);return s!=null&&(!s.full||s.phase!=NeungsaIaidoRules.SLASHING||s.age<NeungsaIaidoRules.DURATION);}
    public static boolean interruptRecovery(UUID id){
        var s=ACTIVE.get(id);
        if(s==null||!s.full||s.phase!=NeungsaIaidoRules.SLASHING||s.age<NeungsaIaidoRules.DURATION)return false;
        remove(id);return true;
    }
    public static int phase(UUID id){var s=ACTIVE.get(id);return s==null?0:s.phase;}
    public static boolean full(UUID id){var s=ACTIVE.get(id);return s!=null&&s.full;}
    public static int breaths(){return breaths;}
    public static int breathSegments(){return breathSegments;}
    private static void remove(UUID id){
        ACTIVE.remove(id);var c=Minecraft.getInstance();if(c.level==null)return;
        var p=c.level.getPlayerByUUID(id);if(p instanceof AbstractClientPlayer player){
            var layer=PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER);if(layer instanceof ModifierLayer<?> l)l.setAnimation(null);
        }
    }
    public static void clear(){for(var id:new ArrayList<>(ACTIVE.keySet()))remove(id);breaths=0;breathSegments=0;NeungsaCutParticle.clear();}
    private NeungsaIaidoVfx(){ }
}
