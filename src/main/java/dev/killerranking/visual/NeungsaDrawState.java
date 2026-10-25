package dev.killerranking.visual;

/** A combo has one draw and one insertion, regardless of its number of cuts. */
public final class NeungsaDrawState {
    public static final int CHAIN_GRACE = 6;
    public static final int SHEATHE_TICKS = 12;
    public enum Phase { SHEATHED, DRAWN, SHEATHING }
    private Phase phase = Phase.SHEATHED;
    private long attackEnd, insertEnd;

    public boolean attack(long now, float length) {
        boolean first = phase == Phase.SHEATHED;
        phase = Phase.DRAWN;
        attackEnd = now + Math.max(1, (int)Math.ceil(length));
        return first;
    }
    public Phase tick(long now, boolean continuing) {
        if (phase == Phase.DRAWN && now >= attackEnd + CHAIN_GRACE && !continuing) {
            phase = Phase.SHEATHING;
            insertEnd = now + SHEATHE_TICKS;
        } else if (phase == Phase.SHEATHING && now >= insertEnd) phase = Phase.SHEATHED;
        return phase;
    }
    public Phase phase() { return phase; }
    public void reset() { phase = Phase.SHEATHED; }
}
