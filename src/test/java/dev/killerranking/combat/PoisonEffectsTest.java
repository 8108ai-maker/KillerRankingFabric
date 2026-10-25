package dev.killerranking.combat;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PoisonEffectsTest {
    @org.junit.jupiter.api.BeforeAll static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }
    @Test void resistanceScalesWithoutStackingAndPreservesEquipment() {
        var attribute = new AttributeInstance(Holder.direct(new RangedAttribute("test.resistance", 0, 0, 1)), ignored -> { });
        attribute.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("test", "gear"), .1, AttributeModifier.Operation.ADD_VALUE));
        for (int level = 1; level <= 3; level++) {
            for (int repeat = 0; repeat < 20; repeat++) PoisonEffects.apply(attribute, PoisonEffects.resistance(level));
            assertEquals(.1 + level * .2, attribute.getValue(), .00001);
            assertEquals(2, attribute.getModifiers().size());
        }
        PoisonEffects.apply(attribute, 20);
        assertEquals(.3, attribute.getValue(), .00001);
        PoisonEffects.apply(attribute, 0);
        assertEquals(.1, attribute.getValue(), .00001);
        assertEquals(1, attribute.getModifiers().size());
    }
    @Test void resistanceRespectsVanillaCap() {
        var attribute = new AttributeInstance(Holder.direct(new RangedAttribute("test.resistance", .8, 0, 1)), ignored -> { });
        PoisonEffects.apply(attribute, 60);
        assertEquals(1, attribute.getValue());
        PoisonEffects.apply(attribute, 0);
        assertEquals(.8, attribute.getValue());
    }
}
