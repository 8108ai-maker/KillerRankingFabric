package dev.killerranking.client.mixin;

import dev.killerranking.client.RookVfx;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class RookPushClientMixin {
    @Inject(method="push(DDD)V",at=@At("HEAD"),cancellable=true)
    private void killer$noClientPush(double x,double y,double z,CallbackInfo ci) {
        if(RookVfx.immobilized((Entity)(Object)this)||dev.killerranking.client.ShoulderStabVfx.casting(((Entity)(Object)this).getUUID()))ci.cancel();
    }
}
