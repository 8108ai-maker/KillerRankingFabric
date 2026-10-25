package dev.killerranking.mission;

import com.google.gson.Gson;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class MissionCatalog {
    public record Definition(String id, String category, String statId, String title, int goal, int reward, int divisor, String unit) {
        public long rewardUnits() { return dev.killerranking.data.KillerPoints.missionReward(reward); }
        public int read(ServerPlayer p) {
            return p.getStats().getValue(stat());
        }
        public net.minecraft.stats.Stat<?> stat() {
            var id = ResourceLocation.parse(statId);
            return switch (category) {
                case "kill" -> Stats.ENTITY_KILLED.get(BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElseThrow());
                case "mine" -> Stats.BLOCK_MINED.get(BuiltInRegistries.BLOCK.getOptional(id).orElseThrow());
                case "craft" -> Stats.ITEM_CRAFTED.get(BuiltInRegistries.ITEM.getOptional(id).orElseThrow());
                // The registry's reverse lookup uses object identity. An equal freshly parsed
                // ResourceLocation is not the registered value and crashes Stat construction.
                default -> Stats.CUSTOM.get(BuiltInRegistries.CUSTOM_STAT.getOptional(id).orElseThrow());
            };
        }
    }
    public static final List<Definition> ALL = load();
    public static Definition find(String id) { return ALL.stream().filter(d -> d.id.equals(id)).findFirst().orElse(null); }
    private static List<Definition> load() {
        try (var in = MissionCatalog.class.getResourceAsStream("/data/killerranking/missions.json")) {
            if (in == null) throw new IllegalStateException("Missing mission catalog");
            var result = List.of(new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), Definition[].class));
            if (result.size() < 10 || result.stream().map(Definition::id).distinct().count() != result.size()) throw new IllegalStateException("Invalid mission catalog");
            for (var d : result) if (d.goal <= 0 || d.reward <= 0 || d.divisor <= 0) throw new IllegalStateException("Invalid mission " + d.id);
            return result;
        } catch (java.io.IOException e) { throw new IllegalStateException(e); }
    }
    public static List<Definition> choose(long seed) {
        var random = new Random(seed);
        var result = new ArrayList<Definition>();
        for (String category : List.of("kill", "mine", "craft", "activity")) {
            var pool = new ArrayList<>(ALL.stream().filter(d -> d.category.equals(category)).toList());
            Collections.shuffle(pool, random);
            result.addAll(pool.subList(0, category.equals("activity") ? 1 : 3));
        }
        return List.copyOf(result);
    }
    private MissionCatalog() { }
}
