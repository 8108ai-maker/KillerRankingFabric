package dev.killerranking.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Unlit on both faces, with ordinary alpha blending for the fading pixel frames. */
final class WeaponTrailRenderType extends RenderType {
    static final RenderType BREATH=create("killerranking_neungsa_breath",
        DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,4096,false,true,
        CompositeState.builder().setShaderState(RENDERTYPE_EYES_SHADER)
            .setTextureState(new TextureStateShard(net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png"),false,false))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL)
            .setWriteMaskState(COLOR_WRITE).createCompositeState(false));
    private static final Map<ResourceLocation, RenderType> SPRITES = new HashMap<>();
    private static final Map<TrailStyle, RenderType> STYLES = new EnumMap<>(TrailStyle.class);

    /** Full-bright translucent quads over any mod texture (trails, hit sprites). */
    static RenderType sprite(ResourceLocation texture) {
        return SPRITES.computeIfAbsent(texture, t -> create("killerranking_sprite_" + t.getPath().replace('/', '_'),
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, false, true,
            CompositeState.builder()
                // Entity-translucent-emissive still applies directional normal lighting.
                // The eyes shader preserves the texture's RGB regardless of blade direction.
                .setShaderState(RENDERTYPE_EYES_SHADER)
                .setTextureState(new TextureStateShard(t, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false)));
    }

    static RenderType of(TrailStyle style) { return STYLES.computeIfAbsent(style, s -> sprite(s.texture)); }

    static final RenderType NEUNGSA = of(TrailStyle.NEUNGSA);
    static final RenderType LAYER = of(TrailStyle.GENERIC);

    private WeaponTrailRenderType() {
        super("killerranking_weapon_trail", DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 1536, false, true, () -> {}, () -> {});
    }
}
