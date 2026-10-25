package dev.killerranking.combat;

import dev.killerranking.mission.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MissionsTest {
    @BeforeAll static void bootstrap() { net.minecraft.SharedConstants.tryDetectVersion(); net.minecraft.server.Bootstrap.bootStrap(); }
    @Test void hundredUniqueMissionsReferToRealMinecraftStatistics() {
        assertEquals(100, MissionCatalog.ALL.size());
        assertEquals(100, MissionCatalog.ALL.stream().map(MissionCatalog.Definition::id).distinct().count());
        for (var d : MissionCatalog.ALL) {
            var registry = switch (d.category()) { case "kill" -> BuiltInRegistries.ENTITY_TYPE; case "mine" -> BuiltInRegistries.BLOCK; case "craft" -> BuiltInRegistries.ITEM; default -> BuiltInRegistries.CUSTOM_STAT; };
            assertTrue(registry.containsKey(ResourceLocation.parse(d.statId())), d.id());
        }
    }
    @Test void everyMissionResolvesAndReadsAnActualStat() {
        var counter = new net.minecraft.stats.StatsCounter();
        for (var d : MissionCatalog.ALL) {
            var stat = assertDoesNotThrow(d::stat, d.id());
            assertNotNull(stat.getName(), d.id());
            assertEquals(0, counter.getValue(stat), d.id());
        }
    }
    @Test void activityMissionsUseCanonicalRegistryValues() {
        var definition = MissionCatalog.find("activity_jump");
        var canonical = net.minecraft.stats.Stats.JUMP;
        assertNotSame(canonical, ResourceLocation.parse(definition.statId()));
        assertSame(net.minecraft.stats.Stats.CUSTOM.get(canonical), definition.stat());
        var counter = new net.minecraft.stats.StatsCounter();
        counter.setValue(null, net.minecraft.stats.Stats.CUSTOM.get(canonical), 17);
        assertEquals(17, counter.getValue(definition.stat()));
    }
    @Test void everyBatchContainsTenDistinctBalancedObjectives() {
        for (long seed = 0; seed < 200; seed++) {
            var choices = MissionCatalog.choose(seed);
            assertEquals(10, choices.size()); assertEquals(10, choices.stream().distinct().count());
            for (String category : List.of("kill", "mine", "craft")) assertEquals(3, choices.stream().filter(d -> d.category().equals(category)).count());
            assertEquals(1, choices.stream().filter(d -> d.category().equals("activity")).count());
        }
    }
    @Test void seedsProduceStableButDifferentBoards() {
        assertEquals(MissionCatalog.choose(87), MissionCatalog.choose(87));
        assertNotEquals(MissionCatalog.choose(87), MissionCatalog.choose(88));
    }
    @Test void rotationOccursAtThreeGameDaysIncludingSkippedDays() {
        var board = new MissionBoard(0, List.of(new MissionBoard.Entry("kill_zombie", 40, 0, false)));
        assertFalse(board.needsRotation(71999)); assertTrue(board.needsRotation(72000)); assertTrue(board.needsRotation(288000));
        assertEquals(1, board.remaining(71999));
        assertFalse(new MissionBoard(3, board.entries()).needsRotation(100));
    }
    @Test void oldStatisticsDoNotCountAndCompletionIsPaidOnlyOnce() {
        var entry = new MissionBoard.Entry("kill_zombie", 40, 0, false);
        var next = entry.advance(45, 20, true); assertEquals(5, next.progress()); assertFalse(next.paid());
        next = next.advance(65, 20, true); assertEquals(20, next.progress()); assertTrue(next.paid());
        assertTrue(next.advance(90, 20, true).paid()); assertEquals(20, next.advance(90, 20, true).progress());
    }
    @Test void resetsAndCreativeActivityCannotBackfillProgress() {
        var entry = new MissionBoard.Entry("kill_zombie", 40, 5, false);
        entry = entry.advance(2, 20, true); assertEquals(5, entry.progress());
        entry = entry.advance(10, 20, false); assertEquals(5, entry.progress());
        entry = entry.advance(11, 20, true); assertEquals(6, entry.progress());
    }
    @Test void savedProgressAndRewardFlagsSurviveRoundTrip() {
        var board = new MissionBoard(8, List.of(new MissionBoard.Entry("kill_zombie", 123, 20, true), new MissionBoard.Entry("craft_chest", 5, 4, false)));
        var encoded = MissionBoard.CODEC.encodeStart(JsonOps.INSTANCE, board).getOrThrow();
        assertEquals(board, MissionBoard.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        assertFalse(board.needsRotation(8 * 72000));
    }
}
