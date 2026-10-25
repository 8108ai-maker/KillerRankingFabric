package dev.killerranking.client.mixin;

import dev.killerranking.client.RookVfx;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class RookMovementClientMixin {
    @Inject(method="travel",at=@At("HEAD"),cancellable=true)
    private void killer$fixedClient(Vec3 input,CallbackInfo ci) {
        var entity=(LivingEntity)(Object)this;
        if(RookVfx.immobilized(entity)){entity.setDeltaMovement(Vec3.ZERO);ci.cancel();}
    }
    @Inject(method="knockback",at=@At("HEAD"),cancellable=true)
    private void killer$noRecoil(double force,double x,double z,CallbackInfo ci) {
        if(RookVfx.immobilized((LivingEntity)(Object)this))ci.cancel();
    }
    @Inject(method="isPushable",at=@At("HEAD"),cancellable=true)
    private void killer$noCollision(CallbackInfoReturnable<Boolean> cir) {
        if(RookVfx.immobilized((LivingEntity)(Object)this))cir.setReturnValue(false);
    }
    @Inject(method="push(Lnet/minecraft/world/entity/Entity;)V",at=@At("HEAD"),cancellable=true)
    private void killer$noPairPush(Entity other,CallbackInfo ci) {
        if(RookVfx.immobilized((LivingEntity)(Object)this)||RookVfx.immobilized(other))ci.cancel();
    }
}
