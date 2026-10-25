package dev.killerranking.client;
import dev.killerranking.visual.WasabariMotion;
import dev.killerranking.visual.HeavyPunchGrip;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.QuadrupedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.killerranking.client.mixin.WasabariQuadrupedAccess;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;

public final class WasabariPose {
    public static void apply(LivingEntity e,EntityModel<?> base,PoseStack stack,float delta){
        var owner=WasabariVfx.captor(e.getUUID());
        if(base instanceof net.minecraft.client.model.HierarchicalModel<?> tree&&owner!=null){
            boolean left=owner.getMainArm()==HumanoidArm.LEFT;
            boolean inverted=dev.killerranking.combat.RookMountPose.inverted(e);
            // The upside-down body reverses its left/right leg sides, as on QuadrupedModel.
            String[] names=inverted?(left?new String[]{"right_hind_leg","right_back_leg","right_leg"}:new String[]{"left_hind_leg","left_back_leg","left_leg"})
                :(left?new String[]{"left_leg"}:new String[]{"right_leg"});
            for(String name:names){var found=tree.getAnyDescendantWithName(name);if(found.isEmpty())continue;
                // Includes parents such as the Warden's bone pivot; these models are not HumanoidModel.
                tree.root().getAllParts().forEach(ModelPart::resetPose);
                var leg=found.get();leg.xRot=inverted?1.1f*(float)WasabariMotion.grabWeight(WasabariVfx.age(owner.getUUID(),delta)):WasabariMotion.raisedLegPitch(WasabariVfx.age(owner.getUUID(),delta));
                leg.yRot=leg.zRot=0;
                WasabariVfx.recordLeg(e,tree,leg,name,stack);return;
            }
        }
        if(base instanceof QuadrupedModel<?> &&base instanceof WasabariQuadrupedAccess quad&&owner!=null){
            var leg=owner.getMainArm()==HumanoidArm.LEFT?quad.wasabari$rightHindLeg():quad.wasabari$leftHindLeg();
            leg.xRot=.55f*(float)WasabariMotion.grabWeight(WasabariVfx.age(owner.getUUID(),delta));
            leg.yRot=leg.zRot=0;WasabariVfx.recordLeg(e,leg,stack);return;
        }
        if(!(base instanceof HumanoidModel<?> model))return;
        if(WasabariVfx.active(e.getUUID())){
            var pose=WasabariVfx.heldPose(e,stack,delta);
            set(model.body,pose.torso());set(model.head,pose.head());set(model.rightArm,pose.rightArm());set(model.leftArm,pose.leftArm());set(model.rightLeg,pose.rightLeg());set(model.leftLeg,pose.leftLeg());
        }else if(WasabariVfx.captured(e.getUUID())){
            model.body.setPos(0,0,0);model.body.setRotation(0,0,0);model.head.setPos(0,0,0);model.head.setRotation(0,0,0);
            model.rightArm.setRotation(0,0,.1f);model.leftArm.setRotation(0,0,-.1f);
            model.rightLeg.setRotation(0,0,.035f);model.leftLeg.setRotation(0,0,-.035f);
            var leg=owner!=null&&owner.getMainArm()==HumanoidArm.LEFT?model.leftLeg:model.rightLeg;
            if(owner!=null){leg.xRot=WasabariMotion.raisedLegPitch(WasabariVfx.age(owner.getUUID(),delta));leg.zRot=0;WasabariVfx.recordLeg(e,leg,stack);}
        }else return;
        model.hat.copyFrom(model.head);
        if(model instanceof PlayerModel<?> p){p.jacket.copyFrom(model.body);p.rightSleeve.copyFrom(model.rightArm);p.leftSleeve.copyFrom(model.leftArm);p.rightPants.copyFrom(model.rightLeg);p.leftPants.copyFrom(model.leftLeg);}
    }
    private static void set(ModelPart p,HeavyPunchGrip.Limb limb){var at=limb.pivot();var r=limb.rotation();p.setPos((float)at.x,(float)at.y,(float)at.z);p.setRotation((float)r.x,(float)r.y,(float)r.z);p.xScale=p.yScale=p.zScale=1;}
    private WasabariPose() { }
}
