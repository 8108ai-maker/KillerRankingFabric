package dev.killerranking.mixin;

import dev.killerranking.combat.MadnessSurvivalService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
abstract class PlayerSurvivalMixin {
    @ModifyVariable(method = "actuallyHurt", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/player/Player;getCombatTracker()Lnet/minecraft/world/damagesource/CombatTracker;"), ordinal = 0)
    private float killerranking$survive(float damage, DamageSource source, float originalDamage) {
        return (Object) this instanceof ServerPlayer player ? MadnessSurvivalService.limitDamage(player, source, damage) : damage;
    }
}
