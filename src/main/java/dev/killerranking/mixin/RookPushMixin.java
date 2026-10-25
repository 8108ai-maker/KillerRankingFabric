package dev.killerranking.mixin;

import dev.killerranking.KillerRankingMod;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class RookPushMixin {
    @Inject(method="push(DDD)V",at=@At("HEAD"),cancellable=true)
    private void killer$fixedGrab(double x,double y,double z,CallbackInfo ci) {
        if(KillerRankingMod.ROOK.locked((Entity)(Object)this)||(Object)this instanceof net.minecraft.server.level.ServerPlayer p&&KillerRankingMod.SHOULDER_STAB.casting(p))ci.cancel();
    }
}
