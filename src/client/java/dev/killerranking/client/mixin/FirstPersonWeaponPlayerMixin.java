package dev.killerranking.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.killerranking.client.FirstPersonWeaponView;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
abstract class FirstPersonWeaponPlayerMixin {
    @Inject(method="render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("HEAD"))
    private void begin(AbstractClientPlayer player,float yaw,float delta,PoseStack pose,MultiBufferSource buffers,int light,CallbackInfo ci) {
        FirstPersonWeaponView.begin(player);
    }
    @Inject(method="render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("RETURN"))
    private void end(CallbackInfo ci) { FirstPersonWeaponView.end(); }
}
