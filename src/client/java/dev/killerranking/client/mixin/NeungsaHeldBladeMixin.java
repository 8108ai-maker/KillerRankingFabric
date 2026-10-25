package dev.killerranking.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.client.NeungsaHeldBlade;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
abstract class NeungsaHeldBladeMixin<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {
    protected NeungsaHeldBladeMixin(RenderLayerParent<T, M> parent) { super(parent); }

    @Inject(method="renderArmWithItem", at=@At("HEAD"), cancellable=true)
    private void kr$attach(T entity, ItemStack stack, ItemDisplayContext display, HumanoidArm arm,
                          PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (!(entity instanceof AbstractClientPlayer player) || !stack.is(KillerRankingMod.NEUNGSA_KATANA)) return;
        // Offhand/inventory swords stay assembled in their body-mounted sheath.
        // Attaching the main-hand wrist channels to the offhand exposed a separate full blade.
        if (arm == player.getMainArm() && player.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA) && !NeungsaHeldBlade.hidden(player))
            NeungsaHeldBlade.render(getParentModel(), player, stack, arm, pose, buffers, light);
        ci.cancel();
    }
}
