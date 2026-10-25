package dev.killerranking.data;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.RookRules;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

public record RookState(boolean unlocked,long readyAt) {
    public static final RookState LOCKED=new RookState(false,0);
    public RookState { readyAt=Math.max(0,readyAt); }
    public static final Codec<RookState> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.BOOL.optionalFieldOf("unlocked",false).forGetter(RookState::unlocked),
        Codec.LONG.optionalFieldOf("ready_at",0L).forGetter(RookState::readyAt)).apply(i,RookState::new));
    public int cooldown(long now) { return (int)Math.clamp(readyAt-now,0,RookRules.COOLDOWN); }
    public RookState owned(boolean value) { return new RookState(value,readyAt); }
    public RookState cast(long now) { return unlocked&&cooldown(now)==0?new RookState(true,now+RookRules.COOLDOWN):this; }
    private static final class Storage {
        static final AttachmentType<RookState> TYPE=AttachmentRegistry.create(KillerRankingMod.id("rook_grab_skill"),b->b.initializer(()->LOCKED).persistent(CODEC).copyOnDeath());
    }
    public static void initialize() { Storage.TYPE.identifier(); }
    public static RookState get(ServerPlayer p) { return p.getAttachedOrCreate(Storage.TYPE); }
    public static void set(ServerPlayer p,RookState state) { p.setAttached(Storage.TYPE,state); }
}
