package dev.killerranking.combat;
import dev.killerranking.data.RookState;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RookRulesTest {
    @Test void sequencesHaveFiveQuickAndSixSeparatedHeavyHits() {
        for(int t=0;t<3;t++)assertFalse(RookRules.strike(RookRules.PUNCH,t),"Wind-up must not deal damage");
        for(int t:new int[]{3,9,15,21,27})assertTrue(RookRules.strike(RookRules.PUNCH,t),"Hit must land at full extension");
        int punch=0,mount=0,previous=-100;
        for(int t=0;t<RookRules.MOUNT_DURATION;t++){
            if(RookRules.strike(RookRules.PUNCH,t))punch++;
            if(RookRules.strike(RookRules.MOUNT,t)){assertTrue(t-previous>=15);previous=t;mount++;}
            assertFalse(RookRules.strike(RookRules.CHOICE,t));assertFalse(RookRules.strike(RookRules.THROW,t));
        }
        assertEquals(5,punch);assertEquals(6,mount);assertEquals(20,punch*RookRules.PUNCH_DAMAGE);assertEquals(48,mount*RookRules.MOUNT_DAMAGE);
        assertFalse(RookRules.strike(RookRules.MOUNT,-1));assertFalse(RookRules.strike(RookRules.MOUNT,RookRules.MOUNT_DURATION));
    }
    @Test void onlyOneSpiritSkillCanBePurchased(){
        for(int mask=0;mask<8;mask++)assertEquals(mask==0,RookRules.canUnlock((mask&1)!=0,(mask&2)!=0,(mask&4)!=0));
    }
    @Test void cooldownCannotBeRestartedOrResetByOwnershipChangesAndPersists(){
        var start=new RookState(true,0).cast(200);assertEquals(400,start.cooldown(200));assertSame(start,start.cast(250));
        var off=start.owned(false);assertEquals(350,off.cooldown(250));assertEquals(start,off.owned(true));
        var data=RookState.CODEC.encodeStart(JsonOps.INSTANCE,start).getOrThrow();assertEquals(start,RookState.CODEC.parse(JsonOps.INSTANCE,data).getOrThrow());
        assertEquals(RookState.LOCKED,RookState.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("{}")).getOrThrow());
        assertEquals(0,start.cooldown(600));assertEquals(0,new RookState(false,-20).readyAt());
    }
}
