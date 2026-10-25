package dev.killerranking.combat;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.Trait;
import dev.killerranking.data.*;
import dev.killerranking.network.*;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.core.BlockPos;
import java.util.*;

public final class CheckmateService {
    private static final class Aim {
        final long started;final ResourceKey<Level> dimension;final ItemStack weapon;
        final Vec3 origin;final List<Vec3> tiles;
        Aim(ServerPlayer p,List<Vec3> tiles) {
            started=now(p);dimension=p.level().dimension();weapon=p.getMainHandItem();
            origin=p.position();this.tiles=List.copyOf(tiles);
        }
    }
    private static final class Cast {
        final long started;
        final ResourceKey<Level> dimension;
        final ItemStack weapon;
        final Vec3 origin;
        final List<Vec3> tiles;
        final int selected;
        Vec3 lastSafe;
        boolean struck;
        Cast(ServerPlayer p,long started,List<Vec3> tiles,int selected) {
            this.started=started;dimension=p.level().dimension();weapon=p.getMainHandItem();
            origin=p.position();lastSafe=origin;this.tiles=List.copyOf(tiles);this.selected=selected;
        }
        Vec3 destination() { return tiles.get(selected); }
    }
    private final Map<UUID,Cast> casts=new HashMap<>();
    private final Map<UUID,Aim> aims=new HashMap<>();
    private final Map<UUID,CheckmateStatusPayload> sent=new HashMap<>();
    public static boolean branch(ServerPlayer p) {
        return PlayerProfiles.get(p).trait()==Trait.FIGHTING_SPIRIT&&SpiritState.get(p).branch()==SpiritBranch.KING_AND_KILL;
    }
    private static boolean eligible(ServerPlayer p) {
        return branch(p)&&CheckmateState.get(p).unlocked()&&p.isAlive()&&!p.isSpectator()&&TraitActivation.enabled(p)
            &&p.getMainHandItem().is(KillerRankingMod.NIGHT_BARBED_BAT)&&!p.isUsingItem()&&!p.isPassenger()&&!KillerRankingMod.ROOK.locked(p);
    }
    private static long now(ServerPlayer p) { return p.server.overworld().getGameTime(); }
    public void unlock(ServerPlayer p) {
        if(!branch(p)||!p.isAlive()||p.isSpectator()) return;
        if(CheckmateState.get(p).unlocked()) { sync(p);return; }
        if(BishopState.get(p).unlocked()||RushState.get(p).unlocked()||RookState.get(p).unlocked()||CleaveState.get(p).unlocked()) { dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_DENY,.8f,1f);p.sendSystemMessage(Component.literal("§c투기 스킬은 하나만 해금할 수 있습니다. 나이트·룩·폰 6·비숍 중 하나를 선택하세요."));sync(p);return; }
        var profile=PlayerProfiles.get(p);
        if(profile.pointUnits()<KillerPoints.whole(CheckmateRules.COST)) { dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_DENY,.8f,1f);p.sendSystemMessage(Component.literal("§c체크 메이트 해금에는 1,000포인트가 필요합니다."));sync(p);return; }
        PlayerProfiles.set(p,profile.addPoints(-CheckmateRules.COST));dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_PURCHASE,1f,1f);
        setOwned(p,true);
        p.sendSystemMessage(Component.literal("§f체크 메이트 해금 완료! 1,000포인트 사용 · 기본 키 Z"));
    }
    /** Existing operator ownership commands also grant/revoke the actual skill. Cooldown is retained. */
    public void setOwned(ServerPlayer p,boolean owned) {
        if(owned) {
            KillerRankingMod.BISHOP.forget(p);BishopState.set(p,BishopState.get(p).owned(false));
            KillerRankingMod.CLEAVE.cancel(p);CleaveState.set(p,CleaveState.get(p).owned(false));
            KillerRankingMod.ROOK.cancel(p);RookState.set(p,RookState.get(p).owned(false));
            KillerRankingMod.RUSH.cancel(p);RushState.set(p,new RushState(false,RushState.get(p).readyAt()));
        }
        CheckmateState.set(p,CheckmateState.get(p).owned(owned));
        SpiritState.set(p,new SpiritState(SpiritState.get(p).branch(),owned));
        KillerRankingMod.assignAndSync(p);
    }
    public void start(ServerPlayer p) {
        if(KillerRankingMod.CLEAVE.active(p)||!eligible(p)||!p.onGround()||p.isInWaterOrBubble()||p.isInLava()||active(p)
            ||KillerRankingMod.RUSH.active(p)||KillerRankingMod.PARRY.window(p)>0) return;
        var state=CheckmateState.get(p);
        if(state.cooldown(now(p))>0) return;
        var origin=p.position();var tiles=new ArrayList<Vec3>();
        for(var move:CheckmateBoard.MOVES) {
            var candidate=CheckmateBoard.endpoint(origin,move);var floor=floor(p,candidate);
            if(floor!=null) tiles.add(floor);
        }
        if(tiles.isEmpty()) { p.displayClientMessage(Component.literal("§7체크 메이트로 도약할 공간과 도착 발판이 없습니다."),true);return; }
        aims.put(p.getUUID(),new Aim(p,tiles));
        broadcast(p);sync(p);
    }
    /** Releasing Z is the only operation that commits a destination and spends cooldown. */
    public void confirm(ServerPlayer p,int selected) {
        var aim=aims.get(p.getUUID());
        if(aim==null) return;
        if(selected<0||selected>=aim.tiles.size()||!validAim(p,aim)
            ||CheckmateState.get(p).cooldown(now(p))>0) { cancel(p);return; }
        var destination=aim.tiles.get(selected);var currentFloor=floor(p,destination);
        if(currentFloor==null||currentFloor.distanceToSqr(destination)>.01||!clearPath(p,p.position(),destination)) {
            cancel(p);p.displayClientMessage(Component.literal("§7선택한 칸으로 도약할 공간이 없습니다."),true);return;
        }
        aims.remove(p.getUUID());
        CheckmateState.set(p,CheckmateState.get(p).cast(now(p)));
        casts.put(p.getUUID(),new Cast(p,now(p),aim.tiles,selected));broadcast(p);sync(p);
        p.serverLevel().playSound(null,p.blockPosition(),dev.killerranking.KillerRankingSounds.CHECKMATE_JUMP,SoundSource.PLAYERS,.9f,1f);
    }
    private static boolean validAim(ServerPlayer p,Aim aim) {
        return eligible(p)&&p.onGround()&&!p.isInWaterOrBubble()&&!p.isInLava()
            &&aim.dimension.equals(p.level().dimension())&&aim.weapon==p.getMainHandItem()
            &&aim.origin.distanceToSqr(p.position())<=.1225&&now(p)-aim.started<CheckmateRules.AIM_DURATION;
    }
    public boolean aiming(ServerPlayer p) { return aims.containsKey(p.getUUID()); }
    public boolean active(ServerPlayer p) { return aiming(p)||remaining(p)>0; }
    public int remaining(ServerPlayer p) {
        var c=casts.get(p.getUUID());return c==null?0:(int)Math.clamp(CheckmateRules.DURATION-(now(p)-c.started),0,CheckmateRules.DURATION);
    }
    public void refresh(ServerPlayer p) {
        var spirit=SpiritState.get(p);var skill=CheckmateState.get(p);
        // Import old releases' checkmate flag without charging existing owners.
        if(spirit.checkmate()&&!skill.unlocked()) { skill=skill.owned(true);CheckmateState.set(p,skill); }
        if(branch(p)&&skill.unlocked()!=spirit.checkmate()) SpiritState.set(p,new SpiritState(spirit.branch(),skill.unlocked()));
        var c=casts.get(p.getUUID());
        if(c!=null&&(!eligible(p)||!c.dimension.equals(p.level().dimension())||c.weapon!=p.getMainHandItem())) cancel(p);
        var aim=aims.get(p.getUUID());if(aim!=null&&!validAim(p,aim)) cancel(p);
    }
    public void tick(MinecraftServer server) {
        for(var p:server.getPlayerList().getPlayers()) {
            refresh(p);var c=casts.get(p.getUUID());
            if(c!=null) {
                int age=(int)(now(p)-c.started);
                if(age>=CheckmateRules.DURATION) cancel(p);
                else {
                    if(age<=CheckmateRules.HIT) {
                        var desired=CheckmateBoard.position(c.origin,c.destination(),age);
                        if(!clearSegment(p,c.lastSafe,desired)) { cancel(p);continue; }
                        p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;
                        // Relative rotation preserves mouse input that arrives between server position corrections.
                        p.teleportTo(p.serverLevel(),desired.x,desired.y,desired.z,
                            Set.of(RelativeMovement.X_ROT,RelativeMovement.Y_ROT),p.getYRot(),p.getXRot());
                        c.lastSafe=desired;
                    }
                    if(age>=CheckmateRules.HIT&&!c.struck) { c.struck=true;p.setOnGround(true);strike(p,c); }
                    if(age%8==0) broadcast(p);
                }
            }
            if(aims.containsKey(p.getUUID())&&server.getTickCount()%4==0) broadcast(p);
            if(server.getTickCount()%2==0) sync(p);
        }
    }
    private static Vec3 floor(ServerPlayer p,Vec3 point) {
        if(!p.serverLevel().hasChunkAt(BlockPos.containing(point))) return null;
        var hit=p.serverLevel().clip(new ClipContext(point.add(0,1.5,0),point.add(0,-3,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        if(hit.getType()!=HitResult.Type.BLOCK||hit.getDirection()!=net.minecraft.core.Direction.UP) return null;
        var landing=new Vec3(point.x,hit.getLocation().y,point.z);
        if(!p.serverLevel().getFluidState(BlockPos.containing(landing.add(0,.01,0))).isEmpty()) return null;
        return clearBox(p,landing)?landing:null;
    }
    private static boolean clearBox(ServerPlayer p,Vec3 position) {
        var box=p.getBoundingBox().move(position.subtract(p.position())).deflate(.001);
        return !p.serverLevel().getBlockCollisions(p,box).iterator().hasNext();
    }
    private static boolean clearSegment(ServerPlayer p,Vec3 a,Vec3 b) {
        int steps=Math.max(1,(int)Math.ceil(a.distanceTo(b)/.18));
        for(int i=1;i<=steps;i++) if(!clearBox(p,a.lerp(b,(double)i/steps))) return false;
        return true;
    }
    private static boolean clearPath(ServerPlayer p,Vec3 origin,Vec3 destination) {
        var previous=origin;
        for(int age=CheckmateRules.HOP;age<=CheckmateRules.HIT;age++) {
            var next=CheckmateBoard.position(origin,destination,age);
            if(!clearSegment(p,previous,next)) return false;previous=next;
        }
        return true;
    }
    private static boolean enemy(ServerPlayer p,LivingEntity e) {
        return e!=p&&e.isAlive()&&!e.isSpectator()&&e.isAttackable()&&!p.isAlliedTo(e)&&p.canAttack(e);
    }
    private void strike(ServerPlayer p,Cast c) {
        var landing=c.destination();double half=CheckmateBoard.HALF;
        var area=new AABB(landing.x-half,landing.y-.25,landing.z-half,landing.x+half,landing.y+2.5,landing.z+half);
        LivingEntity target=p.serverLevel().getEntitiesOfClass(LivingEntity.class,area,
            e->enemy(p,e)&&CheckmateBoard.occupies(landing,e.getBoundingBox(),e.getY())&&p.hasLineOfSight(e))
            .stream().min(Comparator.comparingDouble(e->e.position().distanceToSqr(landing))).orElse(null);
        boolean hit=target!=null&&CheckmateDamage.hit(p,target);
        if(hit) {
            c.weapon.hurtAndBreak(2,p,EquipmentSlot.MAINHAND);
            target.knockback(.65,c.origin.x-target.getX(),c.origin.z-target.getZ());
            p.serverLevel().sendParticles(ParticleTypes.CRIT,target.getX(),target.getY()+target.getBbHeight()*.55,target.getZ(),10,.2,.25,.2,.08);
        }
        var impact=landing;
        p.serverLevel().sendParticles(ParticleTypes.POOF,impact.x,impact.y+.12,impact.z,18,.65,.08,.65,.045);
        p.serverLevel().sendParticles(ParticleTypes.FLASH,impact.x,impact.y+.5,impact.z,1,0,0,0,0);
        p.serverLevel().playSound(null,impact.x,impact.y,impact.z,dev.killerranking.KillerRankingSounds.CHECKMATE_SLAM,
            SoundSource.PLAYERS,.8f,.65f);
        if(hit)p.serverLevel().playSound(null,impact.x,impact.y,impact.z,dev.killerranking.KillerRankingSounds.HIT_BLUNT,SoundSource.PLAYERS,.8f,.8f);
    }
    public void cancel(ServerPlayer p) {
        boolean changed=casts.remove(p.getUUID())!=null;
        changed=aims.remove(p.getUUID())!=null||changed;
        if(changed) { p.fallDistance=0;broadcast(p);sync(p); }
    }
    public void forget(ServerPlayer p) { cancel(p);sent.remove(p.getUUID()); }
    public void clear() { casts.clear();aims.clear();sent.clear(); }
    public void sync(ServerPlayer p) {
        var state=CheckmateState.get(p);
        var value=new CheckmateStatusPayload(branch(p),state.unlocked(),BishopState.get(p).unlocked()||RushState.get(p).unlocked()||RookState.get(p).unlocked()||CleaveState.get(p).unlocked(),remaining(p),state.cooldown(now(p)));
        if(!value.equals(sent.get(p.getUUID()))&&ServerPlayNetworking.canSend(p,CheckmateStatusPayload.TYPE)) {
            ServerPlayNetworking.send(p,value);sent.put(p.getUUID(),value);
        }
    }
    private void broadcast(ServerPlayer p) { send(p,p);for(var viewer:PlayerLookup.tracking(p)) send(p,viewer); }
    public void send(ServerPlayer p,ServerPlayer viewer) {
        int remaining=remaining(p),age=remaining==0?0:CheckmateRules.DURATION-remaining;
        var cast=casts.get(p.getUUID());
        var aim=aims.get(p.getUUID());
        if(aim!=null) { age=(int)(now(p)-aim.started);remaining=CheckmateRules.AIM_DURATION-age; }
        if(ServerPlayNetworking.canSend(viewer,CheckmateVisualPayload.TYPE)) ServerPlayNetworking.send(viewer,new CheckmateVisualPayload(p.getUUID(),remaining,age,
            cast!=null?cast.origin:aim!=null?aim.origin:Vec3.ZERO,cast!=null?cast.tiles:aim!=null?aim.tiles:List.of(),
            cast!=null?cast.selected:aim!=null?CheckmateBoard.select(p.getEyePosition(),p.getLookAngle(),aim.tiles):-1,aim!=null));
    }
}
