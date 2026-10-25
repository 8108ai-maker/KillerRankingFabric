package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.Trait;
import dev.killerranking.combat.*;
import dev.killerranking.data.*;
import dev.killerranking.mission.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class DashboardService {
    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 10 == 0) for (var p : server.getPlayerList().getPlayers()) sync(p);
    }
    public static void sync(ServerPlayer player) {
        KillerRankingMod.BISHOP.effects(player); InheritedWeaponEffects.refresh(player);
        PoisonEffects.refresh(player);
        MadnessEffects.refresh(player);
        KnightWeaponEffects.refresh(player);
        var profile = PlayerProfiles.get(player);
        var state = SpiritState.get(player);
        var branch = profile.trait() == Trait.FIGHTING_SPIRIT ? state.branch() : SpiritBranch.NONE;
        int level = profile.traitLevel();
        var effectBranch = TraitActivation.enabled(player) && player.isAlive() && !player.isSpectator() ? branch : SpiritBranch.NONE;
        double madness = MadnessEffects.damageBonus(player);
        var board = player.getAttachedOrCreate(RotatingMissions.TYPE);
        var missions = board.entries().stream().filter(e -> MissionCatalog.find(e.id()) != null).map(e -> {
            var d = MissionCatalog.find(e.id());
            return new DashboardData.Mission(d.title(), e.progress(), d.goal(), e.displayedReward(d), e.paid(), d.divisor(), d.unit());
        }).toList();
        var killing = player.getAttribute(Attributes.ATTACK_DAMAGE).getModifier(KillingIntentEffect.ID);
        int attackBonus = effectBranch.attack(level) + (killing == null ? 0 : (int) Math.round(killing.amount() * 100));
        boolean inheritedMadness = InheritedWeaponEffects.madnessBonus(player);
        boolean poison = TraitActivation.enabled(player) && profile.trait() == Trait.POISON_AURA && player.isAlive() && !player.isSpectator();
        int speedBonus = effectBranch.attackSpeed(level) + (inheritedMadness ? InheritedWeaponEffects.SPEED_BONUS : 0)
            + MadnessEffects.speed(player) + (poison ? PoisonEffects.speed(level) : 0);
        double morale = MoraleState.get(player);
        boolean knight = KnightWeaponEffects.striking(player);
        if (inheritedMadness) attackBonus += 30;
        var data = new DashboardData(profile.trait().id(), level, branch.id(), state.checkmate(), profile.pointUnits(),
            player.getHealth(), player.getMaxHealth(), player.getAttributeValue(Attributes.ATTACK_DAMAGE) * (1 + madness / 100.0)
                * (inheritedMadness ? InheritedWeaponEffects.DAMAGE_MULTIPLIER : 1) * morale * (knight ? 2 : 1),
            player.getAttributeValue(Attributes.ATTACK_SPEED), player.getAttributeValue(Attributes.MOVEMENT_SPEED),
            player.getAttributeValue(Attributes.ARMOR), player.getAttributeValue(Attributes.ARMOR_TOUGHNESS), SpiritEffects.defense(player),
            attackBonus, speedBonus, effectBranch.movement(level, state.checkmate()) + (poison ? PoisonEffects.speed(level) : 0), madness,
            board.remaining(player.server.overworld().getDayTime()), missions, profile.traitXp(), TraitExperienceService.required(level), player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), morale, knight,
            TraitLevels.bonusPercent(KillerRankingMod.CONFIG.mixedMadnessAttackSpeedPercent, level));
        ServerPlayNetworking.send(player, new DashboardPayload(DashboardData.JSON.toJson(data)));
    }
    private DashboardService() { }
}
