package dev.killerranking.visual;

/** Native item pixels shared by the dagger, its cutting ribbon and the Wasabari grip. */
public final class SalmosaKnifeGeometry {
    public static final float TIP=22.6f;
    public static final float PALM=5f;
    public static final float BLADE_BASE=7.3f;
    public static final float SKILL_SCALE=.6f;
    public static final double SKILL_REACH=(TIP-PALM)*(double)SKILL_SCALE;
    private SalmosaKnifeGeometry() { }
}
