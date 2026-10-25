package dev.killerranking.combat;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.Trait;
import dev.killerranking.data.PlayerProfiles;
import dev.killerranking.data.TraitActivation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Shared cadence for the inherited knife/hatchet pair, in either hand order. */
public final class InheritedWeaponEffects {
    public static final ResourceLocation SPEED_ID = ResourceLocation.fromNamespaceAndPath("killerranking", "madness_inherited_weapons");
    public static final double PAIRED_WEAPON_SPEED = -2.2; // Player base 4 + weapon modifier = 1.8.
    public static final double PAIRED_SPEED_MULTIPLIER = 0.8;
    public static final double DAMAGE_MULTIPLIER = 1.3;
    public static final int SPEED_BONUS = 20;
    private static final ThreadLocal<Player> SWAPPED_PAIR = new ThreadLocal<>();

    public static boolean paired(Player player) {
        return SWAPPED_PAIR.get() == player || paired(player.getMainHandItem(), player.getOffhandItem());
    }

    public static boolean paired(ItemStack main, ItemStack off) {
        return main.is(KillerRankingMod.GOSSI_INHERITED_KNIFE) && off.is(KillerRankingMod.EUNBAL_HATCHET)
            || main.is(KillerRankingMod.EUNBAL_HATCHET) && off.is(KillerRankingMod.GOSSI_INHERITED_KNIFE);
    }

    /** Better Combat temporarily puts the off-hand stack in both inventory slots. */
    public static Runnable preservePair(Player player, Runnable action) {
        if (!paired(player)) return action;
        return () -> {
            Player previous = SWAPPED_PAIR.get();
            SWAPPED_PAIR.set(player);
            try { action.run(); }
            finally {
                if (previous == null) SWAPPED_PAIR.remove(); else SWAPPED_PAIR.set(previous);
            }
        };
    }

    public static boolean madnessBonus(ServerPlayer player) {
        return player.isAlive() && !player.isSpectator() && TraitActivation.enabled(player)
            && PlayerProfiles.get(player).trait() == Trait.MIXED_MADNESS && paired(player);
    }

    public static void refresh(ServerPlayer player) {
        applySpeedBonus(player.getAttribute(Attributes.ATTACK_SPEED), madnessBonus(player));
    }

    public static void applySpeedBonus(AttributeInstance attribute, boolean enabled) {
        if (attribute == null) return;
        var previous = attribute.getModifier(SPEED_ID);
        if (!enabled) {
            if (previous != null) attribute.removeModifier(SPEED_ID);
        } else if (previous == null || previous.amount() != SPEED_BONUS / 100.0) {
            attribute.addOrUpdateTransientModifier(new AttributeModifier(SPEED_ID, SPEED_BONUS / 100.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    /** Average the pair, retain existing modifiers, then reduce its shared cadence by 20%. */
    public static double pairedSpeed(AttributeInstance attribute) {
        double base = attribute.getBaseValue() + PAIRED_WEAPON_SPEED;
        for (var modifier : attribute.getModifiers()) {
            if (!modifier.id().equals(Item.BASE_ATTACK_SPEED_ID)
                && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) base += modifier.amount();
        }
        double result = base;
        for (var modifier : attribute.getModifiers()) {
            if (!modifier.id().equals(Item.BASE_ATTACK_SPEED_ID)
                && modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) result += base * modifier.amount();
        }
        for (var modifier : attribute.getModifiers()) {
            if (!modifier.id().equals(Item.BASE_ATTACK_SPEED_ID)
                && modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) result *= 1 + modifier.amount();
        }
        return attribute.getAttribute().value().sanitizeValue(result * PAIRED_SPEED_MULTIPLIER);
    }

    private InheritedWeaponEffects() { }
}
