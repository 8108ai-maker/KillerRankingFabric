package dev.killerranking.client.mixin;
import dev.killerranking.client.CleaveEyesLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerRenderer.class)
abstract class CleaveEyesMixin extends LivingEntityRenderer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    protected CleaveEyesMixin(EntityRendererProvider.Context context,PlayerModel<AbstractClientPlayer> model,float radius){super(context,model,radius);}
    @Inject(method="<init>",at=@At("TAIL"))
    private void cleave$eyes(EntityRendererProvider.Context context,boolean slim,CallbackInfo ci){addLayer(new CleaveEyesLayer((PlayerRenderer)(Object)this));}
}
