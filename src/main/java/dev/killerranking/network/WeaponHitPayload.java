package dev.killerranking.network;

import dev.killerranking.KillerRankingMod;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * One confirmed melee contact with a Killer Ranking weapon. The client draws the
 * weapon family's pixel impact at the contact point; sound is played by the server.
 *
 * @param family index into {@link dev.killerranking.combat.WeaponFamily}
 * @param yaw    attacker body yaw in degrees, so slash marks lean with the swing
 * @param crit   vanilla-style falling critical
 * @param lethal the contact killed the target
 */
public record WeaponHitPayload(UUID attacker, int family, double x, double y, double z, float yaw, boolean crit, boolean lethal)
    implements CustomPacketPayload {
    public static final Type<WeaponHitPayload> TYPE = new Type<>(KillerRankingMod.id("weapon_hit"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponHitPayload> CODEC = StreamCodec.of(
        (b, p) -> { b.writeUUID(p.attacker()); b.writeVarInt(p.family()); b.writeDouble(p.x()); b.writeDouble(p.y()); b.writeDouble(p.z());
            b.writeFloat(p.yaw()); b.writeBoolean(p.crit()); b.writeBoolean(p.lethal()); },
        b -> new WeaponHitPayload(b.readUUID(), b.readVarInt(), b.readDouble(), b.readDouble(), b.readDouble(), b.readFloat(), b.readBoolean(), b.readBoolean()));
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
