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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Authoritative short-range grab, bounded choice window and three mutually exclusive finishers. */
public final class RookService {
    private record Aim(long started,ResourceKey<Level> dimension) { }
    private static final class Hold {
        final ServerPlayer owner;final LivingEntity victim;final ResourceKey<Level> dimension;
        final Vec3 origin,anchor,direction;final boolean noAi;
        Vec3 ownerAnchor;long started;int phase=RookRules.CHOICE;boolean released;
        Hold(ServerPlayer p,LivingEntity v,Vec3 anchor,Vec3 ownerAt) {
            owner=p;victim=v;dimension=p.level().dimension();origin=p.position();ownerAnchor=ownerAt;
            this.anchor=anchor;direction=horizontal(p);started=now(p);noAi=v instanceof Mob mob&&mob.isNoAi();
        }
    }
    private record Flight(ServerPlayer owner,LivingEntity victim,long started,ResourceKey<Level> dimension,boolean impacted) { }
    private final Map<UUID,Aim> aims=new HashMap<>();
    private final Map<UUID,Hold> holds=new HashMap<>(),victims=new HashMap<>();
    private final Map<UUID,Flight> flights=new HashMap<>();
    private final Map<UUID,RookStatusPayload> sent=new HashMap<>();
    public static boolean branch(ServerPlayer p) { return PlayerProfiles.get(p).trait()==Trait.FIGHTING_SPIRIT&&SpiritState.get(p).branch()==SpiritBranch.KING_AND_KILL; }
    private static long now(ServerPlayer p) { return p.server.overworld().getGameTime(); }
    private static Vec3 horizontal(ServerPlayer p) { var look=p.getLookAngle();var v=new Vec3(look.x,0,look.z);return v.lengthSqr()>.01?v.normalize():Vec3.directionFromRotation(0,p.getYRot()); }
    private boolean eligible(ServerPlayer p) { return branch(p)&&RookState.get(p).unlocked()&&p.isAlive()&&!p.isSpectator()&&TraitActivation.enabled(p)&&!p.isUsingItem()&&!p.isPassenger()&&!captured(p); }
    public boolean active(ServerPlayer p) { return aims.containsKey(p.getUUID())||holds.containsKey(p.getUUID()); }
    public boolean captured(Entity e) { return !e.level().isClientSide&&(victims.containsKey(e.getUUID())||KillerRankingMod.WASABARI.captured(e)); }
    public boolean locked(Entity e) { return !e.level().isClientSide&&(captured(e)||holds.containsKey(e.getUUID())||KillerRankingMod.HEAVY_PUNCH.locked(e)||KillerRankingMod.WASABARI.locked(e)); }
    public int phase(ServerPlayer p) { var h=holds.get(p.getUUID());return h!=null?h.phase:aims.containsKey(p.getUUID())?RookRules.AIM:RookRules.IDLE; }
    public int remaining(ServerPlayer p) {
        var h=holds.get(p.getUUID());var a=aims.get(p.getUUID());
        return h!=null?(int)Math.max(0,RookRules.duration(h.phase)-(now(p)-h.started)):a!=null?(int)Math.max(0,RookRules.AIM_DURATION-(now(p)-a.started)):0;
    }
    public void request(ServerPlayer p,RookRequestPayload request) {
        switch(request.operation()) {
            case RookRequestPayload.START->start(p);
            case RookRequestPayload.CONFIRM->confirm(p,request.target());
            case RookRequestPayload.CANCEL->cancel(p);
            case RookRequestPayload.THROW,RookRequestPayload.PUNCH,RookRequestPayload.MOUNT->choose(p,request.operation());
            case RookRequestPayload.UNLOCK->unlock(p);
        }
    }
    public void unlock(ServerPlayer p) {
        if(!branch(p)||!p.isAlive()||p.isSpectator()||captured(p))return;
        if(RookState.get(p).unlocked()){sync(p);return;}
        if(BishopState.get(p).unlocked()||CleaveState.get(p).unlocked()||!RookRules.canUnlock(RushState.get(p).unlocked(),CheckmateState.get(p).unlocked(),false)) {
            dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_DENY,.8f,1f);p.sendSystemMessage(Component.literal("§c투기 스킬은 하나만 해금할 수 있습니다. 나이트·룩·폰 6·비숍 중 하나를 선택하세요."));sync(p);return;
        }
        var profile=PlayerProfiles.get(p);
        if(profile.pointUnits()<KillerPoints.whole(RookRules.COST)){dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_DENY,.8f,1f);p.sendSystemMessage(Component.literal("§c잡기 해금에는 1,000포인트가 필요합니다."));sync(p);return;}
        PlayerProfiles.set(p,profile.addPoints(-RookRules.COST));dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_PURCHASE,1f,1f);setOwned(p,true);
        p.sendSystemMessage(Component.literal("§f잡기 해금 완료! Z를 눌러 지정 → 놓아서 잡기 → 5초 안에 Z/X/C 선택"));
    }
    public void setOwned(ServerPlayer p,boolean owned) {
        if(owned) {
            KillerRankingMod.BISHOP.forget(p);BishopState.set(p,BishopState.get(p).owned(false));
            KillerRankingMod.CLEAVE.cancel(p);CleaveState.set(p,CleaveState.get(p).owned(false));
            KillerRankingMod.RUSH.cancel(p);KillerRankingMod.CHECKMATE.cancel(p);
            RushState.set(p,new RushState(false,RushState.get(p).readyAt()));
            CheckmateState.set(p,CheckmateState.get(p).owned(false));
            SpiritState.set(p,new SpiritState(SpiritState.get(p).branch(),false));
        }else cancel(p);
        RookState.set(p,RookState.get(p).owned(owned));KillerRankingMod.assignAndSync(p);
    }
    public void start(ServerPlayer p) {
        if(KillerRankingMod.CLEAVE.active(p)||!eligible(p)||active(p)||locked(p)||RookState.get(p).cooldown(now(p))>0||KillerRankingMod.RUSH.active(p)||KillerRankingMod.CHECKMATE.active(p)||KillerRankingMod.PARRY.window(p)>0)return;
        aims.put(p.getUUID(),new Aim(now(p),p.level().dimension()));sync(p);
    }
    private boolean enemy(ServerPlayer p,LivingEntity e) {
        return e!=p&&e.isAlive()&&!e.isRemoved()&&!e.isSpectator()&&e.isAttackable()&&!e.isPassenger()&&!e.isVehicle()
            &&!p.isAlliedTo(e)&&p.canAttack(e)&&!locked(e)&&!(e instanceof ServerPlayer other&&other.isCreative());
    }
    public LivingEntity selected(ServerPlayer p) {
        var eye=p.getEyePosition();var end=eye.add(p.getLookAngle().scale(RookRules.REACH));
        return p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(RookRules.REACH),e->enemy(p,e)
            &&p.distanceToSqr(e)<=RookRules.REACH*RookRules.REACH&&p.hasLineOfSight(e)
            &&e.getBoundingBox().inflate(.12).clip(eye,end).isPresent()).stream()
            .min(Comparator.comparingDouble(e->e.getBoundingBox().inflate(.12).clip(eye,end).orElse(end).distanceToSqr(eye))).orElse(null);
    }
    public void confirm(ServerPlayer p,int targetId) {
        var aim=aims.remove(p.getUUID());
        if(aim==null)return;
        var target=selected(p);
        if(targetId<0||target==null||target.getId()!=targetId||!eligible(p)||!aim.dimension.equals(p.level().dimension())
            ||now(p)-aim.started>=RookRules.AIM_DURATION||RookState.get(p).cooldown(now(p))>0){sync(p);return;}
        var anchor=target.position();
        var ownerAt=anchor.subtract(horizontal(p).scale(.72+target.getBbWidth()*.5));
        if(!clearBox(target,anchor)||!clearBox(p,ownerAt)||!clearSegment(p,p.position(),ownerAt)){sync(p);return;}
        var h=new Hold(p,target,anchor,ownerAt);holds.put(p.getUUID(),h);victims.put(target.getUUID(),h);
        if(target instanceof Mob mob){mob.getNavigation().stop();mob.setNoAi(true);}
        target.stopUsingItem();p.stopUsingItem();RookState.set(p,RookState.get(p).cast(now(p)));
        freeze(h);sound(p,dev.killerranking.KillerRankingSounds.ROOK_GRAB,1f);broadcast(h);sync(p);if(target instanceof ServerPlayer v)sync(v);
    }
    public void choose(ServerPlayer p,int action) {
        var h=holds.get(p.getUUID());
        if(h==null||h.phase!=RookRules.CHOICE||remaining(p)<=0||action<RookRules.THROW||action>RookRules.MOUNT||!valid(h))return;
        if(action==RookRules.MOUNT) {
            // Mount the grounded victim's torso, rather than standing on a
            // still-upright target. The render pose lays the victim on its back.
            var seat=h.anchor.subtract(h.direction.scale(.15)).add(0,RookMountPose.seatHeight(h.victim),0);
            if(!clearBox(p,seat)||!clearSegment(p,p.position(),seat)){p.displayClientMessage(Component.literal("§7올라탈 공간이 없습니다. Z 또는 X를 선택하세요."),true);return;}
            h.ownerAnchor=seat;
        }
        h.phase=action;h.started=now(p);freeze(h);
        if(RookRules.strike(action,0))strike(h);
        broadcast(h);sync(p);
    }
    private static boolean clearBox(LivingEntity e,Vec3 at) {
        return !e.level().getBlockCollisions(e,e.getBoundingBox().move(at.subtract(e.position())).deflate(.001)).iterator().hasNext();
    }
    private static boolean clearSegment(LivingEntity e,Vec3 from,Vec3 to) {
        int steps=Math.max(1,(int)Math.ceil(from.distanceTo(to)/.15));
        for(int i=1;i<=steps;i++)if(!clearBox(e,from.lerp(to,(double)i/steps)))return false;return true;
    }
    private boolean valid(Hold h) {
        return eligible(h.owner)&&h.owner.level().dimension().equals(h.dimension)&&h.victim.level().dimension().equals(h.dimension)
            &&h.victim.isAlive()&&!h.victim.isRemoved()&&!h.victim.isSpectator()&&!h.owner.isPassenger()&&!h.victim.isPassenger()
            &&!(h.victim instanceof ServerPlayer p&&p.isCreative())&&h.owner.position().distanceToSqr(h.ownerAnchor)<2.25
            &&(h.released||h.victim.position().distanceToSqr(h.anchor)<9&&clearBox(h.victim,h.anchor))&&clearBox(h.owner,h.ownerAnchor);
    }
    private static void position(LivingEntity e,Vec3 at) {
        e.setDeltaMovement(Vec3.ZERO);e.fallDistance=0;
        if(e.position().distanceToSqr(at)>.0001) {
            if(e instanceof ServerPlayer p)p.teleportTo(p.serverLevel(),at.x,at.y,at.z,Set.of(RelativeMovement.Y_ROT,RelativeMovement.X_ROT),p.getYRot(),p.getXRot());
            else e.teleportTo(at.x,at.y,at.z);
        }
    }
    private void freeze(Hold h) {
        position(h.owner,h.ownerAnchor);
        if(!h.released)position(h.victim,h.anchor);
    }
    private static void sound(ServerPlayer p,net.minecraft.sounds.SoundEvent sound,float pitch) {p.serverLevel().playSound(null,p.blockPosition(),sound,SoundSource.PLAYERS,.6f,pitch);}
    private void strike(Hold h) {
        boolean heavy=h.phase==RookRules.MOUNT;
        if(RushDamage.hit(h.owner,h.victim,heavy?RookRules.MOUNT_DAMAGE:RookRules.PUNCH_DAMAGE,false)) {
            var v=h.victim;double height=heavy?RookMountPose.impactHeight(v):v.getBbHeight()*.7;
            h.owner.serverLevel().sendParticles(heavy?ParticleTypes.CRIT:ParticleTypes.POOF,v.getX(),v.getY()+height,v.getZ(),heavy?10:5,.13,.12,.13,.04);
            sound(h.owner,heavy?dev.killerranking.KillerRankingSounds.HEAVY_PUNCH_IMPACT:dev.killerranking.KillerRankingSounds.ROOK_PUNCH,heavy?.9f:1f);
        }
    }
    private void releaseVictim(Hold h) {
        if(h.released)return;h.released=true;victims.remove(h.victim.getUUID(),h);
        if(h.victim instanceof Mob mob)mob.setNoAi(h.noAi);
        h.victim.fallDistance=0;if(h.victim instanceof ServerPlayer p)sync(p);
    }
    private void launch(Hold h) {
        RushDamage.hit(h.owner,h.victim,RookRules.THROW_DAMAGE,false);releaseVictim(h);
        launchTarget(h.owner,h.victim,h.direction);
        sound(h.owner,dev.killerranking.KillerRankingSounds.ROOK_THROW,1f);broadcast(h);
    }
    /** Shared physical throw: the caller supplies its own contact damage. */
    public void launchTarget(ServerPlayer owner,LivingEntity victim,Vec3 direction) {
        if(!victim.isAlive()||victim.isRemoved()||owner.level()!=victim.level())return;
        var forward=direction.multiply(1,0,1).normalize();if(forward.lengthSqr()<.9)return;
        victim.setDeltaMovement(forward.scale(1.65).add(0,.38,0));victim.hurtMarked=true;victim.hasImpulse=true;
        flights.put(victim.getUUID(),new Flight(owner,victim,now(owner),owner.level().dimension(),false));
    }
    public void refresh(ServerPlayer p) {
        RookBodySize.refresh(p);
        var aim=aims.get(p.getUUID());if(aim!=null&&(!eligible(p)||!aim.dimension.equals(p.level().dimension())||now(p)-aim.started>=RookRules.AIM_DURATION))cancel(p);
        var h=holds.get(p.getUUID());if(h!=null&&!valid(h))cancel(p);
    }
    public void tick(MinecraftServer server) {
        for(var p:server.getPlayerList().getPlayers())refresh(p);
        for(var h:new ArrayList<>(holds.values())) {
            if(!valid(h)||remaining(h.owner)<=0){cancel(h.owner);continue;}
            int age=(int)(now(h.owner)-h.started);freeze(h);
            if(h.phase==RookRules.THROW&&!h.released&&age>=RookRules.THROW_RELEASE)launch(h);
            if(age>0&&RookRules.strike(h.phase,age))strike(h);
            if(age%5==0)broadcast(h);
        }
        for(var f:new ArrayList<>(flights.values())) {
            var v=f.victim();long age=now(f.owner())-f.started();
            if(age>RookRules.FLIGHT_DURATION||!v.isAlive()||v.isRemoved()||!v.level().dimension().equals(f.dimension())){flights.remove(v.getUUID());continue;}
            // Vanilla disables travel entirely for NoAI mobs. Preserve their
            // AI flag, while giving the external throw ordinary block collision.
            if(age>0&&v instanceof Mob mob&&mob.isNoAi()) {
                var velocity=v.getDeltaMovement();v.move(net.minecraft.world.entity.MoverType.SELF,velocity);
                v.setDeltaMovement(v.onGround()?velocity.multiply(.98,0,.98):velocity.multiply(.98,.98,.98).add(0,-v.getGravity(),0));
            }
            var speed=v.getDeltaMovement();var horizontal=new Vec3(speed.x,0,speed.z);
            var hit=v.level().clip(new ClipContext(v.position().add(0,v.getBbHeight()*.5,0),v.position().add(0,v.getBbHeight()*.5,0).add(horizontal.normalize().scale(.4)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,v));
            if(!f.impacted()&&age>0&&(v.horizontalCollision||horizontal.lengthSqr()>.02&&hit.getType()==HitResult.Type.BLOCK&&hit.getDirection().getAxis()!=net.minecraft.core.Direction.Axis.Y)) {
                // Keep gravity running after impact, including targets whose AI is disabled.
                flights.put(v.getUUID(),new Flight(f.owner(),v,f.started(),f.dimension(),true));RushDamage.hit(f.owner(),v,RookRules.WALL_DAMAGE,false);
                v.setDeltaMovement(speed.multiply(.15,1,.15));v.hurtMarked=true;
                f.owner().serverLevel().sendParticles(ParticleTypes.POOF,v.getX(),v.getY()+v.getBbHeight()*.5,v.getZ(),12,.15,.2,.15,.06);
                sound(f.owner(),dev.killerranking.KillerRankingSounds.ROOK_SLAM,1f);
            }
        }
        if(server.getTickCount()%2==0)for(var p:server.getPlayerList().getPlayers())sync(p);
    }
    public void cancel(ServerPlayer p) {
        aims.remove(p.getUUID());var h=holds.remove(p.getUUID());
        if(h!=null){releaseVictim(h);broadcast(new RookVisualPayload(p.getUUID(),h.victim.getUUID(),0,0,0),p);}
        sync(p);
    }
    public void forget(Entity e) {
        var captive=victims.get(e.getUUID());if(captive!=null)cancel(captive.owner);
        if(e instanceof ServerPlayer p){cancel(p);sent.remove(p.getUUID());}
        flights.values().removeIf(f->f.victim()==e||f.owner()==e);
    }
    public void clear() {for(var h:holds.values())releaseVictim(h);holds.clear();victims.clear();aims.clear();flights.clear();sent.clear();}
    public void sync(ServerPlayer p) {
        var target=phase(p)==RookRules.AIM?selected(p):holds.containsKey(p.getUUID())?holds.get(p.getUUID()).victim:null;
        var state=RookState.get(p);var payload=new RookStatusPayload(branch(p),state.unlocked(),BishopState.get(p).unlocked()||RushState.get(p).unlocked()||CheckmateState.get(p).unlocked()||CleaveState.get(p).unlocked(),phase(p),target==null?-1:target.getId(),remaining(p),state.cooldown(now(p)),captured(p));
        if(!payload.equals(sent.get(p.getUUID()))&&ServerPlayNetworking.canSend(p,RookStatusPayload.TYPE)){ServerPlayNetworking.send(p,payload);sent.put(p.getUUID(),payload);}
    }
    private void broadcast(Hold h) {broadcast(new RookVisualPayload(h.owner.getUUID(),h.released?null:h.victim.getUUID(),h.phase,(int)(now(h.owner)-h.started),remaining(h.owner)),h.owner);}
    private void broadcast(RookVisualPayload payload,ServerPlayer owner) {
        var viewers=new HashSet<>(PlayerLookup.tracking(owner));viewers.add(owner);
        var h=holds.get(owner.getUUID());if(h!=null&&h.victim instanceof ServerPlayer victim)viewers.add(victim);
        for(var viewer:viewers)if(ServerPlayNetworking.canSend(viewer,RookVisualPayload.TYPE))ServerPlayNetworking.send(viewer,payload);
    }
    public void send(ServerPlayer owner,ServerPlayer viewer) {
        var h=holds.get(owner.getUUID());if(h!=null&&ServerPlayNetworking.canSend(viewer,RookVisualPayload.TYPE))ServerPlayNetworking.send(viewer,new RookVisualPayload(owner.getUUID(),h.released?null:h.victim.getUUID(),h.phase,(int)(now(owner)-h.started),remaining(owner)));
    }
}
