package dev.killerranking.combat;

import java.util.*;

/** Each hunter owns one temporary target. Repeated strikes renew expiry without postponing bleeding. */
public final class HuntingMarks {
    public record Mark(UUID target, String dimension, long expires, long nextBleed) { }
    private final Map<UUID, Mark> marks = new HashMap<>();
    public void set(UUID hunter, UUID target, String dimension, long now, int ticks) {
        if (hunter.equals(target) || ticks <= 0) return;
        Mark old = get(hunter, now);
        long next = old != null && old.target().equals(target) && old.dimension().equals(dimension) ? old.nextBleed() : now + 20;
        marks.put(hunter, new Mark(target, dimension, now + ticks, next));
    }
    public Mark get(UUID hunter, long now) {
        var mark = marks.get(hunter);
        return mark != null && mark.expires() > now ? mark : null;
    }
    public Mark due(UUID hunter, long now) {
        Mark mark = marks.get(hunter);
        return mark != null && mark.nextBleed() <= now && mark.expires() >= now ? mark : null;
    }
    public void advance(UUID hunter, long now) {
        Mark m = marks.get(hunter);
        if (m != null) marks.put(hunter, new Mark(m.target(), m.dimension(), m.expires(), now + 20));
    }
    public Set<UUID> owners() { return Set.copyOf(marks.keySet()); }
    public void remove(UUID hunter) { marks.remove(hunter); }
    public void clear() { marks.clear(); }
}
