package dev.killerranking.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Cloth wraps the rigid hand at native size when Rush hides held items. */
final class FistBandages {
    private static final ResourceLocation CLOTH=ResourceLocation.withDefaultNamespace("textures/block/white_wool.png");

    static void render(PoseStack stack,MultiBufferSource buffers,boolean slim,float center,
                       int light,int overlay,int color) {
        var v=buffers.getBuffer(RenderType.entityTranslucent(CLOTH));var pose=stack.last();
        float radius=slim?1.82f:2.32f;
        // Broad, overlapping wraps leave the player's fingertips and sleeve visible.
        for(int band=0;band<6;band++) {
            float y0=6.4f+band*.84f,y1=y0+.98f;
            float r=radius+(band%2==0?.07f:0);
            float[][] faces={
                {-r,y0,-r,r,y0,-r,r,y1,-r,-r,y1,-r},
                {r,y0,r,-r,y0,r,-r,y1,r,r,y1,r},
                {-r,y0,r,-r,y0,-r,-r,y1,-r,-r,y1,r},
                {r,y0,-r,r,y0,r,r,y1,r,r,y1,-r}
            };
            float[][] normals={{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
            for(int face=0;face<4;face++)for(int i=0;i<4;i++) {
                float x=faces[face][i*3],y=faces[face][i*3+1],z=faces[face][i*3+2];
                x+=center;
                var normal=normals[face];
                v.addVertex(pose.pose(),x/16,y/16,z/16).setColor(color)
                    .setUv(i==0||i==3?0:1,(i<2?0:1)*.25f+band*.1f)
                    .setOverlay(overlay).setLight(light)
                    .setNormal(pose,normal[0],normal[1],normal[2]);
            }
        }
    }
    private FistBandages() { }
}
