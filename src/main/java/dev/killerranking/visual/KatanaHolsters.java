package dev.killerranking.visual;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.network.KatanaHolstersPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class KatanaHolsters {
    public static final ResourceLocation EMPTY = ResourceLocation.withDefaultNamespace("air");
    public static final TagKey<Item> KATANAS = TagKey.create(Registries.ITEM,KillerRankingMod.id("katanas"));
    private static final Map<UUID,KatanaHolstersPayload> LAST = new HashMap<>();

    public static boolean katana(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(KATANAS) || BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().contains("katana"));
    }
    public static KatanaHolsterSelection.Slots<ItemStack> inventory(Player player) {
        var items = new ArrayList<ItemStack>(player.getInventory().items);
        items.addAll(player.getInventory().offhand);
        return KatanaHolsterSelection.select(items,KatanaHolsters::katana,s -> BuiltInRegistries.ITEM.getKey(s.getItem()).toString());
    }
    private static ResourceLocation id(ItemStack sword) {
        return sword == null ? EMPTY : BuiltInRegistries.ITEM.getKey(sword.getItem());
    }
    public static KatanaHolstersPayload snapshot(Player player) {
        var slots = inventory(player);
        return new KatanaHolstersPayload(player.getUUID(),id(slots.waist()),id(slots.back()));
    }
    private static void send(ServerPlayer viewer, KatanaHolstersPayload value) {
        if (ServerPlayNetworking.canSend(viewer,KatanaHolstersPayload.TYPE)) ServerPlayNetworking.send(viewer,value);
    }
    public static void register() {
        EntityTrackingEvents.START_TRACKING.register((entity,viewer) -> {
            if (entity instanceof ServerPlayer player) send(viewer,snapshot(player));
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server) -> LAST.remove(handler.player.getUUID()));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> LAST.clear());
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerList().getPlayers()) {
                var value = snapshot(player);
                if (value.equals(LAST.put(player.getUUID(),value))) continue;
                send(player,value);
                for (var viewer : PlayerLookup.tracking(player)) send(viewer,value);
            }
        });
    }
    private KatanaHolsters() { }
}
