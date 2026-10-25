package dev.killerranking.mixin;

import dev.killerranking.combat.InheritedWeaponEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Insert before Better Combat's client off-hand lookup by applying this HEAD injection after its mixin.
@Mixin(value = LivingEntity.class, priority = 900)
abstract class InheritedWeaponSpeedMixin {
    @Inject(method = "getAttributeValue", at = @At("HEAD"), cancellable = true)
    private void killerranking$pairedSpeed(Holder<Attribute> attribute, CallbackInfoReturnable<Double> result) {
        if (attribute.equals(Attributes.ATTACK_SPEED) && (Object) this instanceof Player player
            && InheritedWeaponEffects.paired(player)) {
            result.setReturnValue(InheritedWeaponEffects.pairedSpeed(player.getAttribute(attribute)));
        }
    }
}
