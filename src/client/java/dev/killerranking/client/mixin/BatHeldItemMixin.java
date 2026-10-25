package dev.killerranking.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.killerranking.client.NightBatTrail;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
abstract class BatHeldItemMixin {
    @Inject(method="renderItem",at=@At("HEAD"),cancellable=true)
    private void begin(LivingEntity entity,ItemStack item,ItemDisplayContext display,boolean left,PoseStack pose,MultiBufferSource buffers,int light,CallbackInfo ci) {
        if (entity instanceof net.minecraft.client.player.AbstractClientPlayer player
            && item.is(dev.killerranking.KillerRankingMod.NEUNGSA_KATANA)
            && (display == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || display == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
            && (left != (player.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT)
                || !player.getMainHandItem().is(dev.killerranking.KillerRankingMod.NEUNGSA_KATANA))) {
            ci.cancel();return;
        }
        if(dev.killerranking.client.KnifePursuitVfx.hideItem(entity,item,display,left)){ci.cancel();return;}
        NightBatTrail.begin(entity,item,display,left);
    }
    @Inject(method="renderItem",at=@At("RETURN"))
    private void end(CallbackInfo ci) { NightBatTrail.end(); }
}
