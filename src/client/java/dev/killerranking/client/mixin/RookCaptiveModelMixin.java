package dev.killerranking.client.mixin;
import dev.killerranking.client.RookVfx;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntityRenderer.class)
abstract class RookCaptiveModelMixin {
    @Shadow protected EntityModel<?> model;
    @Inject(method="render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V",shift=At.Shift.AFTER))
    private void rook$proneArms(LivingEntity entity,float yaw,float delta,PoseStack stack,MultiBufferSource buffer,int light,CallbackInfo ci) {
        if(entity instanceof AbstractClientPlayer||RookVfx.mountingOwner(entity.getUUID())==null)return;
        if(!(model instanceof HumanoidModel<?> human))return;
        human.rightArm.xRot=human.leftArm.xRot=0;
        human.rightArm.yRot=human.leftArm.yRot=0;
        human.rightArm.zRot=.06f;human.leftArm.zRot=-.06f;
    }
}
