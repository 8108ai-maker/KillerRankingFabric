package dev.killerranking.client.mixin;

import dev.killerranking.client.NeungsaDrawController;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** The optional Better Combat method also receives other players' attack packets. */
@Mixin(value=AbstractClientPlayer.class, priority=900)
abstract class NeungsaComboMixin {
    @ModifyVariable(method="playAttackAnimation", at=@At("HEAD"), argsOnly=true, ordinal=0, remap=false, require=0)
    private String neungsa$combo(String name, String original, @Coerce Object hand, float length, float upswing) {
        return NeungsaDrawController.attack((AbstractClientPlayer)(Object)this, name, length);
    }
}
