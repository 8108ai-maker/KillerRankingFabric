package dev.killerranking.combat;
import com.google.gson.JsonParser;
import dev.killerranking.config.KillerRankingConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CombatBalanceTest {
    @Test void existingShippedConfigurationMigratesOnceAndPreservesOtherSettings() {
        var old=JsonParser.parseString("{\"mixedMadnessBasePercent\":25,\"parryCooldownTicks\":33}").getAsJsonObject();
        assertTrue(KillerRankingConfig.upgradeCombatBalance(old));assertEquals(25,old.get("mixedMadnessBasePercent").getAsInt());
        assertEquals(33,old.get("parryCooldownTicks").getAsInt());old.addProperty("mixedMadnessBasePercent",25);
        assertFalse(KillerRankingConfig.upgradeCombatBalance(old));assertEquals(25,old.get("mixedMadnessBasePercent").getAsInt());
        var custom=JsonParser.parseString("{\"mixedMadnessBasePercent\":12}").getAsJsonObject();
        KillerRankingConfig.upgradeCombatBalance(custom);assertEquals(12,custom.get("mixedMadnessBasePercent").getAsInt());
    }
    @Test void newBonusesScaleWithTraitLevel() {
        var config=new KillerRankingConfig();for(int level=1;level<=3;level++) {
            assertEquals(level*25,TraitLevels.bonusPercent(config.mixedMadnessBasePercent,level));
            assertEquals(level*5,TraitLevels.bonusPercent(config.mixedMadnessAttackSpeedPercent,level));assertEquals(level*15,PoisonEffects.speed(level));
        }
    }
    @Test void revisionOneDefaultUpgradesWithoutRevertingCustomRevisionTwoValues() {
        var current=JsonParser.parseString("{\"combatBalanceRevision\":1,\"mixedMadnessBasePercent\":18,\"mixedMadnessAttackSpeedPercent\":7}").getAsJsonObject();
        assertTrue(KillerRankingConfig.upgradeCombatBalance(current));assertEquals(25,current.get("mixedMadnessBasePercent").getAsInt());
        assertEquals(2,current.get("combatBalanceRevision").getAsInt());assertEquals(7,current.get("mixedMadnessAttackSpeedPercent").getAsInt());
        current.addProperty("mixedMadnessBasePercent",42);assertFalse(KillerRankingConfig.upgradeCombatBalance(current));assertEquals(42,current.get("mixedMadnessBasePercent").getAsInt());
    }
    @Test void repeatedSpeedRefreshesNeverAccumulateAndDisableRemovesOnlyOwnedModifier() {
        var a=new AttributeInstance(Holder.direct(new RangedAttribute("test.speed",4,0,1024)),ignored->{});
        var id=ResourceLocation.fromNamespaceAndPath("test","poison_speed");
        for(int i=0;i<100;i++)PoisonEffects.applyPercent(a,id,15);
        assertEquals(4*1.15,a.getValue(),1e-9);assertEquals(1,a.getModifiers().size());
        PoisonEffects.applyPercent(a,id,45);assertEquals(4*1.45,a.getValue(),1e-9);
        PoisonEffects.applyPercent(a,id,0);assertEquals(4,a.getValue(),1e-9);
    }
}
