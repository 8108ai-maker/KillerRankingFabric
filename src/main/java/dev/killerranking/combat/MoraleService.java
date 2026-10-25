package dev.killerranking.combat;

import dev.killerranking.data.MoraleState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class MoraleService {
    public static void afterHit(LivingEntity victim, DamageSource source, double actualDamage) {
        if (victim.level().isClientSide || BleedingDamage.current(victim)
            || !(source.getEntity() instanceof LivingEntity attacker) || attacker == victim) return;
        if (victim instanceof ServerPlayer player && !player.isSpectator())
            MoraleState.set(player, MoraleRules.received(MoraleState.get(player)));
        if (actualDamage > 0 && attacker instanceof ServerPlayer player && !player.isSpectator())
            MoraleState.set(player, MoraleRules.dealt(MoraleState.get(player), actualDamage));
    }
    private MoraleService() { }
}
