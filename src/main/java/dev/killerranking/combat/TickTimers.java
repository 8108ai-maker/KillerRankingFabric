package dev.killerranking.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TickTimers {
	private final Map<UUID, Integer> values = new HashMap<>();
	public int get(UUID id) { return values.getOrDefault(id, 0); }
	public void set(UUID id, int ticks) { if (ticks > 0) values.put(id, ticks); else remove(id); }
	public void remove(UUID id) { values.remove(id); }
    public void reduce(UUID id, int ticks) {
        if (ticks > 0) set(id, Math.max(0, get(id) - ticks));
    }
	public void clear() { values.clear(); }
	public void tick() {
		values.replaceAll((id, ticks) -> ticks - 1);
		values.entrySet().removeIf(entry -> entry.getValue() <= 0);
	}
}
