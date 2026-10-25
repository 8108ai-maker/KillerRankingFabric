package dev.killerranking.client;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.RookRules;
import dev.killerranking.network.RookVisualPayload;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import dev.kosmx.playerAnim.minecraftApi.layers.LeftHandedHelperModifier;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;

public final class RookVfx {
    private static final net.minecraft.resources.ResourceLocation LAYER=KillerRankingMod.id("rook_layer");
    private static final Map<UUID,State> STATES=new HashMap<>();
    private static final class State {
        UUID victim;int phase,age,remaining,victimId=-1;float width=.6f;boolean animated,victimAnimated;
        State(RookVisualPayload p){victim=p.victim();phase=p.phase();age=p.age();remaining=p.remaining();}
    }
    public static void register(){PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((p,stack)->{
        var layer=new ModifierLayer<IAnimation>();layer.addModifierLast(new LeftHandedHelperModifier(p));stack.addAnimLayer(3300,layer);PlayerAnimationAccess.getPlayerAssociatedData(p).set(LAYER,layer);
    });net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AFTER_ENTITIES.register(RookMountScene::render);}
    public static void accept(RookVisualPayload p){
        var old=STATES.get(p.owner());
        if(p.remaining()<=0||p.phase()==0){remove(p.owner());return;}
        if(old==null||old.phase!=p.phase()) {remove(p.owner());STATES.put(p.owner(),new State(p));}
        else {if(!Objects.equals(old.victim,p.victim())){animate(old.victim,null,0);old.victimAnimated=false;old.victimId=-1;}old.victim=p.victim();old.age=p.age();old.remaining=p.remaining();}
    }
    public static boolean active(UUID id){return STATES.containsKey(id);}
    public static boolean mounting(UUID owner){var s=STATES.get(owner);return s!=null&&s.phase==RookRules.MOUNT;}
    public static net.minecraft.world.entity.LivingEntity mountVictim(UUID owner) {
        var s=STATES.get(owner);var c=Minecraft.getInstance();
        if(s==null||s.phase!=RookRules.MOUNT||s.victim==null||c.level==null)return null;
        var victim=c.level.getEntity(s.victimId);
        if(victim==null||!s.victim.equals(victim.getUUID()))for(var e:c.level.entitiesForRendering())if(s.victim.equals(e.getUUID())){s.victimId=e.getId();victim=e;break;}
        return victim instanceof net.minecraft.world.entity.LivingEntity living&&living.isAlive()&&!living.isRemoved()?living:null;
    }
    public static float mountedWidth(UUID owner) {
        var s=STATES.get(owner);return s==null?.6f:s.width;
    }
    public static boolean captured(UUID id){return WasabariVfx.captured(id)||STATES.values().stream().anyMatch(s->id.equals(s.victim));}
    public static boolean immobilized(net.minecraft.world.entity.Entity entity) {
        if(WasabariVfx.immobilized(entity))return true;
        if(entity instanceof net.minecraft.world.entity.LivingEntity living&&HeavyPunchVfx.locked(living))return true;
        return entity.level().isClientSide&&(active(entity.getUUID())||captured(entity.getUUID())
            ||entity==Minecraft.getInstance().player&&ClientRook.locked());
    }
    public static AbstractClientPlayer mountingOwner(UUID victim) {
        var c=Minecraft.getInstance();if(c.level==null)return null;
        for(var entry:STATES.entrySet())if(entry.getValue().phase==RookRules.MOUNT&&victim.equals(entry.getValue().victim))
            return c.level.getPlayerByUUID(entry.getKey()) instanceof AbstractClientPlayer owner?owner:null;
        return null;
    }
    public static float mountImpact(UUID victim,float partialTick) {
        for(var s:STATES.values())if(s.phase==RookRules.MOUNT&&victim.equals(s.victim)) {
            float age=s.age+partialTick-1;
            for(int i=0;i<RookRules.MOUNT_HITS;i++){
                int strike=RookRules.MOUNT_FIRST_HIT+i*RookRules.MOUNT_HIT_INTERVAL;
                if(age>=strike&&age<strike+3)return 1-(age-strike)/3;
            }
        }
        return 0;
    }
    public static void tick(Minecraft c){
        if(c.isPaused()||c.level==null)return;
        for(var id:new ArrayList<>(STATES.keySet())) {
            var s=STATES.get(id);var p=c.level.getPlayerByUUID(id);
            if(--s.remaining<=0||p==null||!p.isAlive()){remove(id);continue;}
            if(s.victim!=null) {
                if(s.victimId<0)for(var entity:c.level.entitiesForRendering())if(s.victim.equals(entity.getUUID())){s.victimId=entity.getId();break;}
                var victim=c.level.getEntity(s.victimId);if(victim!=null)s.width=victim.getBbWidth();
            }
            if(!s.animated)s.animated=animate(id,switch(s.phase){case RookRules.THROW->"rook_throw";case RookRules.PUNCH->"rook_punch";case RookRules.MOUNT->"rook_mount";default->"rook_hold";},s.age);
            if(!s.victimAnimated)s.victimAnimated=animate(s.victim,"rook_captive",s.age);
            s.age++;
        }
    }
    private static void remove(UUID id){var old=STATES.remove(id);animate(id,null,0);if(old!=null)animate(old.victim,null,0);}
    public static void clear(){for(var id:new ArrayList<>(STATES.keySet()))remove(id);}
    @SuppressWarnings("unchecked") private static boolean animate(UUID id,String name,int age){
        var c=Minecraft.getInstance();if(id==null||c.level==null||!(c.level.getPlayerByUUID(id) instanceof AbstractClientPlayer p))return false;
        var layer=PlayerAnimationAccess.getPlayerAssociatedData(p).get(LAYER);if(!(layer instanceof ModifierLayer<?>))return false;
        IAnimation animation=null;
        if(name!=null){var data=PlayerAnimationRegistry.getAnimation(KillerRankingMod.id(name));if(data==null)return false;animation=(IAnimation)data.playAnimation();
            if(animation instanceof KeyframeAnimationPlayer frames){for(int i=0;i<age;i++)frames.tick();frames.setFirstPersonMode(dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode.THIRD_PERSON_MODEL);frames.setFirstPersonConfiguration(new dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration(true,true,false,false));}}
        ((ModifierLayer<IAnimation>)layer).setAnimation(animation);return true;
    }
    private RookVfx(){}
}
