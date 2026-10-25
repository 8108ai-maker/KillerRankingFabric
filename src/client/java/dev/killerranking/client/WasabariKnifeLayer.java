package dev.killerranking.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.killerranking.KillerRankingMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** The skill's temporary dagger is visual only; inventory and equipment are preserved. */
public final class WasabariKnifeLayer extends PlayerItemInHandLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    private static long rendered;
    public static long renderedKnives(){return rendered;}
    public WasabariKnifeLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent,ItemInHandRenderer items){super(parent,items);}
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,AbstractClientPlayer p,float swing,float amount,float delta,float age,float yaw,float pitch){
        float time=WasabariVfx.age(p.getUUID(),delta);if(!WasabariVfx.active(p.getUUID())||time<22||time>=60||p.isInvisible())return;
        var arm=p.getMainArm();boolean left=arm==HumanoidArm.LEFT;var direction=WasabariVfx.knifeDirection(p,pose);pose.pushPose();getParentModel().translateToHand(arm,pose);
        // Handle remains in the palm; the blade points inward through the side of the held shin.
        float centre=HeavyPunchPose.center(p);
        var part=left?getParentModel().leftArm:getParentModel().rightArm;
        var local=new org.joml.Matrix4f().rotateZYX(part.zRot,part.yRot,part.xRot).invert()
            .transformDirection(new org.joml.Vector3f((float)direction.x,(float)direction.y,(float)direction.z));
        pose.translate((left?centre:-centre)/16,10.0/16,0);
        pose.mulPose(new org.joml.Quaternionf().rotationTo(new org.joml.Vector3f(0,1,0),local.normalize()));
        float size=dev.killerranking.visual.SalmosaKnifeGeometry.SKILL_SCALE;
        pose.translate(0,(8-dev.killerranking.visual.SalmosaKnifeGeometry.PALM)*size/16,0);pose.scale(size,size,size);
        WasabariVfx.recordKnife(p.getUUID(),pose);
        Minecraft.getInstance().getItemRenderer().renderStatic(p,new ItemStack(KillerRankingMod.SALMOSA_MID_DAGGER),ItemDisplayContext.NONE,false,pose,buffers,p.level(),light,OverlayTexture.NO_OVERLAY,p.getId());pose.popPose();rendered++;
    }
}
