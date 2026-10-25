package dev.killerranking.combat;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.Trait;
import dev.killerranking.data.*;
import dev.killerranking.network.*;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class MadnessLeapService {
    private static final class Action {
        final int phase;final long started;final ResourceKey<Level> dimension;final ItemStack weapon;
        final Vec3 origin,destination;final UUID target;final float yaw;
        Vec3 lastSafe;boolean struck;
        Action(ServerPlayer p,int phase,LivingEntity target,Vec3 destination) {
            this.phase=phase;started=now(p);dimension=p.level().dimension();weapon=p.getMainHandItem();
            origin=p.position();lastSafe=origin;this.destination=destination;this.target=target==null?null:target.getUUID();
            var d=destination.subtract(origin);yaw=phase==MadnessLeapRules.CAST?(float)Math.toDegrees(Math.atan2(-d.x,d.z)):p.getYRot();
        }
    }
    private final Map<UUID,Action> actions=new HashMap<>();
    private final Map<UUID,MadnessLeapStatusPayload> sent=new HashMap<>();
    public static boolean available(ServerPlayer p){return PlayerProfiles.get(p).trait()==Trait.MIXED_MADNESS;}
    private static boolean eligible(ServerPlayer p) {
        return available(p)&&TraitActivation.enabled(p)&&p.isAlive()&&!p.isSpectator()
            &&p.getMainHandItem().is(KillerRankingMod.EUNBAL_HATCHET)&&!p.isUsingItem()&&!p.isPassenger()
            &&!p.isInWaterOrBubble()&&!p.isInLava()&&!KillerRankingMod.ROOK.locked(p);
    }
    private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    public void request(ServerPlayer p,MadnessLeapRequestPayload request) {
        switch(request.operation()) {
            case MadnessLeapRequestPayload.START->start(p);
            case MadnessLeapRequestPayload.RELEASE->release(p,request.target());
            case MadnessLeapRequestPayload.CANCEL->cancel(p);
            default->{ }
        }
    }
    public void start(ServerPlayer p) {
        if(!eligible(p)||!p.onGround()||SkillCombatLock.blocksAttack(p)||MadnessLeapState.get(p).cooldown(now(p))>0)return;
        actions.put(p.getUUID(),new Action(p,MadnessLeapRules.AIM,null,Vec3.ZERO));broadcast(p);sync(p);
    }
    public void release(ServerPlayer p,int targetId) {
        var aim=actions.get(p.getUUID());if(aim==null||aim.phase!=MadnessLeapRules.AIM)return;
        var target=MadnessLeapTargeting.selected(p);
        if(!valid(p,aim)||!p.onGround()||target==null||target.getId()!=targetId||MadnessLeapState.get(p).cooldown(now(p))>0){cancel(p);return;}
        // Stop before the target's body instead of teleporting the two entities into one another.
        var away=p.position().subtract(target.position()).multiply(1,0,1).normalize();
        var point=target.position().add(away.scale((p.getBbWidth()+target.getBbWidth())*.5+.3));
        var destination=floor(p,point);
        if(destination==null||!clearPath(p,p.position(),destination)) {
            cancel(p);p.displayClientMessage(Component.literal("§7도약할 경로에 장애물이 있습니다."),true);return;
        }
        MadnessLeapState.set(p,MadnessLeapState.get(p).cast(now(p)));
        actions.put(p.getUUID(),new Action(p,MadnessLeapRules.CAST,target,destination));broadcast(p);sync(p);
        p.serverLevel().playSound(null,p.blockPosition(),dev.killerranking.KillerRankingSounds.MADNESS_LEAP_JUMP,SoundSource.PLAYERS,1f,1f);
    }
    private static boolean valid(ServerPlayer p,Action a) {
        return eligible(p)&&a.dimension.equals(p.level().dimension())&&a.weapon==p.getMainHandItem()
            &&(a.phase!=MadnessLeapRules.AIM||p.onGround()&&now(p)-a.started<MadnessLeapRules.MAX_HOLD);
    }
    public boolean active(ServerPlayer p){return actions.containsKey(p.getUUID());}
    public boolean casting(ServerPlayer p){var a=actions.get(p.getUUID());return a!=null&&a.phase==MadnessLeapRules.CAST;}
    public int phase(ServerPlayer p){var a=actions.get(p.getUUID());return a==null?0:a.phase;}
    public void refresh(ServerPlayer p){var a=actions.get(p.getUUID());if(a!=null&&!valid(p,a))cancel(p);}
    public void tick(MinecraftServer server) {
        for(var p:server.getPlayerList().getPlayers()) {
            refresh(p);var a=actions.get(p.getUUID());
            if(a!=null&&a.phase==MadnessLeapRules.CAST) {
                int age=(int)(now(p)-a.started);
                if(age>=MadnessLeapRules.DURATION){cancel(p);continue;}
                var desired=MadnessLeapRules.position(a.origin,a.destination,age);
                if(!clearSegment(p,a.lastSafe,desired)){cancel(p);continue;}
                p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;
                p.teleportTo(p.serverLevel(),desired.x,desired.y,desired.z,
                    Set.of(RelativeMovement.X_ROT,RelativeMovement.Y_ROT),p.getYRot(),p.getXRot());
                a.lastSafe=desired;
                if(age>=MadnessLeapRules.HIT&&!a.struck){a.struck=true;p.setOnGround(true);strike(p,a);}
            }
            if(a!=null&&server.getTickCount()%4==0)broadcast(p);
            if(server.getTickCount()%2==0)sync(p);
        }
    }
    private void strike(ServerPlayer p,Action a) {
        var entity=p.serverLevel().getEntity(a.target);
        boolean hit=entity instanceof LivingEntity target&&MadnessLeapTargeting.enemy(p,target)
            &&target.getBoundingBox().inflate(1.25,.9,1.25).contains(p.position().add(0,.65,0))
            &&p.hasLineOfSight(target)&&RushDamage.hit(p,target,MadnessLeapRules.DAMAGE,true);
        if(hit){
            a.weapon.hurtAndBreak(2,p,EquipmentSlot.MAINHAND);
            p.serverLevel().sendParticles(ParticleTypes.CRIT,entity.getX(),entity.getY()+entity.getBbHeight()*.6,entity.getZ(),12,.25,.3,.25,.08);
        }
        var at=a.destination;
        p.serverLevel().sendParticles(ParticleTypes.POOF,at.x,at.y+.1,at.z,14,.45,.06,.45,.04);
        p.serverLevel().playSound(null,at.x,at.y,at.z,dev.killerranking.KillerRankingSounds.MADNESS_LEAP_LAND,SoundSource.PLAYERS,1f,hit?1f:.9f);
    }
    private static Vec3 floor(ServerPlayer p,Vec3 point) {
        if(!p.serverLevel().hasChunkAt(BlockPos.containing(point)))return null;
        var hit=p.serverLevel().clip(new ClipContext(point.add(0,1.5,0),point.add(0,-3,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        if(hit.getType()!=HitResult.Type.BLOCK||hit.getDirection()!=Direction.UP)return null;
        var landing=new Vec3(point.x,hit.getLocation().y,point.z);
        return p.serverLevel().getFluidState(BlockPos.containing(landing.add(0,.01,0))).isEmpty()&&clearBox(p,landing)?landing:null;
    }
    private static boolean clearBox(ServerPlayer p,Vec3 position) {
        var box=p.getBoundingBox().move(position.subtract(p.position())).deflate(.001);
        return !p.serverLevel().getBlockCollisions(p,box).iterator().hasNext();
    }
    private static boolean clearSegment(ServerPlayer p,Vec3 from,Vec3 to) {
        int steps=Math.max(1,(int)Math.ceil(from.distanceTo(to)/.18));
        for(int i=1;i<=steps;i++)if(!clearBox(p,from.lerp(to,(double)i/steps)))return false;
        return true;
    }
    private static boolean clearPath(ServerPlayer p,Vec3 from,Vec3 to) {
        var previous=from;
        for(int tick=MadnessLeapRules.HOP;tick<=MadnessLeapRules.HIT;tick++) {
            var next=MadnessLeapRules.position(from,to,tick);
            if(!clearSegment(p,previous,next))return false;previous=next;
        }
        return true;
    }
    public void cancel(ServerPlayer p){if(actions.remove(p.getUUID())!=null){p.fallDistance=0;broadcast(p);sync(p);}}
    public void forget(ServerPlayer p){cancel(p);sent.remove(p.getUUID());}
    public void clear(){actions.clear();sent.clear();}
    public void sync(ServerPlayer p) {
        var a=actions.get(p.getUUID());var value=new MadnessLeapStatusPayload(available(p),a==null?0:a.phase,
            a==null?0:(int)(now(p)-a.started),MadnessLeapState.get(p).cooldown(now(p)));
        if(!value.equals(sent.get(p.getUUID()))&&ServerPlayNetworking.canSend(p,MadnessLeapStatusPayload.TYPE)){
            ServerPlayNetworking.send(p,value);sent.put(p.getUUID(),value);
        }
    }
    private void broadcast(ServerPlayer p){send(p,p);for(var viewer:PlayerLookup.tracking(p))send(p,viewer);}
    public void send(ServerPlayer p,ServerPlayer viewer){
        var a=actions.get(p.getUUID());
        if(ServerPlayNetworking.canSend(viewer,MadnessLeapVisualPayload.TYPE))ServerPlayNetworking.send(viewer,new MadnessLeapVisualPayload(
            p.getUUID(),a==null?0:a.phase,a==null?0:(int)(now(p)-a.started),a==null?0:a.yaw));
    }
}
