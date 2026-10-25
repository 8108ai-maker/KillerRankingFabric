package dev.killerranking.combat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CombatRulesTest {
	@ParameterizedTest
	@CsvSource({"1.0,0", "0.751,0", "0.75,0.05", "0.501,0.05", "0.5,0.10", "0.251,0.10", "0.25,0.18", "0.01,0.18"})
	void spiritUsesOriginalDamageThresholds(float health, float reduction) {
		assertEquals(reduction, CombatRules.spiritReduction(health), 0.00001F);
	}
	@ParameterizedTest
	@CsvSource({"1.0,0,false", "0.65,0.5,false", "0.5,0.7142857,false", "0.301,0.9985714,false", "0.3,1,false", "0.299,1,true", "0.05,1,true"})
	void madnessGaugeAndExistingSpeedThresholdStayDistinct(float health, float gauge, boolean active) {
		assertEquals(gauge, CombatRules.madnessProgress(health), 0.00001F);
		assertEquals(active, CombatRules.madnessActive(health));
	}
    @ParameterizedTest @CsvSource({"1,0","0.99,1.0714286","0.9,10.7142857","0.65,37.5","0.5,53.5714286","0.3,75","0.299,75","0.025,75","0,75"})
    void levelThreeDamageScalesContinuouslyWithMissingHealth(float health,double percent) {
        assertEquals(percent,CombatRules.madnessDamagePercent(health,25,3),.00002);
    }
    @Test void lowerLevelsCapAtTwentyFiveAndFiftyAndScaleBeforeTheCap() {
        for(int level=1;level<=3;level++) {
            assertEquals(level*25,CombatRules.madnessDamagePercent(.30f,25,level),1e-5);
            assertEquals(level*12.5,CombatRules.madnessDamagePercent(.65f,25,level),1e-5);
            assertEquals(0,CombatRules.madnessDamagePercent(1,25,level),1e-5);
        }
        assertEquals(37.5,CombatRules.madnessDamagePercent(CombatRules.healthRatio(26,40),25,3),1e-5);
        assertEquals(75,CombatRules.madnessDamagePercent(CombatRules.healthRatio(12,40),25,3),1e-5);
        assertEquals(0,CombatRules.madnessDamagePercent(CombatRules.healthRatio(40,40),25,3),1e-5);
    }
	@Test void cooldownFillsTowardReadyAndHandlesZeroCooldown() {
		assertEquals(0, CombatRules.cooldownProgress(20, 20));
		assertEquals(0.5F, CombatRules.cooldownProgress(10, 20));
		assertEquals(1, CombatRules.cooldownProgress(0, 20));
		assertEquals(1, CombatRules.cooldownProgress(0, 0));
	}
	@Test void timersRefreshExpireAndClearPerPlayer() {
		TickTimers timers = new TickTimers();
		UUID player = UUID.randomUUID(), other = UUID.randomUUID();
		timers.set(player, 60);
		timers.set(other, 5);
		for (int i = 0; i < 59; i++) timers.tick();
		assertEquals(1, timers.get(player));
		assertEquals(0, timers.get(other));
		timers.set(player, 60);
		assertEquals(60, timers.get(player));
		for (int i = 0; i < 60; i++) timers.tick();
		assertEquals(0, timers.get(player));
		timers.set(player, 20); timers.remove(player);
		assertEquals(0, timers.get(player));
		timers.set(other, 20); timers.clear();
		assertEquals(0, timers.get(other));
		timers.set(player, -1);
		assertEquals(0, timers.get(player));
	}
	@Test void maxHealthChangesDoNotBreakThresholds() {
		assertFalse(CombatRules.madnessActive(CombatRules.healthRatio(12, 40)));
		assertTrue(CombatRules.madnessActive(CombatRules.healthRatio(11.99F, 40)));
		assertEquals(0.5F, CombatRules.healthRatio(20, 40));
		assertEquals(1, CombatRules.healthRatio(20, 0));
		assertEquals(1, CombatRules.healthRatio(30, 20));
	}
    @Test void cooldownRefundSubtractsTenTicksWithoutChangingOtherPlayers() {
        var timers = new TickTimers(); var player = UUID.randomUUID(); var other = UUID.randomUUID();
        timers.set(player, 20); timers.set(other, 20);
        timers.tick(); timers.tick();
        timers.reduce(player, 10);
        assertEquals(8, timers.get(player)); assertEquals(18, timers.get(other));
        timers.tick(); assertEquals(7, timers.get(player));
    }
    @Test void cooldownRefundStopsAtZeroAndCannotCreateOrExtendCooldowns() {
        var timers = new TickTimers(); var player = UUID.randomUUID();
        timers.set(player, 6); timers.reduce(player, 10); assertEquals(0, timers.get(player));
        timers.reduce(player, 10); assertEquals(0, timers.get(player));
        timers.set(player, 10); timers.reduce(player, 10); assertEquals(0, timers.get(player));
        timers.set(player, 5); timers.reduce(player, -10); assertEquals(5, timers.get(player));
    }
}
