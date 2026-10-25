package dev.killerranking.client;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.entity.BishopFollower;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public final class BishopFollowerRenderer extends HumanoidMobRenderer<BishopFollower,BishopModel> {
    private static final ResourceLocation TEXTURE=KillerRankingMod.id("textures/entity/bishop_brother.png");
    public BishopFollowerRenderer(EntityRendererProvider.Context context){
        super(context,new BishopModel(context.bakeLayer(BishopModel.LAYER)),.35f);
        layers.removeIf(layer->layer instanceof ItemInHandLayer);
        addLayer(new BishopCloakLayer(this));
        addLayer(new BishopMaskLayer(this,context.bakeLayer(BishopMaskLayer.LAYER)));
        addLayer(new BishopDaggerLayer(this,context.getItemInHandRenderer()));
    }
    @Override public ResourceLocation getTextureLocation(BishopFollower entity){return TEXTURE;}
}
