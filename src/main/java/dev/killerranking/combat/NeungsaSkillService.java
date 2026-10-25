package dev.killerranking.combat;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.Trait;
import dev.killerranking.data.*;
import dev.killerranking.network.*;
import java.util.*;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

public final class NeungsaSkillService {
    private record Cast(ServerPlayer owner,long started,Vec3 origin,Vec3 direction,ItemStack weapon,ResourceKey<Level> dimension) { }
    private final Map<UUID,Cast> casts=new HashMap<>();
    private final Map<UUID,Cast> echoes=new HashMap<>();
    private static final class Iaido {
        final ServerPlayer owner;final ItemStack weapon;final ResourceKey<Level> dimension;
        final Set<UUID> hits=new HashSet<>();
        long started;int phase=NeungsaIaidoRules.CHARGING;boolean full,blocked;
        Vec3 origin,direction,feet;double travelled;
        Iaido(ServerPlayer p){owner=p;weapon=p.getMainHandItem();dimension=p.level().dimension();started=now(p);origin=p.getEyePosition().add(0,-.30,0);direction=p.getLookAngle().normalize();feet=p.position();}
    }
    private final Map<UUID,Iaido> iaidos=new HashMap<>();
    private final Map<UUID,NeungsaStatusPayload> sent=new HashMap<>();
    private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    public static boolean branch(ServerPlayer p){return PlayerProfiles.get(p).trait()==Trait.POISON_AURA;}
    public static boolean available(ServerPlayer p){return branch(p)&&NeungsaSkillState.get(p).unlocked();}
    private static boolean eligible(ServerPlayer p){return available(p)&&TraitActivation.enabled(p)&&p.isAlive()&&!p.isSpectator()
        &&!p.isRemoved()&&!p.isPassenger()&&!p.isUsingItem()&&p.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA)
        &&!KillerRankingMod.ROOK.locked(p)&&!KillerRankingMod.WASABARI.locked(p);}
    public void request(ServerPlayer p,NeungsaRequestPayload request){
        switch(request.operation()){case NeungsaRequestPayload.START->start(p);case NeungsaRequestPayload.ECHO->echo(p);case NeungsaRequestPayload.UNLOCK->unlock(p);case NeungsaRequestPayload.IAIDO->chargeIaido(p);case NeungsaRequestPayload.IAIDO_RELEASE->releaseIaido(p);case NeungsaRequestPayload.IAIDO_CANCEL->cancelIaido(p);default->{ }}
    }
    public void unlock(ServerPlayer p){
        if(!branch(p)||!p.isAlive()||p.isSpectator()||SkillCombatLock.blocksAttack(p))return;
        var state=NeungsaSkillState.get(p);if(state.unlocked()){sync(p);return;}
        var profile=PlayerProfiles.get(p);
        if(profile.pointUnits()<KillerPoints.whole(NeungsaSlashRules.COST)){
            dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_DENY,.8f,1f);p.sendSystemMessage(Component.literal("§c능사 해금에는 1,000포인트가 필요합니다."));sync(p);return;
        }
        PlayerProfiles.set(p,profile.addPoints(-NeungsaSlashRules.COST));dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_PURCHASE,1f,1f);NeungsaSkillState.set(p,state.owned());
        KillerRankingMod.assignAndSync(p);
        p.sendSystemMessage(Component.literal("§5능사 해금 완료! 능사 카타나: Z 무형참격 / X 발도술 / C 연속참격"));
    }
    public void start(ServerPlayer p){
        if(!eligible(p)||SkillCombatLock.blocksAttack(p)||NeungsaSkillState.get(p).cooldown(now(p))>0)return;
        var direction=p.getLookAngle().normalize();if(direction.lengthSqr()<.9)return;
        var cast=new Cast(p,now(p),p.getEyePosition().add(0,-.30,0),direction,p.getMainHandItem(),p.level().dimension());
        casts.put(p.getUUID(),cast);NeungsaSkillState.set(p,NeungsaSkillState.get(p).cast(now(p)));
        broadcast(cast,false);sync(p);
        p.serverLevel().playSound(null,p.blockPosition(),dev.killerranking.KillerRankingSounds.NEUNGSA_SLASH_CAST,SoundSource.PLAYERS,1f,1f);
    }
    public boolean active(ServerPlayer p){return casts.containsKey(p.getUUID())||iaidos.containsKey(p.getUUID())||echoes.containsKey(p.getUUID());}
    public void echo(ServerPlayer p){
        if(!eligible(p)||SkillCombatLock.blocksAttack(p)||NeungsaSkillState.get(p).echoCooldown(now(p))>0)return;
        var direction=p.getLookAngle().normalize();if(direction.lengthSqr()<.9)return;
        var c=new Cast(p,now(p),p.getEyePosition().add(0,-.30,0),direction,p.getMainHandItem(),p.level().dimension());
        echoes.put(p.getUUID(),c);NeungsaSkillState.set(p,NeungsaSkillState.get(p).echo(now(p)));
        broadcastEcho(c,false);sync(p);
    }
    public int echoRemaining(ServerPlayer p){var c=echoes.get(p.getUUID());return c==null?0:NeungsaEchoRules.remaining(c.started(),now(p));}
    private void broadcastEcho(Cast c,boolean end){var viewers=new HashSet<>(PlayerLookup.tracking(c.owner()));viewers.add(c.owner());for(var v:viewers)sendEcho(c,v,end);}
    private void sendEcho(Cast c,ServerPlayer viewer,boolean end){
        if(ServerPlayNetworking.canSend(viewer,NeungsaEchoVisualPayload.TYPE))ServerPlayNetworking.send(viewer,new NeungsaEchoVisualPayload(c.owner().getUUID(),(int)Math.clamp(now(c.owner())-c.started(),0,NeungsaEchoRules.DURATION),end?0:echoRemaining(c.owner()),c.origin().x,c.origin().y,c.origin().z,(float)c.direction().x,(float)c.direction().y,(float)c.direction().z));
    }
    private static void strikeEcho(Cast c,int age){
        var p=c.owner();var level=p.serverLevel();var start=c.origin();var end=start.add(c.direction().scale(NeungsaEchoRules.REACH));
        var wall=level.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        double reach=wall.getType()==HitResult.Type.BLOCK?start.distanceTo(wall.getLocation()):NeungsaEchoRules.REACH;
        for(var e:level.getEntitiesOfClass(LivingEntity.class,new AABB(start,end).inflate(NeungsaEchoRules.HALF_WIDTH),e->enemy(p,e))){
            var b=e.getBoundingBox();var entry=NeungsaSlashRules.entry(start,c.direction(),b,reach,NeungsaEchoRules.HALF_WIDTH,NeungsaEchoRules.HALF_HEIGHT);
            if(entry.isEmpty())continue;
            var line=start.add(c.direction().scale(entry.getAsDouble()));
            var hit=new Vec3(Math.clamp(line.x,b.minX,b.maxX),Math.clamp(line.y,b.minY,b.maxY),Math.clamp(line.z,b.minZ,b.maxZ));
            var obstruction=level.clip(new ClipContext(start,hit,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
            if(obstruction.getType()==HitResult.Type.BLOCK&&start.distanceTo(obstruction.getLocation())+.001<start.distanceTo(hit))continue;
            if(!RushDamage.hit(p,e,NeungsaEchoRules.DAMAGE,age==NeungsaEchoRules.FINAL_HIT))continue;
            var viewers=new HashSet<>(PlayerLookup.tracking(e));viewers.add(p);if(e instanceof ServerPlayer target)viewers.add(target);
            for(var v:viewers)if(ServerPlayNetworking.canSend(v,NeungsaCutEffectPayload.TYPE))ServerPlayNetworking.send(v,new NeungsaCutEffectPayload(e.getId(),e.getUUID()));
        }
        boolean big=age==NeungsaEchoRules.OPEN_HIT||age==NeungsaEchoRules.FINAL_HIT;
        level.playSound(null,p.blockPosition(),big?dev.killerranking.KillerRankingSounds.NEUNGSA_BIG_CUT:dev.killerranking.KillerRankingSounds.NEUNGSA_ECHO_CUT,SoundSource.PLAYERS,big?1f:.55f,big?1f:.95f+(age%6)*.04f);
    }
    public void chargeIaido(ServerPlayer p){
        if(!eligible(p)||SkillCombatLock.blocksAttack(p)||NeungsaSkillState.get(p).iaidoCooldown(now(p))>0)return;
        var c=new Iaido(p);iaidos.put(p.getUUID(),c);broadcastIaido(c,false);sync(p);
        p.serverLevel().playSound(null,p.blockPosition(),dev.killerranking.KillerRankingSounds.NEUNGSA_SHEATHE,SoundSource.PLAYERS,.8f,1f);
    }
    /** Immediate short draw remains available to the existing server-side combat regression fixture. */
    public void iaido(ServerPlayer p){chargeIaido(p);releaseIaido(p);}
    public void releaseIaido(ServerPlayer p){
        var c=iaidos.get(p.getUUID());if(c==null||c.phase!=NeungsaIaidoRules.CHARGING)return;
        if(!valid(c)||NeungsaSkillState.get(p).iaidoCooldown(now(p))>0){cancelIaido(p);return;}
        c.full=NeungsaIaidoRules.full(now(p)-c.started);c.phase=NeungsaIaidoRules.SLASHING;c.started=now(p);
        c.origin=p.getEyePosition().add(0,-.30,0);c.feet=p.position();c.direction=p.getLookAngle().normalize();
        NeungsaSkillState.set(p,NeungsaSkillState.get(p).iaido(now(p)));
        broadcastIaido(c,false);sync(p);KillerRankingMod.COMBAT_STATUS.sync(p);
        p.serverLevel().playSound(null,p.blockPosition(),dev.killerranking.KillerRankingSounds.NEUNGSA_IAIDO,SoundSource.PLAYERS,c.full?1.1f:.8f,c.full?1f:1.15f);
    }
    public int iaidoPhase(ServerPlayer p){var c=iaidos.get(p.getUUID());return c==null?0:c.phase;}
    public int iaidoAge(ServerPlayer p){var c=iaidos.get(p.getUUID());return c==null?0:(int)Math.clamp(now(p)-c.started,0,NeungsaIaidoRules.MAX_HOLD);}
    public boolean dashing(ServerPlayer p){var c=iaidos.get(p.getUUID());return c!=null&&c.full&&c.phase==NeungsaIaidoRules.SLASHING&&iaidoAge(p)<=NeungsaIaidoRules.DASH_TICKS;}
    public int iaidoRemaining(ServerPlayer p){var c=iaidos.get(p.getUUID());return c==null||c.phase!=NeungsaIaidoRules.SLASHING||!valid(c)?0:NeungsaIaidoRules.remaining(c.started,now(p));}
    public boolean iaidoActive(ServerPlayer p){return iaidoRemaining(p)>0;}
    public int remaining(ServerPlayer p){var c=casts.get(p.getUUID());return c==null?0:(int)Math.clamp(NeungsaSlashRules.DURATION-(now(p)-c.started()),0,NeungsaSlashRules.DURATION);}
    private static boolean valid(Cast c){return eligible(c.owner())&&c.dimension().equals(c.owner().level().dimension())&&c.weapon()==c.owner().getMainHandItem();}
    private static boolean valid(Iaido c){return eligible(c.owner)&&c.dimension.equals(c.owner.level().dimension())&&c.weapon==c.owner.getMainHandItem();}
    public void refresh(ServerPlayer p){var c=casts.get(p.getUUID());var i=iaidos.get(p.getUUID());var e=echoes.get(p.getUUID());if(c!=null&&!valid(c)||i!=null&&!valid(i)||e!=null&&!valid(e))cancel(p);}
    public void tick(MinecraftServer server){
        for(var c:new ArrayList<>(echoes.values())){
            var p=c.owner();int age=(int)(now(p)-c.started());
            if(!valid(c)||age>=NeungsaEchoRules.DURATION){cancel(p);continue;}
            if(NeungsaEchoRules.hit(age))strikeEcho(c,age);
            if(age%5==0)broadcastEcho(c,false);
        }
        for(var c:new ArrayList<>(iaidos.values())){
            var p=c.owner;int age=iaidoAge(p);
            if(!valid(c)){cancelIaido(p);continue;}
            if(c.phase==NeungsaIaidoRules.CHARGING){
                if(age>=NeungsaIaidoRules.MAX_HOLD){cancelIaido(p);continue;}
                if(age==NeungsaIaidoRules.CHARGE)p.serverLevel().playSound(null,p.blockPosition(),dev.killerranking.KillerRankingSounds.NEUNGSA_CHARGE,SoundSource.PLAYERS,.9f,1f);
            }else{
                if(age>=NeungsaIaidoRules.DURATION){cancelIaido(p);continue;}
                if(c.full&&age>0&&age<=NeungsaIaidoRules.DASH_TICKS){
                    if(!c.blocked){
                        var before=p.position();var step=c.direction.scale(NeungsaIaidoRules.DASH/NeungsaIaidoRules.DASH_TICKS);
                        p.move(MoverType.SELF,step);c.blocked=p.position().subtract(before).dot(c.direction)<step.length()-.03;
                        c.travelled=Math.clamp(p.position().subtract(c.feet).dot(c.direction),0,NeungsaIaidoRules.DASH);
                    }
                    p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;
                    var at=p.position();p.teleportTo(p.serverLevel(),at.x,at.y,at.z,Set.of(RelativeMovement.Y_ROT,RelativeMovement.X_ROT),p.getYRot(),p.getXRot());
                    if(age>=2)strikeIaido(c,Math.min(NeungsaIaidoRules.DASH,c.travelled+.30));
                }else if(!c.full&&age==NeungsaIaidoRules.HIT)strikeIaido(c,NeungsaIaidoRules.REACH);
            }
            if(age%5==0)broadcastIaido(c,false);
        }
        for(var cast:new ArrayList<>(casts.values())){
            var p=cast.owner();if(!valid(cast)){cancel(p);continue;}
            int age=(int)(now(p)-cast.started());
            if(age>=NeungsaSlashRules.DURATION){cancel(p);continue;}
            if(age==NeungsaSlashRules.HIT)strike(cast);
            if(age%5==0)broadcast(cast,false);
        }
        if(server.getTickCount()%2==0)for(var p:server.getPlayerList().getPlayers())sync(p);
    }
    private static boolean enemy(ServerPlayer p,LivingEntity e){return e!=p&&e.isAlive()&&!e.isSpectator()&&e.isAttackable()
        &&!p.isAlliedTo(e)&&p.canAttack(e)&&!(e instanceof ServerPlayer v&&(v.isCreative()||!p.canHarmPlayer(v)));}
    private static void strike(Cast c){
        var p=c.owner();var level=p.serverLevel();var start=c.origin();var end=start.add(c.direction().scale(NeungsaSlashRules.REACH));
        var first=level.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        double limit=first.getType()==HitResult.Type.BLOCK?start.distanceTo(first.getLocation()):NeungsaSlashRules.REACH;
        // Resolve impacts against the original wall before removing its surface. No same-cast damage through that wall.
        var box=new AABB(start,end).inflate(NeungsaSlashRules.HALF_WIDTH);
        for(var e:level.getEntitiesOfClass(LivingEntity.class,box,e->enemy(p,e))){
            var bounds=e.getBoundingBox();var entry=NeungsaSlashRules.entry(start,c.direction(),bounds,limit);
            if(entry.isEmpty())continue;
            var centerline=start.add(c.direction().scale(entry.getAsDouble()));
            var hit=new Vec3(Math.clamp(centerline.x,bounds.minX,bounds.maxX),Math.clamp(centerline.y,bounds.minY,bounds.maxY),Math.clamp(centerline.z,bounds.minZ,bounds.maxZ));
            var obstruction=level.clip(new ClipContext(start,hit,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
            if(obstruction.getType()==HitResult.Type.BLOCK&&start.distanceTo(obstruction.getLocation())+.001<start.distanceTo(hit))continue;
            if(RushDamage.hit(p,e,NeungsaSlashRules.DAMAGE,true)){
                level.sendParticles(ParticleTypes.CRIT,hit.x,hit.y,hit.z,5,.08,.08,.08,.025);
                level.playSound(null,e.blockPosition(),dev.killerranking.KillerRankingSounds.HIT_BLADE,SoundSource.PLAYERS,.8f,1.1f);
            }
        }
        if(first.getType()==HitResult.Type.BLOCK&&p.mayBuild()){
            for(var pos:NeungsaSlashRules.surface(first.getBlockPos(),first.getDirection(),c.direction())){
                var state=level.getBlockState(pos);
                if(state.isAir()||!state.getFluidState().isEmpty()||state.hasBlockEntity()||state.getDestroySpeed(level,pos)<0
                    ||!level.mayInteract(p,pos)||!p.mayUseItemAt(pos,first.getDirection(),c.weapon()))continue;
                // Only exposed cells on the original contact plane; no tucked-away neighbouring cell.
                var facePoint=Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(first.getDirection().getNormal()).scale(.501));
                var visible=level.clip(new ClipContext(start,facePoint,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
                if(visible.getType()==HitResult.Type.BLOCK&&!visible.getBlockPos().equals(pos))continue;
                level.destroyBlock(pos,true,p);
            }
        }
    }
    private static void strikeIaido(Iaido c,double reach){
        var p=c.owner;var level=p.serverLevel();var start=c.origin;var end=start.add(c.direction.scale(reach));
        var wall=level.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        double limit=wall.getType()==HitResult.Type.BLOCK?start.distanceTo(wall.getLocation()):reach;
        double width=c.full?NeungsaIaidoRules.DASH_HALF_WIDTH:NeungsaIaidoRules.HALF_WIDTH;
        for(var e:level.getEntitiesOfClass(LivingEntity.class,new AABB(start,end).inflate(Math.max(width,NeungsaIaidoRules.HALF_HEIGHT)),e->enemy(p,e)&&!c.hits.contains(e.getUUID()))){
            var b=e.getBoundingBox();var d=NeungsaSlashRules.entry(start,c.direction,b,limit,width,NeungsaIaidoRules.HALF_HEIGHT);
            if(d.isEmpty())continue;
            var line=start.add(c.direction.scale(d.getAsDouble()));
            var hit=new Vec3(Math.clamp(line.x,b.minX,b.maxX),Math.clamp(line.y,b.minY,b.maxY),Math.clamp(line.z,b.minZ,b.maxZ));
            var obstruction=level.clip(new ClipContext(start,hit,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
            if(obstruction.getType()==HitResult.Type.BLOCK&&start.distanceTo(obstruction.getLocation())+.001<start.distanceTo(hit))continue;
            c.hits.add(e.getUUID());if(!RushDamage.hit(p,e,NeungsaIaidoRules.damage(c.full),true))continue;
            var viewers=new HashSet<>(PlayerLookup.tracking(e));viewers.add(p);if(e instanceof ServerPlayer target)viewers.add(target);
            for(var viewer:viewers)if(ServerPlayNetworking.canSend(viewer,NeungsaCutEffectPayload.TYPE))ServerPlayNetworking.send(viewer,new NeungsaCutEffectPayload(e.getId(),e.getUUID()));
            level.playSound(null,e.blockPosition(),dev.killerranking.KillerRankingSounds.HIT_BLADE,SoundSource.PLAYERS,.85f,1.2f);
        }
    }
    private void sendIaido(Iaido c,ServerPlayer viewer,boolean end){
        // The cast may already be removed when its final visual packet is sent.
        int age=(int)Math.clamp(now(c.owner)-c.started,0,NeungsaIaidoRules.MAX_HOLD),remaining=c.phase==NeungsaIaidoRules.CHARGING?NeungsaIaidoRules.MAX_HOLD-age:Math.max(0,NeungsaIaidoRules.DURATION-age);
        if(ServerPlayNetworking.canSend(viewer,NeungsaIaidoVisualPayload.TYPE))ServerPlayNetworking.send(viewer,new NeungsaIaidoVisualPayload(c.owner.getUUID(),age,end?0:remaining,end?0:c.phase,c.full,c.feet.x,c.feet.y,c.feet.z,(float)c.direction.x,(float)c.direction.y,(float)c.direction.z));
    }
    private void broadcastIaido(Iaido c,boolean end){var viewers=new HashSet<>(PlayerLookup.tracking(c.owner));viewers.add(c.owner);for(var v:viewers)sendIaido(c,v,end);}
    private void cancelIaido(ServerPlayer p){var c=iaidos.remove(p.getUUID());if(c!=null){broadcastIaido(c,true);KillerRankingMod.COMBAT_STATUS.sync(p);}sync(p);}
    public void cancel(ServerPlayer p){var echo=echoes.remove(p.getUUID());if(echo!=null)broadcastEcho(echo,true);var c=casts.remove(p.getUUID());if(c!=null)broadcast(c,true);cancelIaido(p);sync(p);}
    public void forget(Entity e){if(e instanceof ServerPlayer p)cancel(p);sent.remove(e.getUUID());}
    public void clear(){for(var c:new ArrayList<>(echoes.values()))cancel(c.owner());echoes.clear();for(var c:new ArrayList<>(casts.values()))cancel(c.owner());for(var c:new ArrayList<>(iaidos.values()))cancel(c.owner);casts.clear();iaidos.clear();sent.clear();}
    private void broadcast(Cast c,boolean ending){var viewers=new HashSet<>(PlayerLookup.tracking(c.owner()));viewers.add(c.owner());for(var v:viewers)send(c,v,ending);}
    private void send(Cast c,ServerPlayer viewer,boolean ending){
        if(ServerPlayNetworking.canSend(viewer,NeungsaSlashVisualPayload.TYPE))ServerPlayNetworking.send(viewer,
            new NeungsaSlashVisualPayload(c.owner().getUUID(),(int)Math.clamp(now(c.owner())-c.started(),0,NeungsaSlashRules.DURATION),ending?0:remaining(c.owner())));
    }
    public void send(ServerPlayer p,ServerPlayer viewer){var echo=echoes.get(p.getUUID());if(echo!=null)sendEcho(echo,viewer,false);var c=casts.get(p.getUUID());if(c!=null)send(c,viewer,false);var i=iaidos.get(p.getUUID());if(i!=null)sendIaido(i,viewer,false);}
    public void sync(ServerPlayer p){
        var state=NeungsaSkillState.get(p);var status=new NeungsaStatusPayload(branch(p),state.unlocked(),remaining(p),state.cooldown(now(p)),iaidoRemaining(p),state.iaidoCooldown(now(p)),iaidoPhase(p),iaidoAge(p),echoRemaining(p),state.echoCooldown(now(p)));
        if(!status.equals(sent.get(p.getUUID()))&&ServerPlayNetworking.canSend(p,NeungsaStatusPayload.TYPE)){ServerPlayNetworking.send(p,status);sent.put(p.getUUID(),status);}
    }
}
