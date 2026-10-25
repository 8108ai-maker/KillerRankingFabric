package dev.killerranking.item;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.data.PlayerProfiles;
import dev.killerranking.network.OpenPhonePayload;
import dev.killerranking.network.ProfilePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class KillerPhoneItem extends Item {
	public KillerPhoneItem(Properties properties) { super(properties.stacksTo(1)); }

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			KillerRankingMod.assignAndSync(serverPlayer);
			ServerPlayNetworking.send(serverPlayer, ProfilePayload.from(PlayerProfiles.get(serverPlayer)));
			ServerPlayNetworking.send(serverPlayer, new OpenPhonePayload());
		}
		return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
	}
}
