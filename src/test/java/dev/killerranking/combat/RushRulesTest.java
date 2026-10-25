package dev.killerranking.combat;

import com.mojang.serialization.JsonOps;
import dev.killerranking.data.RushState;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class RushRulesTest {
    @Test void thirtyFiveDamageStrikesFitExactlyWithinFiveSeconds() {
        var ticks=new ArrayList<Integer>();
        for(int tick=-10;tick<120;tick++) if(RushRules.strikesAt(tick)) ticks.add(tick);
        assertEquals(30,ticks.size());assertEquals(3,ticks.getFirst());assertEquals(99,ticks.getLast());
        for(int i=1;i<ticks.size();i++) assertTrue(ticks.get(i)-ticks.get(i-1)==3||ticks.get(i)-ticks.get(i-1)==4);
        assertEquals(150,ticks.size()*RushRules.DAMAGE);
    }
    @Test void lockedSkillCannotStartAndUnlockDoesNotResetCooldown() {
        assertSame(RushState.LOCKED,RushState.LOCKED.cast(50));
        var state=RushState.LOCKED.unlock().cast(50);
        assertTrue(state.unlocked());assertEquals(400,state.cooldown(50));
        assertSame(state,state.unlock());assertSame(state,state.cast(51));
        assertEquals(300,state.cooldown(150));assertEquals(1,state.cooldown(449));
        assertEquals(0,state.cooldown(450));assertEquals(400,state.cast(450).cooldown(450));
    }
    @Test void ownershipAndCooldownSurviveSaveLoad() {
        var state=new RushState(true,1000);
        var json=RushState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();
        var loaded=RushState.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();
        assertEquals(state,loaded);assertEquals(250,loaded.cooldown(750));
        assertEquals(RushState.LOCKED,RushState.CODEC.parse(JsonOps.INSTANCE,new com.google.gson.JsonObject()).getOrThrow());
    }
    @Test void targetingExcludesBehindSideAndOutsideRange() {
        assertTrue(RushRules.inFront(4,1));assertTrue(RushRules.inFront(9,.8));
        assertFalse(RushRules.inFront(4,-1));assertFalse(RushRules.inFront(4,0));
        assertFalse(RushRules.inFront(10.25,1));assertFalse(RushRules.inFront(1,.779));
    }
}
