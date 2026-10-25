package dev.killerranking.client.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.killerranking.client.RookMountScene;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EntityRenderDispatcher.class)
abstract class RookMountSceneMixin {
    @Inject(method="render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("HEAD"),cancellable=true)
    private void rook$hideStandingCaster(Entity entity,double x,double y,double z,float yaw,float delta,PoseStack stack,MultiBufferSource buffer,int light,CallbackInfo ci) {
        if(entity instanceof AbstractClientPlayer player&&RookMountScene.hideOriginal(player))ci.cancel();
    }
}
