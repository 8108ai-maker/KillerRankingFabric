package dev.killerranking.combat;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.data.CheckmateState;
import dev.killerranking.data.TraitActivation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class KnightWeaponEffects {
    public static boolean owner(ServerPlayer p) {
        return p.isAlive() && !p.isSpectator() && TraitActivation.enabled(p)
            && CheckmateService.branch(p) && CheckmateState.get(p).unlocked();
    }
    public static boolean holding(ServerPlayer p) {
        return owner(p) && (p.getMainHandItem().is(KillerRankingMod.NIGHT_BARBED_BAT)
            || p.getOffhandItem().is(KillerRankingMod.NIGHT_BARBED_BAT));
    }
    public static boolean striking(ServerPlayer p) {
        return owner(p) && p.getMainHandItem().is(KillerRankingMod.NIGHT_BARBED_BAT);
    }
    public static void refresh(ServerPlayer p) {
        if (!holding(p)) return;
        // Two-tick leases expire promptly on unequip, while stronger/longer potion effects keep vanilla priority.
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2, 0, true, false, true));
        p.addEffect(new MobEffectInstance(MobEffects.JUMP, 2, 0, true, false, true));
    }
    private KnightWeaponEffects() { }
}
