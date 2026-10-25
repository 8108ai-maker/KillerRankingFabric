package dev.killerranking.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.RushRules;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

public record RushState(boolean unlocked,long readyAt) {
    public static final RushState LOCKED=new RushState(false,0);
    public RushState { readyAt=Math.max(0,readyAt); }
    public static final Codec<RushState> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.BOOL.optionalFieldOf("unlocked",false).forGetter(RushState::unlocked),
        Codec.LONG.optionalFieldOf("ready_at",0L).forGetter(RushState::readyAt)
    ).apply(i,RushState::new));
    public int cooldown(long now) { return (int)Math.clamp(readyAt-now,0,RushRules.COOLDOWN); }
    public RushState unlock() { return unlocked?this:new RushState(true,readyAt); }
    public RushState cast(long now) { return unlocked && cooldown(now)==0?new RushState(true,now+RushRules.COOLDOWN):this; }
    private static final class Storage {
        static final AttachmentType<RushState> TYPE=AttachmentRegistry.create(KillerRankingMod.id("rush_skill"),
            b->b.initializer(()->LOCKED).persistent(CODEC).copyOnDeath());
    }
    public static void initialize() { Storage.TYPE.identifier(); }
    public static RushState get(ServerPlayer p) { return p.getAttachedOrCreate(Storage.TYPE); }
    public static void set(ServerPlayer p,RushState state) { p.setAttached(Storage.TYPE,state); }
}
