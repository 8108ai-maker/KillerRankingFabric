package dev.killerranking.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.killerranking.client.RookVfx;
import dev.killerranking.combat.RookMountPose;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class RookProneMixin {
    @Inject(method="setupRotations",at=@At("HEAD"),cancellable=true)
    private void killer$groundedMount(LivingEntity victim,PoseStack pose,float age,float bodyYaw,float partialTick,float scale,CallbackInfo ci) {
        var owner=RookVfx.mountingOwner(victim.getUUID());
        boolean wasabari=false;if(owner==null){owner=dev.killerranking.client.WasabariVfx.captor(victim.getUUID());wasabari=owner!=null;}if(owner==null)return;
        float progress=wasabari?dev.killerranking.client.WasabariVfx.proneProgress(victim.getUUID(),partialTick):1;
        float impact=RookVfx.mountImpact(victim.getUUID(),partialTick);
        pose.translate(.035*impact,.02*impact,0);
        var direction=victim.position().subtract(owner.position());
        float forwardYaw=(float)Math.toDegrees(Math.atan2(-direction.x,direction.z));
        if(RookMountPose.inverted(victim)) {
            pose.mulPose(Axis.YP.rotationDegrees(180-forwardYaw));
            pose.translate(0,victim.getBbHeight()*.98*progress,0);
            pose.mulPose(Axis.ZP.rotationDegrees(180*progress));
        } else {
            float yaw=forwardYaw+180;
            pose.mulPose(Axis.YP.rotationDegrees(180-yaw));
            pose.translate(0,(.18+victim.getBbWidth()*.15)*progress,-victim.getBbHeight()*.52*progress);
            pose.mulPose(Axis.XP.rotationDegrees(90*progress));
        }
        ci.cancel();
    }
}
