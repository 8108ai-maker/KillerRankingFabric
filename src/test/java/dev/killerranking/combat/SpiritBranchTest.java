package dev.killerranking.combat;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.killerranking.data.SpiritState;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpiritBranchTest {
    @BeforeAll static void bootstrap() { net.minecraft.SharedConstants.tryDetectVersion(); net.minecraft.server.Bootstrap.bootStrap(); }
    @Test void tteokbalBenefitsScaleLinearly() {
        for (int level = 1; level <= 3; level++) {
            assertEquals(5 * level, SpiritBranch.TTEOKBAL.attack(level));
            assertEquals(5 * level, SpiritBranch.TTEOKBAL.defense(level));
            assertEquals(10 * level, SpiritBranch.TTEOKBAL.attackSpeed(level));
            assertEquals(10 * level, SpiritBranch.TTEOKBAL.movement(level, false));
        }
    }
    @Test void kingAndKillScalesBenefitsButNotPenalty() {
        for (int level = 1; level <= 3; level++) {
            assertEquals(5 * level, SpiritBranch.KING_AND_KILL.attack(level));
            assertEquals(20 * level, SpiritBranch.KING_AND_KILL.defense(level));
            assertEquals(-2, SpiritBranch.KING_AND_KILL.movement(level, false));
            assertEquals(2 * level, SpiritBranch.KING_AND_KILL.movement(level, true));
            assertEquals(0, SpiritBranch.KING_AND_KILL.attackSpeed(level));
        }
    }
    @Test void repeatedUpdatesDoNotStackAndCleanupPreservesEquipment() {
        var attack = new AttributeInstance(Attributes.ATTACK_DAMAGE, a -> {});
        attack.setBaseValue(1);
        var weapon = ResourceLocation.withDefaultNamespace("test_weapon");
        attack.addTransientModifier(new AttributeModifier(weapon, 9, AttributeModifier.Operation.ADD_VALUE));
        for (int i = 0; i < 100; i++) SpiritEffects.apply(attack, 15);
        assertEquals(11.5, attack.getValue(), 0.00001);
        SpiritEffects.apply(attack, 5); assertEquals(10.5, attack.getValue(), 0.00001);
        SpiritEffects.apply(attack, 0); assertEquals(10, attack.getValue(), 0.00001);
        assertNotNull(attack.getModifier(weapon));
    }
    @Test void movementPenaltyIsReplacedByCheckmateBenefit() {
        var speed = new AttributeInstance(Attributes.MOVEMENT_SPEED, a -> {}); speed.setBaseValue(.1);
        SpiritEffects.apply(speed, -2); assertEquals(.098, speed.getValue(), .000001);
        SpiritEffects.apply(speed, 6); assertEquals(.106, speed.getValue(), .000001);
        SpiritEffects.apply(speed, 0); assertEquals(.1, speed.getValue(), .000001);
    }
    @Test void legacyAndUnknownBranchesSafelyDefault() {
        var state = SpiritState.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}" )).getOrThrow();
        assertEquals(SpiritBranch.NONE, state.branch()); assertFalse(state.checkmate());
        assertEquals(SpiritBranch.NONE, SpiritBranch.fromId("bogus"));
        assertFalse(new SpiritState(SpiritBranch.TTEOKBAL, true).checkmate());
    }
    @Test void branchAndOwnershipSurviveSerialization() {
        var state = new SpiritState(SpiritBranch.KING_AND_KILL, true);
        assertEquals(state, SpiritState.CODEC.parse(JsonOps.INSTANCE, SpiritState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow()).getOrThrow());
    }
}
