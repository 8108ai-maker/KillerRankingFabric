package dev.killerranking.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

/** A fast boxing hand wrap: six base damage, 2.8 attacks per second. */
public final class BandagedFistItem extends SwordItem {
    public BandagedFistItem() {
        super(Tiers.IRON, new Item.Properties()
            .attributes(SwordItem.createAttributes(Tiers.IRON, 3, -1.2F))
            .rarity(Rarity.UNCOMMON));
    }

    @Override public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return ingredient.is(Items.WHITE_WOOL) || ingredient.is(Items.STRING);
    }
}
