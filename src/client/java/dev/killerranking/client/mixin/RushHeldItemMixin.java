package dev.killerranking.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.killerranking.client.RushVfx;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hide held-item geometry during the fist animation without changing equipment or inventory. */
@Mixin(ItemInHandLayer.class)
abstract class RushHeldItemMixin {
    @Inject(method="renderArmWithItem",at=@At("HEAD"),cancellable=true)
    private void killerranking$rushHands(LivingEntity entity,ItemStack item,ItemDisplayContext display,HumanoidArm arm,
            PoseStack stack,MultiBufferSource buffers,int light,CallbackInfo ci) {
        if(RushVfx.active(entity.getUUID())||dev.killerranking.client.HeavyPunchVfx.active(entity.getUUID())) ci.cancel();
    }
}
