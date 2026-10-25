package dev.killerranking.data;

import com.mojang.serialization.Codec;
import dev.killerranking.KillerRankingMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

/** Existing players keep their abilities enabled until they explicitly switch them off. */
public final class TraitActivation {
    private static final class Storage {
        static final AttachmentType<Boolean> TYPE = AttachmentRegistry.create(KillerRankingMod.id("trait_enabled"),
            b -> b.initializer(() -> true).persistent(Codec.BOOL).copyOnDeath());
    }
    public static void initialize() { Storage.TYPE.identifier(); }
    public static boolean enabled(ServerPlayer player) { return player.getAttachedOrCreate(Storage.TYPE); }
    public static void set(ServerPlayer player, boolean enabled) { player.setAttached(Storage.TYPE, enabled); }
    private TraitActivation() { }
}
