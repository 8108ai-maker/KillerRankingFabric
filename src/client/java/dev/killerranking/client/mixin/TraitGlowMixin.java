package dev.killerranking.client.mixin;

import dev.killerranking.client.TraitVisuals;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
abstract class TraitGlowMixin {
    @Inject(method="shouldEntityAppearGlowing", at=@At("RETURN"), cancellable=true)
    private void killerranking$outline(Entity entity, CallbackInfoReturnable<Boolean> result) {
        if (dev.killerranking.client.ClientShoulderStab.outlines(entity)||dev.killerranking.client.ClientKnifePursuit.outlines(entity)||dev.killerranking.client.ClientMadnessLeap.outlines(entity)||dev.killerranking.client.ClientRook.outlines(entity)||TraitVisuals.glows(entity)) result.setReturnValue(true);
    }
}
