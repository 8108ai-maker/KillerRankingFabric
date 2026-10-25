package dev.killerranking.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.KnifePursuitRules;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

public record KnifePursuitState(long readyAt) {
    public static final KnifePursuitState READY=new KnifePursuitState(0);
    public KnifePursuitState {readyAt=Math.max(0,readyAt);}
    public static final Codec<KnifePursuitState> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.LONG.optionalFieldOf("ready_at",0L).forGetter(KnifePursuitState::readyAt)).apply(i,KnifePursuitState::new));
    public int cooldown(long now){return (int)Math.clamp(readyAt-now,0,KnifePursuitRules.COOLDOWN);}
    public KnifePursuitState cast(long now){return cooldown(now)==0?new KnifePursuitState(now+KnifePursuitRules.COOLDOWN):this;}
    private static final class Storage {
        static final AttachmentType<KnifePursuitState> TYPE=AttachmentRegistry.create(KillerRankingMod.id("knife_pursuit"),
            b->b.initializer(()->READY).persistent(CODEC).copyOnDeath());
    }
    public static void initialize(){Storage.TYPE.identifier();}
    public static KnifePursuitState get(ServerPlayer p){return p.getAttachedOrCreate(Storage.TYPE);}
    public static void set(ServerPlayer p,KnifePursuitState value){p.setAttached(Storage.TYPE,value);}
}
