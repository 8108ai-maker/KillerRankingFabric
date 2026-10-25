package dev.killerranking.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Uses the server's world clock so the success tick cannot shorten the six-tick window. */
public final class ParryProtection {
    public static final int DURATION_TICKS = 6;
    private final Map<UUID, Long> deadlines = new HashMap<>();
    public void onSuccess(UUID player, long now) { deadlines.put(player, now + DURATION_TICKS); }
    public boolean active(UUID player, long now) { return now < deadlines.getOrDefault(player, Long.MIN_VALUE); }
    public void expire(long now) { deadlines.entrySet().removeIf(entry -> now >= entry.getValue()); }
    public void forget(UUID player) { deadlines.remove(player); }
    public void clear() { deadlines.clear(); }
}
