package dev.killerranking.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.CheckmateRules;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

public record CheckmateState(boolean unlocked,long readyAt) {
    public static final CheckmateState LOCKED=new CheckmateState(false,0);
    public CheckmateState { readyAt=Math.max(0,readyAt); }
    public static final Codec<CheckmateState> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.BOOL.optionalFieldOf("unlocked",false).forGetter(CheckmateState::unlocked),
        Codec.LONG.optionalFieldOf("ready_at",0L).forGetter(CheckmateState::readyAt)
    ).apply(i,CheckmateState::new));
    public int cooldown(long now) { return (int)Math.clamp(readyAt-now,0,CheckmateRules.COOLDOWN); }
    public CheckmateState owned(boolean owned) { return new CheckmateState(owned,readyAt); }
    public CheckmateState cast(long now) { return unlocked&&cooldown(now)==0?new CheckmateState(true,now+CheckmateRules.COOLDOWN):this; }
    private static final class Storage {
        static final AttachmentType<CheckmateState> TYPE=AttachmentRegistry.create(KillerRankingMod.id("checkmate_skill"),
            b->b.initializer(()->LOCKED).persistent(CODEC).copyOnDeath());
    }
    public static void initialize() { Storage.TYPE.identifier(); }
    public static CheckmateState get(ServerPlayer p) { return p.getAttachedOrCreate(Storage.TYPE); }
    public static void set(ServerPlayer p,CheckmateState state) { p.setAttached(Storage.TYPE,state); }
}
