package dev.killerranking.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.killerranking.KillerRankingMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class KillerRankingConfig {
	public int traitLevel2Xp = 300;
    public int traitLevel3Xp = 600;
	public int killingIntentBasePercent = 5;
	public int mixedMadnessBasePercent = 25;
    public int mixedMadnessAttackSpeedPercent = 5;
    public int combatBalanceRevision = 2;
	public int spiritHighHealthBasePercent = 5;
	public int spiritHalfHealthBasePercent = 10;
	public int spiritLowHealthBasePercent = 18;
	public int killingIntentWeight = 19;
	public int fightingSpiritWeight = 19;
	public int poisonAuraWeight = 19;
	public int mixedMadnessWeight = 3;
	public int parryCooldownTicks = 20;
	public int huntIntervalMinutes = 30;
	public int huntDurationMinutes = 15;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static KillerRankingConfig load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve("killerranking.json");
		try {
			if (Files.notExists(path)) {
				KillerRankingConfig config = new KillerRankingConfig();
				Files.writeString(path, GSON.toJson(config));
				return config;
			}
			var tree = com.google.gson.JsonParser.parseString(Files.readString(path));
            boolean migrated = tree.isJsonObject() && upgradeCombatBalance(tree.getAsJsonObject());
            KillerRankingConfig loaded = GSON.fromJson(tree, KillerRankingConfig.class);
			if (loaded == null) loaded = new KillerRankingConfig();
			// Add newly introduced defaults to existing config files for discoverability.
			if (tree.isJsonObject()) {
				var object = tree.getAsJsonObject();
				var defaults = GSON.toJsonTree(loaded).getAsJsonObject();
				boolean changed = migrated;
				for (var entry : defaults.entrySet()) {
					if (!object.has(entry.getKey())) { object.add(entry.getKey(), entry.getValue()); changed = true; }
				}
				if (changed) {
					try { Files.writeString(path, GSON.toJson(object)); }
					catch (IOException exception) { KillerRankingMod.LOGGER.warn("Using loaded config; could not write new default fields.", exception); }
				}
			}
			return loaded;
		} catch (IOException | RuntimeException exception) {
			KillerRankingMod.LOGGER.error("Could not load killerranking.json; using defaults.", exception);
			return new KillerRankingConfig();
		}
	}
    /** Upgrade shipped 18%/25% defaults to the progressive damage cap; retain custom settings. */
    public static boolean upgradeCombatBalance(com.google.gson.JsonObject object) {
        if (object.has("combatBalanceRevision") && object.get("combatBalanceRevision").getAsInt() >= 2) return false;
        if (!object.has("mixedMadnessBasePercent") || object.get("mixedMadnessBasePercent").getAsInt() == 18
            || object.get("mixedMadnessBasePercent").getAsInt() == 25)
            object.addProperty("mixedMadnessBasePercent", 25);
        object.addProperty("combatBalanceRevision", 2);
        return true;
    }
}
