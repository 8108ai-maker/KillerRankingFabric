package dev.killerranking.client;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Tint the real animated mesh, armour and held item. No detached duplicate body. */
public final class CleaveSilhouetteBuffers implements MultiBufferSource {
    private final MultiBufferSource original;
    public CleaveSilhouetteBuffers(MultiBufferSource original){this.original=original;}
    public static MultiBufferSource unwrap(MultiBufferSource b){return b instanceof CleaveSilhouetteBuffers s?s.original:b;}
    @Override public VertexConsumer getBuffer(RenderType type){return new DarkVertex(original.getBuffer(type));}
    private record DarkVertex(VertexConsumer v) implements VertexConsumer {
        @Override public VertexConsumer addVertex(float x,float y,float z){v.addVertex(x,y,z);return this;}
        @Override public VertexConsumer setColor(int r,int g,int b,int a){v.setColor(0,0,0,a);return this;}
        @Override public VertexConsumer setUv(float u,float w){v.setUv(u,w);return this;}
        @Override public VertexConsumer setUv1(int u,int w){v.setOverlay(OverlayTexture.NO_OVERLAY);return this;}
        @Override public VertexConsumer setUv2(int u,int w){v.setUv2(u,w);return this;}
        @Override public VertexConsumer setNormal(float x,float y,float z){v.setNormal(x,y,z);return this;}
    }
}
