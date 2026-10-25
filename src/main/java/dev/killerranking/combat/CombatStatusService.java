package dev.killerranking.combat;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.Trait;
import dev.killerranking.data.PlayerProfiles;
import dev.killerranking.network.CombatStatusPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CombatStatusService {
	private final TickTimers killingIntent = new TickTimers();
	private final Map<UUID, CombatStatusPayload> lastSent = new HashMap<>();
	public boolean killingIntentActive(ServerPlayer player) { return killingIntent.get(player.getUUID()) > 0; }
	public void clearKillingIntent(ServerPlayer player) { killingIntent.remove(player.getUUID()); }
	public void onKillingIntentHit(ServerPlayer player) {
        if (!dev.killerranking.data.TraitActivation.enabled(player)) return;
		killingIntent.set(player.getUUID(), CombatRules.KILLING_INTENT_TICKS);
		sync(player);
	}
	public void tick(MinecraftServer server) {
		killingIntent.tick();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			refreshModifier(player);
			if (server.getTickCount() % 2 == 0) sync(player);
		}
	}
	public void sync(ServerPlayer player) {
		refreshModifier(player);
		var profile = PlayerProfiles.get(player);
		Trait trait = profile.trait();
		int level = profile.traitLevel();
		var config = KillerRankingMod.CONFIG;
		float ratio = CombatRules.healthRatio(player.getHealth(), player.getMaxHealth());
		int ticks = killingIntent.get(player.getUUID());
		float gauge = 0;
		boolean active = false;
		int power = 0;
		int duration = trait == Trait.POISON_AURA ? TraitLevels.poisonTicks(level) : CombatRules.KILLING_INTENT_TICKS;
		int high = TraitLevels.defensePercent(config.spiritHighHealthBasePercent, level);
		int half = TraitLevels.defensePercent(config.spiritHalfHealthBasePercent, level);
		int low = TraitLevels.defensePercent(config.spiritLowHealthBasePercent, level);
		if (player.isAlive() && !player.isSpectator() && dev.killerranking.data.TraitActivation.enabled(player)) {
			switch (trait) {
				case KILLING_INTENT -> { gauge = (float) ticks / duration; active = ticks > 0; power = TraitLevels.bonusPercent(config.killingIntentBasePercent, level); }
				case FIGHTING_SPIRIT -> { power = SpiritEffects.defense(player); gauge = dev.killerranking.data.SpiritState.get(player).branch() == SpiritBranch.NONE ? (float) power / Math.max(1, CombatRules.maxSpiritPercent(level, config)) : 1; active = power > 0; }
				case MIXED_MADNESS -> { gauge = CombatRules.madnessProgress(ratio); active = gauge > 0; power = TraitLevels.bonusPercent(config.mixedMadnessBasePercent, level); }
				case POISON_AURA -> { gauge = 1; active = true; }
				default -> { }
			}
		}
		CombatStatusPayload payload = new CombatStatusPayload(trait.id(), gauge, active, ticks, power,
			KillerRankingMod.PARRY.cooldown(player), Math.max(0, config.parryCooldownTicks), KillerRankingMod.PARRY.window(player),
			level, duration, high, half, low, MadnessSurvivalService.protectedTicks(player), MadnessSurvivalService.cooldownTicks(player));
		if (!payload.equals(lastSent.get(player.getUUID()))) {
			ServerPlayNetworking.send(player, payload);
			lastSent.put(player.getUUID(), payload);
		}
	}
	private void refreshModifier(ServerPlayer player) {
		SpiritEffects.refresh(player);
        PoisonEffects.refresh(player);
        MadnessEffects.refresh(player);
        KnightWeaponEffects.refresh(player);
        InheritedWeaponEffects.refresh(player);
		var profile = PlayerProfiles.get(player);
		if (!player.isAlive() || player.isSpectator() || !dev.killerranking.data.TraitActivation.enabled(player) || profile.trait() != Trait.KILLING_INTENT) killingIntent.remove(player.getUUID());
		int percent = killingIntent.get(player.getUUID()) > 0
			? TraitLevels.bonusPercent(KillerRankingMod.CONFIG.killingIntentBasePercent, profile.traitLevel()) : 0;
		KillingIntentEffect.apply(player.getAttribute(Attributes.ATTACK_DAMAGE), percent);
	}
	public void forget(UUID id) { killingIntent.remove(id); lastSent.remove(id); }
	public void clear() { killingIntent.clear(); lastSent.clear(); }
}
