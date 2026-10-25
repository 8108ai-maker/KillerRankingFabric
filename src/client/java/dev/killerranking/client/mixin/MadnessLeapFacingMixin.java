package dev.killerranking.client.mixin;

import dev.killerranking.client.MadnessLeapVfx;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Aim can move freely; only the visible body faces its committed landing point. */
@Mixin(LivingEntityRenderer.class)
abstract class MadnessLeapFacingMixin {
    @ModifyVariable(method="setupRotations",at=@At("HEAD"),argsOnly=true,ordinal=1)
    private float madnessLeap$facing(float yaw,LivingEntity entity,PoseStack pose,float age,float bodyYaw,float partialTick,float scale) {
        return FirstPersonMode.isFirstPersonPass()?yaw:dev.killerranking.client.HeavyPunchVfx.facing(entity,MadnessLeapVfx.yaw(entity.getUUID(),yaw));
    }
}
