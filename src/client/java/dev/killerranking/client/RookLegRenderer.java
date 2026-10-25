package dev.killerranking.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.killerranking.client.mixin.RushCubeAccess;
import dev.killerranking.visual.RookLegShape;
import dev.killerranking.visual.RookMountLayout;
import dev.killerranking.combat.RookMountPose;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/** Keep the original cuboid faces and skin UVs on detached leg blocks. */
public final class RookLegRenderer {
    private record Binding(UUID player,boolean left) { }
    private record Point(float x,float y,float z,float u,float v) { }
    private record Face(List<Point> points,Vector3f normal) { }
    private static final Map<ModelPart,Binding> PARTS=new WeakHashMap<>();
    private static final Map<ModelPart.Cube,List<Face>> MESH=new WeakHashMap<>();
    private static long rendered;
    public static void bind(PlayerModel<?> model,LivingEntity entity) {
        boolean active=entity instanceof AbstractClientPlayer&&RookVfx.mounting(entity.getUUID());
        for(var part:List.of(model.rightLeg,model.rightPants,model.leftLeg,model.leftPants)) {
            if(active) PARTS.put(part,new Binding(entity.getUUID(),part==model.leftLeg||part==model.leftPants));
            else PARTS.remove(part);
        }
    }
    public static boolean render(ModelPart part,List<ModelPart.Cube> cubes,Map<String,ModelPart> children,PoseStack stack,VertexConsumer v,int light,int overlay,int color) {
        var binding=PARTS.get(part);if(binding==null||!RookVfx.mounting(binding.player())) return false;
        var client=Minecraft.getInstance();
        if(client.level==null||!(client.level.getPlayerByUUID(binding.player()) instanceof AbstractClientPlayer owner)) return false;
        var victim=RookVfx.mountVictim(binding.player());if(victim==null)return false;
        if(!part.visible)return false;
        double spread=RookMountLayout.spread(victim.getBbWidth(),owner.getScale());
        double lift=RookMountLayout.legLift(victim.getBbHeight(),victim.getBbWidth(),RookMountPose.inverted(victim),owner.getScale());
        stack.pushPose();part.translateAndRotate(stack);
        if(!part.skipDraw) for(var cube:cubes) for(var face:MESH.computeIfAbsent(cube,RookLegRenderer::faces)) draw(stack,v,face,binding.left(),spread,lift,light,overlay,color);
        for(var child:children.values()) child.render(stack,v,light,overlay,color);
        stack.popPose();rendered++;return true;
    }
    private static List<Face> faces(ModelPart.Cube cube) {
        var faces=new ArrayList<Face>();
        for(var polygon:((RushCubeAccess)(Object)cube).killerranking$rushFaces()) {
            var points=Arrays.stream(polygon.vertices).map(v->new Point(v.pos.x,v.pos.y,v.pos.z,v.u,v.v)).toList();
            faces.add(new Face(points,new Vector3f(polygon.normal)));
        }
        return List.copyOf(faces);
    }
    private static void draw(PoseStack stack,VertexConsumer v,Face face,boolean left,double spread,double lift,int light,int overlay,int color) {
        var pose=stack.last();
        var normal=RookLegShape.normal(face.normal.x,face.normal.y,face.normal.z,left,spread);
        for(var point:face.points) {
            var p=RookLegShape.deform(point.x,point.y,point.z,left,spread,lift);
            v.addVertex(pose.pose(),(float)p.x()/16,(float)p.y()/16,(float)p.z()/16).setColor(color).setUv(point.u,point.v)
                .setOverlay(overlay).setLight(light).setNormal(pose,(float)normal.x(),(float)normal.y(),(float)normal.z());
        }
    }
    public static long renderedParts() { return rendered; }
    private RookLegRenderer() { }
}
