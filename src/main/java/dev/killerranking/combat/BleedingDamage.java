package dev.killerranking.combat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;

/** A scoped damage-over-time hit cannot renew marks, generate XP/morale, or inherit attack bonuses. */
public final class BleedingDamage {
    public static final ResourceKey<DamageType> TYPE = ResourceKey.create(Registries.DAMAGE_TYPE,
        ResourceLocation.fromNamespaceAndPath("killerranking", "bleeding"));
    private static final ThreadLocal<LivingEntity> CURRENT = new ThreadLocal<>();
    private static final ThreadLocal<Float> AMOUNT = new ThreadLocal<>();
    public static boolean current(LivingEntity victim) { return CURRENT.get() == victim; }
    public static float amount(LivingEntity victim) { return current(victim)&&AMOUNT.get()!=null?AMOUNT.get():1; }
    public static boolean hit(ServerPlayer hunter, LivingEntity target) {
        return hit(hunter,target,1);
    }
    public static boolean hit(ServerPlayer hunter,LivingEntity target,float amount) {
        LivingEntity previous = CURRENT.get(); CURRENT.set(target);
        Float previousAmount=AMOUNT.get();AMOUNT.set(amount);
        try {
            var holder=target.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TYPE);
            var source = hunter==null?new DamageSource(holder):new DamageSource(holder,hunter);
            return RushDamage.hit(hunter, target, source, amount, false);
        } finally { if (previous == null) CURRENT.remove(); else CURRENT.set(previous);if(previousAmount==null)AMOUNT.remove();else AMOUNT.set(previousAmount); }
    }
    private BleedingDamage() { }
}
