package dev.killerranking.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.killerranking.entity.BishopFollower;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;

/** The weapon shares the authored wrist timeline and emerges only for the strike. */
public final class BishopDaggerLayer extends RenderLayer<BishopFollower,BishopModel> {
    private final ItemInHandRenderer items;
    public BishopDaggerLayer(RenderLayerParent<BishopFollower,BishopModel> parent,ItemInHandRenderer items){super(parent);this.items=items;}
    @Override public void render(PoseStack stack,MultiBufferSource buffers,int light,BishopFollower e,float walk,float speed,float delta,float age,float yaw,float pitch){
        if(!e.striking()||e.isInvisible())return;
        var model=getParentModel();stack.pushPose();model.globalPose(stack);model.translateToHand(HumanoidArm.RIGHT,stack);
        stack.mulPose(Axis.XP.rotationDegrees(-90));stack.mulPose(Axis.YP.rotationDegrees(180));stack.translate(1/16f,.125,-.625);
        model.daggerPose(stack);items.renderItem(e,e.getMainHandItem(),ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,false,stack,buffers,light);stack.popPose();
    }
}
