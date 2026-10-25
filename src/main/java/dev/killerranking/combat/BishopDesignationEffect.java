package dev.killerranking.combat;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** The effect is visible on the victim; caster ownership lives in BishopDesignations. */
public final class BishopDesignationEffect extends MobEffect {
    public BishopDesignationEffect() { super(MobEffectCategory.HARMFUL, 0xD55C96); }
}
