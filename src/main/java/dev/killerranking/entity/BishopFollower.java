package dev.killerranking.entity;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Summoned human follower: no natural spawning, loot, reproduction or owner damage. */
public final class BishopFollower extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> LEAPING=SynchedEntityData.defineId(BishopFollower.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FIGHTING=SynchedEntityData.defineId(BishopFollower.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_TICK=SynchedEntityData.defineId(BishopFollower.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_STYLE=SynchedEntityData.defineId(BishopFollower.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LEAP_POSE=SynchedEntityData.defineId(BishopFollower.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> BARRAGE_ECHO=SynchedEntityData.defineId(BishopFollower.class,EntityDataSerializers.BOOLEAN);
    private UUID ownerId;private long expires;private int formation,attackDelay,leapDelay,flight,ownerAttackStamp,ownerHurtStamp;
    private Vec3 flightDirection=Vec3.ZERO;
    private int leapWindup,flightAge,landingTicks;
    private int strikeTick,strikeStyle,cycle,comboHits,repathTicks,stalledTicks,unseenTicks;
    private UUID comboVictim,combatTarget;
    private UUID designatedTarget;
    private Vec3 lastWalk=Vec3.ZERO,lastPath=Vec3.ZERO;
    private static final net.minecraft.resources.ResourceLocation COMBO_DAMAGE=KillerRankingMod.id("bishop_combo_contact");
    public BishopFollower(EntityType<? extends PathfinderMob> type,Level level){super(type,level);xpReward=0;}
    public static AttributeSupplier.Builder attributes(){return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,18).add(Attributes.MOVEMENT_SPEED,.62).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,32).add(Attributes.KNOCKBACK_RESISTANCE,.15);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(LEAPING,false);builder.define(FIGHTING,false);builder.define(ATTACK_TICK,0);builder.define(ATTACK_STYLE,1);builder.define(LEAP_POSE,0);builder.define(BARRAGE_ECHO,false);}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new EscortGoal());}
    public void summon(ServerPlayer p,long until,int index,int level){
        ownerId=p.getUUID();expires=until;formation=index;
        // Equipment contributes the dagger's four attack points on the next tick.
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(BishopRules.damage(level)-4);
        setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(KillerRankingMod.BISHOP_DAGGER));setDropChance(EquipmentSlot.MAINHAND,0);
        setPersistenceRequired();
        armAt(p.getLastHurtMobTimestamp(),p.getLastHurtByMobTimestamp());
    }
    public void armAt(int attackStamp,int hurtStamp){ownerAttackStamp=attackStamp;ownerHurtStamp=hurtStamp;}
    public void barrage(ServerPlayer p,long until,int index,int level){
        summon(p,until,index,level);entityData.set(BARRAGE_ECHO,true);entityData.set(FIGHTING,true);entityData.set(ATTACK_STYLE,BishopRules.COMBO);entityData.set(ATTACK_TICK,1);
        noPhysics=true;setNoGravity(true);setInvulnerable(true);
    }
    public boolean barrageEcho(){return entityData.get(BARRAGE_ECHO);}
    @Override public boolean isPushable(){return !barrageEcho()&&super.isPushable();}
    @Override public boolean isPickable(){return !barrageEcho()&&super.isPickable();}
    @Override public boolean isAttackable(){return !barrageEcho()&&super.isAttackable();}
    @Override public void push(Entity other){if(!barrageEcho())super.push(other);}
    public UUID ownerId(){return ownerId;}
    public ServerPlayer owner(){return ownerId==null||level().isClientSide?null:level().getServer().getPlayerList().getPlayer(ownerId);}
    public boolean leaping(){return entityData.get(LEAPING);}
    public boolean fighting(){return entityData.get(FIGHTING);}
    public int attackPoseTick(){return entityData.get(ATTACK_TICK);}
    public int attackStyle(){return entityData.get(ATTACK_STYLE);}
    public boolean striking(){return attackPoseTick()>0;}
    public int leapPoseTick(){return entityData.get(LEAP_POSE);}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public boolean doHurtTarget(Entity victim){
        if(barrageEcho())return false;
        var p=owner();if(!(victim instanceof LivingEntity e)||!BishopTargets.allowed(p,e))return false;
        e.setLastHurtByPlayer(p);
        boolean hit=super.doHurtTarget(victim);
        if(hit){var at=position().add(e.position().subtract(position()).normalize().scale(.65)).add(0,1.05,0);
            ((net.minecraft.server.level.ServerLevel)level()).sendParticles(KillerRankingMod.BISHOP_SLASH,at.x,at.y,at.z,1,0,0,0,0);
        }
        return hit;
    }
    @Override protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable(){return net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY;}
    @Override protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level,net.minecraft.world.damagesource.DamageSource source,boolean recentlyHit){ }
    @Override public boolean killedEntity(net.minecraft.server.level.ServerLevel level,LivingEntity victim){
        // Vanilla awards per-entity mission statistics to the direct killer separately from kill credit.
        var p=owner();if(p!=null&&victim!=p&&KillerRankingMod.BISHOP.eligible(p))p.killedEntity(level,victim);
        return super.killedEntity(level,victim);
    }
    public boolean leapAt(LivingEntity enemy,boolean force){
        if(!onGround()||leapWindup>0||flight>0||landingTicks>0||(!force&&leapDelay>0)||!BishopTargets.allowed(owner(),enemy)||!hasLineOfSight(enemy))return false;
        var offset=BishopTactics.lead(enemy.position(),enemy.getDeltaMovement(),2).subtract(position());double distance=offset.horizontalDistance();
        if(!BishopRules.canLeap(distance)||Math.abs(offset.y)>1.6||level().getBlockCollisions(this,getBoundingBox().move(offset.normalize().scale(.7))).iterator().hasNext())return false;
        flightDirection=new Vec3(offset.x,0,offset.z).normalize();
        leapWindup=BishopRules.LEAP_PREPARE;leapDelay=24;getNavigation().stop();
        entityData.set(ATTACK_STYLE,BishopRules.STAB);
        var v=getDeltaMovement();setDeltaMovement(0,v.y,0);entityData.set(LEAP_POSE,1);return true;
    }
    private void startStrike(int style){
        strikeStyle=style;strikeTick=1;comboHits=0;comboVictim=null;cycle++;
        entityData.set(ATTACK_TICK,1);entityData.set(ATTACK_STYLE,style);entityData.set(LEAP_POSE,0);
        landingTicks=0;getNavigation().stop();var v=getDeltaMovement();setDeltaMovement(0,v.y,0);
    }
    private void stopStrike(){strikeTick=0;entityData.set(ATTACK_TICK,0);attackDelay=3;}
    private void face(LivingEntity enemy){
        getLookControl().setLookAt(enemy,50,35);
        var offset=enemy.position().subtract(position());
        if(offset.horizontalDistanceSqr()>.0001){float yaw=(float)(Math.atan2(offset.z,offset.x)*180/Math.PI-90);
            setYRot(net.minecraft.util.Mth.approachDegrees(getYRot(),yaw,35));yBodyRot=getYRot();}
    }
    private void resetEncounter(LivingEntity enemy){
        combatTarget=enemy.getUUID();cycle=0;attackDelay=leapDelay=unseenTicks=stalledTicks=repathTicks=0;
        flight=leapWindup=landingTicks=strikeTick=comboHits=0;comboVictim=null;lastWalk=position();lastPath=Vec3.ZERO;
        getNavigation().stop();var v=getDeltaMovement();setDeltaMovement(0,v.y,0);
        entityData.set(ATTACK_TICK,0);entityData.set(LEAP_POSE,0);entityData.set(LEAPING,false);
    }
    private LivingEntity alignDesignation(ServerPlayer p){
        var marked=KillerRankingMod.BISHOP.designated(p);
        if(marked!=null){
            designatedTarget=marked.getUUID();
            if(getTarget()!=marked){setTarget(marked);resetEncounter(marked);}
            entityData.set(FIGHTING,true);
        }else if(designatedTarget!=null){
            designatedTarget=null;setTarget(null);combatTarget=null;
            strikeTick=flight=leapWindup=landingTicks=comboHits=0;comboVictim=null;
            getNavigation().stop();setDeltaMovement(0,getDeltaMovement().y,0);
            entityData.set(FIGHTING,false);entityData.set(ATTACK_TICK,0);entityData.set(LEAP_POSE,0);entityData.set(LEAPING,false);
        }
        return marked;
    }
    private LivingEntity nextThreat(ServerPlayer p,LivingEntity excluded){
        return level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(16),e->e!=excluded
            &&e instanceof net.minecraft.world.entity.monster.Enemy&&BishopTargets.allowed(p,e)
            &&(hasLineOfSight(e)||getNavigation().createPath(e,0)!=null)).stream()
            .min(Comparator.comparingDouble(e->distanceToSqr(e)+(e instanceof Mob mob&&mob.getTarget()==p?-32:0)))
            .orElse(null);
    }
    private void pursue(LivingEntity enemy){
        double gap=enemy.position().subtract(position()).horizontalDistance();
        if(gap>3&&position().distanceToSqr(lastWalk)<.003)stalledTicks++;else stalledTicks=0;
        lastWalk=position();
        var aim=BishopTactics.lead(enemy.position(),enemy.getDeltaMovement(),3);
        boolean crowded=hasLineOfSight(enemy)&&gap<6&&level().getEntitiesOfClass(BishopFollower.class,getBoundingBox().inflate(.8),e->e!=this&&Objects.equals(ownerId,e.ownerId())).size()>0;
        if(crowded||stalledTicks>=16)aim=BishopTactics.approach(position(),aim,formation+(stalledTicks/16));
        if(--repathTicks<=0||getNavigation().isDone()||aim.distanceToSqr(lastPath)>.64){
            boolean found=getNavigation().moveTo(aim.x,aim.y,aim.z,BishopRules.RUN_SPEED);
            if(!found)getNavigation().moveTo(enemy,BishopRules.RUN_SPEED);
            repathTicks=4;lastPath=aim;
        }
    }
    @Override protected void customServerAiStep(){
        super.customServerAiStep();var p=owner();
        if(barrageEcho()){
            if(p==null||p.level()!=level()||!KillerRankingMod.BISHOP.eligible(p)||!KillerRankingMod.BISHOP_BARRAGE.manages(p,getUUID())||p.server.overworld().getGameTime()>=expires){
                discard();return;
            }
            getNavigation().stop();setDeltaMovement(Vec3.ZERO);entityData.set(ATTACK_TICK,(int)Math.clamp(BishopBarrageRules.ECHO_TICKS-(expires-p.server.overworld().getGameTime()),1,BishopBarrageRules.ECHO_TICKS));return;
        }
        if(p==null||p.level()!=level()||!KillerRankingMod.BISHOP.eligible(p)||!KillerRankingMod.BISHOP.manages(p,getUUID())
            ||p.server.overworld().getGameTime()>=expires){discard();return;}
        alignDesignation(p);
        if(attackDelay>0)attackDelay--;if(leapDelay>0)leapDelay--;
        if(getTarget()!=null&&!BishopTargets.allowed(p,getTarget()))setTarget(null);
        if(strikeTick>0){
            var enemy=getTarget();
            if(!BishopTargets.allowed(p,enemy)||distanceToSqr(enemy)>49){stopStrike();}
            else{
                face(enemy);var gap=enemy.position().subtract(position());double distance=gap.horizontalDistance();
                if(hasLineOfSight(enemy)){
                    var aim=BishopTactics.lead(enemy.position(),enemy.getDeltaMovement(),2).subtract(position());
                    var step=new Vec3(aim.x,0,aim.z).normalize().scale(BishopTactics.step(distance,strikeStyle==BishopRules.STAB_COMBO));
                    var v=getDeltaMovement();setDeltaMovement(step.add(0,v.y,0));hasImpulse=true;
                }
                if(BishopRules.isHitTick(strikeStyle,strikeTick)&&hasLineOfSight(enemy)
                    &&distanceToSqr(enemy)<Math.pow(BishopRules.reach(strikeStyle,enemy.getBbWidth()),2)){
                    // Confirmed contacts of the same multi-hit sequence pass native armor/parry checks.
                    if(BishopRules.repeated(strikeStyle)&&comboHits>0&&enemy.getUUID().equals(comboVictim))enemy.invulnerableTime=0;
                    swing(InteractionHand.MAIN_HAND);boolean hit;
                    if(BishopRules.repeated(strikeStyle)){
                        var damage=getAttribute(Attributes.ATTACK_DAMAGE);
                        damage.addTransientModifier(new AttributeModifier(COMBO_DAMAGE,BishopRules.COMBO_DAMAGE_SCALE-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                        try{hit=doHurtTarget(enemy);}finally{damage.removeModifier(COMBO_DAMAGE);}
                    }else hit=doHurtTarget(enemy);
                    if(hit&&BishopRules.repeated(strikeStyle)){comboVictim=enemy.getUUID();comboHits++;}
                }
                entityData.set(ATTACK_TICK,strikeTick++);
                if(strikeTick>BishopRules.attackLength(strikeStyle))stopStrike();
            }
        }
        if(landingTicks>0){entityData.set(LEAP_POSE,18-landingTicks);if(--landingTicks==0)entityData.set(LEAP_POSE,0);}
        if(leapWindup>0){
            if(!BishopTargets.allowed(p,getTarget())||!hasLineOfSight(getTarget())){leapWindup=0;entityData.set(LEAP_POSE,0);}
            else if(--leapWindup==0){
                var offset=BishopTactics.lead(getTarget().position(),getTarget().getDeltaMovement(),2).subtract(position());flightDirection=new Vec3(offset.x,0,offset.z).normalize();
                flight=10;flightAge=0;setDeltaMovement(flightDirection.scale(BishopRules.LEAP_SPEED).add(0,BishopRules.LEAP_UP,0));
                hasImpulse=true;hurtMarked=true;entityData.set(LEAPING,true);entityData.set(LEAP_POSE,5);
            }else entityData.set(LEAP_POSE,1+(int)(3.0*(BishopRules.LEAP_PREPARE-leapWindup)/BishopRules.LEAP_PREPARE));
        }
        if(flight>0){
            flight--;flightAge++;fallDistance=0;entityData.set(LEAP_POSE,Math.min(13,4+flightAge));
            var enemy=getTarget();var v=getDeltaMovement();
            boolean valid=BishopTargets.allowed(p,enemy)&&hasLineOfSight(enemy);
            if(valid){var aim=BishopTactics.lead(enemy.position(),enemy.getDeltaMovement(),2).subtract(position());flightDirection=new Vec3(aim.x,0,aim.z).normalize();}
            double remaining=valid?enemy.position().subtract(position()).dot(flightDirection)-1.35:0;
            if(!valid||remaining<=.1||flightAge>2&&(onGround()||horizontalCollision)||flight==0){
                flight=0;entityData.set(LEAPING,false);setDeltaMovement(0,v.y,0);landingTicks=4;entityData.set(LEAP_POSE,14);repathTicks=0;
            }else setDeltaMovement(flightDirection.scale(Math.clamp(remaining,0,BishopRules.LEAP_SPEED)).add(0,v.y,0));
        }
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);if(ownerId!=null)tag.putUUID("BishopOwner",ownerId);tag.putLong("BishopExpires",expires);tag.putInt("BishopFormation",formation);tag.putBoolean("BishopEcho",barrageEcho());}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);ownerId=tag.hasUUID("BishopOwner")?tag.getUUID("BishopOwner"):null;expires=tag.getLong("BishopExpires");formation=tag.getInt("BishopFormation");entityData.set(BARRAGE_ECHO,tag.getBoolean("BishopEcho"));if(barrageEcho()){noPhysics=true;setNoGravity(true);}}
    private final class EscortGoal extends Goal {
        EscortGoal(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){return true;}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){
            if(barrageEcho())return;
            var p=owner();if(p==null)return;var designated=alignDesignation(p);LivingEntity triggered=null;
            if(ownerHurtStamp!=p.getLastHurtByMobTimestamp()){
                ownerHurtStamp=p.getLastHurtByMobTimestamp();if(BishopTargets.allowed(p,p.getLastHurtByMob()))triggered=p.getLastHurtByMob();
            }
            if(ownerAttackStamp!=p.getLastHurtMobTimestamp()){
                ownerAttackStamp=p.getLastHurtMobTimestamp();if(triggered==null&&BishopTargets.allowed(p,p.getLastHurtMob()))triggered=p.getLastHurtMob();
            }
            if(designated==null&&triggered!=null){setTarget(triggered);entityData.set(FIGHTING,true);}
            if(designated==null&&fighting()&&(!BishopTargets.allowed(p,getTarget())||p.distanceToSqr(getTarget())>40*40)){
                setTarget(nextThreat(p,null));if(getTarget()==null)entityData.set(FIGHTING,false);
            }
            var enemy=getTarget();
            if(enemy!=null&&BishopTargets.allowed(p,enemy)&&(designated!=null||p.distanceToSqr(enemy)<=40*40)){
                if(!enemy.getUUID().equals(combatTarget))resetEncounter(enemy);
                face(enemy);if(hasLineOfSight(enemy))unseenTicks=0;else unseenTicks++;
                if(designated==null&&unseenTicks>60&&stalledTicks>=16&&Math.floorMod(tickCount,10)==Math.floorMod(formation,10)){var alternative=nextThreat(p,enemy);if(alternative!=null){setTarget(alternative);resetEncounter(alternative);return;}}
                if(strikeTick>0||flight>0||leapWindup>0||landingTicks>0){getNavigation().stop();return;}
                var offset=enemy.position().subtract(position());double gap=offset.horizontalDistance();
                double escaping=enemy.getDeltaMovement().dot(new Vec3(offset.x,0,offset.z).normalize());
                int planned=BishopTactics.attack(cycle,formation,gap,escaping);
                if(attackDelay==0&&distanceToSqr(enemy)<Math.pow(BishopRules.reach(planned,enemy.getBbWidth()),2)&&hasLineOfSight(enemy)){
                    startStrike(planned);return;
                }
                if(gap>=5&&leapAt(enemy,false))return;
                if(gap<=1.25&&hasLineOfSight(enemy)){getNavigation().stop();var v=getDeltaMovement();setDeltaMovement(0,v.y,0);return;}
                pursue(enemy);
            }else{
                setTarget(null);combatTarget=null;stalledTicks=unseenTicks=0;if(flight>0)return;
                double angle=formation*Math.PI/3+Math.toRadians(p.getYRot());
                double radius=2.1+.85*Math.floorMod(formation/6,5);
                Vec3 at=p.position().add(Math.cos(angle)*radius,0,Math.sin(angle)*radius);
                if(position().distanceToSqr(at)>1.6)getNavigation().moveTo(at.x,at.y,at.z,1.1);else getNavigation().stop();
                getLookControl().setLookAt(p,25,25);
            }
        }
    }
}
