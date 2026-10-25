package dev.killerranking.combat;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MadnessSurvivalTest {
    @Test void protectsForExactlyFifteenSecondsAndCooldownStartsAtActivation() {
        var state = MadnessSurvival.READY.activate(400);
        assertEquals(300, state.protectedTicks(400));
        assertEquals(6000, state.cooldownTicks(400));
        assertEquals(1, state.protectedTicks(699));
        assertEquals(0, state.protectedTicks(700));
        assertEquals(5700, state.cooldownTicks(700));
        assertFalse(state.ready(6399));
        assertTrue(state.ready(6400));
        assertEquals(300, state.activate(6400).protectedTicks(6400));
    }
    @Test void repeatedHitsAndToggleCannotExtendProtectionOrResetCooldown() {
        var state = MadnessSurvival.READY.activate(10);
        assertSame(state, state.activate(20));
        var off = state.cancelProtection();
        assertEquals(0, off.protectedTicks(20));
        assertEquals(state.readyAt(), off.readyAt());
        assertSame(off, off.activate(20));
        assertSame(off, off.cancelProtection());
    }
    @Test void savedCooldownAndProtectionRoundTripWithoutRefresh() {
        var state = MadnessSurvival.READY.activate(210);
        var json = MadnessSurvival.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow();
        var loaded = MadnessSurvival.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(state, loaded);
        assertEquals(250, loaded.protectedTicks(260));
        assertEquals(5950, loaded.cooldownTicks(260));
        assertEquals(5950, loaded.cancelProtection().cooldownTicks(260));
    }
    @Test void legacyAndInvalidStorageAreSafe() {
        assertEquals(MadnessSurvival.READY, MadnessSurvival.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow());
        assertEquals(MadnessSurvival.READY, new MadnessSurvival(-50, -60));
        assertEquals(50, new MadnessSurvival(1000, 50).protectedUntil());
    }
    @Test void halfHeartIsOneHealthPointAndRequiresLivingPlayer() {
        assertTrue(MadnessSurvival.atHalfHeart(1));
        assertTrue(MadnessSurvival.atHalfHeart(.5F));
        assertFalse(MadnessSurvival.atHalfHeart(1.001F));
        assertFalse(MadnessSurvival.atHalfHeart(0));
    }
    @Test void lethalCrossingsStopAtHalfHeartAndZeroDamageDoesNotTrigger() {
        assertTrue(MadnessSurvival.crossesHalfHeart(20, 19));
        assertTrue(MadnessSurvival.crossesHalfHeart(20, 100));
        assertFalse(MadnessSurvival.crossesHalfHeart(20, 18.99F));
        assertFalse(MadnessSurvival.crossesHalfHeart(1, 0));
        assertFalse(MadnessSurvival.crossesHalfHeart(0, 20));
        assertEquals(19, MadnessSurvival.damageToHalfHeart(20));
        assertEquals(0, MadnessSurvival.damageToHalfHeart(.5F));
    }
}
