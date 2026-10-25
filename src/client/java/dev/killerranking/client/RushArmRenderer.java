package dev.killerranking.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.killerranking.client.mixin.RushCubeAccess;
import dev.killerranking.visual.RushTorso;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/** Rigid native skin arms reposition at the shoulder; cloth follows the same matrix. */
public final class RushArmRenderer {
    private record Binding(UUID player,boolean left,boolean hand) { }
    private record Bandage(org.joml.Matrix4f pose,org.joml.Matrix3f normal,boolean slim,float center,
                           int light,int overlay,int color) { }
    private static final Map<UUID,List<Bandage>> BANDAGES=new HashMap<>();
    private record Point(float x,float y,float z,float u,float v) {
        Point between(Point b,float t) { return new Point(x+(b.x-x)*t,y+(b.y-y)*t,z+(b.z-z)*t,u+(b.u-u)*t,v+(b.v-v)*t); }
    }
    private record Face(List<Point> points,Vector3f normal) { }
    private static final Map<ModelPart,Binding> PARTS=new WeakHashMap<>();
    private static final Map<ModelPart.Cube,List<Face>> MESH=new WeakHashMap<>();
    private static long rendered;
    public static void bind(PlayerModel<?> model,LivingEntity entity) {
        SkillArmTrails.bind(model,entity);
        boolean active=entity instanceof AbstractClientPlayer&&(RushVfx.active(entity.getUUID())||HeavyPunchVfx.active(entity.getUUID()));
        if(active)anchorTorso(model);
        for(var part:List.of(model.rightArm,model.rightSleeve,model.leftArm,model.leftSleeve)) {
            if(active) PARTS.put(part,new Binding(entity.getUUID(),part==model.leftArm||part==model.leftSleeve,
                part==model.leftArm||part==model.rightArm));
            else PARTS.remove(part);
        }
    }
    private static void anchorTorso(PlayerModel<?> model){
        var hip=new Vec3((model.leftLeg.x+model.rightLeg.x)*.5,(model.leftLeg.y+model.rightLeg.y)*.5,(model.leftLeg.z+model.rightLeg.z)*.5);
        var pivot=RushTorso.pivot(hip,new Vec3(model.body.xRot,model.body.yRot,model.body.zRot));
        float dx=(float)pivot.x-model.body.x,dy=(float)pivot.y-model.body.y,dz=(float)pivot.z-model.body.z;
        model.body.x=(float)pivot.x;model.body.y=(float)pivot.y;model.body.z=(float)pivot.z;
        model.jacket.copyFrom(model.body);
        for(var part:List.of(model.head,model.hat,model.rightArm,model.rightSleeve,model.leftArm,model.leftSleeve)){
            part.x+=dx;part.y+=dy;part.z+=dz;
        }
    }
    public static boolean render(ModelPart part,List<ModelPart.Cube> cubes,Map<String,ModelPart> children,PoseStack stack,VertexConsumer v,int light,int overlay,int color) {
        SkillArmTrails.sample(part,stack);
        var binding=PARTS.get(part);if(binding==null||!(RushVfx.active(binding.player())||HeavyPunchVfx.active(binding.player()))) return false;
        var client=Minecraft.getInstance();
        if(client.level==null||!(client.level.getPlayerByUUID(binding.player()) instanceof AbstractClientPlayer p)) return false;
        if(!part.visible) return false;
        boolean firstPerson=p==client.player&&client.options.getCameraType().isFirstPerson();
        double center=(p.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM?.5:1)*(binding.left()?1:-1);
        stack.pushPose();part.translateAndRotate(stack);
        if(firstPerson&&!HeavyPunchVfx.active(binding.player()))stack.translate(0,.06,.05);
        if(!part.skipDraw) for(var cube:cubes) for(var face:MESH.computeIfAbsent(cube,RushArmRenderer::subdivide)) draw(stack,v,face,light,overlay,color);
        for(var child:children.values()) child.render(stack,v,light,overlay,color);
        var hand=binding.left()==(p.getMainArm()==net.minecraft.world.entity.HumanoidArm.LEFT)
            ?net.minecraft.world.InteractionHand.MAIN_HAND:net.minecraft.world.InteractionHand.OFF_HAND;
        if(binding.hand()&&!part.skipDraw&&p.getItemInHand(hand).is(dev.killerranking.KillerRankingMod.TTEOKBAL_FIST))
            BANDAGES.computeIfAbsent(binding.player(),id->new ArrayList<>()).add(new Bandage(
                new org.joml.Matrix4f(stack.last().pose()),new org.joml.Matrix3f(stack.last().normal()),
                p.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM,(float)center,light,overlay,color));
        stack.popPose();rendered++;return true;
    }
    public static void beginBandages(LivingEntity entity) { BANDAGES.remove(entity.getUUID()); }
    public static void finishBandages(LivingEntity entity,net.minecraft.client.renderer.MultiBufferSource buffers) {
        var layers=BANDAGES.remove(entity.getUUID());if(layers==null)return;
        // Changing textures only after the model finishes preserves its active skin buffer.
        var stack=new PoseStack();
        for(var b:layers) {
            stack.last().pose().set(b.pose());stack.last().normal().set(b.normal());
            FistBandages.render(stack,buffers,b.slim(),b.center(),b.light(),b.overlay(),b.color());
        }
    }
    private static List<Face> subdivide(ModelPart.Cube cube) {
        var faces=new ArrayList<Face>();
        for(var polygon:((RushCubeAccess)(Object)cube).killerranking$rushFaces()) {
            var points=Arrays.stream(polygon.vertices).map(v->new Point(v.pos.x,v.pos.y,v.pos.z,v.u,v.v)).toList();
            float low=points.stream().map(p->p.y).min(Float::compare).orElse(0f),high=points.stream().map(p->p.y).max(Float::compare).orElse(0f);
            if(high-low<.001) {faces.add(new Face(points,new Vector3f(polygon.normal)));continue;}
            for(float y=low;y<high;y+=.75f) {
                var clipped=clip(clip(points,y,true),Math.min(y+.75f,high),false);
                if(clipped.size()==4) faces.add(new Face(clipped,new Vector3f(polygon.normal)));
            }
        }
        return List.copyOf(faces);
    }
    private static List<Point> clip(List<Point> input,float y,boolean above) {
        var result=new ArrayList<Point>();if(input.isEmpty())return result;
        var previous=input.getLast();boolean prev=above?previous.y>=y:previous.y<=y;
        for(var current:input) {
            boolean inside=above?current.y>=y:current.y<=y;
            if(inside!=prev) result.add(previous.between(current,(y-previous.y)/(current.y-previous.y)));
            if(inside)result.add(current);previous=current;prev=inside;
        }
        return result;
    }
    private static void draw(PoseStack stack,VertexConsumer v,Face face,int light,int overlay,int color) {
        var pose=stack.last();var n=face.normal;
        for(var point:face.points) {
            v.addVertex(pose.pose(),point.x/16,point.y/16,point.z/16).setColor(color).setUv(point.u,point.v)
                .setOverlay(overlay).setLight(light).setNormal(pose,n.x,n.y,n.z);
        }
    }
    public static long renderedParts() { return rendered; }
    private RushArmRenderer() { }
}
