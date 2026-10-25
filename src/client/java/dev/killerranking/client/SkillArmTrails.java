package dev.killerranking.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Captures real hand matrices for bare-hand skills, including Rook's separate mounted scene. */
public final class SkillArmTrails {
    private record Binding(AbstractClientPlayer player,boolean left){ }
    private static final Map<ModelPart,Binding> PARTS=new WeakHashMap<>();
    public static void bind(PlayerModel<?> model,LivingEntity entity){
        if(entity instanceof AbstractClientPlayer p&&(RushVfx.active(p.getUUID())||RookVfx.active(p.getUUID()))){
            PARTS.put(model.rightArm,new Binding(p,false));PARTS.put(model.leftArm,new Binding(p,true));
        }else{PARTS.remove(model.rightArm);PARTS.remove(model.leftArm);}
    }
    public static void sample(ModelPart part,PoseStack stack){
        var b=PARTS.get(part);if(b==null||!part.visible||part.skipDraw)return;
        var p=b.player();var client=Minecraft.getInstance();
        boolean markerLeft=b.left();
        // Player Animator mirrors native arm names, but custom markers need an explicit swap.
        if(RookVfx.active(p.getUUID())&&p.getMainArm()==net.minecraft.world.entity.HumanoidArm.LEFT)markerLeft=!markerLeft;
        var marker=PlayerAnimationAccess.getPlayerAnimLayer(p).get3DTransform(markerLeft?"leftSkillTrail":"rightSkillTrail",TransformType.POSITION,client.getTimer().getGameTimeDeltaPartialTick(false),Vec3f.ZERO);
        float center=(p.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM?.5f:1)*(b.left()?1:-1);
        stack.pushPose();part.translateAndRotate(stack);
        if(RushVfx.active(p.getUUID())&&p==client.player&&client.options.getCameraType().isFirstPerson())stack.translate(0,.06,.05);
        NightBatTrail.hand(p,b.left(),stack,center,marker);stack.popPose();
    }
    static void clear(){PARTS.clear();}
    private SkillArmTrails(){ }
}
