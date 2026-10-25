package dev.killerranking.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.visual.FirstPersonWeaponPose;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

/** Inventory swords occupy distinct body-mounted slots; drawing leaves an empty saya. */
public final class NeungsaSayaLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public NeungsaSayaLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) { super(parent); }

    @Override public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                                float swing, float amount, float delta, float age, float yaw, float pitch) {
        if (player.isInvisible() || player.isSpectator() || NeungsaHeldBlade.hidden(player)) return;
        var slots = ClientKatanaHolsters.slots(player);
        slot(pose,buffers,light,player,slots.waist(),false,delta);
        slot(pose,buffers,light,player,slots.back(),true,delta);
    }

    private void slot(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                      ItemStack sword, boolean back, float delta) {
        if (sword == null || sword.isEmpty()) return;
        boolean neungsa = sword.is(KillerRankingMod.NEUNGSA_KATANA);
        boolean pawn = sword.is(KillerRankingMod.PAWN6_KATANA);
        boolean held = player.getMainHandItem().is(sword.getItem()) || !neungsa && player.getOffhandItem().is(sword.getItem());
        if (FirstPersonWeaponView.active() && (back || !neungsa || !held)) return;
        boolean left = player.getMainArm() == HumanoidArm.LEFT;
        pose.pushPose();
        if (neungsa && player.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA) && !back && (NeungsaIaidoVfx.full(player.getUUID())||NeungsaEchoVfx.active(player.getUUID()))) {
            // The actual opposite palm owns the saya, just as the sword palm
            // owns the blade. Both shoulder pivots stay on the native body.
            var opposite=left?HumanoidArm.RIGHT:HumanoidArm.LEFT;
            getParentModel().translateToHand(opposite,pose);
            pose.translate(left?-1.0/16:1.0/16,10.0/16,0);
            var layer=PlayerAnimationAccess.getPlayerAnimLayer(player);
            var wrist=layer.get3DTransform(left?"rightItem":"leftItem",TransformType.ROTATION,delta,Vec3f.ZERO);
            float grab=layer.get3DTransform("neungsaSaya",TransformType.POSITION,delta,Vec3f.ZERO).getY();
            pose.mulPose(new Quaternionf().rotationZYX(wrist.getZ(),wrist.getY(),wrist.getX()));
            pose.translate(0,-grab/16,0);
            pose.scale(.9f,.9f,.9f);
            if(left)pose.mulPose(Axis.YP.rotationDegrees(180));
        } else if (neungsa && player.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA) && !back && NeungsaKatanaVisuals.sheathed(player,delta) > .999f) {
            NeungsaHeldBlade.attach(getParentModel(),player,player.getMainArm(),pose,delta);
        } else {
            if (!FirstPersonWeaponView.active()) getParentModel().body.translateAndRotate(pose);
            if (back) {
                pose.translate((left ? -2 : 2) / 16f,-3 / 16f,3.8f / 16);
                pose.mulPose(new Quaternionf().rotationZYX(left ? .40f : -.40f,0,.07f));
            } else {
                boolean charged = NeungsaIaidoVfx.phase(player.getUUID())==dev.killerranking.combat.NeungsaIaidoRules.CHARGING||NeungsaIaidoVfx.full(player.getUUID());
                if(FirstPersonWeaponView.active()&&charged)pose.translate((left?4:-4)/16f,-.5f/16,-10/16f);
                else {
                    float viewY = FirstPersonWeaponView.active() ? FirstPersonWeaponPose.NEUNGSA_VIEW_DOWN : 0;
                    float viewZ = FirstPersonWeaponView.active() ? -FirstPersonWeaponPose.NEUNGSA_VIEW_FORWARD : 0;
                    pose.translate((left ? -4.7f : 4.7f) / 16,(10 + viewY) / 16,(-4.4f + viewZ) / 16);
                }
                pose.mulPose(new Quaternionf().rotationZYX(left ? .1650423f : -.1650423f,left ? -.2467399f : .2467399f,1.3809345f));
            }
            pose.scale(.9f,.9f,.9f);
            if (left) pose.mulPose(Axis.YP.rotationDegrees(180));
        }
        var minecraft = Minecraft.getInstance();
        var manager = (FabricBakedModelManager)minecraft.getModelManager();
        var saya = manager.getModel(KillerRankingMod.id("item/" + (neungsa ? "neungsa" : "pawn6") + "_katana_saya"));
        minecraft.getItemRenderer().render(sword,ItemDisplayContext.NONE,false,pose,buffers,light,OverlayTexture.NO_OVERLAY,saya);
        if (!held) {
            var hilt = neungsa || pawn ? manager.getModel(KillerRankingMod.id("item/" + (neungsa ? "neungsa" : "pawn6") + "_katana_sheathed"))
                : minecraft.getItemRenderer().getModel(sword,player.level(),null,player.getId());
            minecraft.getItemRenderer().render(sword,ItemDisplayContext.NONE,false,pose,buffers,light,OverlayTexture.NO_OVERLAY,hilt);
        }
        pose.popPose();
    }
}
