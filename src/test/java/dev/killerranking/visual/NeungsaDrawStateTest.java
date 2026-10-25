package dev.killerranking.visual;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static dev.killerranking.visual.NeungsaDrawState.Phase.*;

class NeungsaDrawStateTest {
    @Test void comboDrawsOnceAndSheathesOnlyAfterItsLastCut() {
        var s=new NeungsaDrawState();
        assertTrue(s.attack(0,12));
        assertEquals(DRAWN,s.tick(11,false));
        assertFalse(s.attack(12,12));
        assertFalse(s.attack(24,12));
        assertEquals(DRAWN,s.tick(41,false));
        assertEquals(SHEATHING,s.tick(42,false));
        assertEquals(SHEATHING,s.tick(53,false));
        assertEquals(SHEATHED,s.tick(54,false));
        assertTrue(s.attack(55,12));
    }
    @Test void heldAttackKeepsBladeOutEvenThroughACombatDelay() {
        var s=new NeungsaDrawState();s.attack(10,12);
        assertEquals(DRAWN,s.tick(100,true));
        assertFalse(s.attack(101,12));
        assertEquals(SHEATHING,s.tick(119,false));
    }
    @Test void interruptingInsertionResumesDrawnAttacksWithoutAnotherDraw() {
        var s=new NeungsaDrawState();s.attack(0,12);s.tick(18,false);
        assertFalse(s.attack(20,12));
        assertEquals(DRAWN,s.tick(30,false));
        s.reset();assertEquals(SHEATHED,s.phase());
    }
}
