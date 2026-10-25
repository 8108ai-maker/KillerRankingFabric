package dev.killerranking.combat;

import dev.killerranking.Trait;
import dev.killerranking.data.PlayerProfile;

/** Pure progression rules; XP is separate from vanilla experience and points. */
public final class TraitExperience {
    public static boolean passive(Trait trait) { return trait == Trait.FIGHTING_SPIRIT || trait == Trait.POISON_AURA; }
    public static int required(int level, int second, int third) { return level >= 3 ? 0 : Math.max(1, level == 1 ? second : third); }
    public static PlayerProfile add(PlayerProfile p, int amount, int second, int third) {
        if (amount <= 0 || p.traitLevel() >= 3 || p.trait() == Trait.UNASSIGNED) return p;
        long xp = (long) p.traitXp() + amount;
        int level = p.traitLevel();
        while (level < 3 && xp >= required(level, second, third)) xp -= required(level++, second, third);
        return p.withProgress(level, level == 3 ? 0 : (int) xp, p.traitActiveTicks());
    }
    public static PlayerProfile activeTick(PlayerProfile p, boolean active, int second, int third) {
        if (!active || passive(p.trait()) || p.trait() == Trait.UNASSIGNED || p.traitLevel() >= 3) return p;
        int ticks = p.traitActiveTicks() + 1;
        return ticks == 20 ? add(p.withProgress(p.traitLevel(), p.traitXp(), 0), 1, second, third)
            : p.withProgress(p.traitLevel(), p.traitXp(), ticks);
    }
    private TraitExperience() { }
}
