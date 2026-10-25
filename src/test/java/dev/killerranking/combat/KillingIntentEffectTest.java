package dev.killerranking.combat;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KillingIntentEffectTest {
	@org.junit.jupiter.api.BeforeAll static void bootstrap() {
		net.minecraft.SharedConstants.tryDetectVersion();
		net.minecraft.server.Bootstrap.bootStrap();
	}
	@Test void bonusIncludesWeaponAndDoesNotStackOnRepeatedHits() {
		var attack = new AttributeInstance(Holder.direct(new RangedAttribute("test.attack", 10, 0, 1024)), ignored -> { });
		attack.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("test", "weapon"), 6, AttributeModifier.Operation.ADD_VALUE));
		KillingIntentEffect.apply(attack, 5); assertEquals(16.8, attack.getValue(), 0.00001);
		KillingIntentEffect.apply(attack, 10); assertEquals(17.6, attack.getValue(), 0.00001);
		KillingIntentEffect.apply(attack, 15); assertEquals(18.4, attack.getValue(), 0.00001);
		KillingIntentEffect.apply(attack, 15); assertEquals(18.4, attack.getValue(), 0.00001);
		KillingIntentEffect.apply(attack, 5); assertEquals(16.8, attack.getValue(), 0.00001);
		KillingIntentEffect.apply(attack, 0); assertEquals(16, attack.getValue(), 0.00001);
		assertEquals(1, attack.getModifiers().size(), "Removing the trait must keep the weapon modifier");
	}
}
