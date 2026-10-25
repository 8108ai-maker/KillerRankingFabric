package dev.killerranking.combat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class KillingIntentEffect {
	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("killerranking", "killing_intent_bonus");
	private KillingIntentEffect() { }
	public static void apply(AttributeInstance attribute, int percent) {
		if (attribute == null) return;
		var previous = attribute.getModifier(ID);
		double amount = percent / 100.0;
		if (amount <= 0) { if (previous != null) attribute.removeModifier(ID); }
		else if (previous == null || Double.compare(previous.amount(), amount) != 0) {
			attribute.addOrUpdateTransientModifier(new AttributeModifier(ID, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}
	}
}
