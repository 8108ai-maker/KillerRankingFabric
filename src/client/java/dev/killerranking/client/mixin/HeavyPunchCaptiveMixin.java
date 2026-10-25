package dev.killerranking.client.mixin;
import dev.killerranking.client.HeavyPunchPose;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Apply the held wrist after each concrete humanoid's complete animation. */
@Mixin(LivingEntityRenderer.class)
abstract class HeavyPunchCaptiveMixin {
    @Shadow protected EntityModel<?> model;
    @Inject(method="render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V",shift=At.Shift.AFTER))
    private void killerranking$heldWrist(LivingEntity e,float yaw,float delta,PoseStack stack,MultiBufferSource buffer,int light,CallbackInfo ci){
        if(model instanceof HumanoidModel<?> human)HeavyPunchPose.apply(e,human,stack,delta);
        dev.killerranking.client.WasabariPose.apply(e,model,stack,delta);
        dev.killerranking.client.ShoulderStabPose.apply(e,model,stack,delta);
    }
}
