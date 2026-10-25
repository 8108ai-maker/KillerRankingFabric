package dev.killerranking.client.mixin;
import dev.killerranking.client.WasabariKnifeLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerRenderer.class)
abstract class WasabariKnifeLayerMixin extends LivingEntityRenderer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    protected WasabariKnifeLayerMixin(EntityRendererProvider.Context context,PlayerModel<AbstractClientPlayer> model,float radius){super(context,model,radius);}
    @Inject(method="<init>",at=@At("TAIL")) private void wasabari$knife(EntityRendererProvider.Context context,boolean slim,CallbackInfo ci){addLayer(new WasabariKnifeLayer((PlayerRenderer)(Object)this,context.getItemInHandRenderer()));addLayer(new dev.killerranking.client.ShoulderStabKnifeLayer((PlayerRenderer)(Object)this,context.getItemInHandRenderer()));}
}
