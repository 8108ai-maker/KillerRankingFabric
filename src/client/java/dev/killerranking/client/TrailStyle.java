package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Per-weapon look of the pixel swing ribbon. Each strip has eight rows (newest first);
 * {@link NightBatTrail} picks the row by age and this style decides texture, how far the
 * ribbon reaches past the blade and toward the hand, its brightness and its shard colour.
 */
public enum TrailStyle {
    //                 texture               tipExt  innerPull alpha  shardRGB  shards/sample
    GENERIC("weapon_swing_strip",  0.70f, 0.25f, 1.0f, 0xFFF4DC, 2),
    KATANA ("trail_katana",        1.15f, 0.35f, 1.0f, 0xCFEBFF, 3),
    SAW    ("trail_saw",           0.85f, 0.30f, 1.0f, 0xFF4A3C, 3),
    DAGGER ("trail_dagger",        0.55f, 0.20f, 1.0f, 0xE6DCFF, 2),
    HATCHET("trail_hatchet",       0.80f, 0.35f, 1.0f, 0xFFB450, 3),
    BAT    ("trail_bat",           0.95f, 0.40f, 1.0f, 0xFFF6DE, 2),
    FIST   ("trail_fist",          0.45f, 0.15f, 0.95f, 0xB9F0B4, 2),
    NEUNGSA("neungsa_swing_strip", 0.60f, 0.25f, 1.0f, 0xC89CFF, 3);

    public final ResourceLocation texture;
    /** Extra reach past the blade tip, in blocks, so the sweep reads far larger than the model. */
    public final float tipExtension;
    /** How far the ribbon's inner edge is pulled back toward the hand, in blocks (widens the sweep). */
    public final float innerPull;
    public final float alpha;
    /** Colour of the pixel shards thrown off the blade and from hits. */
    public final int shardColor;
    /** Shards spawned per accepted trail sample while striking. */
    public final int shardsPerSample;

    TrailStyle(String file, float tipExtension, float innerPull, float alpha, int shardColor, int shardsPerSample) {
        this.texture = KillerRankingMod.id("textures/vfx/" + file + ".png");
        this.tipExtension = tipExtension; this.innerPull = innerPull; this.alpha = alpha;
        this.shardColor = shardColor; this.shardsPerSample = shardsPerSample;
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

    public static TrailStyle of(dev.killerranking.combat.WeaponFamily family) {
        return switch (family) {
            case BLADE -> KATANA;
            case SAW -> SAW;
            case BLUNT -> BAT;
            case FIST, KNUCKLE -> FIST;
            case HATCHET -> HATCHET;
            case PIERCE -> DAGGER;
        };
    }
}
