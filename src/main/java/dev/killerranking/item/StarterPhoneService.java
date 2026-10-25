package dev.killerranking.item;

import com.mojang.serialization.Codec;
import dev.killerranking.KillerRankingMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** One starter phone per player, with delivery deferred until an inventory slot is free. */
public final class StarterPhoneService {
    private static final AttachmentType<Boolean> RECEIVED = AttachmentRegistry.create(KillerRankingMod.id("starter_phone_received"),
        b -> b.initializer(() -> false).persistent(Codec.BOOL).copyOnDeath());

    public static void initialize() { }

    public static void onJoin(ServerPlayer player) {
        if (!deliver(player)) player.sendSystemMessage(Component.literal("§e휴대폰 지급 대기: 인벤토리에 빈칸을 만들면 자동 지급됩니다."));
    }

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        for (var player : server.getPlayerList().getPlayers()) deliver(player);
    }

    private static boolean deliver(ServerPlayer player) {
        if (player.getAttachedOrCreate(RECEIVED)) return true;
        // Existing players who already own a phone do not need another starter item.
        if (containsPhone(player.getInventory()) || containsPhone(player.getEnderChestInventory())) {
            player.setAttached(RECEIVED, true);
            return true;
        }
        if (!player.isAlive() || player.isSpectator()) return true; // Retry when playable.
        if (player.getInventory().getFreeSlot() < 0) return false;
        var phone = new ItemStack(KillerRankingMod.PHONE);
        if (!player.getInventory().add(phone) || !phone.isEmpty()) return false;
        player.setAttached(RECEIVED, true);
        player.inventoryMenu.broadcastChanges();
        player.sendSystemMessage(Component.literal("§b킬러랭킹 휴대폰이 지급되었습니다! 손에 들고 우클릭해서 사용하세요."));
        return true;
    }

    private static boolean containsPhone(Container inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(KillerRankingMod.PHONE)) return true;
        }
        return false;
    }

    private StarterPhoneService() { }
}
