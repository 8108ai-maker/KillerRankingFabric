package dev.killerranking.combat;
import dev.killerranking.*;
import dev.killerranking.data.*;
import dev.killerranking.network.*;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class CleaveService {
    private static final class Action {
        long started;int phase=CleaveRules.CHARGING;boolean full,blocked,struck;
        final ResourceKey<Level> dimension;final ItemStack weapon;
        Vec3 origin,direction;
        final Set<UUID> hits=new HashSet<>();
        Action(ServerPlayer p){started=now(p);dimension=p.level().dimension();weapon=p.getMainHandItem();}
    }
    private final Map<UUID,Action> actions=new HashMap<>();
    private final Map<UUID,CleaveStatusPayload> sent=new HashMap<>();
    public static boolean branch(ServerPlayer p){return PlayerProfiles.get(p).trait()==Trait.FIGHTING_SPIRIT&&SpiritState.get(p).branch()==SpiritBranch.KING_AND_KILL;}
    private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    private static boolean eligible(ServerPlayer p){return branch(p)&&CleaveState.get(p).unlocked()&&TraitActivation.enabled(p)&&p.isAlive()&&!p.isSpectator()
        &&p.getMainHandItem().is(KillerRankingMod.PAWN6_KATANA)&&!p.isUsingItem()&&!p.isPassenger()&&!p.isInWaterOrBubble()&&!p.isInLava()&&!KillerRankingMod.ROOK.locked(p);}
    public void request(ServerPlayer p,CleaveRequestPayload r){switch(r.operation()){
        case CleaveRequestPayload.START->start(p);case CleaveRequestPayload.RELEASE->release(p);
        case CleaveRequestPayload.CANCEL->cancel(p);case CleaveRequestPayload.UNLOCK->unlock(p);
    }}
    public void unlock(ServerPlayer p){
        if(!branch(p)||!p.isAlive()||p.isSpectator()||KillerRankingMod.ROOK.locked(p))return;
        if(CleaveState.get(p).unlocked()){sync(p);return;}
        if(BishopState.get(p).unlocked()||RushState.get(p).unlocked()||CheckmateState.get(p).unlocked()||RookState.get(p).unlocked()){
            dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_DENY,.8f,1f);p.sendSystemMessage(Component.literal("§c투기 스킬은 하나만 해금할 수 있습니다. 나이트·룩·폰 6·비숍 중 하나를 선택하세요."));sync(p);return;
        }
        var profile=PlayerProfiles.get(p);
        if(profile.pointUnits()<KillerPoints.whole(CleaveRules.COST)){dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_DENY,.8f,1f);p.sendSystemMessage(Component.literal("§c일도양단 해금에는 1,000포인트가 필요합니다."));sync(p);return;}
        PlayerProfiles.set(p,profile.addPoints(-CleaveRules.COST));dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_PURCHASE,1f,1f);setOwned(p,true);
        p.sendSystemMessage(Component.literal("§f폰 6 스킬 해금 완료! 카타나를 들고 Z 일도양단 / X 꾹 눌러 자동 패링"));
    }
    public void setOwned(ServerPlayer p,boolean owned){
        cancel(p);
        if(owned){
            KillerRankingMod.BISHOP.forget(p);BishopState.set(p,BishopState.get(p).owned(false));
            KillerRankingMod.ROOK.cancel(p);RookState.set(p,RookState.get(p).owned(false));
            KillerRankingMod.CHECKMATE.cancel(p);CheckmateState.set(p,CheckmateState.get(p).owned(false));
            KillerRankingMod.RUSH.cancel(p);RushState.set(p,new RushState(false,RushState.get(p).readyAt()));
            SpiritState.set(p,new SpiritState(SpiritState.get(p).branch(),false));
        }
        CleaveState.set(p,CleaveState.get(p).owned(owned));KillerRankingMod.assignAndSync(p);
    }
    public void start(ServerPlayer p){
        if(!eligible(p)||!p.onGround()||active(p)||CleaveState.get(p).cooldown(now(p))>0||KillerRankingMod.ROOK.active(p)
            ||KillerRankingMod.CHECKMATE.active(p)||KillerRankingMod.RUSH.active(p)||KillerRankingMod.PARRY.window(p)>0)return;
        actions.put(p.getUUID(),new Action(p));broadcast(p);sync(p);
        p.serverLevel().playSound(null,p.blockPosition(),dev.killerranking.KillerRankingSounds.CLEAVE_CHARGE,SoundSource.PLAYERS,.9f,1f);
    }
    public void release(ServerPlayer p){
        var a=actions.get(p.getUUID());if(a==null||a.phase!=CleaveRules.CHARGING)return;
        if(!valid(p,a)||CleaveState.get(p).cooldown(now(p))>0){cancel(p);return;}
        a.full=CleaveRules.full(age(p));a.phase=CleaveRules.SLASHING;a.started=now(p);a.origin=p.position();
        var look=p.getLookAngle();a.direction=new Vec3(look.x,0,look.z).normalize();
        if(a.direction.lengthSqr()<.001){double yaw=Math.toRadians(p.getYRot());a.direction=new Vec3(-Math.sin(yaw),0,Math.cos(yaw));}
        CleaveState.set(p,CleaveState.get(p).cast(now(p)));broadcast(p);sync(p);
        p.serverLevel().playSound(null,p.blockPosition(),dev.killerranking.KillerRankingSounds.CLEAVE_DASH,SoundSource.PLAYERS,.9f,a.full?.85f:1.0f);
    }
    private boolean valid(ServerPlayer p,Action a){return eligible(p)&&a.dimension.equals(p.level().dimension())&&a.weapon==p.getMainHandItem();}
    public boolean active(ServerPlayer p){return actions.containsKey(p.getUUID());}
    public boolean slashing(ServerPlayer p){return phase(p)==CleaveRules.SLASHING;}
    public int phase(ServerPlayer p){var a=actions.get(p.getUUID());return a==null?0:a.phase;}
    public int age(ServerPlayer p){var a=actions.get(p.getUUID());return a==null?0:(int)Math.clamp(now(p)-a.started,0,CleaveRules.MAX_HOLD);}
    public void refresh(ServerPlayer p){var a=actions.get(p.getUUID());if(a!=null&&!valid(p,a))cancel(p);}
    public void tick(MinecraftServer server){
        for(var p:server.getPlayerList().getPlayers()){
            refresh(p);var a=actions.get(p.getUUID());
            if(a!=null){
                int age=age(p);
                if(a.phase==CleaveRules.CHARGING){
                    if(age>=CleaveRules.MAX_HOLD){cancel(p);continue;}
                    if(age==CleaveRules.CHARGE)p.serverLevel().playSound(null,p.blockPosition(),dev.killerranking.KillerRankingSounds.PARRY_GUARD,SoundSource.PLAYERS,.8f,1.2f);
                    if(age%5==0)broadcast(p);
                }else if(age>=CleaveRules.CAST)cancel(p);
                else{
                    if(a.full){pin(p,a.origin);if(age>=CleaveRules.FULL_HIT&&!a.struck){a.struck=true;strike(p,a);}}
                    else{
                        if(age>0&&age<=CleaveRules.DASH_TICKS&&!a.blocked){
                            var before=p.position();p.move(MoverType.SELF,a.direction.scale(CleaveRules.DASH/CleaveRules.DASH_TICKS));
                            a.blocked=p.position().distanceToSqr(before)<.20;pin(p,p.position());
                        }
                        if(age>=4&&age<=CleaveRules.DASH_TICKS)strike(p,a);
                    }
                    if(age%5==0)broadcast(p);
                }
            }
            if(server.getTickCount()%2==0)sync(p);
        }
    }
    private static void pin(ServerPlayer p,Vec3 at){
        p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;
        p.teleportTo(p.serverLevel(),at.x,at.y,at.z,Set.of(RelativeMovement.Y_ROT,RelativeMovement.X_ROT),p.getYRot(),p.getXRot());
    }
    private void strike(ServerPlayer p,Action a){
        double range=a.full?CleaveRules.FULL_RANGE:CleaveRules.DASH_REACH;
        for(var e:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(range,2.25,range),
            e->e!=p&&e.isAlive()&&!e.isSpectator()&&e.isAttackable()&&!p.isAlliedTo(e)&&p.canAttack(e)&&!a.hits.contains(e.getUUID()))){
            var to=e.getBoundingBox().getCenter().subtract(p.position().add(0,1,0));
            double forward=to.dot(a.direction),side=to.x*a.direction.z-to.z*a.direction.x;
            if(!CleaveRules.inSlash(forward,side,to.y,a.full)||!p.hasLineOfSight(e))continue;
            a.hits.add(e.getUUID());
            if(RushDamage.hit(p,e,CleaveRules.damage(a.full),true)){
                e.knockback(a.full?.65:.35,-a.direction.x,-a.direction.z);
                p.serverLevel().sendParticles(ParticleTypes.CRIT,e.getX(),e.getY()+e.getBbHeight()*.55,e.getZ(),8,.15,.25,.15,.06);
            }
        }
        if(a.full||!a.struck){
            a.struck=true;a.weapon.hurtAndBreak(a.full?3:2,p,EquipmentSlot.MAINHAND);
            p.serverLevel().playSound(null,p.blockPosition(),dev.killerranking.KillerRankingSounds.CLEAVE_RELEASE,SoundSource.PLAYERS,a.full?1.1f:.8f,a.full?.9f:1.15f);
            if(a.full)p.serverLevel().sendParticles(ParticleTypes.FLASH,p.getX(),p.getY()+1,p.getZ(),1,0,0,0,0);
        }
    }
    public void cancel(ServerPlayer p){if(actions.remove(p.getUUID())!=null){broadcast(p);sync(p);}}
    public void forget(ServerPlayer p){cancel(p);sent.remove(p.getUUID());}
    public void clear(){actions.clear();sent.clear();}
    public void sync(ServerPlayer p){
        var state=CleaveState.get(p);var value=new CleaveStatusPayload(branch(p),state.unlocked(),BishopState.get(p).unlocked()||RushState.get(p).unlocked()||CheckmateState.get(p).unlocked()||RookState.get(p).unlocked(),phase(p),age(p),state.cooldown(now(p)));
        if(!value.equals(sent.get(p.getUUID()))&&ServerPlayNetworking.canSend(p,CleaveStatusPayload.TYPE)){ServerPlayNetworking.send(p,value);sent.put(p.getUUID(),value);}
    }
    private void broadcast(ServerPlayer p){send(p,p);for(var viewer:PlayerLookup.tracking(p))send(p,viewer);}
    public void send(ServerPlayer p,ServerPlayer viewer){
        var a=actions.get(p.getUUID());float yaw=a!=null&&a.direction!=null?(float)Math.toDegrees(Math.atan2(-a.direction.x,a.direction.z)):p.getYRot();
        var payload=new CleaveVisualPayload(p.getUUID(),phase(p),age(p),a!=null&&(a.full||a.phase==CleaveRules.CHARGING&&CleaveRules.full(age(p))),yaw);
        if(ServerPlayNetworking.canSend(viewer,CleaveVisualPayload.TYPE))ServerPlayNetworking.send(viewer,payload);
    }
}
