package dev.killerranking.combat;

public enum SpiritBranch {
    NONE("none", "분기 미선택"), TTEOKBAL("tteokbal", "떡발의 투기"), KING_AND_KILL("king_and_kill", "킹앤킬의 투기");
    private final String id, label;
    SpiritBranch(String id, String label) { this.id = id; this.label = label; }
    public String id() { return id; }
    public String label() { return label; }
    public static SpiritBranch fromId(String id) {
        for (var value : values()) if (value.id.equals(id)) return value;
        return NONE;
    }
    public int attack(int level) { return this == NONE ? 0 : 5 * TraitLevels.clamp(level); }
    public int defense(int level) { return (this == TTEOKBAL ? 5 : this == KING_AND_KILL ? 20 : 0) * TraitLevels.clamp(level); }
    public int attackSpeed(int level) { return this == TTEOKBAL ? 10 * TraitLevels.clamp(level) : 0; }
    public int movement(int level, boolean checkmate) {
        return this == TTEOKBAL ? 10 * TraitLevels.clamp(level) : this == KING_AND_KILL ? (checkmate ? 2 * TraitLevels.clamp(level) : -2) : 0;
    }
}
