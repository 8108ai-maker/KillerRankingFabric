package dev.killerranking.client;

import com.mojang.blaze3d.vertex.*;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.entity.BishopFollower;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.joml.Vector3f;
import org.joml.Matrix4f;
import net.minecraft.client.model.geom.ModelPart;

/** One zero-thickness sewn surface. Adjacent quads share the same displaced seam vertices. */
public final class BishopCloakLayer extends RenderLayer<BishopFollower,BishopModel> {
    private static final RenderType MATERIAL=RenderType.entityCutoutNoCull(KillerRankingMod.id("textures/entity/bishop_cloak.png"));
    private static final float[] HEIGHT={-10,-6,0,3,11,21.8f};
    private static final float[] WIDTH={4.3f,5.2f,5.2f,6.9f,6.3f,7.5f};
    private static final float[] DEPTH={4.1f,4.7f,3.8f,3.7f,4,4.4f};
    public BishopCloakLayer(RenderLayerParent<BishopFollower,BishopModel> parent){super(parent);}
    @Override public void render(PoseStack stack,MultiBufferSource buffers,int light,BishopFollower e,float walk,float speed,float delta,float age,float yaw,float pitch){
        if(e.isInvisible())return;
        var m=getParentModel();var ring=surface(m);stack.pushPose();m.globalPose(stack);
        var vertices=buffers.getBuffer(MATERIAL);int overlay=LivingEntityRenderer.getOverlayCoords(e,0);
        for(int row=0;row<ring.length-1;row++)for(int side=0;side<8;side++){
            int next=(side+1)%8;float u0,u1,v0,v1;
            if(row<2){u0=side==0?0:32+((side-1)%3)*32;u1=u0+32;v0=(HEIGHT[row]+10)*3.2f;v1=(HEIGHT[row+1]+10)*3.2f;}
            else{u0=side*16;u1=u0+16;v0=32+HEIGHT[row]*4.35f;v1=32+HEIGHT[row+1]*4.35f;}
            quad(stack,vertices,ring[row][side],ring[row][next],ring[row+1][next],ring[row+1][side],u0/128,v0/128,u1/128,v1/128,light,overlay);
        }
        // The hood roof is sewn to precisely the same upper ring.
        quad(stack,vertices,ring[0][0],ring[0][1],ring[0][4],ring[0][5],.26f,.01f,.49f,.08f,light,overlay);
        stack.popPose();
    }
    public static Vector3f[][] surface(float age,float flow){
        var rings=new Vector3f[HEIGHT.length][8];
        float[] sx={-.78f,.78f,1,1,.78f,-.78f,-1,-1},sz={-1,-1,-.65f,.65f,1,1,.65f,-.65f};
        for(int row=0;row<rings.length;row++)for(int side=0;side<8;side++){
            float trailing=Math.max(0,HEIGHT[row])/21.8f;trailing*=trailing;
            float wave=(float)Math.sin(age*.48f-row*.85f+sx[side]*.5f)*(.16f+flow*.65f)*trailing;
            float x=sx[side]*(WIDTH[row]+trailing*flow*1.3f)+wave;
            float y=HEIGHT[row]-trailing*flow*3.8f+wave*.2f;
            float z=sz[side]*DEPTH[row]+trailing*(flow*8.5f+wave);
            rings[row][side]=new Vector3f(x/16,y/16,z/16);
        }
        return rings;
    }
    public static Vector3f[][] surface(BishopModel model){
        var rings=surface(model.clothAge,model.clothFlow);
        var head=bone(model.head);var torso=bone(model.body);
        // A stitched collar blends two independent bones. Lower cloth follows the hips,
        // so rotating the chest cannot turn the entire person into one rigid tube.
        float[] headWeight={1,1,.45f,0,0,0},torsoWeight={0,0,.55f,1,.5f,.12f};
        for(int row=0;row<rings.length;row++)for(int side=0;side<8;side++){
            var base=new Vector3f(rings[row][side]);var h=head.transformPosition(new Vector3f(base));var t=torso.transformPosition(new Vector3f(base));
            rings[row][side].mul(1-headWeight[row]-torsoWeight[row]).fma(headWeight[row],h).fma(torsoWeight[row],t);
        }
        return rings;
    }
    private static Matrix4f bone(ModelPart part){
        return new Matrix4f().translation(part.x/16,part.y/16,part.z/16).rotateZ(part.zRot).rotateY(part.yRot).rotateX(part.xRot);
    }
    private static void quad(PoseStack stack,VertexConsumer out,Vector3f a,Vector3f b,Vector3f c,Vector3f d,float u0,float v0,float u1,float v1,int light,int overlay){
        var n=new Vector3f(b).sub(a).cross(new Vector3f(d).sub(a)).normalize();var pose=stack.last();var points=new Vector3f[]{a,b,c,d};
        for(int i=0;i<4;i++){var p=points[i];out.addVertex(pose.pose(),p.x,p.y,p.z).setColor(-1).setUv(i==0||i==3?u0:u1,i<2?v0:v1).setOverlay(overlay).setLight(light).setNormal(pose,n.x,n.y,n.z);}
    }
}
