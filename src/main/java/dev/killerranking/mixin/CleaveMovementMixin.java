package dev.killerranking.mixin;
import dev.killerranking.KillerRankingMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntity.class)
abstract class CleaveMovementMixin {
    @Inject(method="travel",at=@At("HEAD"),cancellable=true)
    private void cleave$casting(Vec3 movement,CallbackInfo ci){
        if((Object)this instanceof ServerPlayer p&&(KillerRankingMod.NEUNGSA_SKILL.dashing(p)||KillerRankingMod.SHOULDER_STAB.casting(p)||KillerRankingMod.CLEAVE.slashing(p)||KillerRankingMod.MADNESS_LEAP.casting(p))){p.setDeltaMovement(Vec3.ZERO);ci.cancel();}
    }
}
