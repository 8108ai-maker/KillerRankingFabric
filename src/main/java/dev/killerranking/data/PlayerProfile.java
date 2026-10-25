package dev.killerranking.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.killerranking.Trait;
import dev.killerranking.combat.TraitLevels;

public record PlayerProfile(int points, Trait trait, int zombieKills, boolean zombieMissionComplete, int traitLevel, int traitXp, int traitActiveTicks, int pointFraction) {
	public PlayerProfile {
        points = Math.max(0, points);
        pointFraction = points == Integer.MAX_VALUE ? 0 : Math.clamp(pointFraction, 0, (int) KillerPoints.UNITS_PER_POINT - 1);
        traitLevel = TraitLevels.clamp(traitLevel); traitXp = traitLevel == 3 ? 0 : Math.max(0, traitXp); traitActiveTicks = traitLevel == 3 ? 0 : Math.clamp(traitActiveTicks, 0, 19);
    }
    public PlayerProfile(int points, Trait trait, int kills, boolean complete, int level, int xp, int ticks) { this(points, trait, kills, complete, level, xp, ticks, 0); }
	public PlayerProfile(int points, Trait trait, int zombieKills, boolean complete) { this(points, trait, zombieKills, complete, 1, 0, 0); }
	public static final Codec<PlayerProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.fieldOf("points").forGetter(PlayerProfile::points),
		Codec.STRING.fieldOf("trait").forGetter(profile -> profile.trait().id()),
		Codec.INT.fieldOf("zombieKills").forGetter(PlayerProfile::zombieKills),
		Codec.BOOL.fieldOf("zombieMissionComplete").forGetter(PlayerProfile::zombieMissionComplete),
		Codec.INT.optionalFieldOf("traitLevel", 1).forGetter(PlayerProfile::traitLevel),
        Codec.INT.optionalFieldOf("traitXp", 0).forGetter(PlayerProfile::traitXp),
        Codec.INT.optionalFieldOf("traitActiveTicks", 0).forGetter(PlayerProfile::traitActiveTicks),
        Codec.INT.optionalFieldOf("pointFraction", 0).forGetter(PlayerProfile::pointFraction)
	).apply(instance, (points, trait, zombieKills, complete, level, xp, ticks, fraction) -> new PlayerProfile(points, Trait.fromId(trait), zombieKills, complete, level, xp, ticks, fraction)));

	public static PlayerProfile fresh() { return new PlayerProfile(0, Trait.UNASSIGNED, 0, false); }
	public PlayerProfile withTrait(Trait value) { return new PlayerProfile(points, value, zombieKills, zombieMissionComplete, traitLevel, traitXp, value == trait ? traitActiveTicks : 0, pointFraction); }
	public PlayerProfile withLevel(int level) { return new PlayerProfile(points, trait, zombieKills, zombieMissionComplete, level, 0, 0, pointFraction); }
    /** Whole legacy points plus the saved sub-point remainder. Never use floating-point addition. */
    public long pointUnits() { return KillerPoints.whole(points) + pointFraction; }
	public PlayerProfile addPoints(int amount) { return addPointUnits(KillerPoints.whole(amount)); }
    public PlayerProfile addPointUnits(long amount) {
        long balance = KillerPoints.add(pointUnits(), amount);
        return new PlayerProfile((int)(balance / KillerPoints.UNITS_PER_POINT), trait, zombieKills, zombieMissionComplete, traitLevel, traitXp, traitActiveTicks, (int)(balance % KillerPoints.UNITS_PER_POINT));
    }
	public PlayerProfile recordZombieKill() { return new PlayerProfile(points, trait, zombieKills + 1, zombieMissionComplete, traitLevel, traitXp, traitActiveTicks, pointFraction); }
    // Kept for legacy callers; the current game uses RotatingMissions instead.
	public PlayerProfile completeZombieMission() {
        var paid = addPoints(100);
        return new PlayerProfile(paid.points, trait, zombieKills, true, traitLevel, traitXp, traitActiveTicks, paid.pointFraction);
    }
    public PlayerProfile withProgress(int level, int xp, int ticks) { return new PlayerProfile(points, trait, zombieKills, zombieMissionComplete, level, xp, ticks, pointFraction); }
}
