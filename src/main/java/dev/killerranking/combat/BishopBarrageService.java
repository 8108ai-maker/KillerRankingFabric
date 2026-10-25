package dev.killerranking.combat;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.data.*;
import dev.killerranking.entity.BishopFollower;
import dev.killerranking.network.BishopBarrageVisualPayload;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraft.sounds.*;
import java.util.*;

/** Four brief attackers introduce a fixed, server-owned five-second cutting zone. */
public final class BishopBarrageService {
    private static final class Action {
        final long started;final Vec3 origin,direction;final float yaw;final ResourceKey<Level> dimension;
        final List<UUID> echoes=new ArrayList<>();
        Action(ServerPlayer p){started=BishopService.now(p);origin=p.position();yaw=p.getYRot();dimension=p.level().dimension();double angle=Math.toRadians(yaw);direction=new Vec3(-Math.sin(angle),0,Math.cos(angle));}
    }
    private final Map<UUID,Action> actions=new HashMap<>();
    public boolean active(ServerPlayer p){return actions.containsKey(p.getUUID());}
    public int remaining(ServerPlayer p){var a=actions.get(p.getUUID());return a==null?0:(int)Math.clamp(BishopBarrageRules.DURATION-(BishopService.now(p)-a.started),0,BishopBarrageRules.DURATION);}
    public boolean manages(ServerPlayer p,UUID unit){var a=actions.get(p.getUUID());return a!=null&&a.dimension.equals(p.level().dimension())&&a.echoes.contains(unit);}
    public List<BishopFollower> echoes(ServerPlayer p){var a=actions.get(p.getUUID());return a==null?List.of():a.echoes.stream().map(p.serverLevel()::getEntity).filter(e->e instanceof BishopFollower&&e.isAlive()).map(e->(BishopFollower)e).toList();}
    public void start(ServerPlayer p){
        if(!KillerRankingMod.BISHOP.eligible(p)||active(p)||BishopState.get(p).barrageCooldown(BishopService.now(p))>0
            ||p.isPassenger()||p.isUsingItem()||p.isInWaterOrBubble()||p.isInLava()||SkillCombatLock.blocksAttack(p))return;
        var a=new Action(p);actions.put(p.getUUID(),a);
        var old=BishopState.get(p);BishopState.set(p,new BishopState(old.unlocked(),old.readyAt(),a.started+BishopRules.cooldown(PlayerProfiles.get(p).traitLevel())));
        var side=new Vec3(a.direction.z,0,-a.direction.x);
        for(int i=0;i<4;i++){
            var unit=KillerRankingMod.BISHOP_FOLLOWER.create(p.serverLevel());if(unit==null)continue;
            var at=a.origin.add(side.scale(i<2?1.4:-1.4)).add(a.direction.scale(i%2==0?.65:-.65));
            unit.barrage(p,a.started+BishopBarrageRules.ECHO_TICKS,i,PlayerProfiles.get(p).traitLevel());
            unit.moveTo(at.x,at.y,at.z,a.yaw,0);unit.yBodyRot=unit.yBodyRotO=a.yaw;unit.setYHeadRot(a.yaw);unit.yHeadRotO=a.yaw;
            if(p.serverLevel().addFreshEntity(unit))a.echoes.add(unit.getUUID());
        }
        p.serverLevel().playSound(null,p.blockPosition(),dev.killerranking.KillerRankingSounds.BISHOP_BARRAGE,SoundSource.PLAYERS,1f,1f);
        broadcast(p);KillerRankingMod.BISHOP.sync(p);
    }
    public void tick(MinecraftServer server){for(var p:server.getPlayerList().getPlayers()){
        var a=actions.get(p.getUUID());if(a==null)continue;
        if(!KillerRankingMod.BISHOP.eligible(p)||!a.dimension.equals(p.level().dimension())){cancel(p);continue;}
        int age=(int)Math.min(Integer.MAX_VALUE,BishopService.now(p)-a.started);
        if(age==BishopBarrageRules.ECHO_TICKS){
            p.serverLevel().playSound(null,a.origin.x,a.origin.y+1,a.origin.z,dev.killerranking.KillerRankingSounds.BISHOP_VANISH,SoundSource.PLAYERS,.7f,1.1f);
        }
        if(BishopBarrageRules.contact(age))strike(p,a);
        if(age>=BishopBarrageRules.DURATION){cancel(p);continue;}
        if(age%10==0)broadcast(p);
    }}
    private void strike(ServerPlayer p,Action a){
        var region=new AABB(a.origin.x-7,a.origin.y-.5,a.origin.z-7,a.origin.x+7,a.origin.y+3.5,a.origin.z+7);
        for(var e:p.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,region,e->BishopTargets.allowed(p,e))){
            var center=e.getBoundingBox().getCenter();var to=center.subtract(a.origin);
            double forward=to.dot(a.direction),side=to.x*a.direction.z-to.z*a.direction.x;
            if(!BishopBarrageRules.inZone(forward,side,to.y,e.getBbWidth()*.5))continue;
            var trace=p.serverLevel().clip(new ClipContext(a.origin.add(0,p.getEyeHeight(),0),e.getEyePosition(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
            if(trace.getType()!=HitResult.Type.MISS)continue;
            RushDamage.hit(p,e,BishopBarrageRules.DAMAGE,false);
        }
        int age=(int)(BishopService.now(p)-a.started);
        if(age>BishopBarrageRules.ECHO_TICKS&&age%4==0)p.serverLevel().playSound(null,a.origin.x,a.origin.y+1,a.origin.z,dev.killerranking.KillerRankingSounds.BISHOP_CUT,SoundSource.PLAYERS,.45f,.95f+(age%12)*.03f);
    }
    public void send(ServerPlayer p,ServerPlayer viewer){
        var a=actions.get(p.getUUID());var at=a==null?p.position():a.origin;
        if(ServerPlayNetworking.canSend(viewer,BishopBarrageVisualPayload.TYPE))ServerPlayNetworking.send(viewer,new BishopBarrageVisualPayload(p.getUUID(),remaining(p),at.x,at.y,at.z,a==null?p.getYRot():a.yaw));
    }
    private void broadcast(ServerPlayer p){send(p,p);for(var viewer:PlayerLookup.tracking(p))send(p,viewer);}
    public void cancel(ServerPlayer p){var a=actions.remove(p.getUUID());if(a==null)return;
        var level=p.server.getLevel(a.dimension);if(level!=null)for(var id:a.echoes){var e=level.getEntity(id);if(e!=null)e.discard();}
        broadcast(p);KillerRankingMod.BISHOP.sync(p);
    }
    public void clear(){actions.clear();}
}
