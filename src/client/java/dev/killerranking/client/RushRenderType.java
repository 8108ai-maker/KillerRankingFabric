package dev.killerranking.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Unlit anime speed strokes with ordinary alpha and depth testing. */
final class RushRenderType extends RenderType {
    static final RenderType STREAK = create("killerranking_rush_strokes", DefaultVertexFormat.NEW_ENTITY,
        VertexFormat.Mode.QUADS, 2048, false, true, CompositeState.builder()
            .setShaderState(RENDERTYPE_EYES_SHADER)
            .setTextureState(new TextureStateShard(ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png"), false, false))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL)
            .setWriteMaskState(COLOR_WRITE).createCompositeState(false));
    private RushRenderType() {
        super("killerranking_rush_strokes", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
            2048, false, true, () -> {}, () -> {});
    }
}
