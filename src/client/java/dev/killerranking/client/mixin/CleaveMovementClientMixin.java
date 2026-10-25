package dev.killerranking.client.mixin;
import dev.killerranking.client.CleaveVfx;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntity.class)
abstract class CleaveMovementClientMixin {
    @Inject(method="travel",at=@At("HEAD"),cancellable=true)
    private void cleave$casting(Vec3 movement,CallbackInfo ci){var e=(LivingEntity)(Object)this;if(dev.killerranking.client.ShoulderStabVfx.casting(e.getUUID())||dev.killerranking.client.KnifePursuitVfx.casting(e.getUUID())||CleaveVfx.slashing(e.getUUID())||dev.killerranking.client.MadnessLeapVfx.casting(e.getUUID())){e.setDeltaMovement(Vec3.ZERO);ci.cancel();}}
}
