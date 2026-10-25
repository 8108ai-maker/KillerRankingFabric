package dev.killerranking.combat;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.Item;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InheritedWeaponEffectsTest {
    @BeforeAll static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }
    private static AttributeInstance speed(double weapon) {
        var result = new AttributeInstance(Holder.direct(new RangedAttribute("test.speed", 4, 0, 1024)), ignored -> { });
        result.addTransientModifier(new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, weapon, AttributeModifier.Operation.ADD_VALUE));
        return result;
    }
    private static void modifier(AttributeInstance speed, String name, double amount, AttributeModifier.Operation operation) {
        speed.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("test", name), amount, operation));
    }
    @Test void bothHandsUseReducedMeanAndLeaveWeaponAttributesUntouched() {
        var knife = speed(-1.6); var axe = speed(-2.8);
        assertEquals(1.44, InheritedWeaponEffects.pairedSpeed(knife), 0.00001);
        assertEquals(1.44, InheritedWeaponEffects.pairedSpeed(axe), 0.00001);
        assertEquals(2.4, knife.getValue(), 0.00001);
        assertEquals(1.2, axe.getValue(), 0.00001);
    }
    @Test void otherSpeedModifiersKeepTheirVanillaOrderAndMadnessMultipliesOnce() {
        var speed = speed(-2.8);
        modifier(speed, "extra_flat", .5, AttributeModifier.Operation.ADD_VALUE);
        modifier(speed, "dual", .2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        modifier(speed, "haste", .4, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        double expected = (1.8 + .5) * 1.2 * 1.4 * .8;
        assertEquals(expected, InheritedWeaponEffects.pairedSpeed(speed), 0.00001);
        for (int i = 0; i < 100; i++) InheritedWeaponEffects.applySpeedBonus(speed, true);
        assertEquals(expected * 1.2, InheritedWeaponEffects.pairedSpeed(speed), 0.00001);
        assertEquals(5, speed.getModifiers().size());
        InheritedWeaponEffects.applySpeedBonus(speed, false);
        assertEquals(expected, InheritedWeaponEffects.pairedSpeed(speed), 0.00001);
        assertEquals(4, speed.getModifiers().size());
    }
    @Test void normalizedSpeedStillUsesAttributeBounds() {
        var speed = speed(-1.6);
        modifier(speed, "slow", -20, AttributeModifier.Operation.ADD_VALUE);
        assertEquals(0, InheritedWeaponEffects.pairedSpeed(speed));
        speed.setBaseValue(2000);
        assertEquals(1024, InheritedWeaponEffects.pairedSpeed(speed));
    }
}
