package dev.killerranking.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/** Brush strokes, a flickering hot rim, and repeated downward blood flows; entirely cosmetic. */
final class MadnessGlyph {
    private static final RenderType CORE=layer("madness_kanji",false), EDGE=layer("madness_edge",false), GLOW=layer("madness_glow",true);
    private static final RenderType LIQUID=RenderType.entityTranslucent(ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png"));
    private static final float WIDTH=3.55f, HEIGHT=4.20f, CENTER=1.96f;
    // Sources lie on brush-stroke ends/edges in the 512px glyph. Length is capped above the owner's feet.
    private static final float[][] SOURCES={{87,119,.043f,2.35f},{58,237,.063f,1.85f},{42,354,.055f,.95f},
        {166,315,.040f,1.35f},{238,170,.032f,2.30f},{393,157,.058f,2.50f},
        {381,269,.044f,1.72f},{449,363,.064f,1.01f},{304,371,.033f,1.00f}};
    private static RenderType layer(String name,boolean glow) {
        var texture=ResourceLocation.fromNamespaceAndPath("killerranking","textures/vfx/"+name+".png");
        return glow?RenderType.entityTranslucentEmissive(texture):RenderType.entityTranslucent(texture);
    }
    static void render(WorldRenderContext context,Player player,float delta,float alpha,float time) {
        var pose=context.matrixStack();var camera=context.camera().getPosition();
        float yaw=Mth.rotLerp(delta,player.yBodyRotO,player.yBodyRot);
        pose.pushPose();
        pose.translate(Mth.lerp(delta,player.xOld,player.getX())-camera.x,Mth.lerp(delta,player.yOld,player.getY())-camera.y,Mth.lerp(delta,player.zOld,player.getZ())-camera.z);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.translate(Math.sin(time*.045)*.025,CENTER+Math.sin(time*.07)*.025,-1.00);
        double radians=Math.toRadians(yaw);
        if((camera.x-player.getX())*-Math.sin(radians)+(camera.z-player.getZ())*Math.cos(radians)<0) pose.mulPose(Axis.YP.rotationDegrees(180));
        float flicker=.83f+.10f*(float)Math.sin(time*.51)+.07f*(float)Math.sin(time*1.31);
        quad(context,GLOW,WIDTH*1.04f,HEIGHT*1.04f,-.024f,0xEB342A,alpha*.80f*flicker);
        quad(context,CORE,WIDTH,HEIGHT,0,0x8F1020,alpha*.87f);
        quad(context,EDGE,WIDTH,HEIGHT,.008f,0xFFAC83,alpha*.87f*flicker);
        renderBlood(context,time,alpha);
        pose.popPose();
    }
    private static void renderBlood(WorldRenderContext context,float time,float alpha) {
        var v=context.consumers().getBuffer(LIQUID);
        for(int index=0;index<SOURCES.length;index++) {
            var source=SOURCES[index];float x=(source[0]/512f-.5f)*WIDTH,y=(.5f-source[1]/512f)*HEIGHT;
            float length=Math.min(source[3],CENTER+y-.045f);
            float t=(time/(34+index%3*7f)+index*.173f)%1;
            float head=Math.min(1,t*1.9f),tail=Math.max(0,(t-.48f)*1.72f);
            float opacity=alpha*Math.min(1,t*10)*Math.min(1,(1-t)*9);
            for(int part=0;part<10;part++) {
                float a=tail+(head-tail)*part/10f,b=tail+(head-tail)*(part+1)/10f;
                float xa=x+bend(index,a,time),xb=x+bend(index,b,time),ya=y-a*length,yb=y-b*length;
                float wa=source[2]*(1-a*.55f),wb=source[2]*(1-b*.55f);
                ribbon(context,v,xa,ya,xb,yb,wa+.012f,wb+.012f,.025f,0x4C0610,opacity*.86f);
                ribbon(context,v,xa,ya,xb,yb,wa,wb,.028f,0xA71825,opacity*.95f);
                float flow=(float)Math.pow(Math.max(0,Math.sin(time*.32f-a*14+index)),6);
                ribbon(context,v,xa-wa*.2f,ya,xb-wb*.2f,yb,wa*.23f,wb*.23f,.031f,0xE14135,opacity*(.18f+flow*.58f));
            }
            // Rounded droplet heads stretch and fall free below each flowing streak.
            float fall=Math.max(0,(t-.52f)/.48f),dropY=y-head*length-fall*fall*.43f;
            if(CENTER+dropY>.03f) {
                float dropX=x+bend(index,head,time);
                droplet(context,v,dropX,dropY,source[2]*1.45f,source[2]*(1.8f+fall*1.7f),.035f,0xAD1521,opacity);
                droplet(context,v,dropX-source[2]*.3f,dropY+source[2]*.15f,source[2]*.3f,source[2]*.75f,.039f,0xE34132,opacity*.65f);
            }
        }
    }
    private static float bend(int index,float amount,float time) { return (float)Math.sin(time*.07+index*1.9+amount*3)*.025f*amount; }
    private static void quad(WorldRenderContext context,RenderType type,float width,float height,float z,int color,float alpha) {
        var v=context.consumers().getBuffer(type);
        vertex(context,v,-width/2,-height/2,z,0,1,color,alpha);vertex(context,v,width/2,-height/2,z,1,1,color,alpha);
        vertex(context,v,width/2,height/2,z,1,0,color,alpha);vertex(context,v,-width/2,height/2,z,0,0,color,alpha);
    }
    private static void ribbon(WorldRenderContext context,VertexConsumer v,float xa,float ya,float xb,float yb,float wa,float wb,float z,int color,float alpha) {
        vertex(context,v,xb-wb,yb,z,0,1,color,alpha);vertex(context,v,xb+wb,yb,z,1,1,color,alpha);
        vertex(context,v,xa+wa,ya,z,1,0,color,alpha);vertex(context,v,xa-wa,ya,z,0,0,color,alpha);
    }
    private static void droplet(WorldRenderContext context,VertexConsumer v,float x,float y,float w,float h,float z,int color,float alpha) {
        // Four quads form a tapered eight-sided silhouette instead of a square particle.
        float[][] edge={{0,h},{w*.7f,h*.35f},{w,-h*.3f},{w*.65f,-h},{0,-h*1.15f},{-w*.65f,-h},{-w,-h*.3f},{-w*.7f,h*.35f}};
        for(int i=0;i<8;i+=2) {
            vertex(context,v,x,y,z,.5f,.5f,color,alpha);
            for(int k=0;k<3;k++) { var point=edge[(i+k)%8];vertex(context,v,x+point[0],y+point[1],z,.5f,.5f,color,alpha); }
        }
    }
    private static void vertex(WorldRenderContext context,VertexConsumer v,float x,float y,float z,float u,float uv,int color,float alpha) {
        var pose=context.matrixStack().last();
        v.addVertex(pose.pose(),x,y,z).setColor((color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f,alpha)
            .setUv(u,uv).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose,0,1,0);
    }
    private MadnessGlyph() { }
}
