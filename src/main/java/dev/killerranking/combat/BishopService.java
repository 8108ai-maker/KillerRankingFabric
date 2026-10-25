package dev.killerranking.combat;
import dev.killerranking.*;
import dev.killerranking.data.*;
import dev.killerranking.entity.BishopFollower;
import dev.killerranking.network.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class BishopService {
    private static final ResourceLocation WEAKNESS=KillerRankingMod.id("bishop_personal_damage");
    private static final class Squad {
        final long started,expires,readyAt;final ResourceKey<Level> dimension;final List<UUID> units=new ArrayList<>();final int attackStamp,hurtStamp,level,formationOffset;
        boolean spawned;Squad(ServerPlayer p,int offset){started=now(p);expires=started+BishopRules.CAST+BishopRules.DURATION;level=PlayerProfiles.get(p).traitLevel();readyAt=started+BishopRules.cooldown(level);formationOffset=offset;dimension=p.level().dimension();attackStamp=p.getLastHurtMobTimestamp();hurtStamp=p.getLastHurtByMobTimestamp();}
    }
    private final Map<UUID,List<Squad>> squads=new HashMap<>();
    private final BishopDesignations designations=new BishopDesignations();
    private final Map<UUID,Long> designationRequests=new HashMap<>();
    public static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    public static boolean branch(ServerPlayer p){return CleaveService.branch(p);}
    public boolean eligible(ServerPlayer p){return branch(p)&&BishopState.get(p).unlocked()&&TraitActivation.enabled(p)&&p.isAlive()&&!p.isSpectator();}
    public static boolean otherOwned(ServerPlayer p){return RushState.get(p).unlocked()||CheckmateState.get(p).unlocked()||RookState.get(p).unlocked()||CleaveState.get(p).unlocked();}
    public void request(ServerPlayer p,BishopRequestPayload r){switch(r.operation()){case BishopRequestPayload.SUMMON->summon(p);case BishopRequestPayload.BARRAGE->KillerRankingMod.BISHOP_BARRAGE.start(p);case BishopRequestPayload.DESIGNATE->designate(p);case BishopRequestPayload.UNLOCK->unlock(p);default->{}}}
    public void unlock(ServerPlayer p){
        if(!branch(p)||!p.isAlive()||p.isSpectator()||KillerRankingMod.ROOK.locked(p))return;
        if(BishopState.get(p).unlocked()){sync(p);return;}
        if(otherOwned(p)){dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_DENY,.8f,1f);p.sendSystemMessage(Component.literal("§c투기 스킬은 하나만 해금할 수 있습니다. 나이트·룩·폰 6·비숍 중 선택하세요."));return;}
        var profile=PlayerProfiles.get(p);
        if(profile.pointUnits()<KillerPoints.whole(BishopRules.COST)){dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_DENY,.8f,1f);p.sendSystemMessage(Component.literal("§c비숍 해금에는 1,000포인트가 필요합니다."));return;}
        PlayerProfiles.set(p,profile.addPoints(-BishopRules.COST));dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_PURCHASE,1f,1f);setOwned(p,true);
        p.sendSystemMessage(Component.literal("§d비숍 해금! Z 신의 부름 / X 상대지정 / C 형제들의 도움"));
    }
    public void setOwned(ServerPlayer p,boolean owned){
        KillerRankingMod.BISHOP_BARRAGE.cancel(p);
        dismiss(p);
        if(owned){
            KillerRankingMod.ROOK.cancel(p);RookState.set(p,RookState.get(p).owned(false));
            KillerRankingMod.CLEAVE.cancel(p);CleaveState.set(p,CleaveState.get(p).owned(false));
            KillerRankingMod.CHECKMATE.cancel(p);CheckmateState.set(p,CheckmateState.get(p).owned(false));
            KillerRankingMod.RUSH.cancel(p);RushState.set(p,new RushState(false,RushState.get(p).readyAt()));
            SpiritState.set(p,new SpiritState(SpiritState.get(p).branch(),false));
        }
        BishopState.set(p,BishopState.get(p).owned(owned));KillerRankingMod.assignAndSync(p);
    }
    private List<Squad> waves(ServerPlayer p){return squads.getOrDefault(p.getUUID(),List.of());}
    public boolean casting(ServerPlayer p){return waves(p).stream().anyMatch(s->!s.spawned);}
    public int count(ServerPlayer p){return units(p).size();}
    public List<BishopFollower> units(ServerPlayer p){return waves(p).stream().filter(s->s.dimension.equals(p.level().dimension())).flatMap(s->s.units.stream()).map(p.serverLevel()::getEntity).filter(e->e instanceof BishopFollower&&e.isAlive()).map(e->(BishopFollower)e).toList();}
    public boolean manages(ServerPlayer p,UUID entity){return waves(p).stream().anyMatch(s->s.dimension.equals(p.level().dimension())&&s.units.contains(entity));}
    public void designate(ServerPlayer p){
        if(!eligible(p)||KillerRankingMod.ROOK.locked(p))return;
        // Limit repeated packet work, without tying designation to either skill cooldown.
        if(now(p)-designationRequests.getOrDefault(p.getUUID(),now(p)-4)<4)return;
        designationRequests.put(p.getUUID(),now(p));
        var target=BishopDesignationTargeting.selected(p);
        if(target==null){p.displayClientMessage(Component.literal("§7바라보는 공격 가능한 대상이 없습니다."),true);return;}
        var old=designations.set(p.getUUID(),target.getUUID(),target.level().dimension().location().toString());
        if(old!=null&&!old.equals(designations.get(p.getUUID())))releaseEffect(p.server,old);
        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(KillerRankingMod.BISHOP_DESIGNATION,-1,0,false,false,true),p);
        p.displayClientMessage(Component.literal("§d상대지정: ").append(target.getName()),true);
        p.serverLevel().playSound(null,p.blockPosition(),dev.killerranking.KillerRankingSounds.BISHOP_VANISH,SoundSource.PLAYERS,.6f,.9f);
        sync(p);
    }
    public LivingEntity designated(ServerPlayer p){
        var mark=designations.get(p.getUUID());
        if(!eligible(p)||mark==null||!mark.dimension().equals(p.level().dimension().location().toString()))return null;
        var entity=p.serverLevel().getEntity(mark.target());
        return entity instanceof LivingEntity target&&BishopTargets.allowed(p,target)&&target.hasEffect(KillerRankingMod.BISHOP_DESIGNATION)?target:null;
    }
    private void releaseEffect(MinecraftServer server,BishopDesignations.Mark mark){
        if(mark==null||designations.used(mark))return;
        for(var level:server.getAllLevels())if(mark.dimension().equals(level.dimension().location().toString())){
            var entity=level.getEntity(mark.target());if(entity instanceof LivingEntity target)target.removeEffect(KillerRankingMod.BISHOP_DESIGNATION);return;
        }
    }
    public void clearDesignation(ServerPlayer p){releaseEffect(p.server,designations.remove(p.getUUID()));}
    public void targetRemoved(LivingEntity target){
        var mark=new BishopDesignations.Mark(target.getUUID(),target.level().dimension().location().toString());
        for(var owner:designations.owners())if(mark.equals(designations.get(owner))){
            designations.remove(owner);var player=target.getServer().getPlayerList().getPlayer(owner);if(player!=null)sync(player);
        }
        target.removeEffect(KillerRankingMod.BISHOP_DESIGNATION);
    }
    public void cleanOrphanEffect(LivingEntity target){
        if(!designations.used(new BishopDesignations.Mark(target.getUUID(),target.level().dimension().location().toString())))
            target.removeEffect(KillerRankingMod.BISHOP_DESIGNATION);
    }
    public void summon(ServerPlayer p){
        refresh(p);
        if(!eligible(p)||p.isPassenger()||p.isUsingItem()||SkillCombatLock.blocksAttack(p)||p.isInWaterOrBubble()||p.isInLava()||BishopState.get(p).cooldown(now(p))>0||casting(p))return;
        if(count(p)+BishopRules.count(PlayerProfiles.get(p).traitLevel())>BishopRules.MAX_UNITS){p.sendSystemMessage(Component.literal("§c현재 형제들이 모두 전투 중입니다."),true);return;}
        int offset=waves(p).stream().mapToInt(s->s.formationOffset+BishopRules.count(s.level)).max().orElse(0);
        var squad=new Squad(p,offset);squads.computeIfAbsent(p.getUUID(),id->new ArrayList<>()).add(squad);var old=BishopState.get(p);BishopState.set(p,new BishopState(true,squad.readyAt,old.barrageReadyAt()));
        p.serverLevel().playSound(null,p.blockPosition(),dev.killerranking.KillerRankingSounds.BISHOP_SUMMON,SoundSource.PLAYERS,1f,1f);sync(p);
    }
    public void effects(ServerPlayer p){
        var attribute=p.getAttribute(Attributes.ATTACK_DAMAGE);
        if(!eligible(p)){attribute.removeModifier(WEAKNESS);return;}
        if(!attribute.hasModifier(WEAKNESS))attribute.addTransientModifier(new AttributeModifier(WEAKNESS,BishopRules.PERSONAL_DAMAGE-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    public void refresh(ServerPlayer p){
        effects(p);if(designations.get(p.getUUID())!=null&&designated(p)==null)clearDesignation(p);
        var list=squads.get(p.getUUID());if(list==null)return;
        if(!eligible(p)||list.stream().anyMatch(s->!s.dimension.equals(p.level().dimension()))){dismiss(p);return;}
        for(var iterator=list.iterator();iterator.hasNext();){var squad=iterator.next();
            if(now(p)>=squad.expires){discard(p,squad);iterator.remove();continue;}
            if(squad.spawned){squad.units.removeIf(id->{var entity=p.serverLevel().getEntity(id);return entity==null||!entity.isAlive();});if(squad.units.isEmpty())iterator.remove();}
        }
        if(list.isEmpty())squads.remove(p.getUUID());
    }
    private void spawn(ServerPlayer p,Squad squad){
        squad.spawned=true;int desired=BishopRules.count(squad.level);
        for(int index=0;index<desired;index++){
            Vec3 floor=null;
            for(int attempt=0;attempt<18&&floor==null;attempt++){
                double angle=(index+attempt*.37)*Math.PI/3,radius=1.8+attempt*.12;
                var at=p.position().add(Math.cos(angle)*radius,0,Math.sin(angle)*radius);
                for(int dy=2;dy>=-2;dy--){var pos=BlockPos.containing(at).offset(0,dy,0);var block=p.serverLevel().getBlockState(pos.below());
                    var candidate=new Vec3(at.x,pos.getY(),at.z);
                    if(!block.getCollisionShape(p.serverLevel(),pos.below()).isEmpty()&&p.serverLevel().getWorldBorder().isWithinBounds(pos)
                        &&p.serverLevel().getBlockState(pos).getFluidState().isEmpty()&&p.serverLevel().noCollision(new net.minecraft.world.phys.AABB(candidate.x-.3,candidate.y,candidate.z-.3,candidate.x+.3,candidate.y+1.9,candidate.z+.3))){floor=candidate;break;}
                }
            }
            if(floor==null)continue;
            var unit=KillerRankingMod.BISHOP_FOLLOWER.create(p.serverLevel());if(unit==null)continue;
            unit.summon(p,squad.expires,squad.formationOffset+index,squad.level);unit.armAt(squad.attackStamp,squad.hurtStamp);unit.moveTo(floor.x,floor.y,floor.z,p.getYRot(),0);
            if(p.serverLevel().addFreshEntity(unit)){squad.units.add(unit.getUUID());p.serverLevel().sendParticles(ParticleTypes.WITCH,floor.x,floor.y+.7,floor.z,14,.25,.65,.25,.05);}
        }
        if(squad.units.isEmpty()){var old=BishopState.get(p);if(old.readyAt()==squad.readyAt)BishopState.set(p,new BishopState(true,0,old.barrageReadyAt()));p.sendSystemMessage(Component.literal("§c부하가 설 공간이 없습니다. 넓은 곳에서 사용하세요."),true);}
    }
    public void tick(MinecraftServer server){for(var p:server.getPlayerList().getPlayers()){
        refresh(p);for(var squad:waves(p))if(!squad.spawned&&now(p)-squad.started>=BishopRules.CAST)spawn(p,squad);
        if(server.getTickCount()%10==0)sync(p);
    }}
    public void send(ServerPlayer p,ServerPlayer viewer){if(ServerPlayNetworking.canSend(viewer,BishopStatusPayload.TYPE))ServerPlayNetworking.send(viewer,status(p));KillerRankingMod.BISHOP_BARRAGE.send(p,viewer);}
    private BishopStatusPayload status(ServerPlayer p){var state=BishopState.get(p);var list=waves(p);
        int remaining=list.stream().filter(s->s.spawned&&!s.units.isEmpty()).mapToInt(s->(int)Math.clamp(s.expires-now(p),0,BishopRules.DURATION)).min().orElse(0);
        int cast=list.stream().filter(s->!s.spawned).mapToInt(s->(int)Math.clamp(BishopRules.CAST-(now(p)-s.started),0,BishopRules.CAST)).max().orElse(0);
        var target=designated(p);String targetName=target==null?"":target.getName().getString();
        return new BishopStatusPayload(p.getUUID(),branch(p),state.unlocked(),otherOwned(p),eligible(p),count(p),remaining,state.cooldown(now(p)),cast,KillerRankingMod.BISHOP_BARRAGE.remaining(p),state.barrageCooldown(now(p)),targetName);}
    public void sync(ServerPlayer p){var value=status(p);if(ServerPlayNetworking.canSend(p,BishopStatusPayload.TYPE))ServerPlayNetworking.send(p,value);for(var viewer:PlayerLookup.tracking(p))if(ServerPlayNetworking.canSend(viewer,BishopStatusPayload.TYPE))ServerPlayNetworking.send(viewer,value);}
    private void discard(ServerPlayer p,Squad squad){var level=p.server.getLevel(squad.dimension);if(level!=null)for(var id:squad.units){var e=level.getEntity(id);if(e!=null)e.discard();}}
    public void dismiss(ServerPlayer p){clearDesignation(p);var list=squads.remove(p.getUUID());if(list!=null)for(var squad:list)discard(p,squad);}
    public void forget(ServerPlayer p){designationRequests.remove(p.getUUID());KillerRankingMod.BISHOP_BARRAGE.cancel(p);dismiss(p);p.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(WEAKNESS);}
    public void clear(MinecraftServer server){for(var p:server.getPlayerList().getPlayers())forget(p);squads.clear();designations.clear();designationRequests.clear();KillerRankingMod.BISHOP_BARRAGE.clear();}
}
