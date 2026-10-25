package dev.killerranking.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.killerranking.combat.InheritedWeaponEffects;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Optional compatibility: both overloads keep the original pair during the off-hand callback. */
@Pseudo
@Mixin(targets = "net.bettercombat.logic.PlayerAttackHelper", remap = false)
abstract class InheritedWeaponSwapMixin {
    @ModifyVariable(method = "swapHandAttributes*", at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false, require = 2)
    private static Runnable killerranking$preservePair(Runnable action, @Local(argsOnly = true) Player player) {
        return InheritedWeaponEffects.preservePair(player, action);
    }
}
