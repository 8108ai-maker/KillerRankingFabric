package dev.killerranking.mission;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record MissionBoard(long cycle, List<Entry> entries) {
    public static final long PERIOD = 72000L;
    public MissionBoard { entries = List.copyOf(entries); }
    public record Entry(String id, int lastValue, int progress, boolean paid, long paidRewardUnits) {
        public Entry(String id, int lastValue, int progress, boolean paid) { this(id,lastValue,progress,paid,-1); }
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("id").forGetter(Entry::id), Codec.INT.fieldOf("lastValue").forGetter(Entry::lastValue),
            Codec.INT.fieldOf("progress").forGetter(Entry::progress), Codec.BOOL.fieldOf("paid").forGetter(Entry::paid),
            Codec.LONG.optionalFieldOf("paidRewardUnits", -1L).forGetter(Entry::paidRewardUnits)
        ).apply(i, Entry::new));
        public Entry advance(int current, int goal, boolean eligible) {
            // Persist a moving baseline, so /stats resets and creative play cannot backfill progress.
            long delta = eligible && !paid ? Math.max(0L, (long) current - lastValue) : 0;
            int next = (int) Math.min(goal, progress + delta);
            return new Entry(id, current, next, paid || (eligible && next >= goal), paidRewardUnits);
        }
        public Entry withPaidReward(long units) { return new Entry(id,lastValue,progress,paid,units); }
        public long displayedReward(MissionCatalog.Definition definition) {
            // Pre-0.11.1 completed missions already paid whole points; preserve that history.
            return !paid ? definition.rewardUnits() : paidRewardUnits >= 0 ? paidRewardUnits : dev.killerranking.data.KillerPoints.whole(definition.reward());
        }
    }
    public static final Codec<MissionBoard> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.LONG.fieldOf("cycle").forGetter(MissionBoard::cycle), Entry.CODEC.listOf().fieldOf("entries").forGetter(MissionBoard::entries)
    ).apply(i, MissionBoard::new));
    public static long cycleAt(long dayTime) { return Math.max(0, Math.floorDiv(dayTime, PERIOD)); }
    public boolean needsRotation(long dayTime) { return entries.isEmpty() || cycleAt(dayTime) > cycle; }
    public long remaining(long dayTime) { return Math.max(0, (cycle + 1) * PERIOD - dayTime); }
}
