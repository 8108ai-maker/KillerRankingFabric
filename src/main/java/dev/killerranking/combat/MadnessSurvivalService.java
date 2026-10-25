package dev.killerranking.combat;

import dev.killerranking.Trait;
import dev.killerranking.data.MadnessSurvivalState;
import dev.killerranking.data.PlayerProfiles;
import dev.killerranking.data.TraitActivation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class MadnessSurvivalService {
    public static long now(ServerPlayer p) { return p.server.overworld().getGameTime(); }
    public static boolean eligible(ServerPlayer p) {
        return p.isAlive() && !p.isSpectator() && !p.isCreative() && TraitActivation.enabled(p)
            && PlayerProfiles.get(p).trait() == Trait.MIXED_MADNESS;
    }
    public static void cancel(ServerPlayer p) {
        var old = MadnessSurvivalState.get(p);
        var next = old.cancelProtection();
        if (next != old) {
            MadnessSurvivalState.set(p, next);
            if (p.isAlive() && !p.isSpectator() && !p.isCreative()) {
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, MadnessSurvival.AFTER_EFFECT_TICKS, 0));
                p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, MadnessSurvival.AFTER_EFFECT_TICKS, 0));
            }
        }
    }
    public static void refresh(ServerPlayer p) {
        var state = MadnessSurvivalState.get(p);
        if (state.protectedUntil() > 0 && state.protectedTicks(now(p)) == 0) cancel(p);
        if (!eligible(p)) { cancel(p); return; }
        if (MadnessSurvival.atHalfHeart(p.getHealth())) activate(p);
    }
    private static boolean activate(ServerPlayer p) {
        var before = MadnessSurvivalState.get(p);
        long time = now(p);
        if (!before.ready(time)) return false;
        MadnessSurvivalState.set(p, before.activate(time));
        if (p.getHealth() < MadnessSurvival.HALF_HEART) p.setHealth(MadnessSurvival.HALF_HEART);
        dev.killerranking.KillerRankingSounds.play(p, dev.killerranking.KillerRankingSounds.MADNESS_SURVIVE, 1.0F, 1.0F);
        p.sendSystemMessage(Component.literal("§c미친 고씨 발동 · 15초간 체력 반 칸 유지 · 종료 후 구속Ⅰ·어둠 5초 · 재사용 300초"));
        return true;
    }
    public static void tick(MinecraftServer server) { for (var p : server.getPlayerList().getPlayers()) refresh(p); }
    public static int protectedTicks(ServerPlayer p) { return eligible(p) ? MadnessSurvivalState.get(p).protectedTicks(now(p)) : 0; }
    public static int cooldownTicks(ServerPlayer p) { return MadnessSurvivalState.get(p).cooldownTicks(now(p)); }
    /** Invoked only after armor, resistance and absorption have determined actual health damage. */
    public static float limitDamage(ServerPlayer p, DamageSource source, float damage) {
        if (source.is(DamageTypes.GENERIC_KILL) || !eligible(p) || damage <= 0) return damage;
        refresh(p);
        if (protectedTicks(p) > 0 || MadnessSurvival.crossesHalfHeart(p.getHealth(), damage) && activate(p))
            return Math.min(damage, MadnessSurvival.damageToHalfHeart(p.getHealth()));
        return damage;
    }
    private MadnessSurvivalService() { }
}
