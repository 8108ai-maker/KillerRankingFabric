package dev.killerranking.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Native hurt gates still decide whether the blow connects. Only its final damage is replaced. */
public final class CheckmateDamage {
    private static final ThreadLocal<LivingEntity> CURRENT=new ThreadLocal<>();
    public static boolean current(LivingEntity target) { return CURRENT.get()==target; }
    public static float finalDamage(LivingEntity target,float damage) {
        return CURRENT.get()==target&&damage>0
            ?CheckmateRules.damageToHalfHeart(target.getHealth(),target.getAbsorptionAmount()):damage;
    }
    public static boolean hit(ServerPlayer attacker,LivingEntity target) {
        float damage=CheckmateRules.damageToHalfHeart(target.getHealth(),target.getAbsorptionAmount());
        if(damage<=0) return false;
        var previous=CURRENT.get();CURRENT.set(target);
        try { return target.hurt(attacker.damageSources().playerAttack(attacker),damage); }
        finally { if(previous==null) CURRENT.remove();else CURRENT.set(previous); }
    }
    private CheckmateDamage() { }
}
