package dev.killerranking.client.mixin;
import dev.killerranking.client.RookLegRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
abstract class RookPlayerModelMixin {
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("TAIL"))
    private void killerranking$rookParts(LivingEntity entity,float walk,float speed,float age,float yaw,float pitch,CallbackInfo ci) {
        RookLegRenderer.bind((PlayerModel<?>)(Object)this,entity);
    }
}
