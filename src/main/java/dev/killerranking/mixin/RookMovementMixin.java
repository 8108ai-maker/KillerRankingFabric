package dev.killerranking.mixin;
import dev.killerranking.KillerRankingMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class RookMovementMixin {
    @Inject(method="travel",at=@At("HEAD"),cancellable=true)
    private void killerranking$grabTravel(Vec3 input,CallbackInfo ci) {
        var entity=(LivingEntity)(Object)this;
        if(KillerRankingMod.ROOK.locked(entity)){entity.setDeltaMovement(Vec3.ZERO);ci.cancel();}
    }
    @Inject(method="knockback",at=@At("HEAD"),cancellable=true)
    private void killerranking$grabKnockback(double strength,double x,double z,CallbackInfo ci) {
        if(KillerRankingMod.ROOK.locked((LivingEntity)(Object)this))ci.cancel();
    }
    @Inject(method="isPushable",at=@At("HEAD"),cancellable=true)
    private void killerranking$grabNoPush(CallbackInfoReturnable<Boolean> cir) {
        if(KillerRankingMod.ROOK.locked((LivingEntity)(Object)this))cir.setReturnValue(false);
    }
    @Inject(method="push(Lnet/minecraft/world/entity/Entity;)V",at=@At("HEAD"),cancellable=true)
    private void killerranking$grabPair(net.minecraft.world.entity.Entity other,CallbackInfo ci) {
        if(KillerRankingMod.ROOK.locked((LivingEntity)(Object)this)||KillerRankingMod.ROOK.locked(other))ci.cancel();
    }
}
