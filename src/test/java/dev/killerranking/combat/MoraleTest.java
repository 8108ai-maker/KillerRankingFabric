package dev.killerranking.combat;
import com.mojang.serialization.JsonOps;
import dev.killerranking.data.MoraleState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MoraleTest {
    @Test void outgoingDamageGainsOnePercentPerPointIncludingFractionalDamage() {
        assertEquals(1.05,MoraleRules.dealt(1,5),1e-9);assertEquals(1.075,MoraleRules.dealt(1.05,2.5),1e-9);
        assertEquals(1.25,MoraleRules.dealt(1,10000),1e-9);
    }
    @Test void receivingDamageLosesOnePercentPerHitAndStopsAtLowerBound() {
        double morale=1;for(int i=0;i<100;i++)morale=MoraleRules.received(morale);
        assertEquals(.75,morale,1e-9);assertEquals(1.24,MoraleRules.received(1.25),1e-9);
    }
    @Test void rejectedAndInvalidDamageCannotGenerateMorale() {
        for(double damage:new double[]{0,-1,Double.NaN,Double.POSITIVE_INFINITY})assertEquals(1,MoraleRules.dealt(1,damage),1e-9);
        assertEquals(1,MoraleRules.clamp(Double.NaN));
    }
    @Test void actualDamageCountsAbsorptionAndClampsOverkillToRemainingHealth() {
        assertEquals(6,MoraleRules.actualDamage(20,4,18,0));assertEquals(2,MoraleRules.actualDamage(2,0,0,0));
        assertEquals(0,MoraleRules.actualDamage(20,0,20,0));assertEquals(0,MoraleRules.actualDamage(20,0,22,2));
    }
    @Test void persistedMoraleRoundTripsAndOldInvalidSavesClamp() {
        var json=MoraleState.CODEC.encodeStart(JsonOps.INSTANCE,1.137).getOrThrow();
        assertEquals(1.137,MoraleState.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow(),1e-9);
        assertEquals(.75,MoraleState.CODEC.parse(JsonOps.INSTANCE,new com.google.gson.JsonPrimitive(-10)).getOrThrow());
        assertEquals(1.25,MoraleState.CODEC.parse(JsonOps.INSTANCE,new com.google.gson.JsonPrimitive(10)).getOrThrow());
    }
}
