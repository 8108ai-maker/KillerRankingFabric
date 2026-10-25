package dev.killerranking.combat;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.KillerRankingSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

/**
 * Groups the mod's weapons by how they cut, so impact sound and pixel VFX share one table.
 * Ordinal is sent over the network; append new families at the end.
 */
public enum WeaponFamily {
    BLADE(KillerRankingSounds.HIT_BLADE, 0.85f, 1.0f),
    SAW(KillerRankingSounds.HIT_SAW, 0.9f, 1.0f),
    BLUNT(KillerRankingSounds.HIT_BLUNT, 1.0f, 1.0f),
    FIST(KillerRankingSounds.HIT_FIST, 0.9f, 1.0f),
    HATCHET(KillerRankingSounds.HIT_HATCHET, 0.95f, 1.0f),
    PIERCE(KillerRankingSounds.HIT_PIERCE, 0.8f, 1.05f),
    KNUCKLE(KillerRankingSounds.HIT_FIST, 1.0f, 0.85f);

    public final SoundEvent hit;
    public final float volume, pitch;

    WeaponFamily(SoundEvent hit, float volume, float pitch) { this.hit = hit; this.volume = volume; this.pitch = pitch; }

    public static WeaponFamily of(ItemStack stack) {
        if (stack.is(KillerRankingMod.NEUNGSA_KATANA) || stack.is(KillerRankingMod.PAWN6_KATANA)) return BLADE;
        if (stack.is(KillerRankingMod.YUHYEOLMOKI_SAW_KNIFE)) return SAW;
        if (stack.is(KillerRankingMod.NIGHT_BARBED_BAT)) return BLUNT;
        if (stack.is(KillerRankingMod.TTEOKBAL_FIST)) return FIST;
        if (stack.is(KillerRankingMod.ROOK_KNUCKLE_GLOVES)) return KNUCKLE;
        if (stack.is(KillerRankingMod.EUNBAL_HATCHET)) return HATCHET;
        if (stack.is(KillerRankingMod.SALMOSA_MID_DAGGER) || stack.is(KillerRankingMod.GOSSI_INHERITED_KNIFE) || stack.is(KillerRankingMod.BISHOP_DAGGER)) return PIERCE;
        return null;
    }

    public static WeaponFamily byOrdinal(int index) {
        var all = values();
        return index < 0 || index >= all.length ? null : all[index];
    }
}
