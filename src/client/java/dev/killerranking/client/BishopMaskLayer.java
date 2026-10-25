package dev.killerranking.client;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.entity.BishopFollower;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

/** A dedicated 64-pixel face is held clear of the hood opening. */
public final class BishopMaskLayer extends RenderLayer<BishopFollower,BishopModel> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(KillerRankingMod.id("bishop_mask"),"main");
    private final ModelPart face;
    public BishopMaskLayer(RenderLayerParent<BishopFollower,BishopModel> parent,ModelPart root){super(parent);face=root.getChild("face");}
    public static LayerDefinition layer(){
        var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("face",CubeListBuilder.create().texOffs(0,0).addBox(-32,-32,0,64,64,0),PartPose.ZERO);
        return LayerDefinition.create(mesh,64,64);
    }
    @Override public void render(PoseStack stack,MultiBufferSource buffers,int light,BishopFollower e,float walk,float speed,float delta,float age,float yaw,float pitch){
        if(e.isInvisible())return;
        stack.pushPose();getParentModel().globalPose(stack);getParentModel().head.translateAndRotate(stack);
        stack.translate(0,-3.9/16,-4.98/16);stack.scale(.122f,.122f,.122f);
        face.render(stack,buffers.getBuffer(RenderType.entityCutout(KillerRankingMod.id("textures/entity/bishop_mask.png"))),light,LivingEntityRenderer.getOverlayCoords(e,0));stack.popPose();
    }
}
