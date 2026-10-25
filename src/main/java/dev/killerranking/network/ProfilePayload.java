package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.data.PlayerProfile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ProfilePayload(long pointUnits, String traitName, int zombieKills, boolean zombieMissionComplete) implements CustomPacketPayload {
	public static final Type<ProfilePayload> TYPE = new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(KillerRankingMod.MOD_ID, "profile_v2"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ProfilePayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_LONG, ProfilePayload::pointUnits,
		ByteBufCodecs.STRING_UTF8, ProfilePayload::traitName,
		ByteBufCodecs.VAR_INT, ProfilePayload::zombieKills,
		ByteBufCodecs.BOOL, ProfilePayload::zombieMissionComplete,
		ProfilePayload::new);

	public static ProfilePayload from(PlayerProfile profile) {
		return new ProfilePayload(profile.pointUnits(), profile.trait().koreanName(), profile.zombieKills(), profile.zombieMissionComplete());
	}

	@Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
