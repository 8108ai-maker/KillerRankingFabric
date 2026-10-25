package dev.killerranking.combat;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.Trait;
import dev.killerranking.data.PlayerProfiles;
import dev.killerranking.network.ParryEffectPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public final class ParryService {
	private final TickTimers activeWindows = new TickTimers();
	private final TickTimers cooldowns = new TickTimers();
    private final ParryProtection protection = new ParryProtection();
    private final Map<UUID, Boolean> nextKatanaSides = new HashMap<>();
    private final AutomaticParryState automatic = new AutomaticParryState();
    private final Map<UUID, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>> automaticDimensions = new HashMap<>();

    private static boolean automaticEligible(ServerPlayer player) {
        return CleaveService.branch(player) && dev.killerranking.data.CleaveState.get(player).unlocked()
            && dev.killerranking.data.TraitActivation.enabled(player) && player.isAlive() && !player.isSpectator()
            && player.getMainHandItem().is(KillerRankingMod.PAWN6_KATANA) && !player.isUsingItem()
            && !KillerRankingMod.CLEAVE.active(player) && !KillerRankingMod.ROOK.active(player)
            && !KillerRankingMod.ROOK.locked(player) && !KillerRankingMod.RUSH.active(player) && !KillerRankingMod.CHECKMATE.active(player);
    }
    public boolean automatic(ServerPlayer player) {
        return automatic.active(player.getUUID(), player.server.overworld().getGameTime()) && automaticEligible(player)
            && player.level().dimension().equals(automaticDimensions.get(player.getUUID()));
    }
    public void holdAutomatic(ServerPlayer player, boolean held) {
        if (!held) { stopAutomatic(player); return; }
        if (!automaticEligible(player) || activeWindows.get(player.getUUID()) > 0) { stopAutomatic(player); return; }
        if (automatic.contains(player.getUUID()) && !player.level().dimension().equals(automaticDimensions.get(player.getUUID()))) {
            stopAutomatic(player); return;
        }
        boolean started = automatic.hold(player.getUUID(), player.server.overworld().getGameTime());
        automaticDimensions.put(player.getUUID(), player.level().dimension());
        if (started) { broadcastAutomatic(player); KillerRankingMod.COMBAT_STATUS.sync(player); }
    }
    public void stopAutomatic(ServerPlayer player) {
        if (!automatic.contains(player.getUUID())) return;
        automatic.release(player.getUUID()); automaticDimensions.remove(player.getUUID());
        broadcastAutomatic(player); KillerRankingMod.COMBAT_STATUS.sync(player);
    }
    public void refreshAutomatic(ServerPlayer player) {
        if (automatic.contains(player.getUUID()) && !automatic(player)) stopAutomatic(player);
    }
    public void sendAutomatic(ServerPlayer player, ServerPlayer viewer) {
        if (ServerPlayNetworking.canSend(viewer, dev.killerranking.network.AutoParryStatusPayload.TYPE))
            ServerPlayNetworking.send(viewer, new dev.killerranking.network.AutoParryStatusPayload(player.getUUID(), automatic(player)));
    }
    private void broadcastAutomatic(ServerPlayer player) {
        for (ServerPlayer viewer : player.serverLevel().players()) if (viewer.distanceToSqr(player) <= 4096) sendAutomatic(player, viewer);
    }

	public void tryStart(ServerPlayer player) {
		if (automatic(player)||KillerRankingMod.NEUNGSA_SKILL.active(player)) return;
		if (KillerRankingMod.MADNESS_LEAP.active(player) || KillerRankingMod.CLEAVE.active(player) || KillerRankingMod.ROOK.locked(player) || KillerRankingMod.ROOK.active(player) || KillerRankingMod.RUSH.active(player) || KillerRankingMod.CHECKMATE.active(player)) return;
		if (!player.isAlive() || player.isSpectator() || cooldowns.get(player.getUUID()) > 0 || activeWindows.get(player.getUUID()) > 0) return;
		var profile = PlayerProfiles.get(player);
		int window = dev.killerranking.data.TraitActivation.enabled(player) && profile.trait() == Trait.FIGHTING_SPIRIT ? TraitLevels.parryWindow(profile.traitLevel()) : 5;
		activeWindows.set(player.getUUID(), window);
		cooldowns.set(player.getUUID(), KillerRankingMod.CONFIG.parryCooldownTicks);
		boolean reversed = nextKatanaSides.getOrDefault(player.getUUID(), false);
		nextKatanaSides.put(player.getUUID(), !reversed);
		// Begin the defensive motion when the server accepts the key press.
		dev.killerranking.KillerRankingSounds.play(player, dev.killerranking.KillerRankingSounds.PARRY_GUARD, 0.6F, dev.killerranking.KillerRankingSounds.vary(player, 1.0F, 0.05F));
		for (ServerPlayer listener : player.serverLevel().players()) {
			if (listener.distanceToSqr(player) <= 4096) ServerPlayNetworking.send(listener, new ParryEffectPayload(player.getUUID(), reversed));
		}
		KillerRankingMod.COMBAT_STATUS.sync(player);
	}

	public boolean tryParry(ServerPlayer defender, LivingEntity attacker) {
        return tryParryHit(defender,attacker,false);
    }
    /** A thrown dagger is checked at contact; its thrower can be farther than melee reach. */
    public boolean tryParryProjectile(ServerPlayer defender,LivingEntity attacker){
        return tryParryHit(defender,attacker,true);
    }
    private boolean tryParryHit(ServerPlayer defender,LivingEntity attacker,boolean projectile){
        boolean auto = automatic(defender);
        boolean iaido=KillerRankingMod.NEUNGSA_SKILL.iaidoActive(defender);
		if (KillerRankingMod.ROOK.locked(defender) || !defender.isAlive() || defender.isSpectator() || (!auto&&!iaido && activeWindows.get(defender.getUUID()) <= 0) || !projectile&&defender.distanceToSqr(attacker) > 20.25D) return false;
		activeWindows.remove(defender.getUUID());
        if (auto) cooldowns.remove(defender.getUUID());
        else cooldowns.reduce(defender.getUUID(), 10); // Ordinary success refunds 0.5 seconds.
        protection.onSuccess(defender.getUUID(), defender.server.overworld().getGameTime());
		if(!projectile)attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 4));
        if (!auto&&!projectile) attacker.knockback(0.8D, defender.getX() - attacker.getX(), defender.getZ() - attacker.getZ());
        if (auto) {
            var strike = automatic.strike(defender.getUUID(), defender.server.overworld().getGameTime());
            boolean reversed = nextKatanaSides.getOrDefault(defender.getUUID(), false);
            nextKatanaSides.put(defender.getUUID(), !reversed);
            var payload = new dev.killerranking.network.AutoParryEffectPayload(defender.getUUID(), reversed, strike.echo(), strike.sequence(),
                defender.getX(), defender.getY(), defender.getZ(), defender.getYRot(), defender.getXRot());
            for (ServerPlayer viewer : defender.serverLevel().players()) if (viewer.distanceToSqr(defender) <= 4096
                && ServerPlayNetworking.canSend(viewer, dev.killerranking.network.AutoParryEffectPayload.TYPE)) ServerPlayNetworking.send(viewer, payload);
        }
        var direction=attacker.position().subtract(defender.position()).multiply(1,0,1).normalize();
        if(direction.lengthSqr()<.01)direction=defender.getLookAngle().multiply(1,0,1).normalize();
        var contact=defender.position().add(0,defender.getEyeHeight()*.90,0).add(direction.scale(.9));
        var impact=new dev.killerranking.network.ParryImpactPayload(defender.getUUID(),contact.x,contact.y,contact.z);
		for(ServerPlayer listener:defender.serverLevel().players())if(listener.distanceToSqr(defender)<=4096
            &&ServerPlayNetworking.canSend(listener,dev.killerranking.network.ParryImpactPayload.TYPE))ServerPlayNetworking.send(listener,impact);
		dev.killerranking.KillerRankingSounds.play(defender.serverLevel(), contact,
			auto ? dev.killerranking.KillerRankingSounds.PARRY_AUTO : dev.killerranking.KillerRankingSounds.PARRY_SUCCESS,
			1.0F, dev.killerranking.KillerRankingSounds.vary(defender, 1.0F, 0.04F));
		defender.displayClientMessage(Component.literal("§b패링 성공! 0.3초 무적"), true);
		KillerRankingMod.COMBAT_STATUS.sync(defender);
		return true;
	}

	public void tick(MinecraftServer server) {
		activeWindows.tick();
		cooldowns.tick();
        protection.expire(server.overworld().getGameTime());
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            refreshAutomatic(player);
            if (automatic(player) && server.getTickCount() % 5 == 0) broadcastAutomatic(player);
			if (!player.isAlive() || player.isSpectator()) forget(player.getUUID());
		}
	}

	public int cooldown(ServerPlayer player) { return cooldowns.get(player.getUUID()); }
    public void resetCooldown(ServerPlayer player) {
        cooldowns.remove(player.getUUID());
        KillerRankingMod.COMBAT_STATUS.sync(player);
    }
	public int window(ServerPlayer player) { return Math.max(KillerRankingMod.NEUNGSA_SKILL.iaidoRemaining(player),automatic(player) ? 1 : activeWindows.get(player.getUUID())); }
    public boolean isProtected(ServerPlayer player) {
        return player.isAlive() && !player.isSpectator() && protection.active(player.getUUID(), player.server.overworld().getGameTime());
    }
	public void forget(UUID id) { activeWindows.remove(id); cooldowns.remove(id); protection.forget(id); nextKatanaSides.remove(id); automatic.release(id); automaticDimensions.remove(id); }
	public void clear() { activeWindows.clear(); cooldowns.clear(); protection.clear(); nextKatanaSides.clear(); automatic.clear(); automaticDimensions.clear(); }
}
