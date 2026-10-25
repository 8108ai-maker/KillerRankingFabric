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
import net.minecraft.world.level.Level;
import java.util.*;

public final class RushService {
    private record Cast(long started,ResourceKey<Level> dimension) { }
    private final Map<UUID,Cast> casts=new HashMap<>();
    private final Map<UUID,RushStatusPayload> sent=new HashMap<>();
    public static boolean branch(ServerPlayer p) {
        return PlayerProfiles.get(p).trait()==Trait.FIGHTING_SPIRIT && SpiritState.get(p).branch()==SpiritBranch.TTEOKBAL;
    }
    private static boolean eligible(ServerPlayer p) { return branch(p)&&p.isAlive()&&!p.isSpectator()&&TraitActivation.enabled(p); }
    private static long now(ServerPlayer p) { return p.server.overworld().getGameTime(); }
    public void unlock(ServerPlayer p) {
        if(!branch(p)||!p.isAlive()||p.isSpectator()) return;
        var state=RushState.get(p);var profile=PlayerProfiles.get(p);
        if(state.unlocked()) { sync(p);return; }
        if(BishopState.get(p).unlocked()||CheckmateState.get(p).unlocked()||RookState.get(p).unlocked()||CleaveState.get(p).unlocked()) { dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_DENY,.8f,1f);p.sendSystemMessage(Component.literal("§c투기 스킬은 연타·체크 메이트·잡기·일도양단·신의 부름 중 하나만 해금할 수 있습니다."));sync(p);return; }
        if(profile.pointUnits()<KillerPoints.whole(RushRules.COST)) { dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_DENY,.8f,1f);p.sendSystemMessage(Component.literal("§c연타 해금에는 1,000포인트가 필요합니다."));sync(p);return; }
        PlayerProfiles.set(p,profile.addPoints(-RushRules.COST));RushState.set(p,state.unlock());
        dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_PURCHASE,1f,1f);p.sendSystemMessage(Component.literal("§a떡발의 연타 해금 완료! 1,000포인트 사용 · 기본 키 Z"));
        KillerRankingMod.assignAndSync(p);sync(p);
    }
    public void start(ServerPlayer p) {
        if(KillerRankingMod.CLEAVE.active(p)||!eligible(p)||KillerRankingMod.ROOK.locked(p)||KillerRankingMod.ROOK.active(p)||KillerRankingMod.CHECKMATE.active(p)||p.isUsingItem()||casts.containsKey(p.getUUID())||KillerRankingMod.PARRY.window(p)>0) return;
        var state=RushState.get(p);
        if(!state.unlocked()||state.cooldown(now(p))>0) return;
        RushState.set(p,state.cast(now(p)));casts.put(p.getUUID(),new Cast(now(p),p.level().dimension()));
        broadcast(p);sync(p);
    }
    public boolean active(ServerPlayer p) { return remaining(p)>0; }
    public int remaining(ServerPlayer p) {
        var cast=casts.get(p.getUUID());return cast==null?0:(int)Math.clamp(RushRules.DURATION-(now(p)-cast.started()),0,RushRules.DURATION);
    }
    public void refresh(ServerPlayer p) {
        var cast=casts.get(p.getUUID());
        if(cast!=null && (!eligible(p)||!cast.dimension().equals(p.level().dimension())||p.isUsingItem())) cancel(p);
    }
    public void tick(MinecraftServer server) {
        for(var p:server.getPlayerList().getPlayers()) {
            refresh(p);var cast=casts.get(p.getUUID());
            if(cast!=null) {
                int age=(int)(now(p)-cast.started());
                if(age>=RushRules.DURATION) cancel(p);
                else {
                    if(RushRules.strikesAt(age)) strike(p,age==RushRules.DURATION-1);
                    if(age%10==0) broadcast(p);
                }
            }
            if(server.getTickCount()%2==0) sync(p);
        }
    }
    private void strike(ServerPlayer p,boolean finisher) {
        var eye=p.getEyePosition();var direction=p.getLookAngle();
        LivingEntity target=p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(RushRules.RANGE),
            e->e!=p&&e.isAlive()&&!e.isSpectator()&&e.isAttackable()&&!p.isAlliedTo(e)&&p.canAttack(e))
            .stream().filter(e->{
                var point=e.getBoundingBox().getCenter();var to=point.subtract(eye);double distance=to.lengthSqr();
                return RushRules.inFront(distance,distance<.0001?1:direction.dot(to.normalize()))&&p.hasLineOfSight(e);
            }).min(Comparator.comparingDouble(p::distanceToSqr)).orElse(null);
        boolean hit=target!=null&&RushDamage.hit(p,target,finisher);
        if(hit) {
            var contact=target.getBoundingBox().getCenter().add(direction.scale(-target.getBbWidth()*.45));
            p.serverLevel().playSound(null,contact.x,contact.y,contact.z,dev.killerranking.KillerRankingSounds.RUSH_PUNCH,
                SoundSource.PLAYERS,finisher?.9f:.65f,.65f+p.getRandom().nextFloat()*.16f);
            p.serverLevel().sendParticles(ParticleTypes.CRIT,contact.x,contact.y,contact.z,finisher?9:5,.09,.12,.09,.12);
            p.serverLevel().sendParticles(ParticleTypes.ENCHANTED_HIT,contact.x,contact.y,contact.z,finisher?4:2,.06,.08,.06,.05);
        } else {
            p.serverLevel().playSound(null,p.getX(),p.getY()+1,p.getZ(),dev.killerranking.KillerRankingSounds.SWING_FIST,
                SoundSource.PLAYERS,.22f,.95f+p.getRandom().nextFloat()*.15f);
        }
    }
    public void cancel(ServerPlayer p) { if(casts.remove(p.getUUID())!=null) { broadcast(p);sync(p); } }
    public void forget(ServerPlayer p) { cancel(p);sent.remove(p.getUUID()); }
    public void clear() { casts.clear();sent.clear(); }
    public void sync(ServerPlayer p) {
        var state=RushState.get(p);var value=new RushStatusPayload(branch(p),state.unlocked(),remaining(p),state.cooldown(now(p)));
        if(!value.equals(sent.get(p.getUUID()))&&ServerPlayNetworking.canSend(p,RushStatusPayload.TYPE)) {
            ServerPlayNetworking.send(p,value);sent.put(p.getUUID(),value);
        }
    }
    private void broadcast(ServerPlayer p) { send(p,p);for(var viewer:PlayerLookup.tracking(p)) send(p,viewer); }
    public void send(ServerPlayer p,ServerPlayer viewer) {
        int remaining=remaining(p);int age=remaining==0?0:RushRules.DURATION-remaining;
        if(ServerPlayNetworking.canSend(viewer,RushVisualPayload.TYPE)) ServerPlayNetworking.send(viewer,new RushVisualPayload(p.getUUID(),remaining,age));
    }
}
