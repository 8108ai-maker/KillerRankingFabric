package dev.killerranking.client.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.killerranking.client.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
@Mixin(LivingEntityRenderer.class)
abstract class CleaveSilhouetteMixin {
    @ModifyVariable(method="render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("HEAD"),argsOnly=true)
    private MultiBufferSource cleave$shadow(MultiBufferSource buffers,LivingEntity e,float yaw,float delta,PoseStack stack,MultiBufferSource original,int light){
        return e instanceof AbstractClientPlayer p&&CleaveVfx.full(p.getUUID())?new CleaveSilhouetteBuffers(buffers):buffers;
    }
}
