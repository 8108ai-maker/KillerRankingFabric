package dev.killerranking.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Block-built, coiled purple apparition; no entity, armor slot or gameplay hitbox. */
final class PoisonSnakeModel {
    private static final RenderType LAYER = RenderType.entityTranslucent(ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png"));
    private static final int BODY = 0x7042A8, SCALE = 0xA674E5, BELLY = 0xC5A4DD;
    static void render(WorldRenderContext context, Player player, float delta, float alpha) {
        if (PoisonVfx.salmosa(player.getUUID())) { SalmosaSnakeModel.render(context, player, delta, alpha); return; }
        var stack = context.matrixStack();
        var camera = context.camera().getPosition();
        var vertices = context.consumers().getBuffer(LAYER);
        stack.pushPose();
        stack.translate(Mth.lerp(delta, player.xOld, player.getX()) - camera.x,
            Mth.lerp(delta, player.yOld, player.getY()) - camera.y,
            Mth.lerp(delta, player.zOld, player.getZ()) - camera.z);
        stack.mulPose(Axis.YP.rotationDegrees(-Mth.rotLerp(delta, player.yBodyRotO, player.yBodyRot)));
        float time = player.tickCount + delta;
        float sway = (float) Math.sin(time * .055) * .10f;
        // Long, thick Minecraft-style blocks with a small visible joint gap.
        Vector3f previous = coil(0, time);
        for (int i = 1; i <= 16; i++) {
            float progress = i / 16f;
            var next = coil(progress, time);
            segment(stack, vertices, previous, next, .13f + .39f * Math.min(1, progress * 4), i, alpha);
            previous = next;
        }
        var base = previous;
        for (int i = 1; i <= 4; i++) {
            float t = i / 4f;
            var next = new Vector3f(base.x - (float) Math.sin(t * Math.PI) * .52f + sway * t,
                base.y + t * 1.65f, base.z + t * .95f);
            segment(stack, vertices, previous, next, .52f + .04f * t, i + 16, alpha);
            previous = next;
        }
        float age = PoisonVfx.age(player.getUUID()) + delta;
        stack.pushPose();
        stack.translate(previous.x, previous.y, previous.z);
        stack.mulPose(Axis.YP.rotationDegrees((float) Math.sin(time * .04) * 12));
        stack.mulPose(Axis.XP.rotationDegrees(headPitch(time, age)));
        head(stack, vertices, alpha, age);
        stack.popPose();
        stack.popPose();
    }
    private static void head(PoseStack s, VertexConsumer v, float alpha, float age) {
        float threat = threat(age);
        // Broad flat skull and a short blunt muzzle; no triangular beak or pupils.
        box(s,v,-.47f,-.045f,-.40f,.94f,.36f,.60f,BODY,alpha);
        box(s,v,-.34f,-.040f,.13f,.68f,.24f,.49f,BODY,alpha);
        box(s,v,-.30f,.202f,.18f,.60f,.025f,.36f,SCALE,alpha);
        box(s,v,-.29f,-.068f,.05f,.58f,.022f,.53f,0x2D163D,alpha);
        for (int side : new int[]{-1, 1}) {
            // Long fangs fold into the closed mouth and unfold during the hiss.
            s.pushPose();s.translate(side*.235f,-.065f,.36f);
            s.mulPose(Axis.XP.rotationDegrees(72*(1-threat)));
            box(s,v,-.045f,-.12f,-.045f,.09f,.12f,.09f,0xEDE2F7,alpha);
            box(s,v,-.031f,-.255f,-.031f,.062f,.135f,.062f,0xEDE2F7,alpha);
            box(s,v,-.015f,-.36f,-.015f,.03f,.105f,.03f,0xF6EDFF,alpha);
            s.popPose();
        }
        s.pushPose();s.translate(0,-.035f,-.20f);
        s.mulPose(Axis.XP.rotationDegrees(34*threat));
        box(s,v,-.39f,-.13f,0,.78f,.13f,.56f,0x76538D,alpha);
        box(s,v,-.29f,-.12f,.44f,.58f,.12f,.38f,BELLY,alpha);
        box(s,v,-.33f,.001f,.08f,.66f,.012f,.65f,0x381F46,alpha);
        float flick = tongue(age)*threat;
        if(flick>.02f){
            // The jaw drops, but the tongue probes forward out of the mouth.
            s.pushPose();s.translate(0,.018f,.69f);s.mulPose(Axis.XP.rotationDegrees(-24*threat));
            float length=.12f+.46f*flick;
            box(s,v,-.024f,0,0,.048f,.022f,length,0xCF3764,alpha);
            for(int side:new int[]{-1,1}){
                s.pushPose();s.translate(side*.014f,0,length-.015f);
                s.mulPose(Axis.YP.rotationDegrees(side*24));
                box(s,v,-.012f,0,0,.024f,.018f,.15f*flick,0xF06488,alpha);
                s.popPose();
            }
            s.popPose();
        }
        s.popPose();
    }
    static Vector3f headPoint(Player player, float delta, float x, float y, float z) {
        if (PoisonVfx.salmosa(player.getUUID())) return SalmosaSnakeModel.headPoint(player, delta, x, y, z);
        float time = player.tickCount + delta;
        var origin = new Vector3f(x, y, z)
            .rotateX((float) Math.toRadians(headPitch(time, PoisonVfx.age(player.getUUID())+delta)))
            .rotateY((float) Math.toRadians(Math.sin(time * .04) * 12));
        var base = coil(1, time);
        origin.add(base.x + (float) Math.sin(time * .055) * .10f, base.y + 1.65f, base.z + .95f);
        return origin.rotateY((float) Math.toRadians(-Mth.rotLerp(delta, player.yBodyRotO, player.yBodyRot)));
    }
    private static float headPitch(float time,float age){return 24+(float)Math.sin(time*.03)*2-6*threat(age);}
    private static float threat(float age){
        float t=age%72;
        return smooth((t-7)/10)*smooth((47-t)/12);
    }
    private static float smooth(float t){t=Mth.clamp(t,0,1);return t*t*(3-2*t);}
    private static float tongue(float age){
        float t=age%72,value=0;
        for(int center:new int[]{20,28,36})value=Math.max(value,Math.max(0,1-Math.abs(t-center)/3));
        return smooth(value);
    }
    private static Vector3f coil(float t, float time) {
        double angle = -Math.PI / 2 + t * Math.PI * 3.5;
        float radius = .80f + .43f * (float) Math.sin(t * Math.PI);
        return new Vector3f((float) Math.sin(angle) * radius,
            .28f + t * 1.22f + (float) Math.sin(angle * 2 + time * .065) * .045f,
            -1.55f + (float) Math.cos(angle) * .57f);
    }
    private static void segment(PoseStack stack, VertexConsumer v, Vector3f a, Vector3f b, float width, int index, float alpha) {
        var direction = new Vector3f(b).sub(a);
        float length = direction.length();
        stack.pushPose(); stack.translate((a.x + b.x) * .5, (a.y + b.y) * .5, (a.z + b.z) * .5);
        stack.mulPose(new Quaternionf().rotationTo(new Vector3f(0, 1, 0), direction.normalize()));
        float half = width * .5f;
        float blockLength=length*.90f;
        box(stack, v, -half, -blockLength * .5f, -half, width, blockLength, width, BODY, alpha);
        // Flush pixel panels, rather than more small raised geometric parts.
        float patch = width * (index % 3 == 0 ? .65f : .45f);
        face(stack,v,SCALE,alpha,0,0,1,-patch*.5f,-blockLength*.26f,half+.001f,patch*.5f,-blockLength*.26f,half+.001f,patch*.5f,blockLength*.26f,half+.001f,-patch*.5f,blockLength*.26f,half+.001f);
        face(stack,v,BELLY,alpha,0,0,-1,half*.7f,-blockLength*.40f,-half-.001f,-half*.7f,-blockLength*.40f,-half-.001f,-half*.7f,blockLength*.40f,-half-.001f,half*.7f,blockLength*.40f,-half-.001f);
        stack.popPose();
    }
    static void box(PoseStack s, VertexConsumer v, float x, float y, float z, float w, float h, float d, int color, float alpha) {
        float X = x + w, Y = y + h, Z = z + d;
        face(s,v,color,alpha,0,0,-1, X,y,z, x,y,z, x,Y,z, X,Y,z);
        face(s,v,color,alpha,0,0,1, x,y,Z, X,y,Z, X,Y,Z, x,Y,Z);
        face(s,v,color,alpha,-1,0,0, x,y,z, x,y,Z, x,Y,Z, x,Y,z);
        face(s,v,color,alpha,1,0,0, X,y,Z, X,y,z, X,Y,z, X,Y,Z);
        face(s,v,color,alpha,0,1,0, x,Y,Z, X,Y,Z, X,Y,z, x,Y,z);
        face(s,v,color,alpha,0,-1,0, x,y,z, X,y,z, X,y,Z, x,y,Z);
    }
    static void face(PoseStack s, VertexConsumer v, int c, float a, float nx, float ny, float nz, float... p) {
        for (int i = 0; i < 4; i++) v.addVertex(s.last().pose(), p[i*3], p[i*3+1], p[i*3+2])
            .setColor((c >> 16 & 255) / 255f, (c >> 8 & 255) / 255f, (c & 255) / 255f, a)
            .setUv(i == 0 || i == 3 ? 0 : 1, i < 2 ? 1 : 0).setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(0xF000F0).setNormal(s.last(), nx, ny, nz);
    }
    private PoisonSnakeModel() { }
}
