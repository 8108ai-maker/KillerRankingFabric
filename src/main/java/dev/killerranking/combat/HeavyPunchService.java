package dev.killerranking.combat;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.data.*;
import dev.killerranking.network.*;
import java.util.*;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;

/** Brace the caster's own wrist and deliver one rising punch on its animation beat. */
public final class HeavyPunchService {
    private static final class Cast {
        final ServerPlayer owner;final LivingEntity target;final Vec3 ownerAt,direction;
        final long started;final float yaw;boolean struck;
        Cast(ServerPlayer p,LivingEntity e){owner=p;target=e;ownerAt=p.position();
            started=now(p);direction=e.position().subtract(ownerAt).multiply(1,0,1).normalize();
            yaw=(float)Math.toDegrees(Math.atan2(-direction.x,direction.z));}
    }
    private final Map<UUID,Cast> casts=new HashMap<>();
    private final Map<UUID,HeavyPunchStatusPayload> sent=new HashMap<>();
    private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    public static boolean available(ServerPlayer p){return RushService.branch(p)&&RushState.get(p).unlocked();}
    private static boolean eligible(ServerPlayer p){return available(p)&&p.isAlive()&&!p.isSpectator()&&TraitActivation.enabled(p)
        &&p.getMainHandItem().is(KillerRankingMod.TTEOKBAL_FIST)&&!p.isUsingItem()&&!p.isPassenger();}
    public boolean active(ServerPlayer p){return casts.containsKey(p.getUUID());}
    public boolean captured(Entity e){return false;}
    public boolean locked(Entity e){return !e.level().isClientSide&&casts.containsKey(e.getUUID());}
    public int remaining(ServerPlayer p){var c=casts.get(p.getUUID());return c==null?0:(int)Math.clamp(HeavyPunchRules.DURATION-(now(p)-c.started),0,HeavyPunchRules.DURATION);}
    public LivingEntity selected(ServerPlayer p){
        var eye=p.getEyePosition();var end=eye.add(p.getLookAngle().scale(HeavyPunchRules.REACH));
        return p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(HeavyPunchRules.REACH),e->
            e!=p&&e.isAlive()&&!e.isRemoved()&&!e.isSpectator()&&e.isAttackable()&&!e.isPassenger()&&!e.isVehicle()
            &&!p.isAlliedTo(e)&&p.canAttack(e)&&!KillerRankingMod.ROOK.locked(e)
            &&!(e instanceof ServerPlayer other&&other.isCreative())&&p.hasLineOfSight(e)
            &&p.distanceToSqr(e)<=HeavyPunchRules.REACH*HeavyPunchRules.REACH
            &&e.getBoundingBox().inflate(.15).clip(eye,end).isPresent())
            .stream().min(Comparator.comparingDouble(p::distanceToSqr)).orElse(null);
    }
    public void start(ServerPlayer p){
        if(!eligible(p)||SkillCombatLock.blocksAttack(p)||HeavyPunchState.get(p).cooldown(now(p))>0)return;
        var target=selected(p);
        if(target==null){p.displayClientMessage(Component.literal("§7강타: 2.4m 안의 상대를 바라보세요."),true);return;}
        var direction=target.position().subtract(p.position()).multiply(1,0,1).normalize();
        if(direction.lengthSqr()<.9)return;
        var cast=new Cast(p,target);casts.put(p.getUUID(),cast);
        HeavyPunchState.set(p,HeavyPunchState.get(p).cast(now(p)));freeze(cast);broadcast(cast,false);sync(p);
    }
    private static void position(LivingEntity e,Vec3 at){
        e.setDeltaMovement(Vec3.ZERO);e.fallDistance=0;
        if(e.position().distanceToSqr(at)>.0001){
            if(e instanceof ServerPlayer p)p.teleportTo(p.serverLevel(),at.x,at.y,at.z,Set.of(RelativeMovement.X_ROT,RelativeMovement.Y_ROT),p.getYRot(),p.getXRot());
            else e.teleportTo(at.x,at.y,at.z);
        }
    }
    private static void freeze(Cast c){position(c.owner,c.ownerAt);}
    private static boolean valid(Cast c){return eligible(c.owner)&&!c.owner.isRemoved()&&c.owner.level()==c.target.level()
        &&c.owner.position().distanceToSqr(c.ownerAt)<4&&(c.struck||c.target.isAlive()&&!c.target.isRemoved()&&!c.target.isPassenger()
        &&!c.target.isSpectator()&&!(c.target instanceof ServerPlayer p&&p.isCreative()));}
    public void refresh(ServerPlayer p){var c=casts.get(p.getUUID());if(c!=null&&!valid(c))cancel(p);}
    public void tick(MinecraftServer server){
        for(var p:server.getPlayerList().getPlayers()){
            refresh(p);var c=casts.get(p.getUUID());
            if(c!=null){int age=(int)(now(p)-c.started);
                if(age>=HeavyPunchRules.DURATION)cancel(p);
                else {freeze(c);
                    if(age==HeavyPunchRules.SPLASH)sound(c,dev.killerranking.KillerRankingSounds.HEAVY_PUNCH_CHARGE,.9f,1f);
                    if(HeavyPunchRules.contact(age)&&!c.struck)strike(c);
                    if(age%5==0)broadcast(c,false);
                }
            }
            if(server.getTickCount()%2==0)sync(p);
        }
    }
    private void strike(Cast c){
        c.struck=true;
        var offset=c.target.position().subtract(c.ownerAt).multiply(1,0,1);
        boolean hit=c.target.isAlive()&&!c.target.isRemoved()&&c.owner.hasLineOfSight(c.target)
            &&c.owner.distanceToSqr(c.target)<=HeavyPunchRules.REACH*HeavyPunchRules.REACH
            &&offset.normalize().dot(c.direction)>.65&&RushDamage.hit(c.owner,c.target,HeavyPunchRules.DAMAGE,true);
        sound(c,hit?dev.killerranking.KillerRankingSounds.HEAVY_PUNCH_IMPACT:dev.killerranking.KillerRankingSounds.SWING_FIST,1.1f,hit?1f:.7f);
        if(hit){var at=c.target.getEyePosition().add(0,-.12,0);
            c.owner.serverLevel().sendParticles(ParticleTypes.CRIT,at.x,at.y,at.z,14,.15,.18,.15,.18);
            c.owner.serverLevel().sendParticles(ParticleTypes.EXPLOSION,at.x,at.y,at.z,1,0,0,0,0);
            KillerRankingMod.ROOK.launchTarget(c.owner,c.target,c.direction);
        }
        broadcast(c,false);
    }
    private static void sound(Cast c,net.minecraft.sounds.SoundEvent sound,float volume,float pitch){c.owner.serverLevel().playSound(null,c.owner.blockPosition(),sound,SoundSource.PLAYERS,volume,pitch);}
    private void broadcast(Cast c,boolean ending){
        var viewers=new HashSet<>(PlayerLookup.tracking(c.owner));viewers.addAll(PlayerLookup.tracking(c.target));viewers.add(c.owner);
        if(c.target instanceof ServerPlayer p)viewers.add(p);
        for(var v:viewers)send(c,v,ending);
    }
    private void send(Cast c,ServerPlayer v,boolean ending){if(ServerPlayNetworking.canSend(v,HeavyPunchVisualPayload.TYPE))
        ServerPlayNetworking.send(v,new HeavyPunchVisualPayload(c.owner.getUUID(),c.target.getId(),ending?0:remaining(c.owner),(int)Math.clamp(now(c.owner)-c.started,0,HeavyPunchRules.DURATION),c.yaw));}
    public void send(ServerPlayer p,ServerPlayer v){var c=casts.get(p.getUUID());if(c!=null)send(c,v,false);}
    public void cancel(ServerPlayer p){var c=casts.remove(p.getUUID());if(c!=null){broadcast(c,true);sync(p);}}
    public void forget(Entity e){for(var c:new ArrayList<>(casts.values()))if(c.owner==e||c.target==e)cancel(c.owner);sent.remove(e.getUUID());}
    public void clear(){casts.clear();sent.clear();}
    public void sync(ServerPlayer p){var value=new HeavyPunchStatusPayload(available(p),remaining(p),HeavyPunchState.get(p).cooldown(now(p)));
        if(!value.equals(sent.get(p.getUUID()))&&ServerPlayNetworking.canSend(p,HeavyPunchStatusPayload.TYPE)){ServerPlayNetworking.send(p,value);sent.put(p.getUUID(),value);}}
}
