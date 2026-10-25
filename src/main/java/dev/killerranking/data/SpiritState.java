package dev.killerranking.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.SpiritBranch;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

public record SpiritState(SpiritBranch branch, boolean checkmate) {
    public SpiritState { checkmate = checkmate && branch == SpiritBranch.KING_AND_KILL; }
    public static final Codec<SpiritState> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.STRING.optionalFieldOf("branch", "none").forGetter(s -> s.branch.id()),
        Codec.BOOL.optionalFieldOf("checkmate", false).forGetter(SpiritState::checkmate)
    ).apply(i, (branch, skill) -> new SpiritState(SpiritBranch.fromId(branch), skill)));
    private static final class Storage {
        static final AttachmentType<SpiritState> TYPE = AttachmentRegistry.create(KillerRankingMod.id("spirit"), b -> b
            .initializer(() -> new SpiritState(SpiritBranch.NONE, false)).persistent(CODEC).copyOnDeath());
    }
    public static void initialize() { Storage.TYPE.identifier(); }
    public static SpiritState get(ServerPlayer p) { return p.getAttachedOrCreate(Storage.TYPE); }
    public static void set(ServerPlayer p, SpiritState s) { p.setAttached(Storage.TYPE, s); }
}
