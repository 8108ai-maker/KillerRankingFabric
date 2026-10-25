package dev.killerranking.client;
import dev.killerranking.entity.BishopFollower;
import dev.killerranking.KillerRankingMod;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Authored weapon-style clips drive the whole skeleton; the cloak is one connected sheet. */
public final class BishopModel extends HumanoidModel<BishopFollower> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("killerranking","bishop_follower"),"main");
    private KeyframeAnimationPlayer motion;
    private float partial;
    public float clothFlow,clothAge;
    public BishopModel(ModelPart root){super(root,net.minecraft.client.renderer.RenderType::entityCutout);}
    public static LayerDefinition layer(){
        var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);var root=mesh.getRoot();
        root.addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.ZERO);
        return LayerDefinition.create(mesh,128,128);
    }
    @Override public void setupAnim(BishopFollower e,float walk,float speed,float age,float yaw,float pitch){
        for(var part:new ModelPart[]{head,hat,body,rightArm,leftArm,rightLeg,leftLeg})part.resetPose();
        super.setupAnim(e,walk,speed,age,yaw,pitch);
        partial=Mth.clamp(age-e.tickCount,0,1);motion=null;
        String clip=null;float time=0;
        if(e.striking()){clip=switch(e.attackStyle()){case 3->"bishop_combo";case 4->"bishop_stab_combo";case 2->"bishop_cut";default->"bishop_stab";};time=e.attackPoseTick()+partial;}
        else if(e.leapPoseTick()>0){clip="bishop_leap";time=e.leapPoseTick()+partial;}
        else if(e.fighting()&&speed>.02f){clip="bishop_sprint";time=walk*1.65f;}
        if(e.barrageEcho())time=Math.min(27,time*1.4f);
        if(clip!=null&&PlayerAnimationRegistry.getAnimation(KillerRankingMod.id(clip)) instanceof KeyframeAnimation frames){
            if(clip.equals("bishop_sprint"))time%=12;
            motion=new KeyframeAnimationPlayer(frames,(int)time);partial=time-(int)time;motion.setupAnim(partial);
            apply("torso",body);apply("head",head);apply("rightArm",rightArm);apply("leftArm",leftArm);apply("rightLeg",rightLeg);apply("leftLeg",leftLeg);
        }
        rightArm.visible=leftArm.visible=e.striking();
        // Head/look direction and the mask retain their own bone, independent of the torso clip.
        hat.copyFrom(head);
        clothAge=age;clothFlow=e.leaping()?1:Mth.clamp(speed*(e.fighting()?1.2f:.4f),0,1);
    }
    private Vec3f sample(String name,TransformType type,Vec3f base){return motion==null?base:motion.get3DTransform(name,type,partial,base);}
    private void apply(String name,ModelPart part){
        var p=sample(name,TransformType.POSITION,new Vec3f(part.x,part.y,part.z));var r=sample(name,TransformType.ROTATION,new Vec3f(part.xRot,part.yRot,part.zRot));
        part.setPos(p.getX(),p.getY(),p.getZ());part.setRotation(r.getX(),r.getY(),r.getZ());part.xScale=part.yScale=part.zScale=1;
    }
    public void globalPose(PoseStack stack){pose(stack,"body",Vec3f.ZERO);}
    public Vec3f trail(){return sample("nightBatTrail",TransformType.POSITION,Vec3f.ZERO);}
    public void daggerPose(PoseStack stack){
        // Rotate about the handle inside the palm, not the item's center.
        stack.translate(0,-2/16f,0);pose(stack,"rightItem",Vec3f.ZERO);stack.translate(0,2/16f,0);
    }
    private void pose(PoseStack stack,String name,Vec3f base){
        var p=sample(name,TransformType.POSITION,base);var r=sample(name,TransformType.ROTATION,base);
        stack.translate(p.getX()/16,p.getY()/16,p.getZ()/16);stack.mulPose(Axis.ZP.rotation(r.getZ()));stack.mulPose(Axis.YP.rotation(r.getY()));stack.mulPose(Axis.XP.rotation(r.getX()));
    }
    @Override public void renderToBuffer(PoseStack stack,com.mojang.blaze3d.vertex.VertexConsumer v,int light,int overlay,int color){stack.pushPose();globalPose(stack);super.renderToBuffer(stack,v,light,overlay,color);stack.popPose();}
}
