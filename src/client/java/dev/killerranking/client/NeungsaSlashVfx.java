package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.NeungsaSlashRules;
import dev.killerranking.network.NeungsaSlashVisualPayload;
import dev.kosmx.playerAnim.api.firstPerson.*;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.minecraftApi.*;
import dev.kosmx.playerAnim.minecraftApi.layers.LeftHandedHelperModifier;
import java.util.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

/** Fast single cut with the established rigid grip. The ranged attack itself stays invisible. */
public final class NeungsaSlashVfx {
    // The authored katana curves use 32 samples per combat tick.
    private static final int CURVE_SPEED=96;
    private static final ResourceLocation LAYER=KillerRankingMod.id("neungsa_slash_layer");
    private static final Map<UUID,State> ACTIVE=new HashMap<>();
    private static final class State {int age,remaining;boolean animated;State(NeungsaSlashVisualPayload p){age=p.age();remaining=p.remaining();}}
    public static void register(){
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((p,stack)->{
            var layer=new ModifierLayer<IAnimation>();layer.addModifierLast(new SpeedModifier(CURVE_SPEED));
            layer.addModifierLast(new LeftHandedHelperModifier(p));stack.addAnimLayer(2900,layer);
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
    public static void accept(NeungsaSlashVisualPayload p){
        if(p.remaining()==0){remove(p.player());return;}
        var old=ACTIVE.get(p.player());
        if(old==null||p.age()<old.age-3)ACTIVE.put(p.player(),new State(p));
        else old.remaining=p.remaining();
    }
    @SuppressWarnings("unchecked")
    private static boolean animate(AbstractClientPlayer p,State s){
        var raw=PlayerAnimationAccess.getPlayerAssociatedData(p).get(LAYER);if(!(raw instanceof ModifierLayer<?> layer))return false;
        String name=NeungsaDrawController.attack(p,"killerranking:neungsa_katana_slash_right",NeungsaSlashRules.DURATION);
        var clip=PlayerAnimationRegistry.getAnimation(ResourceLocation.parse(name));if(clip==null)return false;
        var animation=(IAnimation)clip.playAnimation();
        if(animation instanceof KeyframeAnimationPlayer k){
            for(int i=0;i<s.age*CURVE_SPEED;i++)k.tick();
            k.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);k.setFirstPersonConfiguration(new FirstPersonConfiguration(true,true,true,false));
        }
        ((ModifierLayer<IAnimation>)layer).setAnimation(animation);return true;
    }
    public static boolean active(UUID id){return ACTIVE.containsKey(id);}
    private static void remove(UUID id){
        ACTIVE.remove(id);var c=Minecraft.getInstance();if(c.level==null)return;
        var p=c.level.getPlayerByUUID(id);if(p instanceof AbstractClientPlayer player){
            var layer=PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER);if(layer instanceof ModifierLayer<?> l)l.setAnimation(null);
        }
    }
    public static void clear(){for(var id:new ArrayList<>(ACTIVE.keySet()))remove(id);}
    private NeungsaSlashVfx(){ }
}
