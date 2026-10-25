package dev.killerranking.client.mixin;
import dev.killerranking.client.RookVfx;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
abstract class RookHeldItemMixin {
    @Inject(method="renderArmWithItem",at=@At("HEAD"),cancellable=true)
    private void killerranking$grabHands(LivingEntity entity,ItemStack item,ItemDisplayContext context,HumanoidArm arm,PoseStack pose,MultiBufferSource source,int light,CallbackInfo ci){if(dev.killerranking.client.ShoulderStabVfx.casting(entity.getUUID())||dev.killerranking.client.WasabariVfx.active(entity.getUUID())){ci.cancel();return;}if(RookVfx.active(entity.getUUID())&&!item.is(dev.killerranking.KillerRankingMod.ROOK_KNUCKLE_GLOVES))ci.cancel();}
}
