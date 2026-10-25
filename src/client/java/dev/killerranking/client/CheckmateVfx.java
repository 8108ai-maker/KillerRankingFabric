package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.CheckmateBoard;
import dev.killerranking.combat.CheckmateRules;
import dev.killerranking.network.CheckmateVisualPayload;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import dev.kosmx.playerAnim.minecraftApi.layers.LeftHandedHelperModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class CheckmateVfx {
    private static final ResourceLocation LAYER=KillerRankingMod.id("checkmate_layer"),ANIMATION=KillerRankingMod.id("checkmate");
    private static final Map<UUID,State> ACTIVE=new HashMap<>();
    private static final RenderType FLOOR=RenderType.entityTranslucentEmissive(ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png"));
    private static final class State {
        int remaining,age;boolean animated;final boolean aiming;
        final Vec3 origin;final List<Vec3> tiles;int selected;
        State(CheckmateVisualPayload p) { remaining=p.remaining();age=p.age();origin=p.origin();tiles=p.tiles();selected=p.selected();aiming=p.aiming(); }
    }
    public static void register() {
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((p,stack)-> {
            var layer=new ModifierLayer<IAnimation>();layer.addModifierLast(new LeftHandedHelperModifier(p));
            stack.addAnimLayer(2600,layer);PlayerAnimationAccess.getPlayerAssociatedData(p).set(LAYER,layer);
        });
        WorldRenderEvents.AFTER_ENTITIES.register(CheckmateVfx::render);
    }
    public static void accept(CheckmateVisualPayload payload) {
        if(payload.remaining()<=0) { remove(payload.player());return; }
        ACTIVE.compute(payload.player(),(key,old)-> {
            if(old==null||old.aiming!=payload.aiming()||payload.age()<old.age-2) {
                if(old!=null&&old.animated) {
                    var client=Minecraft.getInstance();var p=client.level==null?null:client.level.getPlayerByUUID(key);
                    if(p!=null) animate(p,-1);
                }
                return new State(payload);
            }
            old.remaining=payload.remaining();old.age=payload.age();old.selected=payload.selected();return old;
        });
    }
    public static boolean active(UUID id) { return ACTIVE.containsKey(id); }
    public static boolean aiming(UUID id) { var s=ACTIVE.get(id);return s!=null&&s.aiming; }
    public static int selectedTile() {
        var client=Minecraft.getInstance();if(client.player==null||client.level==null) return -1;
        var state=ACTIVE.get(client.player.getUUID());
        return state==null||!state.aiming?-1:select(client.player,state,1);
    }
    private static int select(net.minecraft.world.entity.player.Player p,State state,float delta) {
        var eye=p.getEyePosition(delta);var look=p.getViewVector(delta);
        int selected=CheckmateBoard.select(eye,look,state.tiles);
        if(selected<0) return -1;
        var tile=state.tiles.get(selected);double distance=(tile.y+.025-eye.y)/look.y;
        var point=eye.add(look.scale(distance));
        var hit=p.level().clip(new net.minecraft.world.level.ClipContext(eye,point,
            net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
        return hit.getType()==net.minecraft.world.phys.HitResult.Type.MISS||hit.getLocation().distanceToSqr(point)<.01?selected:-1;
    }
    public static void tick(Minecraft client) {
        if(client.isPaused()||client.level==null) return;
        for(var id:new ArrayList<>(ACTIVE.keySet())) {
            var state=ACTIVE.get(id);var p=client.level.getPlayerByUUID(id);
            if(--state.remaining<=0||p==null||!p.isAlive()||p.isSpectator()) { remove(id);continue; }
            if(!state.aiming&&!state.animated) state.animated=animate(p,state.age);
            state.age++;
        }
    }
    private static void remove(UUID id) {
        ACTIVE.remove(id);var client=Minecraft.getInstance();
        if(client.level!=null) { var p=client.level.getPlayerByUUID(id);if(p!=null) animate(p,-1); }
    }
    public static void clear() { for(var id:new ArrayList<>(ACTIVE.keySet())) remove(id); }
    public static int platformCount() { return ACTIVE.values().stream().mapToInt(s->s.tiles.size()).sum(); }
    private static void render(WorldRenderContext context) {
        if(context.matrixStack()==null||context.consumers()==null) return;
        var camera=context.camera().getPosition();var stack=context.matrixStack();
        VertexConsumer vertices=null;
        float delta=context.tickCounter().getGameTimeDeltaPartialTick(false);
        for(var entry:ACTIVE.entrySet()) {
            var p=context.world().getPlayerByUUID(entry.getKey());var state=entry.getValue();
            if(p==null||p.isInvisible()||p.isSpectator()||state.origin.distanceToSqr(camera)>1024) continue;
            if(vertices==null) vertices=context.consumers().getBuffer(FLOOR);
            float fade=state.aiming?1:(float)Math.clamp((CheckmateRules.HIT+8-state.age-delta)/8,0,1);
            if(fade<=0) continue;
            int selected=state.aiming&&p==Minecraft.getInstance().player?select(p,state,delta):state.selected;
            for(int i=0;i<state.tiles.size();i++) tile(stack,vertices,state.tiles.get(i).subtract(camera),i==selected,fade);
            if(selected>=0&&selected<state.tiles.size()) {
                var end=state.tiles.get(selected);var corner=CheckmateBoard.corner(state.origin,end);
                guide(stack,vertices,state.origin.subtract(camera),corner.subtract(camera),fade);
                guide(stack,vertices,corner.subtract(camera),end.subtract(camera),fade);
            }
            tile(stack,vertices,state.origin.subtract(camera),false,fade*.65f);
        }
    }
    private static void tile(PoseStack stack,VertexConsumer v,Vec3 c,boolean selected,float fade) {
        double h=CheckmateBoard.HALF,y=c.y+.025;
        for(int x=0;x<2;x++) for(int z=0;z<2;z++) {
            float shade=(x+z)%2==0?1f:.06f;
            rect(stack,v,c.x-h+x*h,y,c.z-h+z*h,c.x-h+(x+1)*h,c.z-h+(z+1)*h,shade,fade*(selected?.8f:.48f));
        }
        double w=selected?.065:.03;float alpha=fade*(selected?1:.65f);
        rect(stack,v,c.x-h,y+.002,c.z-h,c.x+h,c.z-h+w,1,alpha);
        rect(stack,v,c.x-h,y+.002,c.z+h-w,c.x+h,c.z+h,1,alpha);
        rect(stack,v,c.x-h,y+.002,c.z-h,c.x-h+w,c.z+h,1,alpha);
        rect(stack,v,c.x+h-w,y+.002,c.z-h,c.x+h,c.z+h,1,alpha);
    }
    private static void guide(PoseStack stack,VertexConsumer v,Vec3 a,Vec3 b,float fade) {
        double y=Math.min(a.y,b.y)+.035,w=.055;
        rect(stack,v,Math.min(a.x,b.x)-w,y,Math.min(a.z,b.z)-w,Math.max(a.x,b.x)+w,Math.max(a.z,b.z)+w,1,fade*.8f);
    }
    private static void rect(PoseStack stack,VertexConsumer v,double x1,double y,double z1,double x2,double z2,float shade,float alpha) {
        var pose=stack.last();
        for(int i=0;i<4;i++) v.addVertex(pose.pose(),(float)(i<2?x1:x2),(float)y,(float)(i==0||i==3?z1:z2))
            .setColor(shade,shade,shade,alpha).setUv(i<2?0:1,i==0||i==3?0:1)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose,0,1,0);
    }
    @SuppressWarnings("unchecked")
    private static boolean animate(net.minecraft.world.entity.player.Player entity,int age) {
        if(!(entity instanceof AbstractClientPlayer player)) return false;
        var layer=PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER);
        if(!(layer instanceof ModifierLayer<?>)) return false;
        IAnimation animation=null;
        if(age>=0) {
            var data=PlayerAnimationRegistry.getAnimation(ANIMATION);if(data==null) return false;
            animation=(IAnimation)data.playAnimation();
            if(animation instanceof KeyframeAnimationPlayer frames) {
                for(int t=0;t<age;t++) frames.tick();
                frames.setFirstPersonMode(dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode.THIRD_PERSON_MODEL);
                frames.setFirstPersonConfiguration(new dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration(true,true,true,true));
            }
        }
        ((ModifierLayer<IAnimation>)layer).setAnimation(animation);return true;
    }
    private CheckmateVfx() { }
}
