package dev.killerranking.client.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.killerranking.client.RookLegRenderer;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
abstract class RookLegRenderMixin {
    @Shadow @Final private List<ModelPart.Cube> cubes;
    @Shadow @Final private Map<String,ModelPart> children;
    @Inject(method="render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",at=@At("HEAD"),cancellable=true)
    private void killerranking$kneelingLeg(PoseStack pose,VertexConsumer vertices,int light,int overlay,int color,CallbackInfo ci) {
        if(RookLegRenderer.render((ModelPart)(Object)this,cubes,children,pose,vertices,light,overlay,color)) ci.cancel();
    }
}
