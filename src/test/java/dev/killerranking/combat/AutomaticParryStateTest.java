package dev.killerranking.combat;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AutomaticParryStateTest {
    @Test void holdNeedsRenewalAndExpiresAtTheExactLeaseBoundary(){
        var state=new AutomaticParryState();var id=UUID.randomUUID();assertTrue(state.hold(id,100));
        assertTrue(state.active(id,114));assertFalse(state.active(id,115));
    }
    @Test void renewalKeepsTheCurrentMotionAndEchoChain(){
        var state=new AutomaticParryState();var id=UUID.randomUUID();state.hold(id,0);
        assertFalse(state.strike(id,1).echo());assertFalse(state.hold(id,5));assertTrue(state.strike(id,6).echo());assertTrue(state.active(id,19));
    }
    @Test void everyOverlappingAttackGetsAnIndependentSuccessSequence(){
        var state=new AutomaticParryState();var id=UUID.randomUUID();state.hold(id,0);
        assertEquals(new AutomaticParryState.Strike(false,0),state.strike(id,1));
        for(int hit=1;hit<=10;hit++)assertEquals(new AutomaticParryState.Strike(true,hit),state.strike(id,1));
        assertTrue(state.strike(id,12).echo());state.hold(id,15);assertFalse(state.strike(id,24).echo());
    }
    @Test void releaseRemovesTheLeaseAndPriorMotion(){
        var state=new AutomaticParryState();var id=UUID.randomUUID();state.hold(id,0);state.strike(id,1);state.release(id);
        assertFalse(state.active(id,2));assertThrows(IllegalStateException.class,()->state.strike(id,2));
        state.hold(id,2);assertEquals(new AutomaticParryState.Strike(false,0),state.strike(id,2));
    }
    @Test void playersHaveIndependentChainsAndShutdownClearsBoth(){
        var state=new AutomaticParryState();var a=UUID.randomUUID();var b=UUID.randomUUID();state.hold(a,0);state.hold(b,0);
        state.strike(a,1);assertFalse(state.strike(b,2).echo());state.release(a);assertTrue(state.active(b,3));state.clear();assertFalse(state.active(b,3));
    }
    @Test void anExpiredLeaseStartsWithTheBodyAgain(){
        var state=new AutomaticParryState();var id=UUID.randomUUID();state.hold(id,0);state.strike(id,12);
        assertTrue(state.hold(id,15));assertEquals(new AutomaticParryState.Strike(false,0),state.strike(id,15));
    }
}
