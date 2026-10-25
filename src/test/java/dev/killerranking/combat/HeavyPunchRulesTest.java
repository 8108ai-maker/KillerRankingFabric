package dev.killerranking.combat;
import dev.killerranking.data.HeavyPunchState;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HeavyPunchRulesTest {
    @Test void oneContactFollowsTheDropAndLeavesRecoveryTime(){
        int count=0;for(int age=-10;age<100;age++)if(HeavyPunchRules.contact(age))count++;
        assertEquals(1,count);assertTrue(HeavyPunchRules.DROP<HeavyPunchRules.SPLASH);
        assertTrue(HeavyPunchRules.SPLASH<HeavyPunchRules.IMPACT);
        assertTrue(HeavyPunchRules.DURATION>HeavyPunchRules.IMPACT+5);assertEquals(30,HeavyPunchRules.DAMAGE);
    }
    @Test void repeatedRequestsCannotResetIndependentCooldown(){
        var state=new HeavyPunchState(0).cast(120);assertEquals(500,state.cooldown(120));
        assertSame(state,state.cast(121));assertEquals(1,state.cooldown(619));
        assertEquals(0,state.cooldown(620));assertEquals(500,state.cast(620).cooldown(620));
    }
    @Test void cooldownSurvivesSavingAndBadNegativeDatesAreClamped(){
        var state=new HeavyPunchState(9876);var json=HeavyPunchState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();
        assertEquals(state,HeavyPunchState.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow());
        assertEquals(0,new HeavyPunchState(-123).readyAt());assertEquals(500,state.cooldown(0));
    }
}
