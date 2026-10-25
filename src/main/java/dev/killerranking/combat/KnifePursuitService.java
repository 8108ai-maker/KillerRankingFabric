package dev.killerranking.combat;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.Trait;
import dev.killerranking.data.*;
import dev.killerranking.network.*;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import java.util.*;

/** One selected target; visual weapon never consumes or duplicates the real inventory item. */
public final class KnifePursuitService {
    private static final UUID NONE=new UUID(0,0);
    private static final class Action {
        final ServerPlayer owner;final LivingEntity target;final ItemStack weapon;
        final long started;final int phase;final Vec3 origin,throwOrigin;final float yaw;final ResourceKey<Level> dimension;
        Vec3 destination,lastSafe,lastKnife,embeddedAt;boolean embedded,extracted;
        Action(ServerPlayer p,LivingEntity e,int phase,Vec3 destination){
            owner=p;target=e;this.phase=phase;this.destination=destination;started=now(p);
            weapon=p.getMainHandItem();dimension=p.level().dimension();origin=p.position();lastSafe=origin;
            throwOrigin=p.getEyePosition().add(0,-.2,0);lastKnife=throwOrigin;
            var d=e==null?p.getLookAngle():e.position().subtract(origin);
            yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));
            embeddedAt=e==null?throwOrigin:embed(e,yaw);
        }
        Action(Action thrown,Vec3 at){
            owner=thrown.owner;target=thrown.target;weapon=thrown.weapon;started=now(owner);phase=KnifePursuitRules.DEFLECTED;
            origin=owner.position();throwOrigin=at;lastKnife=at;embeddedAt=at;destination=origin;lastSafe=origin;yaw=thrown.yaw;dimension=thrown.dimension;
        }
    }
    private final Map<UUID,Action> actions=new HashMap<>();
    private final Map<UUID,KnifePursuitStatusPayload> sent=new HashMap<>();
    private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    public static boolean available(ServerPlayer p){return SalmosaSkillService.available(p);}
    private static boolean eligible(ServerPlayer p){return available(p)&&TraitActivation.enabled(p)&&p.isAlive()&&!p.isRemoved()&&!p.isSpectator()
        &&p.getMainHandItem().is(KillerRankingMod.SALMOSA_MID_DAGGER)&&!p.isPassenger()&&!p.isUsingItem()&&!KillerRankingMod.ROOK.locked(p);}
    public boolean active(ServerPlayer p){return actions.containsKey(p.getUUID());}
    public boolean casting(ServerPlayer p){var a=actions.get(p.getUUID());return a!=null&&a.phase==KnifePursuitRules.CAST;}
    public int phase(ServerPlayer p){var a=actions.get(p.getUUID());return a==null?0:a.phase;}
    public int age(ServerPlayer p){var a=actions.get(p.getUUID());return a==null?0:(int)(now(p)-a.started);}
    public record Counter(LivingEntity target,Vec3 knife,Vec3 catchKnife){ }
    public Counter counter(ServerPlayer p){
        var a=actions.get(p.getUUID());if(a==null||a.phase!=KnifePursuitRules.DEFLECTED||!valid(a)||!KnifePursuitRules.counterWindow(age(p)))return null;
        return new Counter(a.target,KnifePursuitRules.deflected(a.throwOrigin,age(p)),KnifePursuitRules.deflected(a.throwOrigin,age(p)+ShoulderStabRules.COUNTER_CATCH));
    }
    public void request(ServerPlayer p,KnifePursuitRequestPayload request){switch(request.operation()){
        case KnifePursuitRequestPayload.START->start(p);
        case KnifePursuitRequestPayload.RELEASE->release(p,request.target());
        case KnifePursuitRequestPayload.CANCEL->cancel(p);
        default->{ }
    }}
    public void start(ServerPlayer p){
        if(!eligible(p)||SkillCombatLock.blocksAttack(p)||KnifePursuitState.get(p).cooldown(now(p))>0)return;
        actions.put(p.getUUID(),new Action(p,null,KnifePursuitRules.AIM,p.position()));broadcast(p);sync(p);
    }
    public void release(ServerPlayer p,int targetId){
        var aim=actions.get(p.getUUID());if(aim==null||aim.phase!=KnifePursuitRules.AIM)return;
        var target=KnifePursuitTargeting.selected(p);
        if(!valid(aim)||target==null||target.getId()!=targetId||KnifePursuitState.get(p).cooldown(now(p))>0){cancel(p);return;}
        var destination=landing(p,target);
        if(destination==null||!clearSegment(p,p.position(),destination)){cancel(p);p.displayClientMessage(Component.literal("§7칼날 추격: 접근 경로에 장애물이 있습니다."),true);return;}
        var action=new Action(p,target,KnifePursuitRules.CAST,destination);
        actions.put(p.getUUID(),action);KnifePursuitState.set(p,KnifePursuitState.get(p).cast(now(p)));broadcast(p);sync(p);
    }
    private static boolean valid(Action a){return eligible(a.owner)&&a.dimension.equals(a.owner.level().dimension())&&a.weapon==a.owner.getMainHandItem()
        &&(a.phase==KnifePursuitRules.AIM?now(a.owner)-a.started<KnifePursuitRules.MAX_HOLD:
            a.target.level()==a.owner.level()&&KnifePursuitTargeting.enemy(a.owner,a.target)&&!KillerRankingMod.ROOK.locked(a.target)
                &&a.owner.distanceToSqr(a.target)<24*24);}
    public void refresh(ServerPlayer p){var a=actions.get(p.getUUID());if(a!=null&&!valid(a))cancel(p);}
    public void tick(MinecraftServer server){
        for(var a:new ArrayList<>(actions.values())){
            var p=a.owner;if(!valid(a)){cancel(p);continue;}
            int age=(int)(now(p)-a.started);
            if(a.phase==KnifePursuitRules.DEFLECTED){
                if(age>=KnifePursuitRules.AIR_DURATION){cancel(p);continue;}
                a.embeddedAt=KnifePursuitRules.deflected(a.throwOrigin,age);
            }
            if(a.phase==KnifePursuitRules.CAST){
                if(age>=KnifePursuitRules.DURATION){cancel(p);continue;}
                if(age==KnifePursuitRules.THROW)sound(a,dev.killerranking.KillerRankingSounds.KNIFE_PURSUIT_THROW,.9f,1f);
                if(age>=KnifePursuitRules.THROW&&age<=KnifePursuitRules.IMPACT&&!a.embedded){
                    var at=KnifePursuitRules.flight(a.throwOrigin,embed(a.target,a.yaw),age);
                    if(p.serverLevel().clip(new ClipContext(a.lastKnife,at,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()!=HitResult.Type.MISS){cancel(p);continue;}
                    a.lastKnife=at;
                    if(age==KnifePursuitRules.IMPACT){
                        if(a.target instanceof ServerPlayer defender&&KillerRankingMod.PARRY.tryParryProjectile(defender,p)){
                            actions.put(p.getUUID(),new Action(a,at));broadcast(p);sync(p);continue;
                        }
                        var holder=p.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.THROWN);
                        if(!RushDamage.hit(p,a.target,new DamageSource(holder,p,p),KnifePursuitRules.THROW_DAMAGE,false)){cancel(p);continue;}
                        if(actions.get(p.getUUID())!=a)continue;
                        a.embedded=true;blood(a);sound(a,dev.killerranking.KillerRankingSounds.KNIFE_PURSUIT_EMBED,.9f,1f);broadcast(p);
                    }
                }
                if(age==KnifePursuitRules.BLINK){
                    a.destination=landing(p,a.target);
                    if(!a.embedded||a.destination==null||!clearSegment(p,a.origin,a.destination)){cancel(p);continue;}
                    sound(a,KillerRankingMod.KNIFE_PURSUIT_BLINK,.9f,1);
                    p.serverLevel().sendParticles(ParticleTypes.POOF,p.getX(),p.getY()+.9,p.getZ(),12,.18,.5,.18,.045);
                    var facing=a.target.position().subtract(a.destination);
                    p.setYRot((float)Math.toDegrees(Math.atan2(-facing.x,facing.z)));
                    p.yBodyRot=p.yHeadRot=p.getYRot();
                    move(p,a.destination);a.lastSafe=a.destination;
                    sound(a,KillerRankingMod.KNIFE_PURSUIT_BLINK,.55f,1.2f);
                    p.serverLevel().sendParticles(ParticleTypes.POOF,p.getX(),p.getY()+.9,p.getZ(),12,.18,.5,.18,.045);
                    broadcast(p);
                }else if(age<KnifePursuitRules.BLINK)move(p,a.origin);
                else if(age<KnifePursuitRules.EXTRACT)move(p,a.destination);
                if(age==KnifePursuitRules.EXTRACT&&!a.extracted){
                    if(!a.embedded||p.distanceToSqr(a.target)>6.25||!p.hasLineOfSight(a.target)){cancel(p);continue;}
                    a.extracted=true;
                    if(RushDamage.hit(p,a.target,KnifePursuitRules.EXTRACT_DAMAGE,false)){
                        if(actions.get(p.getUUID())!=a)continue;
                        KillerRankingMod.WASABARI.hemorrhage(p,a.target,WasabariRules.EFFECT_TICKS);
                        a.weapon.hurtAndBreak(2,p,EquipmentSlot.MAINHAND);blood(a);sound(a,dev.killerranking.KillerRankingSounds.HIT_PIERCE,1f,.9f);
                    }
                    broadcast(p);
                }
                a.embeddedAt=embed(a.target,a.yaw);
            }
            if(server.getTickCount()%2==0&&actions.get(p.getUUID())==a)broadcast(p);
        }
        if(server.getTickCount()%2==0)for(var p:server.getPlayerList().getPlayers())sync(p);
    }
    public static Vec3 embed(LivingEntity e,float yaw){return e.position().add(0,e.getBbHeight()*.62,0)
        .subtract(Vec3.directionFromRotation(0,yaw).scale(Math.max(.08,e.getBbWidth()*.5-.07)));}
    private static Vec3 landing(ServerPlayer p,LivingEntity e){
        // The destination is in front of the selected enemy, with room for both bodies.
        var away=Vec3.directionFromRotation(0,e.getYRot());
        var point=e.position().add(away.scale((p.getBbWidth()+e.getBbWidth())*.5+.35));
        if(!p.serverLevel().hasChunkAt(BlockPos.containing(point)))return null;
        var hit=p.serverLevel().clip(new ClipContext(point.add(0,1.5,0),point.add(0,-3,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        if(hit.getType()!=HitResult.Type.BLOCK||hit.getDirection()!=Direction.UP)return null;
        var at=new Vec3(point.x,hit.getLocation().y,point.z);
        return p.serverLevel().getFluidState(BlockPos.containing(at.add(0,.01,0))).isEmpty()&&clearBox(p,at)?at:null;
    }
    private static boolean clearBox(ServerPlayer p,Vec3 at){return !p.serverLevel().getBlockCollisions(p,p.getBoundingBox().move(at.subtract(p.position())).deflate(.001)).iterator().hasNext();}
    private static boolean clearSegment(ServerPlayer p,Vec3 from,Vec3 to){int n=Math.max(1,(int)Math.ceil(from.distanceTo(to)/.15));for(int i=1;i<=n;i++)if(!clearBox(p,from.lerp(to,(double)i/n)))return false;return true;}
    private static void move(ServerPlayer p,Vec3 at){p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;if(p.position().distanceToSqr(at)>.00001)
        p.teleportTo(p.serverLevel(),at.x,at.y,at.z,Set.of(),p.getYRot(),p.getXRot());}
    private static void blood(Action a){var at=embed(a.target,a.yaw);a.owner.serverLevel().sendParticles(new DustParticleOptions(new Vector3f(.83f,.02f,.06f),1),at.x,at.y,at.z,12,.17,.17,.17,.03);}
    private static void sound(Action a,net.minecraft.sounds.SoundEvent event,float volume,float pitch){a.owner.serverLevel().playSound(null,a.owner.blockPosition(),event,SoundSource.PLAYERS,volume,pitch);}
    public void cancel(ServerPlayer p){var a=actions.remove(p.getUUID());if(a!=null){p.fallDistance=0;send(a,true);sync(p);}}
    public void forget(Entity e){for(var a:new ArrayList<>(actions.values()))if(a.owner==e||a.target==e)cancel(a.owner);sent.remove(e.getUUID());}
    public void clear(){for(var a:new ArrayList<>(actions.values()))cancel(a.owner);actions.clear();sent.clear();}
    public void sync(ServerPlayer p){var a=actions.get(p.getUUID());var status=new KnifePursuitStatusPayload(available(p),a==null?0:a.phase,a==null?0:age(p),KnifePursuitState.get(p).cooldown(now(p)));
        if(!status.equals(sent.get(p.getUUID()))&&ServerPlayNetworking.canSend(p,KnifePursuitStatusPayload.TYPE)){ServerPlayNetworking.send(p,status);sent.put(p.getUUID(),status);}}
    private void broadcast(ServerPlayer p){var a=actions.get(p.getUUID());if(a!=null)send(a,false);}
    private void send(Action a,boolean ending){var viewers=new HashSet<>(PlayerLookup.tracking(a.owner));viewers.add(a.owner);if(a.target!=null){viewers.addAll(PlayerLookup.tracking(a.target));if(a.target instanceof ServerPlayer p)viewers.add(p);}for(var viewer:viewers)send(a,viewer,ending);}
    public void send(ServerPlayer p,ServerPlayer viewer){var a=actions.get(p.getUUID());if(a!=null)send(a,viewer,false);}
    private void send(Action a,ServerPlayer viewer,boolean ending){if(ServerPlayNetworking.canSend(viewer,KnifePursuitVisualPayload.TYPE))ServerPlayNetworking.send(viewer,new KnifePursuitVisualPayload(a.owner.getUUID(),a.target==null?NONE:a.target.getUUID(),ending?0:a.phase,(int)Math.clamp(now(a.owner)-a.started,0,KnifePursuitRules.MAX_HOLD),a.yaw,a.throwOrigin,a.embeddedAt,a.destination,a.embedded&&!a.extracted));}
}
