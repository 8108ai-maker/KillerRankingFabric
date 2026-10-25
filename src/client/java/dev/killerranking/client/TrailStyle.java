package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Per-weapon look of the pixel swing ribbon. Each strip has eight rows (newest first);
 * {@link NightBatTrail} picks the row by age and this style decides texture, how long the
 * ribbon lingers, how wide it is drawn beyond the blade and how bright it starts.
 */
public enum TrailStyle {
    GENERIC("weapon_swing_strip", 1.0f, 0.0f, 0.9f),
    KATANA("trail_katana", 0.85f, 0.10f, 0.95f),
    SAW("trail_saw", 1.15f, 0.06f, 0.95f),
    DAGGER("trail_dagger", 0.7f, 0.0f, 0.9f),
    HATCHET("trail_hatchet", 1.2f, 0.12f, 0.95f),
    BAT("trail_bat", 1.3f, 0.16f, 0.9f),
    FIST("trail_fist", 0.8f, 0.0f, 0.8f),
    NEUNGSA("neungsa_swing_strip", 1.0f, 0.0f, 0.9f);

    public final ResourceLocation texture;
    /** Multiplies the base ribbon lifetime: heavy weapons leave longer dust. */
    public final float lifetime;
    /** Extra width past the blade tip, in blocks, so chunky weapons read wider. */
    public final float tipExtension;
    public final float alpha;

    TrailStyle(String file, float lifetime, float tipExtension, float alpha) {
        this.texture = KillerRankingMod.id("textures/vfx/" + file + ".png");
        this.lifetime = lifetime; this.tipExtension = tipExtension; this.alpha = alpha;
    }

    public static TrailStyle of(Item item) {
        if (item == null) return FIST;
        if (item == KillerRankingMod.NEUNGSA_KATANA) return NEUNGSA;
        if (item == KillerRankingMod.PAWN6_KATANA) return KATANA;
        if (item == KillerRankingMod.YUHYEOLMOKI_SAW_KNIFE) return SAW;
        if (item == KillerRankingMod.SALMOSA_MID_DAGGER || item == KillerRankingMod.GOSSI_INHERITED_KNIFE || item == KillerRankingMod.BISHOP_DAGGER) return DAGGER;
        if (item == KillerRankingMod.EUNBAL_HATCHET) return HATCHET;
        if (item == KillerRankingMod.NIGHT_BARBED_BAT) return BAT;
        return GENERIC;
    }
}
