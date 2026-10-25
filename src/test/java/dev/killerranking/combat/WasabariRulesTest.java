package dev.killerranking.combat;
import dev.killerranking.data.WasabariState;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WasabariRulesTest {
    @Test void twoSweepBeatsPrecedeExactlySixKnifeContacts(){
        assertTrue(WasabariRules.WA<WasabariRules.BARI);assertTrue(WasabariRules.BARI<WasabariRules.FIRST_STAB);
        int hits=0;for(int age=-10;age<100;age++)if(WasabariRules.stab(age))hits++;
        assertEquals(6,hits);assertEquals(2,WasabariRules.STAB_DAMAGE);
        assertTrue(WasabariRules.stab(26));assertTrue(WasabariRules.stab(56));assertFalse(WasabariRules.stab(60));
        assertEquals(40,WasabariRules.RELEASE-WasabariRules.BARI);assertTrue(WasabariRules.DURATION>WasabariRules.RELEASE);
    }
    @Test void fiveSecondsOfHemorrhageContainTenFixedTwoDamagePulses(){
        int pulses=0;for(int remaining=WasabariRules.EFFECT_TICKS;remaining>=0;remaining--)if(WasabariRules.bleedPulse(remaining))pulses++;
        assertEquals(10,pulses);assertEquals(2,WasabariRules.BLEED_DAMAGE);assertEquals(20,pulses*WasabariRules.BLEED_DAMAGE);
        assertEquals(10,WasabariRules.BLEED_INTERVAL);assertEquals(1,WasabariRules.SLOWNESS_AMPLIFIER);
        assertFalse(WasabariRules.bleedPulse(0));assertFalse(WasabariRules.bleedPulse(-10));
    }
    @Test void independentCooldownCannotBeRestartedAndSurvivesSaving(){
        var state=new WasabariState(0).cast(100);assertEquals(400,state.cooldown(100));assertSame(state,state.cast(120));
        assertEquals(1,state.cooldown(499));assertEquals(0,state.cooldown(500));assertEquals(400,state.cast(500).cooldown(500));
        var encoded=WasabariState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();assertEquals(state,WasabariState.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow());
        assertEquals(0,new WasabariState(-1).readyAt());assertEquals(400,new WasabariState(Long.MAX_VALUE).cooldown(0));
    }
}
