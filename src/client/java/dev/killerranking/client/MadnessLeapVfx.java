package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.MadnessLeapRules;
import dev.killerranking.network.MadnessLeapVisualPayload;
import dev.kosmx.playerAnim.api.firstPerson.*;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.minecraftApi.*;
import dev.kosmx.playerAnim.minecraftApi.layers.LeftHandedHelperModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import java.util.*;

/** Three upright aerial turns followed by an axe strike. Targeting never locks the model or camera. */
public final class MadnessLeapVfx {
    private static final ResourceLocation LAYER=KillerRankingMod.id("madness_leap_layer"),ANIMATION=KillerRankingMod.id("madness_leap");
    private static final Map<UUID,State> ACTIVE=new HashMap<>();
    private static final class State {
        int phase,age,ttl=25;float yaw;boolean animated;
        State(MadnessLeapVisualPayload p){update(p);}
        void update(MadnessLeapVisualPayload p){phase=p.phase();age=p.age();yaw=p.yaw();ttl=25;}
    }
    public static void register(){
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((p,stack)->{
            var layer=new ModifierLayer<IAnimation>();layer.addModifierLast(new LeftHandedHelperModifier(p));
            stack.addAnimLayer(2850,layer);PlayerAnimationAccess.getPlayerAssociatedData(p).set(LAYER,layer);
        });
    }
    public static void accept(MadnessLeapVisualPayload p){
        if(p.phase()==0){remove(p.player());return;}
        ACTIVE.compute(p.player(),(id,old)->{
            if(old==null||old.phase!=p.phase()||p.age()<old.age-3)return new State(p);
            old.update(p);return old;
        });
    }
    public static boolean active(UUID id){return ACTIVE.containsKey(id);}
    public static boolean casting(UUID id){var s=ACTIVE.get(id);return s!=null&&s.phase==MadnessLeapRules.CAST;}
    public static float yaw(UUID id,float original){var s=ACTIVE.get(id);return s!=null&&s.phase==MadnessLeapRules.CAST?s.yaw:original;}
    public static void clear(){for(var id:new ArrayList<>(ACTIVE.keySet()))remove(id);}
    private static void remove(UUID id){
        ACTIVE.remove(id);var c=Minecraft.getInstance();if(c.level==null)return;var p=c.level.getPlayerByUUID(id);
        if(p instanceof AbstractClientPlayer player){var layer=PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER);if(layer instanceof ModifierLayer<?> l)l.setAnimation(null);}
    }
    @SuppressWarnings("unchecked")
    private static boolean animate(AbstractClientPlayer p,State s){
        var layer=PlayerAnimationAccess.getPlayerAssociatedData(p).get(LAYER);var data=PlayerAnimationRegistry.getAnimation(ANIMATION);
        if(!(layer instanceof ModifierLayer<?> l)||data==null)return false;
        var animation=(IAnimation)data.playAnimation();
        if(animation instanceof KeyframeAnimationPlayer k){
            for(int i=0;i<Math.min(s.age,MadnessLeapRules.DURATION);i++)k.tick();
            k.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);k.setFirstPersonConfiguration(new FirstPersonConfiguration(true,true,true,true));
        }
        ((ModifierLayer<IAnimation>)l).setAnimation(animation);return true;
    }
    public static void tick(Minecraft c){
        if(c.level==null||c.isPaused())return;
        for(var id:new ArrayList<>(ACTIVE.keySet())){
            var s=ACTIVE.get(id);var p=c.level.getPlayerByUUID(id);
            if(--s.ttl<=0||p==null||!p.isAlive()||p.isSpectator()
                ||s.phase==MadnessLeapRules.CAST&&s.age>=MadnessLeapRules.DURATION){remove(id);continue;}
            if(s.phase==MadnessLeapRules.CAST&&!s.animated&&p instanceof AbstractClientPlayer player)s.animated=animate(player,s);
            s.age++;
        }
    }
    private MadnessLeapVfx() { }
}
