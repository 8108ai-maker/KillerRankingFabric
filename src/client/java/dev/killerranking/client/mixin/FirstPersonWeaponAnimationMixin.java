package dev.killerranking.client.mixin;

import dev.killerranking.client.FirstPersonWeaponView;
import dev.killerranking.visual.FirstPersonWeaponPose;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=KeyframeAnimationPlayer.class,remap=false)
abstract class FirstPersonWeaponAnimationMixin {
    @Shadow public abstract KeyframeAnimation getData();
    @Shadow public abstract int getCurrentTick();

    @Inject(method="getFirstPersonMode",at=@At("RETURN"),cancellable=true)
    private void katanaView(float delta,CallbackInfoReturnable<FirstPersonMode> ci) {
        if(FirstPersonWeaponPose.isKatana(getData().extraData.get("name")))
            ci.setReturnValue(FirstPersonMode.THIRD_PERSON_MODEL);
    }

    @Inject(method="getFirstPersonConfiguration",at=@At("RETURN"),cancellable=true)
    private void katanaHands(float delta,CallbackInfoReturnable<FirstPersonConfiguration> ci) {
        if(!FirstPersonWeaponPose.isKatana(getData().extraData.get("name")))return;
        var original=ci.getReturnValue();
        ci.setReturnValue(new FirstPersonConfiguration(true,!FirstPersonWeaponPose.isChargedIaido(getData().extraData.get("name")),original.isShowRightItem(),original.isShowLeftItem()));
    }

    @Inject(method="get3DTransform",at=@At("RETURN"),cancellable=true)
    private void viewPose(String part,TransformType type,float delta,Vec3f baseline,CallbackInfoReturnable<Vec3f> ci) {
        if(!FirstPersonWeaponView.active()||!FirstPersonWeaponPose.supports(getData().extraData.get("name"))) return;
        if((FirstPersonWeaponPose.isKatana(getData().extraData.get("name"))
            ||FirstPersonWeaponPose.isTteokbalFist(getData().extraData.get("name")))&&type==TransformType.POSITION
            &&(part.equals("rightArm")||part.equals("leftArm"))) {
            var channel=getData().getPart(part);
            if(channel==null||!channel.x.isEnabled())return;
        }
        ci.setReturnValue(FirstPersonWeaponPose.adjust(getData().extraData.get("name"),part,type,ci.getReturnValue(),getCurrentTick()+delta,getData().endTick,getData().stopTick));
    }
}
