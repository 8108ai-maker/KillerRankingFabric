package dev.killerranking.mixin;

import dev.killerranking.combat.SkillCombatLock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
abstract class CheckmateAttackMixin {
    @Inject(method="attack",at=@At("HEAD"),cancellable=true)
    private void killerranking$checkmateAttack(Entity target,CallbackInfo ci) {
        if((Object)this instanceof ServerPlayer p&&SkillCombatLock.blocksAttack(p)) ci.cancel();
    }
}
