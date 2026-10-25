package dev.killerranking.combat;

import java.util.*;

/** One target per caster; several casters may designate the same victim independently. */
public final class BishopDesignations {
    public record Mark(UUID target, String dimension) { }
    private final Map<UUID, Mark> marks = new HashMap<>();
    public Mark get(UUID owner) { return marks.get(owner); }
    public Mark set(UUID owner, UUID target, String dimension) {
        return marks.put(owner, new Mark(target, dimension));
    }
    public Mark remove(UUID owner) { return marks.remove(owner); }
    public boolean used(Mark mark) { return mark != null && marks.containsValue(mark); }
    public Set<UUID> owners() { return Set.copyOf(marks.keySet()); }
    public void clear() { marks.clear(); }
}
