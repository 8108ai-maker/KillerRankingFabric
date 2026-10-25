package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import java.util.*;

public final class RushVfx {
    private static final ResourceLocation LAYER=KillerRankingMod.id("rush_layer"), ANIMATION=KillerRankingMod.id("rush");
    private static final Map<UUID,State> ACTIVE=new HashMap<>();
    private static final class State {
        int remaining,age; boolean animated;
        State(int remaining,int age) { this.remaining=remaining;this.age=age; }
    }
    public static void register() {
        RushModel.register();
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((p,stack)-> {
            var layer=new ModifierLayer<IAnimation>();stack.addAnimLayer(2500,layer);
            PlayerAnimationAccess.getPlayerAssociatedData(p).set(LAYER,layer);
        });
        WorldRenderEvents.AFTER_ENTITIES.register(context-> {
            var client=Minecraft.getInstance();
            if(client.level==null||client.getCameraEntity()==null||context.matrixStack()==null||context.consumers()==null) return;
            float delta=context.tickCounter().getGameTimeDeltaPartialTick(false);
            var players=ACTIVE.keySet().stream().map(client.level::getPlayerByUUID).filter(Objects::nonNull)
                .filter(p->p.isAlive()&&!p.isInvisible()&&!p.isSpectator()&&p.distanceToSqr(client.getCameraEntity())<=1024)
                .sorted(Comparator.comparingDouble(p->p.distanceToSqr(client.getCameraEntity()))).limit(4).toList();
            for(var p:players) {
                var state=ACTIVE.get(p.getUUID());
                float fade=Math.min(1,(state.age+delta)/2)*Math.min(1,(state.remaining-delta)/3);
                if(p instanceof AbstractClientPlayer skinPlayer)RushModel.render(context,skinPlayer,Math.max(0,fade));
            }
        });
    }
    /** Cosmetic state only. Gameplay duration and hit cadence come from the server. */
    public static void accept(UUID id,int remaining,int age) {
        if(remaining<=0) { remove(id);return; }
        ACTIVE.compute(id,(key,old)-> {
            if(old==null) return new State(Math.min(remaining,1200),Math.max(0,age));
            old.remaining=Math.min(remaining,1200);old.age=Math.max(0,age);return old;
        });
    }
    public static boolean active(UUID id) { return ACTIVE.containsKey(id); }
    public static void tick(Minecraft client) {
        if(client.isPaused()||client.level==null) return;
        for(var id:new ArrayList<>(ACTIVE.keySet())) {
            var state=ACTIVE.get(id);var p=client.level.getPlayerByUUID(id);
            if(--state.remaining<=0||p==null||!p.isAlive()||p.isSpectator()||p.isInvisible()) { remove(id);continue; }
            if(!state.animated) state.animated=animate(p,state.age);
            if(p instanceof AbstractClientPlayer skinPlayer)RushModel.start(skinPlayer,state.age);
            RushModel.tick(id);
            state.age++;
        }
    }
    static KeyframeAnimationPlayer motion(AbstractClientPlayer player){
        var layer=PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER);
        return layer instanceof ModifierLayer<?> modifiers&&modifiers.getAnimation() instanceof KeyframeAnimationPlayer keyframes?keyframes:null;
    }
    private static void remove(UUID id) {
        RushModel.forget(id);
        ACTIVE.remove(id);var client=Minecraft.getInstance();
        if(client.level!=null) { var p=client.level.getPlayerByUUID(id);if(p!=null) animate(p,-1); }
    }
    public static void clear() { for(var id:new ArrayList<>(ACTIVE.keySet())) remove(id);RushModel.clear(); }
    public static float echoPositionError(UUID player){return RushModel.positionError(player);}
    public static float echoPoseDifference(UUID player){return RushModel.poseDifference(player);}
    public static long renderedEchoes(){return RushModel.renderedEchoes();}
    public static long renderedUpperBodies(){return RushModel.renderedUpperBodies();}
    public static int upperBodyEchoCount(UUID player){return RushModel.upperBodyEchoCount(player);}
    @SuppressWarnings("unchecked")
    private static boolean animate(net.minecraft.world.entity.player.Player entity,int age) {
        if (!(entity instanceof AbstractClientPlayer player)) return false;
        var layer=PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER);
        if(!(layer instanceof ModifierLayer<?>)) return false;
        IAnimation animation=null;
        if(age>=0) {
            RushModel.start(player,age);
            var data=PlayerAnimationRegistry.getAnimation(ANIMATION);
            if(data==null) return false;
            animation=(IAnimation)data.playAnimation();
            if(animation instanceof KeyframeAnimationPlayer keyframes) {
                for(int tick=0;tick<age;tick++) keyframes.tick();
                keyframes.setFirstPersonMode(dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode.THIRD_PERSON_MODEL);
                keyframes.setFirstPersonConfiguration(new dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration(true,true,false,false));
            }
        }
        ((ModifierLayer<IAnimation>)layer).setAnimation(animation);return true;
    }
    private RushVfx() { }
}
