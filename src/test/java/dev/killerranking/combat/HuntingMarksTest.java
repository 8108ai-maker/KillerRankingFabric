package dev.killerranking.combat;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class HuntingMarksTest {
    private final HuntingMarks marks = new HuntingMarks();
    private final UUID hunter = UUID.randomUUID(), first = UUID.randomUUID(), second = UUID.randomUUID();
    private static final String WORLD = "minecraft:overworld";

    @Test void switchingTargetsReplacesTheOnlyMarkAndBleedingSchedule() {
        marks.set(hunter,first,WORLD,100,60);marks.set(hunter,second,WORLD,110,120);
        assertEquals(second,marks.get(hunter,111).target());assertNull(marks.due(hunter,129));
        assertEquals(second,marks.due(hunter,130).target());assertEquals(1,marks.owners().size());
    }
    @Test void rapidRehitsExtendExpiryWithoutStarvingBleeding() {
        marks.set(hunter,first,WORLD,100,60);marks.set(hunter,first,WORLD,119,180);
        assertEquals(120,marks.get(hunter,119).nextBleed());assertNotNull(marks.due(hunter,120));
        marks.advance(hunter,120);assertNull(marks.due(hunter,139));assertNotNull(marks.due(hunter,140));
        assertNotNull(marks.get(hunter,298));assertNull(marks.get(hunter,299));
    }
    @Test void everyLevelDeliversAllPulsesIncludingTheLastAtExpiry() {
        for(int level=1;level<=3;level++) {
            marks.clear();
            marks.set(hunter,first,WORLD,100,TraitLevels.poisonTicks(level));int pulses=0;
            for(long now=101;now<=100+level*60;now++)if(marks.due(hunter,now)!=null){pulses++;marks.advance(hunter,now);}
            assertEquals(level*3,pulses);assertNull(marks.get(hunter,100+level*60));assertNull(marks.due(hunter,101+level*60));
        }
    }
    @Test void otherHuntersAndWorldsRemainIndependent() {
        marks.set(hunter,first,WORLD,100,180);marks.set(second,first,"minecraft:the_nether",100,60);
        assertEquals(2,marks.owners().size());marks.remove(second);assertNotNull(marks.get(hunter,200));assertNull(marks.get(second,101));
    }
    @Test void selfInvalidExpiryAndClearDoNotLeaveMarks() {
        marks.set(hunter,hunter,WORLD,0,60);marks.set(hunter,first,WORLD,0,0);assertTrue(marks.owners().isEmpty());
        marks.set(hunter,first,WORLD,0,60);marks.clear();assertNull(marks.get(hunter,1));assertNull(marks.due(hunter,20));
    }
}
