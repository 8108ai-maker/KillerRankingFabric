package dev.killerranking.client;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.*;
import dev.killerranking.network.CleaveVisualPayload;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.api.firstPerson.*;
import dev.kosmx.playerAnim.minecraftApi.*;
import dev.kosmx.playerAnim.minecraftApi.layers.LeftHandedHelperModifier;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import java.util.*;

/** Full charge silhouettes the actual player and keeps the white fighting-spirit glyph alive. */
public final class CleaveVfx {
    private static final ResourceLocation LAYER=KillerRankingMod.id("cleave_layer");
    private static final RenderType WHITE=RenderType.entityTranslucentEmissive(ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png"));
    private static final Map<UUID,State> ACTIVE=new HashMap<>();
    private static final class State {
        int phase,age,ttl=25;boolean full,animated;float yaw;
        State(CleaveVisualPayload p){update(p);}
        void update(CleaveVisualPayload p){phase=p.phase();age=p.age();full=p.full();yaw=p.yaw();ttl=25;}
    }
    public static void register(){
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((p,stack)->{
            var layer=new ModifierLayer<IAnimation>();layer.addModifierLast(new LeftHandedHelperModifier(p));
            stack.addAnimLayer(2800,layer);PlayerAnimationAccess.getPlayerAssociatedData(p).set(LAYER,layer);
        });
        WorldRenderEvents.AFTER_ENTITIES.register(CleaveVfx::render);
    }
    public static void accept(CleaveVisualPayload p){
        if(p.phase()==0){remove(p.player());return;}
        ACTIVE.compute(p.player(),(id,old)->{
            if(old==null||old.phase!=p.phase()||p.age()<old.age-3){return new State(p);}
            old.update(p);return old;
        });
    }
    public static boolean active(UUID id){return ACTIVE.containsKey(id);}
    public static boolean slashing(UUID id){var s=ACTIVE.get(id);return s!=null&&s.phase==CleaveRules.SLASHING;}
    public static boolean full(UUID id){var s=ACTIVE.get(id);return s!=null&&(s.full||s.phase==CleaveRules.CHARGING&&s.age>=CleaveRules.CHARGE);}
    public static void clear(){for(var id:new ArrayList<>(ACTIVE.keySet()))remove(id);}
    private static void remove(UUID id){
        ACTIVE.remove(id);var c=Minecraft.getInstance();if(c.level==null)return;var p=c.level.getPlayerByUUID(id);
        if(p instanceof AbstractClientPlayer player){var layer=PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER);if(layer instanceof ModifierLayer<?> l)l.setAnimation(null);}
    }
    @SuppressWarnings("unchecked")
    private static boolean animate(AbstractClientPlayer p,State s){
        var layer=PlayerAnimationAccess.getPlayerAssociatedData(p).get(LAYER);if(!(layer instanceof ModifierLayer<?> l))return false;
        String name="pawn6_katana_cleave_"+(s.phase==CleaveRules.CHARGING?"charge":s.full?"full":"dash");
        var data=PlayerAnimationRegistry.getAnimation(KillerRankingMod.id(name));if(data==null)return false;
        var animation=(IAnimation)data.playAnimation();
        if(animation instanceof KeyframeAnimationPlayer k){
            for(int i=0;i<Math.min(s.age,CleaveRules.MAX_HOLD);i++)k.tick();
            k.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);k.setFirstPersonConfiguration(new FirstPersonConfiguration(true,true,true,false));
        }
        ((ModifierLayer<IAnimation>)l).setAnimation(animation);return true;
    }
    public static void tick(Minecraft c){
        if(c.level==null||c.isPaused())return;
        for(var id:new ArrayList<>(ACTIVE.keySet())){
            var s=ACTIVE.get(id);var player=c.level.getPlayerByUUID(id);
            if(--s.ttl<=0||player==null||!player.isAlive()||player.isSpectator()||s.phase==CleaveRules.SLASHING&&s.age>=CleaveRules.CAST){remove(id);continue;}
            if(!s.animated&&player instanceof AbstractClientPlayer p)s.animated=animate(p,s);
            if(full(id)&&!KillingIntentVfx.hiddenForCamera(player)&&c.player!=null&&c.player.distanceToSqr(player)<=1024){
                int count=c.options.particles().get()==ParticleStatus.MINIMAL?3:c.options.particles().get()==ParticleStatus.DECREASED?8:16;
                SpiritVfx.emitWhite(player,count);
            }
            s.age++;
        }
    }
    private static void render(WorldRenderContext context){
        if(context.matrixStack()==null||context.consumers()==null)return;
        var camera=context.camera().getPosition();float delta=context.tickCounter().getGameTimeDeltaPartialTick(false);int rendered=0;
        for(var entry:ACTIVE.entrySet()){
            var player=context.world().getPlayerByUUID(entry.getKey());var s=entry.getValue();
            if(player==null||player.position().distanceToSqr(camera)>1024||KillingIntentVfx.hiddenForCamera(player))continue;
            if(++rendered>4)break;
            if(full(entry.getKey()))SpiritGlyph.render(context,player,delta,1,SpiritBranch.KING_AND_KILL);
            var stack=context.matrixStack();stack.pushPose();
            stack.translate(Mth.lerp(delta,player.xOld,player.getX())-camera.x,Mth.lerp(delta,player.yOld,player.getY())-camera.y,Mth.lerp(delta,player.zOld,player.getZ())-camera.z);
            stack.mulPose(Axis.YP.rotationDegrees(-s.yaw));
            var vertices=context.consumers().getBuffer(WHITE);float time=s.age+delta;
            if(full(entry.getKey())){
                stack.pushPose();stack.translate(0,1.45,-1.12);
                float pulse=.22f+.12f*(float)Math.sin(time*.25);
                for(int i=0;i<28;i++){
                    double angle=i*Math.PI*2/28+.012*Math.sin(time*.07+i);float x=(float)Math.cos(angle),y=(float)Math.sin(angle);
                    float inner=.85f+(i%3)*.12f,outer=2.6f+(i%4)*.19f;
                    ray(stack,vertices,x,y,inner,outer,.05f+(i%3)*.025f,pulse);
                }
                stack.popPose();
            }
            if(s.phase==CleaveRules.SLASHING){
                float strike=s.full?CleaveRules.FULL_HIT:5;
                if(time>=strike-1&&time<=strike+7){
                    float fade=Math.max(0,1-(time-strike)/7),range=s.full?6:2.5f;
                    // The sword descends in the forward vertical plane, never sideways.
                    for(int i=0;i<9;i++){
                        float z=.35f+i*range/9,width=(.055f+i*.004f)*fade;
                        float bottom=.08f,top=2.8f-i*.035f;
                        quad(stack,vertices,new float[]{-width,width,width,-width},new float[]{bottom,bottom,top,top},z,fade*.85f);
                    }
                }
            }
            stack.popPose();
        }
    }
    private static void ray(PoseStack s,VertexConsumer v,float x,float y,float inner,float outer,float width,float a){
        float px=-y*width,py=x*width;
        quad(s,v,new float[]{x*inner+px,x*outer+px*.2f,x*outer-px*.2f,x*inner-px},new float[]{y*inner+py,y*outer+py*.2f,y*outer-py*.2f,y*inner-py},0,a);
    }
    private static void quad(PoseStack s,VertexConsumer v,float[] xs,float[] ys,float z,float a){
        for(int i=0;i<4;i++)v.addVertex(s.last().pose(),xs[i],ys[i],z).setColor(1f,1f,1f,a).setUv(i<2?0:1,i==0||i==3?0:1)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(s.last(),0,0,1);
    }
    private CleaveVfx() { }
}
