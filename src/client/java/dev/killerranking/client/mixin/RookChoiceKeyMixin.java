package dev.killerranking.client.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev.killerranking.client.KillerRankingClient;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public abstract class RookChoiceKeyMixin {
    @Inject(method = "set", at = @At("HEAD"))
    private static void killer$sharedSkillKey(InputConstants.Key key,boolean down,CallbackInfo ci){
        KillerRankingClient.sharedSkillKey(key,down);
    }
    // Vanilla's key map stores one binding per physical key. X/C also belong to
    // creative toolbar presets; handle the short grab choice before that map.
    @Inject(method = "click", at = @At("HEAD"), cancellable = true)
    private static void killer$rookChoice(InputConstants.Key key, CallbackInfo ci) {
        if (KillerRankingClient.rookChoiceKey(key)) ci.cancel();
    }
}
