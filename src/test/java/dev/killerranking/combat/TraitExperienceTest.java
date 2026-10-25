package dev.killerranking.combat;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.killerranking.Trait;
import dev.killerranking.data.PlayerProfile;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TraitExperienceTest {
    private PlayerProfile fresh(Trait trait) { return PlayerProfile.fresh().withTrait(trait); }
    @Test void thresholdsCarryOverflowAndStopAtThree() {
        var p = TraitExperience.add(fresh(Trait.KILLING_INTENT), 299, 300, 600);
        assertEquals(1, p.traitLevel());
        p = TraitExperience.add(p, 2, 300, 600);
        assertEquals(2, p.traitLevel()); assertEquals(1, p.traitXp());
        p = TraitExperience.add(p, 599, 300, 600);
        assertEquals(3, p.traitLevel()); assertEquals(0, p.traitXp());
        assertEquals(p, TraitExperience.add(p, Integer.MAX_VALUE, 300, 600));
    }
    @Test void twentyActiveTicksEarnOneAndIdleDoesNot() {
        var p = fresh(Trait.KILLING_INTENT);
        for (int i = 0; i < 19; i++) p = TraitExperience.activeTick(p, true, 300, 600);
        assertEquals(0, p.traitXp()); assertEquals(19, p.traitActiveTicks());
        assertEquals(p, TraitExperience.activeTick(p, false, 300, 600));
        p = TraitExperience.activeTick(p, true, 300, 600);
        assertEquals(1, p.traitXp()); assertEquals(0, p.traitActiveTicks());
        for (int i = 0; i < 40; i++) p = TraitExperience.activeTick(p, true, 300, 600);
        assertEquals(3, p.traitXp());
    }
    @Test void passivesNeverEarnIdleTimeExperience() {
        for (var trait : new Trait[]{Trait.POISON_AURA, Trait.FIGHTING_SPIRIT, Trait.UNASSIGNED}) {
            var p = fresh(trait);
            assertEquals(p, TraitExperience.activeTick(p, true, 300, 600));
        }
        assertFalse(TraitExperience.passive(Trait.MIXED_MADNESS));
        assertTrue(TraitExperience.passive(Trait.POISON_AURA));
    }
    @Test void oldSaveGetsZeroXpAndExistingLevelSurvives() {
        var p = PlayerProfile.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"points\":23,\"trait\":\"killing_intent\",\"zombieKills\":5,\"zombieMissionComplete\":true,\"traitLevel\":2}")).getOrThrow();
        assertEquals(2, p.traitLevel()); assertEquals(0, p.traitXp()); assertEquals(23, p.points());
    }
    @Test void saveReloadAndRewardsPreserveXpAndFraction() {
        var p = fresh(Trait.MIXED_MADNESS).withProgress(2, 123, 17).addPoints(80).recordZombieKill().completeZombieMission();
        var encoded = PlayerProfile.CODEC.encodeStart(JsonOps.INSTANCE, p).getOrThrow();
        var decoded = PlayerProfile.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(p, decoded); assertEquals(123, decoded.traitXp()); assertEquals(17, decoded.traitActiveTicks());
        assertEquals(123, p.withTrait(Trait.POISON_AURA).traitXp());
        assertEquals(0, p.withTrait(Trait.POISON_AURA).traitActiveTicks());
        assertEquals(0, p.withLevel(1).traitXp());
    }
    @Test void invalidSettingsAndLargeAwardsAreSafe() {
        assertEquals(3, TraitExperience.add(fresh(Trait.KILLING_INTENT), Integer.MAX_VALUE, 0, -1).traitLevel());
        assertEquals(0, fresh(Trait.KILLING_INTENT).withProgress(1, -30, -4).traitXp());
        assertEquals(0, TraitExperience.required(3, 300, 600));
    }
}
