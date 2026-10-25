package dev.killerranking.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.killerranking.client.RushArmRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
abstract class FistBandageLayerMixin {
    @Inject(method="render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("HEAD"))
    private void killerranking$beginWraps(LivingEntity entity,float yaw,float delta,PoseStack stack,MultiBufferSource buffers,int light,CallbackInfo ci) {
        RushArmRenderer.beginBandages(entity);
    }
    @Inject(method="render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("TAIL"))
    private void killerranking$finishWraps(LivingEntity entity,float yaw,float delta,PoseStack stack,MultiBufferSource buffers,int light,CallbackInfo ci) {
        RushArmRenderer.finishBandages(entity,buffers);
    }
}
