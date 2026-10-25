package dev.killerranking.client;
import com.mojang.blaze3d.vertex.*;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.client.mixin.RushCubeAccess;
import dev.killerranking.visual.RushBarrage;
import dev.killerranking.visual.RushUpperBody;
import dev.killerranking.visual.RushTorso;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import java.util.*;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** One detached native-size head, torso and arm pair with its own punching rhythm. */
final class RushModel {
    private record EchoPart(String bone,Mesh mesh,Matrix4f pose) { }
    private record Point(float x,float y,float z,float u,float v) {
        Point between(Point b,float t){return new Point(x+(b.x-x)*t,y+(b.y-y)*t,z+(b.z-z)*t,u+(b.u-u)*t,v+(b.v-v)*t);}
    }
    private record Face(List<Point> points,Vector3f normal) { }
    private record Mesh(List<Face> faces,float center) { }
    private record Arms(Mesh right,Mesh left,Mesh rightSleeve,Mesh leftSleeve) { }
    private record Upper(Mesh head,Mesh hat,Mesh torso,Mesh jacket,Arms arms) { }
    private static final Map<UUID,KeyframeAnimationPlayer> MOTIONS=new HashMap<>();
    private static final Map<UUID,Float> POSITION_ERRORS=new HashMap<>(),POSE_DIFFERENCES=new HashMap<>();
    private static ClientLevel world;
    private static Upper wide,slim;private static long rendered,upperBodies;
    static void register(){
        WorldRenderEvents.START.register(c->{
            if(world!=c.world()){clear();world=c.world();}
        });
    }
    static void start(AbstractClientPlayer player,int age){
        if(MOTIONS.containsKey(player.getUUID()))return;
        var data=PlayerAnimationRegistry.getAnimation(KillerRankingMod.id("rush_echo"));if(data==null)return;
        if(!(data.playAnimation() instanceof KeyframeAnimationPlayer motion))return;
        for(int i=0;i<age;i++)motion.tick();MOTIONS.put(player.getUUID(),motion);
    }
    static void render(WorldRenderContext context,AbstractClientPlayer player,float fade){
        var motion=MOTIONS.get(player.getUUID());if(motion==null||context.matrixStack()==null||context.consumers()==null)return;
        float partial=context.tickCounter().getGameTimeDeltaPartialTick(false);motion.setupAnim(partial);
        // A shared world origin does not share the animation clock or pose.
        var source=RushVfx.motion(player);
        if(source!=null){
            source.setupAnim(partial);
            var a=motion.get3DTransform("torso",TransformType.ROTATION,partial,Vec3f.ZERO);
            var b=source.get3DTransform("torso",TransformType.ROTATION,partial,Vec3f.ZERO);
            POSE_DIFFERENCES.put(player.getUUID(),Math.max(Math.abs(a.getX()-b.getX()),Math.max(Math.abs(a.getY()-b.getY()),Math.abs(a.getZ()-b.getZ()))));
        }
        var torsoRotation=motion.get3DTransform("torso",TransformType.ROTATION,partial,Vec3f.ZERO);
        var torsoPosition=motion.get3DTransform("torso",TransformType.POSITION,partial,Vec3f.ZERO);
        var pinned=RushTorso.pivot(new Vec3(0,12,0),new Vec3(torsoRotation.getX(),torsoRotation.getY(),torsoRotation.getZ()));
        var correction=pinned.subtract(torsoPosition.getX(),torsoPosition.getY(),torsoPosition.getZ());
        var bodyPosition=motion.get3DTransform("body",TransformType.POSITION,partial,Vec3f.ZERO);
        var bodyRotation=motion.get3DTransform("body",TransformType.ROTATION,partial,Vec3f.ZERO);
        // Follow the source world position/facing while the echo pose has its own beat.
        float facing=net.minecraft.util.Mth.rotLerp(partial,player.yBodyRotO,player.yBodyRot);
        var root=RushUpperBody.root(player.getPosition(partial),facing,player.getScale(),
            new Vec3(bodyPosition.getX(),bodyPosition.getY(),bodyPosition.getZ()),
            new Vec3(bodyRotation.getX(),bodyRotation.getY(),bodyRotation.getZ()));
        var waist=part("torso",null,root,motion,partial,correction,0).pose().transformPosition(new Vector3f(0,.75f,0));
        var anchor=new Matrix4f(root).transformPosition(new Vector3f(0,.75f,0));
        POSITION_ERRORS.put(player.getUUID(),waist.distance(anchor)+(float)RushUpperBody.origin(player.getPosition(partial),facing,player.getScale()).distanceTo(player.getPosition(partial)));
        var skin=upper(player.getSkin().model()==PlayerSkin.Model.SLIM);
        boolean fp=player==Minecraft.getInstance().player&&Minecraft.getInstance().options.getCameraType().isFirstPerson();
        var echoes=new ArrayList<EchoPart>();
        // Match the source player's first-person visibility: its own head/body
        // intersect the camera, while the hands remain visible. Other viewers
        // still see the entire detached upper-body afterimage.
        if(!fp){
            echoes.add(part("torso",skin.torso(),root,motion,partial,correction,0));
            echoes.add(part("head",skin.head(),root,motion,partial,correction,player.getXRot()*(float)Math.PI/180));
            if(player.isModelPartShown(PlayerModelPart.JACKET))echoes.add(part("torso",skin.jacket(),root,motion,partial,correction,0));
            if(player.isModelPartShown(PlayerModelPart.HAT))echoes.add(part("head",skin.hat(),root,motion,partial,correction,player.getXRot()*(float)Math.PI/180));
        }
        for(boolean left:new boolean[]{false,true}){
            String arm=left?"leftArm":"rightArm";
            echoes.add(part(arm,left?skin.arms().left():skin.arms().right(),root,motion,partial,correction,0));
            if(player.isModelPartShown(left?PlayerModelPart.LEFT_SLEEVE:PlayerModelPart.RIGHT_SLEEVE))
                echoes.add(part(arm,left?skin.arms().leftSleeve():skin.arms().rightSleeve(),root,motion,partial,correction,0));
        }
        int color=((int)(255*Math.clamp(RushBarrage.ALPHA*fade*(fp?.6f:1),0,1))<<24)|0xFFFFFF;
        var vertices=context.consumers().getBuffer(RenderType.entityTranslucent(player.getSkin().texture()));
        for(var e:echoes){
            var stack=pose(context,e.pose());draw(stack,vertices,e.mesh(),color);
            stack.popPose();
        }
        for(var e:echoes){
            if(!(e.bone().equals("leftArm")&&e.mesh()==skin.arms().left()||e.bone().equals("rightArm")&&e.mesh()==skin.arms().right()))continue;
            boolean left=e.bone().equals("leftArm");
            var hand=left==(player.getMainArm()==HumanoidArm.LEFT)?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;
            if(!player.getItemInHand(hand).is(KillerRankingMod.TTEOKBAL_FIST))continue;
            var stack=pose(context,e.pose());
            FistBandages.render(stack,context.consumers(),player.getSkin().model()==PlayerSkin.Model.SLIM,e.mesh().center(),0xF000F0,OverlayTexture.NO_OVERLAY,color);
            stack.popPose();
        }
        rendered++;
        if(!fp&&!skin.head().faces().isEmpty()&&!skin.torso().faces().isEmpty())upperBodies++;
    }
    private static EchoPart part(String bone,Mesh mesh,Matrix4f root,KeyframeAnimationPlayer motion,float partial,Vec3 correction,float headPitch){
        var pos=motion.get3DTransform(bone,TransformType.POSITION,partial,new Vec3f(bone.equals("leftArm")?5:bone.equals("rightArm")?-5:0,bone.endsWith("Arm")?2:0,0));
        var rot=motion.get3DTransform(bone,TransformType.ROTATION,partial,new Vec3f(headPitch,0,0));
        var matrix=new Matrix4f(root).translate((pos.getX()+(float)correction.x)/16,(pos.getY()+(float)correction.y)/16,(pos.getZ()+(float)correction.z)/16)
            .rotateZYX(rot.getZ(),rot.getY(),rot.getX());
        return new EchoPart(bone,mesh,matrix);
    }
    private static PoseStack pose(WorldRenderContext context,Matrix4f matrix){
        var camera=context.camera().getPosition();var stack=context.matrixStack();stack.pushPose();stack.translate(-camera.x,-camera.y,-camera.z);
        stack.last().pose().mul(matrix);stack.last().normal().mul(new Matrix3f(matrix).invert().transpose());return stack;
    }
    private static Upper upper(boolean narrow){
        var found=narrow?slim:wide;if(found!=null)return found;
        var root=Minecraft.getInstance().getEntityModels().bakeLayer(narrow?ModelLayers.PLAYER_SLIM:ModelLayers.PLAYER);
        var arms=new Arms(mesh(root.getChild("right_arm")),mesh(root.getChild("left_arm")),mesh(root.getChild("right_sleeve")),mesh(root.getChild("left_sleeve")));
        found=new Upper(mesh(root.getChild("head")),mesh(root.getChild("hat")),mesh(root.getChild("body")),mesh(root.getChild("jacket")),arms);
        if(narrow)slim=found;else wide=found;return found;
    }
    private static Mesh mesh(ModelPart part){
        var faces=new ArrayList<Face>();float[] bounds={Float.MAX_VALUE,-Float.MAX_VALUE};
        part.visit(new PoseStack(),(pose,path,index,cube)->{for(var polygon:((RushCubeAccess)(Object)cube).killerranking$rushFaces()){
            var points=Arrays.stream(polygon.vertices).map(v->new Point(v.pos.x,v.pos.y,v.pos.z,v.u,v.v)).toList();
            for(var p:points){bounds[0]=Math.min(bounds[0],p.x);bounds[1]=Math.max(bounds[1],p.x);}
            float low=points.stream().map(p->p.y).min(Float::compare).orElse(0f),high=points.stream().map(p->p.y).max(Float::compare).orElse(0f);
            if(high-low<.001)faces.add(new Face(points,new Vector3f(polygon.normal)));
            else for(float y=low;y<high;y+=.75f){var clipped=clip(clip(points,y,true),Math.min(y+.75f,high),false);if(clipped.size()==4)faces.add(new Face(clipped,new Vector3f(polygon.normal)));}
        }});return new Mesh(List.copyOf(faces),(bounds[0]+bounds[1])*.5f);
    }
    private static List<Point> clip(List<Point> input,float y,boolean above){
        var result=new ArrayList<Point>();if(input.isEmpty())return result;
        var previous=input.getLast();boolean prev=above?previous.y>=y:previous.y<=y;
        for(var current:input){boolean inside=above?current.y>=y:current.y<=y;if(inside!=prev)result.add(previous.between(current,(y-previous.y)/(current.y-previous.y)));if(inside)result.add(current);previous=current;prev=inside;}return result;
    }
    private static void draw(PoseStack stack,VertexConsumer vertices,Mesh mesh,int color){
        var pose=stack.last();for(var face:mesh.faces()){
            var normal=face.normal();
            for(var p:face.points()){
                vertices.addVertex(pose.pose(),p.x()/16,p.y()/16,p.z()/16).setColor(color).setUv(p.u(),p.v()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose,normal.x,normal.y,normal.z);
            }
        }
    }
    static void forget(UUID player){MOTIONS.remove(player);POSITION_ERRORS.remove(player);POSE_DIFFERENCES.remove(player);}
    static void clear(){MOTIONS.clear();POSITION_ERRORS.clear();POSE_DIFFERENCES.clear();world=null;wide=slim=null;rendered=upperBodies=0;}
    static float positionError(UUID player){return POSITION_ERRORS.getOrDefault(player,Float.POSITIVE_INFINITY);}
    static float poseDifference(UUID player){return POSE_DIFFERENCES.getOrDefault(player,0f);}
    static void tick(UUID player){var motion=MOTIONS.get(player);if(motion!=null)motion.tick();}
    static long renderedEchoes(){return rendered;}
    static long renderedUpperBodies(){return upperBodies;}
    static int upperBodyEchoCount(UUID player){return MOTIONS.containsKey(player)?1:0;}
}
