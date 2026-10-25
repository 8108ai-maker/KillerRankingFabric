package dev.killerranking;

public enum Trait {
	UNASSIGNED("unassigned", "미배정"),
	KILLING_INTENT("killing_intent", "살기"),
	FIGHTING_SPIRIT("fighting_spirit", "투기"),
	POISON_AURA("poison_aura", "독기"),
	MIXED_MADNESS("mixed_madness", "살기 섞인 광기");

	private final String id;
	private final String koreanName;

	Trait(String id, String koreanName) {
		this.id = id;
		this.koreanName = koreanName;
	}

	public String id() { return id; }
	public String koreanName() { return koreanName; }

	public static Trait fromId(String id) {
		for (Trait trait : values()) if (trait.id.equals(id)) return trait;
		return UNASSIGNED;
	}
}
