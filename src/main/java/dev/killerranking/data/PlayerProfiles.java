package dev.killerranking.data;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.Trait;
import dev.killerranking.config.KillerRankingConfig;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.ThreadLocalRandom;

public final class PlayerProfiles {
	public static final AttachmentType<PlayerProfile> PROFILE = AttachmentRegistry.create(KillerRankingMod.id("profile"), builder -> builder
		.initializer(PlayerProfile::fresh)
		.persistent(PlayerProfile.CODEC)
		.copyOnDeath());

	private PlayerProfiles() { }

	public static PlayerProfile get(ServerPlayer player) { return player.getAttachedOrCreate(PROFILE); }

	public static PlayerProfile ensureTrait(ServerPlayer player, KillerRankingConfig config) {
		PlayerProfile profile = get(player);
		if (profile.trait() != Trait.UNASSIGNED) return profile;
		PlayerProfile assigned = profile.withTrait(chooseTrait(config));
		player.setAttached(PROFILE, assigned);
		return assigned;
	}

	public static void set(ServerPlayer player, PlayerProfile profile) { player.setAttached(PROFILE, profile); }
    public static PlayerProfile reroll(ServerPlayer player, KillerRankingConfig config) {
        var profile = get(player).withTrait(chooseTrait(config));
        set(player, profile);
        return profile;
    }

	private static Trait chooseTrait(KillerRankingConfig config) {
		int killing = Math.max(0, config.killingIntentWeight);
		int spirit = Math.max(0, config.fightingSpiritWeight);
		int poison = Math.max(0, config.poisonAuraWeight);
		int madness = Math.max(0, config.mixedMadnessWeight);
		int total = killing + spirit + poison + madness;
		if (total == 0) return Trait.KILLING_INTENT;
		int roll = ThreadLocalRandom.current().nextInt(total);
		if ((roll -= killing) < 0) return Trait.KILLING_INTENT;
		if ((roll -= spirit) < 0) return Trait.FIGHTING_SPIRIT;
		if ((roll -= poison) < 0) return Trait.POISON_AURA;
		return Trait.MIXED_MADNESS;
	}
}
