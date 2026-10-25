package dev.killerranking.data;

import com.mojang.serialization.Codec;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.MoraleRules;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

public final class MoraleState {
    public static final Codec<Double> CODEC = Codec.DOUBLE.xmap(MoraleRules::clamp, MoraleRules::clamp);
    private static final class Storage {
        static final AttachmentType<Double> TYPE = AttachmentRegistry.create(KillerRankingMod.id("morale"),
            b -> b.initializer(() -> MoraleRules.DEFAULT).persistent(CODEC).copyOnDeath());
    }
    public static void initialize() { Storage.TYPE.identifier(); }
    public static double get(ServerPlayer player) { return player.getAttachedOrCreate(Storage.TYPE); }
    public static void set(ServerPlayer player, double value) { player.setAttached(Storage.TYPE, MoraleRules.clamp(value)); }
    private MoraleState() { }
}
