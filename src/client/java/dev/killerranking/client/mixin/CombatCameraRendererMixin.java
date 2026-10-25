package dev.killerranking.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.killerranking.client.CombatCameraFeedback;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(GameRenderer.class)
abstract class CombatCameraRendererMixin {
    @Inject(method="getFov",at=@At("RETURN"),cancellable=true)
    private void combatCamera$focus(Camera camera,float delta,boolean useOptions,CallbackInfoReturnable<Double> ci){
        // Both world and hand projections receive the same relative focus.
        ci.setReturnValue(CombatCameraFeedback.fov(ci.getReturnValueD(),delta));
    }
    @Inject(method="bobHurt",at=@At("RETURN"))
    private void combatCamera$shake(PoseStack stack,float delta,CallbackInfo ci){CombatCameraFeedback.shake(stack,delta);}
}
