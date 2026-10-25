package dev.killerranking.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.killerranking.combat.CheckmateDamage;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
abstract class CheckmatePlayerDamageMixin {
    @ModifyExpressionValue(method="actuallyHurt",at=@At(value="INVOKE",
        target="Lnet/minecraft/world/entity/player/Player;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float killerranking$checkmateDamage(float damage) {
        return CheckmateDamage.finalDamage((Player)(Object)this,damage);
    }
}
