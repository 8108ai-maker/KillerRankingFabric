package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.item.ItemProperties;

/** The sword stays inside its hip-mounted saya until the authored drawing motion. */
public final class NeungsaKatanaVisuals {
    private NeungsaKatanaVisuals() { }

    public static void register() {
        NeungsaDrawController.register();
        ClientKatanaHolsters.register();
        ModelLoadingPlugin.register(context -> context.addModels(KillerRankingMod.id("item/neungsa_katana"),KillerRankingMod.id("item/neungsa_katana_iaido"),KillerRankingMod.id("item/neungsa_katana_saya"),
            KillerRankingMod.id("item/neungsa_katana_sheathed"),KillerRankingMod.id("item/pawn6_katana_saya"),
            KillerRankingMod.id("item/pawn6_katana_sheathed")));
        ItemProperties.register(KillerRankingMod.NEUNGSA_KATANA, KillerRankingMod.id("sheathed"),
            (stack, level, entity, seed) -> entity instanceof AbstractClientPlayer player
                && (stack == player.getOffhandItem() || stack == player.getMainHandItem() && sheathed(player, 0) > .5f) ? 1 : 0);
    }

    public static float sheathed(AbstractClientPlayer player, float delta) {
        // F may move the sword before the old attack animation/packet has ended.
        // Only the main-hand sword owns the authored draw channels.
        if (!player.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA)) return 1;
        return PlayerAnimationAccess.getPlayerAnimLayer(player)
            .get3DTransform("neungsaSheath", TransformType.POSITION, delta, new Vec3f(0, 1, 0)).getY();
    }
}
