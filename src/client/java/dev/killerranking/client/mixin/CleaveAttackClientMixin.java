package dev.killerranking.client.mixin;
import dev.killerranking.client.CleaveVfx;
import dev.killerranking.client.ClientSkillCombat;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.*;
// Earlier injector order places these guards before Better Combat's default-order callbacks.
@Mixin(value=Minecraft.class,priority=1200)
abstract class CleaveAttackClientMixin {
    private static boolean casting(){var p=Minecraft.getInstance().player;return p!=null&&(CleaveVfx.active(p.getUUID())||dev.killerranking.client.MadnessLeapVfx.casting(p.getUUID()));}
    @Inject(method="startAttack",at=@At("HEAD"),cancellable=true,order=900)
    private void cleave$attack(CallbackInfoReturnable<Boolean> ci){if(ClientSkillCombat.blocksAttack())ci.setReturnValue(false);}
    @Inject(method="continueAttack",at=@At("HEAD"),cancellable=true,order=900)
    private void cleave$mine(boolean held,CallbackInfo ci){if(ClientSkillCombat.blocksAttack())ci.cancel();}
    @Inject(method="tick",at=@At("HEAD"),order=900)
    private void killerranking$pendingAttack(CallbackInfo ci){
        if(ClientSkillCombat.blocksAttack())ClientSkillCombat.cancelPendingAttack((Minecraft)(Object)this);
    }
    @Inject(method="startUseItem",at=@At("HEAD"),cancellable=true)
    private void cleave$use(CallbackInfo ci){if(casting())ci.cancel();}
}
