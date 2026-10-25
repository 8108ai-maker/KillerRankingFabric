package dev.killerranking.combat;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.data.*;
import dev.killerranking.network.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import org.joml.Vector3f;
import java.util.*;

public final class ShoulderStabService {
    private static final UUID NONE=new UUID(0,0);
    private static final class Action {
        final ServerPlayer owner;final LivingEntity target;final ItemStack weapon;final long started;
        final int phase;final Vec3 origin;final ResourceKey<Level> dimension;final boolean left;
        Vec3 airStart=Vec3.ZERO,airCatch=Vec3.ZERO;
        Vec3 destination;float yaw;boolean hit;
        Action(ServerPlayer p,LivingEntity e,int phase,Vec3 destination){
            owner=p;target=e;this.phase=phase;this.destination=destination;weapon=p.getMainHandItem();
            started=now(p);origin=p.position();dimension=p.level().dimension();left=p.getMainArm()==HumanoidArm.LEFT;
            var d=e==null?p.getLookAngle():e.position().subtract(origin);yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));
        }
    }
    private final Map<UUID,Action> actions=new HashMap<>();
    private final Map<UUID,ShoulderStabStatusPayload> sent=new HashMap<>();
    private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    public static boolean available(ServerPlayer p){return SalmosaSkillService.available(p);}
    private static boolean eligible(ServerPlayer p){return available(p)&&TraitActivation.enabled(p)&&p.isAlive()&&!p.isRemoved()&&!p.isSpectator()
        &&p.getMainHandItem().is(KillerRankingMod.SALMOSA_MID_DAGGER)&&!p.isPassenger()&&!p.isUsingItem()&&!KillerRankingMod.ROOK.locked(p);}
    public boolean active(ServerPlayer p){return actions.containsKey(p.getUUID());}
    public boolean casting(ServerPlayer p){var a=actions.get(p.getUUID());return a!=null&&a.phase>=ShoulderStabRules.CAST;}
    public int phase(ServerPlayer p){var a=actions.get(p.getUUID());return a==null?0:a.phase;}
    public int age(ServerPlayer p){var a=actions.get(p.getUUID());return a==null?0:(int)(now(p)-a.started);}
    public void request(ServerPlayer p,ShoulderStabRequestPayload request){switch(request.operation()){
        case ShoulderStabRequestPayload.START->start(p);
        case ShoulderStabRequestPayload.RELEASE->release(p,request.target());
        case ShoulderStabRequestPayload.CANCEL->cancel(p);
        case ShoulderStabRequestPayload.COUNTER->counter(p);
        default->{ }
    }}
    public void start(ServerPlayer p){
        if(!eligible(p)||SkillCombatLock.blocksAttack(p)||ShoulderStabState.get(p).cooldown(now(p))>0)return;
        actions.put(p.getUUID(),new Action(p,null,ShoulderStabRules.AIM,p.position()));broadcast(p);sync(p);
    }
    public void release(ServerPlayer p,int targetId){
        var aim=actions.get(p.getUUID());if(aim==null||aim.phase!=ShoulderStabRules.AIM)return;
        var target=ShoulderStabTargeting.selected(p);
        if(!valid(aim)||target==null||target.getId()!=targetId||ShoulderStabState.get(p).cooldown(now(p))>0){cancel(p);return;}
        var destination=landing(p,target);
        if(destination==null||!clearSegment(p,p.position(),destination)){cancel(p);p.displayClientMessage(Component.literal("§7어깨 찌르기: 접근 경로에 장애물이 있습니다."),true);return;}
        actions.put(p.getUUID(),new Action(p,target,ShoulderStabRules.CAST,destination));
        ShoulderStabState.set(p,ShoulderStabState.get(p).cast(now(p)));broadcast(p);sync(p);
    }
    public void counter(ServerPlayer p){
        if(!eligible(p)||active(p)||ShoulderStabState.get(p).cooldown(now(p))>0)return;
        var opportunity=KillerRankingMod.KNIFE_PURSUIT.counter(p);if(opportunity==null)return;
        var destination=landing(p,opportunity.target());if(destination==null)return;
        Vec3 last=p.position();
        for(int tick=1;tick<=ShoulderStabRules.COUNTER_IMPACT;tick++){
            var next=ShoulderStabRules.counterPosition(p.position(),destination,opportunity.catchKnife(),tick);
            if(!clearSegment(p,last,next)){p.displayClientMessage(Component.literal("§7공중 내려찍기: 도약 경로가 막혔습니다."),true);return;}last=next;
        }
        var action=new Action(p,opportunity.target(),ShoulderStabRules.COUNTER,destination);
        action.airStart=opportunity.knife();action.airCatch=opportunity.catchKnife();
        KillerRankingMod.KNIFE_PURSUIT.cancel(p);actions.put(p.getUUID(),action);
        ShoulderStabState.set(p,ShoulderStabState.get(p).cast(now(p)));
        sound(action,KillerRankingMod.KNIFE_PURSUIT_BLINK,.8f,1.35f);broadcast(p);sync(p);
    }
    private static boolean valid(Action a){return eligible(a.owner)&&a.weapon==a.owner.getMainHandItem()&&a.dimension.equals(a.owner.level().dimension())
        &&(a.phase==ShoulderStabRules.AIM?now(a.owner)-a.started<ShoulderStabRules.MAX_HOLD:
            a.target.level()==a.owner.level()&&ShoulderStabTargeting.enemy(a.owner,a.target)&&a.owner.distanceToSqr(a.target)<16*16);}
    public void refresh(ServerPlayer p){var a=actions.get(p.getUUID());if(a!=null&&!valid(a))cancel(p);}
    public void tick(MinecraftServer server){
        for(var a:new ArrayList<>(actions.values())){
            var p=a.owner;if(!valid(a)){cancel(p);continue;}int age=age(p);
            if(a.phase>=ShoulderStabRules.CAST){
                boolean counter=a.phase==ShoulderStabRules.COUNTER;
                if(age>=(counter?ShoulderStabRules.COUNTER_DURATION:ShoulderStabRules.DURATION)){cancel(p);continue;}
                if(counter){
                    var next=ShoulderStabRules.counterPosition(a.origin,a.destination,a.airCatch,age);
                    if(!clearSegment(p,p.position(),next)){cancel(p);continue;}
                    move(p,next);
                    if(age==ShoulderStabRules.COUNTER_CATCH){sound(a,dev.killerranking.KillerRankingSounds.SHOULDER_STAB_CATCH,.9f,1f);broadcast(p);}
                    if(age==ShoulderStabRules.COUNTER_DROP)sound(a,dev.killerranking.KillerRankingSounds.AIR_COUNTER_DROP,1f,1f);
                }else if(age==ShoulderStabRules.BLINK){
                    var destination=landing(p,a.target);
                    if(destination==null||!clearSegment(p,a.origin,destination)||!p.hasLineOfSight(a.target)){cancel(p);continue;}
                    a.destination=destination;sound(a,KillerRankingMod.KNIFE_PURSUIT_BLINK,.9f,1.15f);smoke(p,a.origin);
                    var facing=a.target.position().subtract(destination);a.yaw=(float)Math.toDegrees(Math.atan2(-facing.x,facing.z));
                    move(p,destination);smoke(p,destination);sound(a,KillerRankingMod.KNIFE_PURSUIT_BLINK,.55f,1.35f);broadcast(p);
                }else move(p,ShoulderStabRules.position(a.origin,a.destination,age));
                if(age==(counter?ShoulderStabRules.COUNTER_IMPACT:ShoulderStabRules.IMPACT)&&!a.hit){
                    a.hit=true;
                    if(p.distanceToSqr(a.target)>6.25||!p.hasLineOfSight(a.target)){cancel(p);continue;}
                    if(RushDamage.hit(p,a.target,ShoulderStabRules.DAMAGE,false)){
                        if(actions.get(p.getUUID())!=a)continue;
                        a.weapon.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);var at=wound(a);
                        p.serverLevel().sendParticles(new DustParticleOptions(new Vector3f(.85f,.025f,.055f),1.1f),at.x,at.y,at.z,14,.13,.15,.13,.03);
                        sound(a,dev.killerranking.KillerRankingSounds.SHOULDER_STAB_STAB,1f,1f);
                    }
                    broadcast(p);
                }
            }
            if(server.getTickCount()%2==0&&actions.get(p.getUUID())==a)broadcast(p);
        }
        if(server.getTickCount()%2==0)for(var p:server.getPlayerList().getPlayers())sync(p);
    }
    private static Vec3 wound(Action a){return a.target==null?a.origin:ShoulderStabRules.shoulder(a.target.position(),a.target.getBbWidth(),a.target.getBbHeight(),a.yaw,a.left);}
    private static Vec3 landing(ServerPlayer p,LivingEntity target){
        var forward=target.position().subtract(p.position()).multiply(1,0,1).normalize();if(forward.lengthSqr()<.1)forward=Vec3.directionFromRotation(0,p.getYRot());
        var side=new Vec3(forward.z,0,-forward.x);double gap=(p.getBbWidth()+target.getBbWidth())*.5+.45;
        var point=target.position().subtract(forward.scale(gap)).add(side.scale(p.getMainArm()==HumanoidArm.LEFT?.18:-.18));
        if(!p.serverLevel().hasChunkAt(BlockPos.containing(point)))return null;
        var hit=p.serverLevel().clip(new ClipContext(point.add(0,1.5,0),point.add(0,-3,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        if(hit.getType()!=HitResult.Type.BLOCK||hit.getDirection()!=Direction.UP)return null;
        var at=new Vec3(point.x,hit.getLocation().y,point.z);
        return p.serverLevel().getFluidState(BlockPos.containing(at.add(0,.01,0))).isEmpty()&&clearBox(p,at)?at:null;
    }
    private static boolean clearBox(ServerPlayer p,Vec3 at){return !p.serverLevel().getBlockCollisions(p,p.getBoundingBox().move(at.subtract(p.position())).deflate(.001)).iterator().hasNext();}
    private static boolean clearSegment(ServerPlayer p,Vec3 from,Vec3 to){int count=Math.max(1,(int)Math.ceil(from.distanceTo(to)/.15));for(int i=1;i<=count;i++)if(!clearBox(p,from.lerp(to,(double)i/count)))return false;return true;}
    private static void move(ServerPlayer p,Vec3 at){p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;if(p.position().distanceToSqr(at)>.00001)p.teleportTo(p.serverLevel(),at.x,at.y,at.z,Set.of(),p.getYRot(),p.getXRot());}
    private static void smoke(ServerPlayer p,Vec3 at){p.serverLevel().sendParticles(ParticleTypes.POOF,at.x,at.y+.95,at.z,14,.18,.5,.18,.04);}
    private static void sound(Action a,SoundEvent event,float volume,float pitch){a.owner.serverLevel().playSound(null,a.owner.blockPosition(),event,SoundSource.PLAYERS,volume,pitch);}
    public void cancel(ServerPlayer p){var a=actions.remove(p.getUUID());if(a!=null){p.fallDistance=0;send(a,true);sync(p);}}
    public void forget(Entity e){for(var a:new ArrayList<>(actions.values()))if(a.owner==e||a.target==e)cancel(a.owner);sent.remove(e.getUUID());}
    public void clear(){for(var a:new ArrayList<>(actions.values()))cancel(a.owner);actions.clear();sent.clear();}
    public void sync(ServerPlayer p){var a=actions.get(p.getUUID());var status=new ShoulderStabStatusPayload(available(p),a==null?0:a.phase,a==null?0:age(p),ShoulderStabState.get(p).cooldown(now(p)));
        if(!status.equals(sent.get(p.getUUID()))&&ServerPlayNetworking.canSend(p,ShoulderStabStatusPayload.TYPE)){ServerPlayNetworking.send(p,status);sent.put(p.getUUID(),status);}}
    private void broadcast(ServerPlayer p){var a=actions.get(p.getUUID());if(a!=null)send(a,false);}
    private void send(Action a,boolean ending){var viewers=new HashSet<>(PlayerLookup.tracking(a.owner));viewers.add(a.owner);if(a.target!=null){viewers.addAll(PlayerLookup.tracking(a.target));if(a.target instanceof ServerPlayer p)viewers.add(p);}for(var viewer:viewers)send(a,viewer,ending);}
    public void send(ServerPlayer p,ServerPlayer viewer){var a=actions.get(p.getUUID());if(a!=null)send(a,viewer,false);}
    private void send(Action a,ServerPlayer viewer,boolean ending){if(ServerPlayNetworking.canSend(viewer,ShoulderStabVisualPayload.TYPE))ServerPlayNetworking.send(viewer,new ShoulderStabVisualPayload(a.owner.getUUID(),a.target==null?NONE:a.target.getUUID(),ending?0:a.phase,(int)Math.clamp(now(a.owner)-a.started,0,ShoulderStabRules.MAX_HOLD),a.yaw,a.origin,wound(a),a.destination,a.airStart,a.airCatch));}
}
