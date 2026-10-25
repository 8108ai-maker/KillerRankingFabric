package dev.killerranking.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Only the current rush hit skips vanilla hurt cooldown. Shields, parries and immunity still apply. */
public final class RushDamage {
    private record Strike(LivingEntity target,boolean finisher) { }
    private static final ThreadLocal<Strike> CURRENT=new ThreadLocal<>();
    public static boolean current(LivingEntity entity) { var s=CURRENT.get();return s!=null && s.target()==entity; }
    public static boolean suppressKnockback(LivingEntity entity) { var s=CURRENT.get();return s!=null && s.target()==entity && !s.finisher(); }
    public static boolean hit(ServerPlayer attacker,LivingEntity target,boolean finisher) {
        return hit(attacker,target,RushRules.DAMAGE,finisher);
    }
    /** Scoped rapid hits also serve Rook's sequence, retaining unrelated vanilla immunity windows. */
    public static boolean hit(ServerPlayer attacker,LivingEntity target,float amount,boolean finisher) {
        return hit(attacker,target,attacker.damageSources().playerAttack(attacker),amount,finisher);
    }
    public static boolean hit(ServerPlayer attacker,LivingEntity target,net.minecraft.world.damagesource.DamageSource source,float amount,boolean finisher) {
        var previous=CURRENT.get();int oldTime=target.invulnerableTime;
        float oldDamage=((RushDamageAccess)target).killerranking$lastDamage();
        CURRENT.set(new Strike(target,finisher));
        try { return target.hurt(source,amount); }
        finally {
            // Preserve an existing vanilla window for unrelated attacks; do not clear it globally.
            if(oldTime>0) { target.invulnerableTime=oldTime;((RushDamageAccess)target).killerranking$lastDamage(oldDamage); }
            if(previous==null) CURRENT.remove();else CURRENT.set(previous);
        }
    }
    public interface RushDamageAccess { float killerranking$lastDamage();void killerranking$lastDamage(float value); }
    private RushDamage() { }
}
