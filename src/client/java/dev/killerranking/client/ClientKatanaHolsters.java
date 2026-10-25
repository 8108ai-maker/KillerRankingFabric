package dev.killerranking.client;

import dev.killerranking.network.KatanaHolstersPayload;
import dev.killerranking.visual.KatanaHolsters;
import dev.killerranking.visual.KatanaHolsterSelection;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientKatanaHolsters {
    private static final Map<UUID,KatanaHolstersPayload> SLOTS = new HashMap<>();
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(KatanaHolstersPayload.TYPE,(v,c) -> c.client().execute(() -> SLOTS.put(v.player(),v)));
        ClientPlayConnectionEvents.JOIN.register((h,s,c) -> SLOTS.clear());
        ClientPlayConnectionEvents.DISCONNECT.register((h,c) -> SLOTS.clear());
    }
    public static KatanaHolsterSelection.Slots<ItemStack> slots(AbstractClientPlayer player) {
        if (player == Minecraft.getInstance().player) return KatanaHolsters.inventory(player);
        var value = SLOTS.get(player.getUUID());
        if (value == null) return KatanaHolsterSelection.select(java.util.List.of(player.getMainHandItem(),player.getOffhandItem()),
            KatanaHolsters::katana,s -> BuiltInRegistries.ITEM.getKey(s.getItem()).toString());
        return new KatanaHolsterSelection.Slots<>(new ItemStack(BuiltInRegistries.ITEM.get(value.waist())),new ItemStack(BuiltInRegistries.ITEM.get(value.back())));
    }
    private ClientKatanaHolsters() { }
}
