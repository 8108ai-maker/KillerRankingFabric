package dev.killerranking.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.killerranking.KillerRankingMod;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

/** The palm owns the hilt pivot, including crossfades and first-person rendering. */
public final class NeungsaHeldBlade {
    // An untracked item channel is still holding the katana. Fading toward
    // zero Euler angles would rotate it independently of the rendered fist.
    private static final Vec3f RIGHT_GRIP = new Vec3f(-1.240704479f, -0.295051218f, 2.249155638f);
    private static final Vec3f LEFT_GRIP = new Vec3f(-1.240704479f, 0.295051218f, -2.249155638f);
    public static boolean hidden(AbstractClientPlayer player) {
        var id=player.getUUID();
        return RushVfx.active(id)||HeavyPunchVfx.active(id)||RookVfx.active(id)
            ||WasabariVfx.active(id)||ShoulderStabVfx.casting(id);
    }
    public static void attach(ArmedModel model, AbstractClientPlayer player, HumanoidArm arm,
                              PoseStack pose, float delta) {
        model.translateToHand(arm, pose);
        pose.translate(arm == HumanoidArm.LEFT ? 1.0 / 16 : -1.0 / 16, 10.0 / 16, 0);
        var layer = PlayerAnimationAccess.getPlayerAnimLayer(player);
        Vec3f wrist = layer.get3DTransform(arm == HumanoidArm.LEFT ? "leftItem" : "rightItem",
            TransformType.ROTATION, delta, arm == HumanoidArm.LEFT ? LEFT_GRIP : RIGHT_GRIP);
        pose.mulPose(new Quaternionf().rotationZYX(wrist.getZ(), wrist.getY(), wrist.getX()));
        // Mirroring a proper rotation reverses the blade's local +X edge.
        if (arm == HumanoidArm.LEFT) pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.scale(.9f, .9f, .9f);
    }

    public static void render(ArmedModel model, AbstractClientPlayer player, ItemStack stack,
                              HumanoidArm arm, PoseStack pose, MultiBufferSource buffers, int light) {
        var minecraft = Minecraft.getInstance();
        float delta = minecraft.getTimer().getGameTimeDeltaPartialTick(false);
        pose.pushPose();
        attach(model, player, arm, pose, delta);
        var display = arm == HumanoidArm.LEFT ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        var baked = minecraft.getItemRenderer().getModel(stack, player.level(), player, player.getId());
        if(NeungsaIaidoVfx.active(player.getUUID())&&!NeungsaIaidoVfx.full(player.getUUID())&&NeungsaKatanaVisuals.sheathed(player,delta)<.5f)
            baked=((net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager)minecraft.getModelManager()).getModel(KillerRankingMod.id("item/neungsa_katana_iaido"));
        NightBatTrail.begin(player, stack, display, arm == HumanoidArm.LEFT);
        try {
            // NONE has identity display transform. ItemRenderer centers (8,8,8),
            // exactly the grip pivot; no independently blended item position.
            minecraft.getItemRenderer().render(stack, ItemDisplayContext.NONE, false, pose, buffers,
                light, OverlayTexture.NO_OVERLAY, baked);
        } finally {
            NightBatTrail.end();
            pose.popPose();
        }
    }

    private NeungsaHeldBlade() { }
}
