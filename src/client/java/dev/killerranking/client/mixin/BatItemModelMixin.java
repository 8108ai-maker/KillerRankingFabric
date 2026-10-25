package dev.killerranking.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.killerranking.client.NightBatTrail;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
abstract class BatItemModelMixin {
    @Inject(method="renderModelLists",at=@At("HEAD"))
    private void sample(BakedModel model,ItemStack item,int light,int overlay,PoseStack pose,VertexConsumer vertices,CallbackInfo ci) {
        NightBatTrail.sample(item,pose);
    }
}
