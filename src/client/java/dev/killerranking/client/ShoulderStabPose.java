package dev.killerranking.client;
import dev.killerranking.visual.HeavyPunchGrip;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public final class ShoulderStabPose {
    public static void apply(LivingEntity e,EntityModel<?> base,PoseStack stack,float delta){
        if(!ShoulderStabVfx.casting(e.getUUID())||!(base instanceof HumanoidModel<?> model))return;
        var pose=ShoulderStabVfx.pose(e,stack,delta);
        set(model.body,pose.torso());set(model.head,pose.head());set(model.rightArm,pose.rightArm());set(model.leftArm,pose.leftArm());set(model.rightLeg,pose.rightLeg());set(model.leftLeg,pose.leftLeg());
        model.hat.copyFrom(model.head);if(model instanceof PlayerModel<?> p){p.jacket.copyFrom(model.body);p.rightSleeve.copyFrom(model.rightArm);p.leftSleeve.copyFrom(model.leftArm);p.rightPants.copyFrom(model.rightLeg);p.leftPants.copyFrom(model.leftLeg);}
    }
    private static void set(ModelPart part,HeavyPunchGrip.Limb limb){var p=limb.pivot();var r=limb.rotation();part.setPos((float)p.x,(float)p.y,(float)p.z);part.setRotation((float)r.x,(float)r.y,(float)r.z);part.xScale=part.yScale=part.zScale=1;}
    private ShoulderStabPose(){ }
}
