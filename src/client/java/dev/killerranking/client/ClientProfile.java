package dev.killerranking.client;

import dev.killerranking.network.ProfilePayload;

public final class ClientProfile {
	private static ProfilePayload value = new ProfilePayload(0, "동기화 대기", 0, false);
	private ClientProfile() { }
	public static ProfilePayload get() { return value; }
	public static void set(ProfilePayload payload) { value = payload; }
	public static void reset() { value = new ProfilePayload(0, "동기화 대기", 0, false); }
}
