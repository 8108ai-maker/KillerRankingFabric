package dev.killerranking.client;

import dev.killerranking.visual.HeavyPunchGrip;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Apply the complete reference pose after vanilla and the animation library finish setup. */
public final class HeavyPunchPose {
    public static void apply(LivingEntity e,HumanoidModel<?> model,PoseStack stack,float delta){
        if(!HeavyPunchVfx.active(e.getUUID()))return;
        boolean left=e.getMainArm()==HumanoidArm.LEFT;
        boolean fp=e==Minecraft.getInstance().player&&dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode.isFirstPersonPass();
        var pose=HeavyPunchGrip.pose(HeavyPunchVfx.age(e.getUUID(),delta),left,fp,center(e));
        set(model.body,pose.torso());set(model.head,pose.head());
        set(left?model.leftArm:model.rightArm,pose.punch());set(left?model.rightArm:model.leftArm,pose.brace());
        set(model.rightLeg,pose.rightLeg());set(model.leftLeg,pose.leftLeg());
        model.hat.copyFrom(model.head);
        if(model instanceof PlayerModel<?> player){
            player.leftSleeve.copyFrom(model.leftArm);player.rightSleeve.copyFrom(model.rightArm);
            player.jacket.copyFrom(model.body);player.leftPants.copyFrom(model.leftLeg);player.rightPants.copyFrom(model.rightLeg);
        }
    }
    private static void set(ModelPart part,HeavyPunchGrip.Limb pose){
        var p=pose.pivot();var r=pose.rotation();
        part.setPos((float)p.x,(float)p.y,(float)p.z);part.setRotation((float)r.x,(float)r.y,(float)r.z);
        part.xScale=part.yScale=part.zScale=1;
    }
    public static float center(LivingEntity e){return e instanceof AbstractClientPlayer p&&p.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM?.5f:1;}
    public static Vector3f localPoint(ModelPart arm,float handX,float handY){
        return new Matrix4f().translation(arm.x,arm.y,arm.z).rotateZYX(arm.zRot,arm.yRot,arm.xRot).transformPosition(new Vector3f(handX,handY,0));
    }
    private HeavyPunchPose() { }
}
