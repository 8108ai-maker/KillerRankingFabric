package dev.killerranking.combat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Deadlines use overworld game time; changing daylight time cannot reset the skill. */
public record MadnessSurvival(long protectedUntil, long readyAt) {
    public static final int DURATION = 15 * 20, COOLDOWN = 300 * 20, AFTER_EFFECT_TICKS = 5 * 20;
    public static final float HALF_HEART = 1F;
    public static final MadnessSurvival READY = new MadnessSurvival(0, 0);
    public static final Codec<MadnessSurvival> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.LONG.optionalFieldOf("protected_until", 0L).forGetter(MadnessSurvival::protectedUntil),
        Codec.LONG.optionalFieldOf("ready_at", 0L).forGetter(MadnessSurvival::readyAt)
    ).apply(i, MadnessSurvival::new));
    public MadnessSurvival {
        readyAt = Math.max(0, readyAt);
        protectedUntil = Math.clamp(protectedUntil, 0, readyAt);
    }
    public boolean ready(long now) { return now >= readyAt; }
    public int protectedTicks(long now) { return (int) Math.clamp(protectedUntil - now, 0, DURATION); }
    public int cooldownTicks(long now) { return (int) Math.clamp(readyAt - now, 0, COOLDOWN); }
    public MadnessSurvival activate(long now) { return ready(now) ? new MadnessSurvival(now + DURATION, now + COOLDOWN) : this; }
    public MadnessSurvival cancelProtection() { return protectedUntil == 0 ? this : new MadnessSurvival(0, readyAt); }
    public static boolean atHalfHeart(float health) { return health > 0 && health <= HALF_HEART; }
    public static boolean crossesHalfHeart(float health, float damage) {
        return health > 0 && damage > 0 && health - damage <= HALF_HEART;
    }
    public static float damageToHalfHeart(float health) { return Math.max(0, health - HALF_HEART); }
}
