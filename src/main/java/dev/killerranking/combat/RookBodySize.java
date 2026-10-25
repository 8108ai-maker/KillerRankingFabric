package dev.killerranking.combat;

import dev.killerranking.data.RookState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Skill ownership changes native model, camera and collision size together. */
public final class RookBodySize {
    public static final double HEIGHT = 2.17;
    public static final double SCALE = HEIGHT / 1.8;
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("killerranking", "rook_body_scale");

    private RookBodySize() { }

    public static void refresh(ServerPlayer player) {
        var attribute = player.getAttribute(Attributes.SCALE);
        if (attribute == null) return;
        var previous = attribute.getModifier(ID);
        if (!RookService.branch(player) || !RookState.get(player).unlocked()) {
            if (previous != null) attribute.removeModifier(ID);
            return;
        }
        double amount = SCALE - 1;
        if (previous == null || Double.compare(previous.amount(), amount) != 0
                || previous.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
            attribute.addOrUpdateTransientModifier(new AttributeModifier(ID, amount,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }
}
