package dev.killerranking.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.killerranking.Trait;
import dev.killerranking.mission.MissionCatalog;
import dev.killerranking.network.*;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KillerPointsTest {
    @BeforeAll static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }
    @Test void missionRewardsFollowWonConversionAndNeverDisplayScientificNotation() {
        assertEquals(10_000, KillerPoints.missionReward(100));
        assertEquals("0.001", KillerPoints.format(KillerPoints.missionReward(100)));
        assertEquals("0.0000001", KillerPoints.format(1));
        assertEquals(4_000, MissionCatalog.ALL.stream().mapToLong(MissionCatalog.Definition::rewardUnits).min().orElseThrow());
        assertEquals(30_000, MissionCatalog.ALL.stream().mapToLong(MissionCatalog.Definition::rewardUnits).max().orElseThrow());
        assertEquals("1", KillerPoints.format(10_000_000));
    }
    @Test void tinyRewardsAccumulateExactlyIntoOnePoint() {
        var p = PlayerProfile.fresh();
        for (int i=0; i<10_000; i++) p=p.addPointUnits(KillerPoints.fromDecimal(.0001));
        assertEquals(1, p.points()); assertEquals(0, p.pointFraction());
        assertEquals(10_000_000L, p.pointUnits());
    }
    @Test void spendingWholePointsPreservesRemainderAndHandlesBoundaryCarry() {
        var p = PlayerProfile.fresh().addPoints(999).addPointUnits(9_999_999);
        assertTrue(p.pointUnits() < KillerPoints.whole(1000));
        p=p.addPointUnits(1);
        assertEquals(KillerPoints.whole(1000), p.pointUnits());
        p=p.addPointUnits(1000).addPoints(-1000);
        assertEquals("0.0001", KillerPoints.format(p.pointUnits()));
    }
    @Test void legacyIntegerBalanceIsNotRescaledOnLoad() {
        var legacy=JsonParser.parseString("{\"points\":430,\"trait\":\"fighting_spirit\",\"zombieKills\":19,\"zombieMissionComplete\":false}");
        var p=PlayerProfile.CODEC.parse(JsonOps.INSTANCE,legacy).getOrThrow();
        assertEquals(KillerPoints.whole(430), p.pointUnits());
        assertEquals("430.001", KillerPoints.format(p.addPointUnits(10_000).pointUnits()));
    }
    @Test void fractionSurvivesNbtJsonAndUnrelatedProfileUpdates() {
        var original=PlayerProfile.fresh().addPoints(123).addPointUnits(4567);
        var changed=original.withTrait(Trait.POISON_AURA).withLevel(2).withProgress(2,300,12).recordZombieKill();
        assertEquals(original.pointUnits(),changed.pointUnits());
        var nbt=PlayerProfile.CODEC.encodeStart(NbtOps.INSTANCE,changed).getOrThrow();
        assertEquals(changed,PlayerProfile.CODEC.parse(NbtOps.INSTANCE,nbt).getOrThrow());
        var json=PlayerProfile.CODEC.encodeStart(JsonOps.INSTANCE,changed).getOrThrow();
        assertEquals(changed,PlayerProfile.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow());
    }
    @Test void networkCarriesExactLargeAndFractionalBalances() {
        for(long units:new long[]{1,10_000,KillerPoints.whole(123)+4567,KillerPoints.MAX_UNITS-1}) {
            var p=PlayerProfile.fresh().addPointUnits(units);
            var payload=ProfilePayload.from(p);
            var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
            try {
                ProfilePayload.CODEC.encode(buffer,payload);
                assertEquals(payload,ProfilePayload.CODEC.decode(buffer));
                assertEquals(units,payload.pointUnits());
            } finally { buffer.release(); }
            var m=new DashboardData.Mission("mission",1,10,units,false,1,"hits");
            assertEquals(m,DashboardData.JSON.fromJson(DashboardData.JSON.toJson(m),DashboardData.Mission.class));
        }
    }
    @Test void currencySaturatesWithoutOverflowAndRejectsInvalidDecimals() {
        var p=PlayerProfile.fresh().addPointUnits(Long.MAX_VALUE);
        assertEquals(KillerPoints.MAX_UNITS,p.pointUnits());
        assertEquals(0,p.addPointUnits(Long.MIN_VALUE).pointUnits());
        assertEquals(10_000,KillerPoints.fromDecimal(.001));
        assertThrows(IllegalArgumentException.class,()->KillerPoints.fromDecimal(Double.NaN));
        assertThrows(IllegalArgumentException.class,()->KillerPoints.fromDecimal(Double.POSITIVE_INFINITY));
    }
    @Test void completedMissionHistoryRetainsAmountActuallyPaidAcrossMigration() {
        var d=MissionCatalog.ALL.stream().filter(m->m.reward()==100).findFirst().orElseThrow();
        var old=new dev.killerranking.mission.MissionBoard.Entry(d.id(),d.goal(),d.goal(),true);
        assertEquals(KillerPoints.whole(100),old.displayedReward(d));
        var unpaid=new dev.killerranking.mission.MissionBoard.Entry(d.id(),0,0,false);
        assertEquals(10000,unpaid.displayedReward(d));
        var paid=unpaid.advance(d.goal(),d.goal(),true).withPaidReward(d.rewardUnits());
        var codec=dev.killerranking.mission.MissionBoard.Entry.CODEC;
        var saved=codec.encodeStart(JsonOps.INSTANCE,paid).getOrThrow();
        var restored=codec.parse(JsonOps.INSTANCE,saved).getOrThrow();
        assertEquals(10000,restored.displayedReward(d));
        assertEquals(paid,restored.advance(d.goal(),d.goal(),true));
    }
}
