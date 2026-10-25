package dev.killerranking.combat;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.killerranking.Trait;
import dev.killerranking.config.KillerRankingConfig;
import dev.killerranking.data.PlayerProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class TraitLevelsTest {
	@ParameterizedTest @CsvSource({"1,5,25,60,7", "2,10,50,120,9", "3,15,75,180,11"})
	void levelsScaleBonusesLinearly(int level, int attack, int madness, int poisonTicks, int window) {
		assertEquals(attack, TraitLevels.bonusPercent(5, level));
		assertEquals(madness, TraitLevels.bonusPercent(new KillerRankingConfig().mixedMadnessBasePercent, level));
		assertEquals(poisonTicks, TraitLevels.poisonTicks(level));
		assertEquals(window, TraitLevels.parryWindow(level));
	}
	@ParameterizedTest @CsvSource({"1,5,10,18", "2,10,20,36", "3,15,30,54"})
	void spiritRetainsHealthThresholds(int level, int high, int half, int low) {
		var config = new KillerRankingConfig();
		assertEquals(0, CombatRules.spiritPercent(0.751F, level, config));
		assertEquals(high, CombatRules.spiritPercent(0.75F, level, config));
		assertEquals(half, CombatRules.spiritPercent(0.50F, level, config));
		assertEquals(low, CombatRules.spiritPercent(0.25F, level, config));
		assertEquals(low, CombatRules.maxSpiritPercent(level, config));
	}
	@Test void oldProfilesUpgradeWithoutLosingProgress() {
		var old = JsonParser.parseString("{\"points\":430,\"trait\":\"fighting_spirit\",\"zombieKills\":19,\"zombieMissionComplete\":false}");
		var profile = PlayerProfile.CODEC.parse(JsonOps.INSTANCE, old).getOrThrow();
		assertEquals(1, profile.traitLevel()); assertEquals(430, profile.points());
		assertEquals(Trait.FIGHTING_SPIRIT, profile.trait()); assertEquals(19, profile.zombieKills());
	}
	@Test void levelSurvivesRewardsAndSaveReload() {
		var expected = new PlayerProfile(430, Trait.KILLING_INTENT, 29, false).withLevel(3)
			.recordZombieKill().completeZombieMission().addPoints(300);
		var json = PlayerProfile.CODEC.encodeStart(JsonOps.INSTANCE, expected).getOrThrow();
		var actual = PlayerProfile.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
		assertEquals(expected, actual); assertEquals(3, actual.traitLevel()); assertEquals(830, actual.points());
	}
	@Test void invalidLevelsClampAndCustomDefenseCannotGiveImmunity() {
		assertEquals(1, PlayerProfile.fresh().withLevel(-3).traitLevel());
		assertEquals(3, PlayerProfile.fresh().withLevel(99).traitLevel());
		assertEquals(90, TraitLevels.defensePercent(100, 3));
		assertEquals(0, TraitLevels.bonusPercent(-2, 3));
	}
}
