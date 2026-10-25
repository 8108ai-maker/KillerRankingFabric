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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;

public final class WasabariService {
    private static final class Cast {
        final ServerPlayer owner;final LivingEntity victim;final Vec3 direction;final long started;final float yaw;
        Vec3 ownerAt,victimAt;boolean tripped,noAi,effects,released;
        Cast(ServerPlayer p,LivingEntity e){owner=p;victim=e;ownerAt=p.position();direction=e.position().subtract(ownerAt).multiply(1,0,1).normalize();started=now(p);yaw=(float)Math.toDegrees(Math.atan2(-direction.x,direction.z));}
    }
    private final Map<UUID,Cast> casts=new HashMap<>(),victims=new HashMap<>();
    private final Map<UUID,UUID> bleeders=new HashMap<>();
    private final Map<UUID,WasabariStatusPayload> sent=new HashMap<>();
    private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    public static boolean available(ServerPlayer p){return SalmosaSkillService.available(p);}
    private static boolean eligible(ServerPlayer p){return available(p)&&TraitActivation.enabled(p)&&p.isAlive()&&!p.isSpectator()&&!p.isRemoved()&&!p.isPassenger()&&!p.isUsingItem();}
    public boolean active(ServerPlayer p){return casts.containsKey(p.getUUID());}
    public boolean captured(Entity e){return !e.level().isClientSide&&victims.containsKey(e.getUUID());}
    public boolean locked(Entity e){return !e.level().isClientSide&&(casts.containsKey(e.getUUID())||victims.containsKey(e.getUUID()));}
    public int remaining(ServerPlayer p){var c=casts.get(p.getUUID());return c==null?0:(int)Math.clamp(WasabariRules.DURATION-(now(p)-c.started),0,WasabariRules.DURATION);}
    public ServerPlayer bleeder(LivingEntity victim){var owner=bleeders.get(victim.getUUID());return owner==null||victim.level().getServer()==null?null:victim.level().getServer().getPlayerList().getPlayer(owner);}
    public void hemorrhage(ServerPlayer owner,LivingEntity victim,int ticks){bleeders.put(victim.getUUID(),owner.getUUID());victim.addEffect(new MobEffectInstance(KillerRankingMod.HEMORRHAGE,ticks,0,false,true));}
    private static boolean enemy(ServerPlayer p,LivingEntity e){return e!=p&&e.isAlive()&&!e.isRemoved()&&!e.isSpectator()&&e.isAttackable()&&!e.isPassenger()&&!e.isVehicle()&&!p.isAlliedTo(e)&&p.canAttack(e)&&!(e instanceof ServerPlayer v&&v.isCreative());}
    public LivingEntity selected(ServerPlayer p){
        var eye=p.getEyePosition();var end=eye.add(p.getLookAngle().scale(WasabariRules.REACH));
        return p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(WasabariRules.REACH),e->enemy(p,e)&&!KillerRankingMod.ROOK.locked(e)
            &&p.distanceToSqr(e)<=WasabariRules.REACH*WasabariRules.REACH&&p.hasLineOfSight(e)&&e.getBoundingBox().inflate(.12).clip(eye,end).isPresent())
            .stream().min(Comparator.comparingDouble(p::distanceToSqr)).orElse(null);
    }
    public void start(ServerPlayer p){
        if(!eligible(p)||SkillCombatLock.blocksAttack(p)||WasabariState.get(p).cooldown(now(p))>0)return;
        var target=selected(p);if(target==null){p.displayClientMessage(Component.literal("§7와사바리: 2m 안의 상대를 바라보세요."),true);return;}
        var c=new Cast(p,target);if(c.direction.lengthSqr()<.9)return;
        casts.put(p.getUUID(),c);WasabariState.set(p,WasabariState.get(p).cast(now(p)));freeze(c);broadcast(c,false);sync(p);
    }
    private static boolean clearBox(LivingEntity e,Vec3 at){return !e.level().getBlockCollisions(e,e.getBoundingBox().move(at.subtract(e.position())).deflate(.001)).iterator().hasNext();}
    private static boolean clearSegment(LivingEntity e,Vec3 from,Vec3 to){int n=Math.max(1,(int)Math.ceil(from.distanceTo(to)/.15));for(int i=1;i<=n;i++)if(!clearBox(e,from.lerp(to,(double)i/n)))return false;return true;}
    private static void position(LivingEntity e,Vec3 at){e.setDeltaMovement(Vec3.ZERO);e.fallDistance=0;if(e.position().distanceToSqr(at)>.0001){
        if(e instanceof ServerPlayer p)p.teleportTo(p.serverLevel(),at.x,at.y,at.z,Set.of(),p.getYRot(),p.getXRot());else e.teleportTo(at.x,at.y,at.z);
    }}
    private static void freeze(Cast c){position(c.owner,c.ownerAt);if(c.tripped&&!c.released)position(c.victim,c.victimAt);}
    private static boolean valid(Cast c){return eligible(c.owner)&&c.owner.level()==c.victim.level()&&enemy(c.owner,c.victim)
        &&c.owner.position().distanceToSqr(c.ownerAt)<2.25&&clearBox(c.owner,c.ownerAt)
        &&(!c.tripped||c.released||c.victim.position().distanceToSqr(c.victimAt)<4&&clearBox(c.victim,c.victimAt));}
    public void refresh(ServerPlayer p){var c=casts.get(p.getUUID());if(c!=null&&!valid(c))cancel(p);}
    private void trip(Cast c){
        var e=c.victim;var offset=e.position().subtract(c.ownerAt).multiply(1,0,1);
        if(!enemy(c.owner,e)||KillerRankingMod.ROOK.locked(e)||c.owner.distanceToSqr(e)>4||!c.owner.hasLineOfSight(e)||offset.normalize().dot(c.direction)<.5){cancel(c.owner);return;}
        if(e instanceof ServerPlayer p&&(KillerRankingMod.PARRY.isProtected(p)||KillerRankingMod.PARRY.tryParry(p,c.owner))){cancel(c.owner);return;}
        if(e.isBlocking()&&e.getLookAngle().multiply(1,0,1).normalize().dot(c.owner.position().subtract(e.position()).multiply(1,0,1).normalize())>0){cancel(c.owner);return;}
        var anchor=e.position();double gap=dev.killerranking.visual.WasabariLayout.gap(c.owner.getBbWidth(),e.getBbWidth(),e.getBbHeight(),RookMountPose.inverted(e));var ownerAt=anchor.subtract(c.direction.scale(gap));
        if(!clearBox(e,anchor)||!clearBox(c.owner,ownerAt)||!clearSegment(c.owner,c.ownerAt,ownerAt)){cancel(c.owner);return;}
        c.tripped=true;c.victimAt=anchor;c.ownerAt=ownerAt;victims.put(e.getUUID(),c);
        if(e instanceof Mob mob){c.noAi=mob.isNoAi();mob.getNavigation().stop();mob.setNoAi(true);}
        e.stopUsingItem();c.owner.stopUsingItem();freeze(c);
        sound(c,dev.killerranking.KillerRankingSounds.WASABARI_TRIP,1f,1f);c.owner.serverLevel().sendParticles(ParticleTypes.POOF,e.getX(),e.getY()+.1,e.getZ(),9,.25,.04,.25,.035);
        broadcast(c,false);if(e instanceof ServerPlayer p)KillerRankingMod.ROOK.sync(p);
    }
    private void stab(Cast c){
        if(!c.tripped)return;
        if(RushDamage.hit(c.owner,c.victim,WasabariRules.STAB_DAMAGE,false)){
            if(!c.effects){c.effects=true;bleeders.put(c.victim.getUUID(),c.owner.getUUID());
                c.victim.addEffect(new MobEffectInstance(KillerRankingMod.HEMORRHAGE,WasabariRules.EFFECT_TICKS,0,false,true));
                c.victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,WasabariRules.EFFECT_TICKS,WasabariRules.SLOWNESS_AMPLIFIER,false,true));
            }
            sound(c,dev.killerranking.KillerRankingSounds.WASABARI_STAB,.9f,1f);
            var at=c.victimAt.subtract(c.direction.scale(Math.min(1,c.victim.getBbHeight()*.27)))
                .add(0,RookMountPose.inverted(c.victim)?c.victim.getBbHeight()*.8:Math.min(1,c.victim.getBbHeight()*.36),0);
            c.owner.serverLevel().sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.8f,.025f,.06f),.8f),at.x,at.y,at.z,6,.13,.06,.13,.015);
        }
    }
    private static void sound(Cast c,net.minecraft.sounds.SoundEvent event,float volume,float pitch){c.owner.serverLevel().playSound(null,c.owner.blockPosition(),event,SoundSource.PLAYERS,volume,pitch);}
    public void tick(MinecraftServer server){
        for(var c:new ArrayList<>(casts.values())){
            if(!valid(c)){cancel(c.owner);continue;}int age=(int)(now(c.owner)-c.started);
            if(age>=WasabariRules.DURATION){cancel(c.owner);continue;}
            freeze(c);if(age==WasabariRules.WA)sound(c,dev.killerranking.KillerRankingSounds.SWING_DAGGER,.6f,.9f);
            if(age==WasabariRules.BARI){sound(c,dev.killerranking.KillerRankingSounds.SWING_FIST,.8f,.8f);trip(c);}
            if(!casts.containsKey(c.owner.getUUID()))continue;
            if(c.tripped&&age>=WasabariRules.RELEASE)releaseVictim(c);
            if(WasabariRules.stab(age))stab(c);if(age%5==0)broadcast(c,false);
        }
        if(server.getTickCount()%2==0)for(var p:server.getPlayerList().getPlayers())sync(p);
        if(server.getTickCount()%20==0)bleeders.entrySet().removeIf(entry->{
            for(var level:server.getAllLevels()){var e=level.getEntity(entry.getKey());if(e instanceof LivingEntity victim&&victim.hasEffect(KillerRankingMod.HEMORRHAGE))return false;}return true;
        });
    }
    private void releaseVictim(Cast c){if(!c.tripped||c.released)return;c.released=true;victims.remove(c.victim.getUUID(),c);if(c.victim instanceof Mob mob)mob.setNoAi(c.noAi);c.victim.fallDistance=0;if(c.victim instanceof ServerPlayer v)KillerRankingMod.ROOK.sync(v);}
    public void cancel(ServerPlayer p){var c=casts.remove(p.getUUID());if(c==null)return;
        releaseVictim(c);
        broadcast(c,true);sync(p);if(c.victim instanceof ServerPlayer v)KillerRankingMod.ROOK.sync(v);
    }
    public void forget(Entity e){for(var c:new ArrayList<>(casts.values()))if(c.owner==e||c.victim==e)cancel(c.owner);sent.remove(e.getUUID());bleeders.remove(e.getUUID());bleeders.values().removeIf(e.getUUID()::equals);}
    public void clear(){for(var c:new ArrayList<>(casts.values()))cancel(c.owner);casts.clear();victims.clear();sent.clear();bleeders.clear();}
    private void broadcast(Cast c,boolean ending){var viewers=new HashSet<>(PlayerLookup.tracking(c.owner));viewers.addAll(PlayerLookup.tracking(c.victim));viewers.add(c.owner);if(c.victim instanceof ServerPlayer v)viewers.add(v);for(var v:viewers)send(c,v,ending);}
    private void send(Cast c,ServerPlayer v,boolean ending){if(ServerPlayNetworking.canSend(v,WasabariVisualPayload.TYPE))ServerPlayNetworking.send(v,new WasabariVisualPayload(c.owner.getUUID(),c.victim.getUUID(),(int)Math.clamp(now(c.owner)-c.started,0,WasabariRules.DURATION),ending?0:remaining(c.owner),c.yaw,c.tripped&&!c.released));}
    public void send(ServerPlayer p,ServerPlayer v){var c=casts.get(p.getUUID());if(c!=null)send(c,v,false);}
    public void sync(ServerPlayer p){var value=new WasabariStatusPayload(available(p),remaining(p),WasabariState.get(p).cooldown(now(p)));if(!value.equals(sent.get(p.getUUID()))&&ServerPlayNetworking.canSend(p,WasabariStatusPayload.TYPE)){ServerPlayNetworking.send(p,value);sent.put(p.getUUID(),value);}}
}
