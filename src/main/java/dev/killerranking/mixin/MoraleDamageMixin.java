package dev.killerranking.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.killerranking.combat.MoraleRules;
import dev.killerranking.combat.MoraleService;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
abstract class MoraleDamageMixin {
    // Observe the final health loss, including lethal hits and absorption. Canceled/blocked hits give no morale.
    @WrapMethod(method = "hurt")
    private boolean killerranking$recordMorale(DamageSource source, float amount, Operation<Boolean> original) {
        LivingEntity victim = (LivingEntity) (Object) this;
        float health = victim.getHealth(), absorption = victim.getAbsorptionAmount();
        boolean shieldBlocked = victim.isDamageSourceBlocked(source);
        boolean hit = original.call(source, amount);
        if (hit && !shieldBlocked) MoraleService.afterHit(victim, source,
            MoraleRules.actualDamage(health, absorption, victim.getHealth(), victim.getAbsorptionAmount()));
        return hit;
    }
}
