package dev.killerranking.client.mixin;
import dev.killerranking.client.ClientRook;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
abstract class RookInputMixin extends Input {
    @Inject(method="tick",at=@At("TAIL"))
    private void killerranking$grabKeys(boolean slow,float multiplier,CallbackInfo ci){
        if(ClientRook.locked()||dev.killerranking.client.ClientShoulderStab.phase()>=dev.killerranking.combat.ShoulderStabRules.CAST){leftImpulse=0;forwardImpulse=0;jumping=false;shiftKeyDown=false;up=false;down=false;left=false;right=false;}
    }
}
