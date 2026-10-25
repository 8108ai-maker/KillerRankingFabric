package dev.killerranking.combat;
import dev.killerranking.data.BishopState;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BishopRulesTest {
    @Test void primaryPatternsHaveSixTimedCutsOrSixStraightThrusts(){
        for(int formation=0;formation<6;formation++){
            var styles=new java.util.HashSet<Integer>();
            for(int cycle=0;cycle<4;cycle++)styles.add(BishopTactics.attack(cycle,formation,1.4,0));
            assertEquals(java.util.Set.of(BishopRules.COMBO,BishopRules.STAB_COMBO),styles,"Both continuous patterns are used at melee distance");
        }
        for(int style=1;style<=4;style++){
            var contacts=new java.util.ArrayList<Integer>();
            for(int tick=0;tick<=BishopRules.attackLength(style)+1;tick++)if(BishopRules.isHitTick(style,tick))contacts.add(tick);
            assertEquals(style==3?java.util.List.of(4,8,12,16,20,24):style==4?java.util.List.of(5,10,15,20,25,30):java.util.List.of(BishopRules.hitTick(style)),contacts);
        }
        assertEquals(3,BishopRules.COMBO_HITS*BishopRules.COMBO_DAMAGE_SCALE,.001,"More contacts preserve the existing total damage");
        assertTrue(BishopRules.reach(BishopRules.STAB_COMBO,.6f)>BishopRules.reach(BishopRules.COMBO,.6f));
    }
    @Test void levelControlsArmySizeAndDaggerDamage(){
        assertEquals(2,BishopRules.count(1));assertEquals(4,BishopRules.count(2));assertEquals(6,BishopRules.count(3));
        assertEquals(4,BishopRules.damage(1));assertEquals(5,BishopRules.damage(2));assertEquals(6,BishopRules.damage(3));
        assertEquals(6,BishopRules.count(Integer.MAX_VALUE));assertEquals(2,BishopRules.count(-10));
    }
    @Test void leapHasBoundedLowArc(){
        assertTrue(BishopRules.canLeap(3));assertTrue(BishopRules.canLeap(16));assertFalse(BishopRules.canLeap(2.99));assertFalse(BishopRules.canLeap(16.01));assertFalse(BishopRules.canLeap(Double.NaN));
        assertTrue(BishopRules.LEAP_UP<.4);assertTrue(BishopRules.LEAP_SPEED>=1.6);assertEquals(.75,BishopRules.PERSONAL_DAMAGE);
    }
    @Test void cooldownPersistsThroughOwnershipTransfer(){
        var owned=new BishopState(true,1200);assertEquals(300,owned.cooldown(100));assertEquals(140,owned.cooldown(1060));assertEquals(0,owned.cooldown(1200));
        assertEquals(1200,owned.owned(false).readyAt());assertEquals(1200,owned.owned(false).owned(true).readyAt());
        assertEquals(0,new BishopState(true,-5).readyAt());
    }
    @Test void levelsAllowOverlappingWavesWithinThePopulationLimit(){
        assertEquals(300,BishopRules.cooldown(1));assertEquals(200,BishopRules.cooldown(2));assertEquals(140,BishopRules.cooldown(3));
        assertEquals(300,BishopRules.cooldown(-10));assertEquals(140,BishopRules.cooldown(99));
        for(int level=1;level<=3;level++){
            int simultaneousWaves=(BishopRules.DURATION+BishopRules.cooldown(level)-1)/BishopRules.cooldown(level);
            assertTrue(simultaneousWaves*BishopRules.count(level)<=BishopRules.MAX_UNITS);
            assertTrue(BishopRules.cooldown(level)<BishopRules.DURATION);
        }
    }
    @Test void missingLegacyPlayerFieldsLoadAsLocked(){
        assertEquals(BishopState.LOCKED,BishopState.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("{}")).getOrThrow());
        var stored=new BishopState(true,555);assertEquals(stored,BishopState.CODEC.parse(JsonOps.INSTANCE,BishopState.CODEC.encodeStart(JsonOps.INSTANCE,stored).getOrThrow()).getOrThrow());
    }
}
