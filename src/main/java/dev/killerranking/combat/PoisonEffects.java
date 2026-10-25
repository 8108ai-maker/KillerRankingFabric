package dev.killerranking.combat;

import dev.killerranking.Trait;
import dev.killerranking.data.PlayerProfiles;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class PoisonEffects {
    public static final ResourceLocation RESISTANCE = ResourceLocation.fromNamespaceAndPath("killerranking", "poison_knockback_resistance");
    public static int resistance(int level) { return TraitLevels.clamp(level) * 20; }
    public static final ResourceLocation SPEED = ResourceLocation.fromNamespaceAndPath("killerranking", "poison_speed");
    public static int speed(int level) { return TraitLevels.bonusPercent(15, level); }
    public static void applyPercent(AttributeInstance attribute, ResourceLocation id, int percent) {
        if (attribute == null) return;
        if (percent == 0) { attribute.removeModifier(id); return; }
        double amount = percent / 100.0;
        var previous = attribute.getModifier(id);
        if (previous == null || previous.amount() != amount) attribute.addOrUpdateTransientModifier(
            new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    public static void apply(AttributeInstance attribute, int percent) {
        if (attribute == null) return;
        if (percent <= 0) { attribute.removeModifier(RESISTANCE); return; }
        double amount = Math.clamp(percent / 100.0, 0, 1);
        var previous = attribute.getModifier(RESISTANCE);
        if (previous == null || previous.amount() != amount) attribute.addOrUpdateTransientModifier(
            new AttributeModifier(RESISTANCE, amount, AttributeModifier.Operation.ADD_VALUE));
    }
    public static void refresh(ServerPlayer player) {
        var p = PlayerProfiles.get(player);
        boolean enabled = player.isAlive() && !player.isSpectator() && dev.killerranking.data.TraitActivation.enabled(player) && p.trait() == Trait.POISON_AURA;
        apply(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), enabled ? resistance(p.traitLevel()) : 0);
        applyPercent(player.getAttribute(Attributes.ATTACK_SPEED), SPEED, enabled ? speed(p.traitLevel()) : 0);
        applyPercent(player.getAttribute(Attributes.MOVEMENT_SPEED), SPEED, enabled ? speed(p.traitLevel()) : 0);
    }
    private PoisonEffects() { }
}
