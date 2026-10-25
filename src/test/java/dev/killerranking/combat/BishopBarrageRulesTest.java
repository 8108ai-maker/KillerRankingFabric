package dev.killerranking.combat;
import dev.killerranking.data.BishopState;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BishopBarrageRulesTest {
    @Test void fiveSecondsContainFiftyTwoPointHits(){
        var contacts=new java.util.ArrayList<Integer>();
        for(int age=-1;age<=101;age++)if(BishopBarrageRules.contact(age))contacts.add(age);
        assertEquals(50,contacts.size());assertEquals(2,contacts.getFirst());assertEquals(100,contacts.getLast());
        assertEquals(100,contacts.size()*BishopBarrageRules.DAMAGE);
        assertTrue(BishopBarrageRules.ECHO_TICKS<=4,"helpers only introduce the skill");
    }
    @Test void orientedZoneAcceptsFrontAndRejectsBackAndOutside(){
        assertTrue(BishopBarrageRules.inZone(3,1,1,.3));assertTrue(BishopBarrageRules.inZone(6.2,2.2,1,.3));
        assertFalse(BishopBarrageRules.inZone(-1,0,1,.3));assertFalse(BishopBarrageRules.inZone(6.4,0,1,.3));
        assertFalse(BishopBarrageRules.inZone(3,2.4,1,.3));assertFalse(BishopBarrageRules.inZone(3,0,3,.3));
        assertFalse(BishopBarrageRules.inZone(Double.NaN,0,1,.3));
    }
    @Test void independentCooldownSurvivesOwnershipAndCodec(){
        var stored=new BishopState(true,1200,900);
        assertEquals(250,stored.barrageCooldown(650));assertEquals(300,stored.cooldown(650));
        assertEquals(900,stored.owned(false).owned(true).barrageReadyAt());
        assertEquals(stored,BishopState.CODEC.parse(JsonOps.INSTANCE,BishopState.CODEC.encodeStart(JsonOps.INSTANCE,stored).getOrThrow()).getOrThrow());
    }
    @Test void existingPlayerAttachmentHasReadyBarrageWithoutLosingSummon(){
        var old=BishopState.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("{\"unlocked\":true,\"ready_at\":1200}")).getOrThrow();
        assertTrue(old.unlocked());assertEquals(1200,old.readyAt());assertEquals(0,old.barrageReadyAt());
        assertEquals(0,new BishopState(true,-1,-2).barrageReadyAt());
    }
}
