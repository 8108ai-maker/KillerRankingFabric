package dev.killerranking.mixin;

import dev.killerranking.Trait;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.TraitLevels;
import dev.killerranking.combat.CombatRules;
import dev.killerranking.data.PlayerProfiles;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    // Preserve each scoped bleed's fixed pulse after armor and enchantment mitigation.
    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"), cancellable = true)
    private void killerranking$bleeding(DamageSource source, float amount, CallbackInfoReturnable<Float> result) {
        if (dev.killerranking.combat.BleedingDamage.current((LivingEntity) (Object) this) && result.getReturnValue() > 0)
            result.setReturnValue(dev.killerranking.combat.BleedingDamage.amount((LivingEntity)(Object)this));
    }
	@ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float killerranking$applyFightingSpirit(float amount, DamageSource source) {
        if (dev.killerranking.combat.BleedingDamage.current((LivingEntity) (Object) this)) return amount;
        if (source.getEntity() instanceof ServerPlayer attacker) {
            amount *= (float) dev.killerranking.data.MoraleState.get(attacker);
            if (source.getDirectEntity() == attacker && !source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)
                && dev.killerranking.combat.KnightWeaponEffects.striking(attacker)) amount *= 2;
        }
		if ((Object) this instanceof ServerPlayer defender && source.getEntity() != null && PlayerProfiles.get(defender).trait() == Trait.FIGHTING_SPIRIT) {
			float reduction = dev.killerranking.combat.SpiritEffects.defense(defender) / 100.0F;
			amount *= 1.0F - reduction;
		}
        if (source.getEntity() instanceof ServerPlayer attacker)
            amount *= 1.0F + (float)(dev.killerranking.combat.MadnessEffects.damageBonus(attacker)/100.0);
        if (source.getEntity() instanceof ServerPlayer attacker && source.getDirectEntity() == attacker
            && !source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)
            && dev.killerranking.combat.InheritedWeaponEffects.madnessBonus(attacker)) {
            amount *= (float) dev.killerranking.combat.InheritedWeaponEffects.DAMAGE_MULTIPLIER;
        }
		return amount;
	}
}
