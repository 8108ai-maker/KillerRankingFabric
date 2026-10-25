package dev.killerranking.combat;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.Trait;
import dev.killerranking.data.PlayerProfiles;
import dev.killerranking.data.SpiritState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class SpiritEffects {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("killerranking", "spirit_bonus");
    public static void apply(AttributeInstance attribute, int percent) {
        if (attribute == null) return;
        if (percent == 0) { attribute.removeModifier(ID); return; }
        double amount = percent / 100.0;
        var previous = attribute.getModifier(ID);
        if (previous == null || previous.amount() != amount) attribute.addOrUpdateTransientModifier(
            new AttributeModifier(ID, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    public static void refresh(ServerPlayer player) {
        var profile = PlayerProfiles.get(player);
        var state = SpiritState.get(player);
        var branch = profile.trait() == Trait.FIGHTING_SPIRIT && dev.killerranking.data.TraitActivation.enabled(player) && player.isAlive() && !player.isSpectator() ? state.branch() : SpiritBranch.NONE;
        apply(player.getAttribute(Attributes.ATTACK_DAMAGE), branch.attack(profile.traitLevel()));
        apply(player.getAttribute(Attributes.ATTACK_SPEED), branch.attackSpeed(profile.traitLevel()));
        apply(player.getAttribute(Attributes.MOVEMENT_SPEED), branch.movement(profile.traitLevel(), state.checkmate()));
    }
    public static int defense(ServerPlayer player) {
        var p = PlayerProfiles.get(player);
        if (!dev.killerranking.data.TraitActivation.enabled(player) || p.trait() != Trait.FIGHTING_SPIRIT || !player.isAlive() || player.isSpectator()) return 0;
        var branch = SpiritState.get(player).branch();
        return branch == SpiritBranch.NONE ? CombatRules.spiritPercent(CombatRules.healthRatio(player.getHealth(), player.getMaxHealth()), p.traitLevel(), KillerRankingMod.CONFIG) : branch.defense(p.traitLevel());
    }
    private SpiritEffects() { }
}
