package dev.killerranking.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.killerranking.combat.RushDamage;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class RushDamageMixin implements RushDamage.RushDamageAccess {
    @Shadow protected float lastHurt;
    @Override public float killerranking$lastDamage() { return lastHurt; }
    @Override public void killerranking$lastDamage(float value) { lastHurt=value; }
    @ModifyExpressionValue(method="hurt",at=@At(value="FIELD",target="Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I",opcode=org.objectweb.asm.Opcodes.GETFIELD))
    private int killerranking$rushCooldown(int value) { return RushDamage.current((LivingEntity)(Object)this)?0:value; }
    @Inject(method="knockback",at=@At("HEAD"),cancellable=true)
    private void killerranking$rushHold(double strength,double x,double z,CallbackInfo info) {
        if(RushDamage.suppressKnockback((LivingEntity)(Object)this)) info.cancel();
    }
}
