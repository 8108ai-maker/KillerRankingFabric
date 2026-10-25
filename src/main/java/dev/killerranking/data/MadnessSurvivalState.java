package dev.killerranking.data;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.MadnessSurvival;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

public final class MadnessSurvivalState {
    private static final class Storage {
        static final AttachmentType<MadnessSurvival> TYPE = AttachmentRegistry.create(KillerRankingMod.id("madness_survival"),
            b -> b.initializer(() -> MadnessSurvival.READY).persistent(MadnessSurvival.CODEC).copyOnDeath());
    }
    public static void initialize() { Storage.TYPE.identifier(); }
    public static MadnessSurvival get(ServerPlayer p) { return p.getAttachedOrCreate(Storage.TYPE); }
    public static void set(ServerPlayer p, MadnessSurvival value) { p.setAttached(Storage.TYPE, value); }
    private MadnessSurvivalState() { }
}
