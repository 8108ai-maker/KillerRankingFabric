package dev.killerranking.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
public final class CleaveEyesLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    private static final RenderType EYES=RenderType.eyes(ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png"));
    public CleaveEyesLayer(PlayerRenderer renderer){super(renderer);}
    @Override public void render(PoseStack stack,MultiBufferSource buffers,int light,AbstractClientPlayer p,float walk,float speed,float delta,float time,float yaw,float pitch){
        if(!CleaveVfx.full(p.getUUID())||p.isInvisible()||p.isSpectator())return;
        if(net.minecraft.client.Minecraft.getInstance().options.getCameraType().isFirstPerson()&&p==net.minecraft.client.Minecraft.getInstance().player)return;
        stack.pushPose();getParentModel().head.translateAndRotate(stack);
        var v=CleaveSilhouetteBuffers.unwrap(buffers).getBuffer(EYES);
        for(float sign:new float[]{-1,1}){
            quad(stack,v,sign,.19f,.055f,.20f,.16f);
            quad(stack,v,sign,.16f,.065f,.13f,1);
        }
        stack.popPose();
    }
    private static void quad(PoseStack s,VertexConsumer v,float sign,float outer,float inner,float h,float alpha){
        float[] xs={sign*outer,sign*inner,sign*inner,sign*outer};float[] ys={-.285f,-.245f,-.225f,-.255f};
        if(alpha<1){for(int i=0;i<4;i++)ys[i]-=.012f;}
        for(int i=0;i<4;i++)v.addVertex(s.last().pose(),xs[i],ys[i],-.287f-(alpha<1?.0001f:0)).setColor(1f,1f,1f,alpha)
            .setUv(i==0||i==3?0:1,i<2?0:1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(s.last(),0,0,-1);
    }
}
