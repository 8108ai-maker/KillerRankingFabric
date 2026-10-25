package dev.killerranking.client.mixin;

import dev.killerranking.client.TraitVisuals;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Cosmetic outline color without changing scoreboard teams or persistent glowing flags. */
@Mixin(Entity.class)
abstract class TraitGlowColorMixin {
    @Inject(method="getTeamColor", at=@At("HEAD"), cancellable=true)
    private void killerranking$color(CallbackInfoReturnable<Integer> result) {
        var entity=(Entity)(Object)this;
        if(entity.level().isClientSide&&dev.killerranking.client.ClientShoulderStab.outlines(entity)){result.setReturnValue(0xDAABFF);return;}
        if(entity.level().isClientSide&&dev.killerranking.client.ClientKnifePursuit.outlines(entity)){result.setReturnValue(0xB566EF);return;}
        if(entity.level().isClientSide&&dev.killerranking.client.ClientMadnessLeap.outlines(entity)){result.setReturnValue(0xFF563A);return;}
        if(entity.level().isClientSide&&dev.killerranking.client.ClientRook.outlines(entity)){result.setReturnValue(0xFFE8A9);return;}
        if (entity.level().isClientSide && TraitVisuals.glows(entity)) result.setReturnValue(TraitVisuals.color(entity.getUUID()));
    }
}
