package dev.killerranking.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.ShoulderStabRules;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

public record ShoulderStabState(long readyAt) {
    public static final ShoulderStabState READY=new ShoulderStabState(0);
    public ShoulderStabState {readyAt=Math.max(0,readyAt);}
    public static final Codec<ShoulderStabState> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.LONG.optionalFieldOf("ready_at",0L).forGetter(ShoulderStabState::readyAt)).apply(i,ShoulderStabState::new));
    public int cooldown(long now){return (int)Math.clamp(readyAt-now,0,ShoulderStabRules.COOLDOWN);}
    public ShoulderStabState cast(long now){return cooldown(now)==0?new ShoulderStabState(now+ShoulderStabRules.COOLDOWN):this;}
    private static final class Storage {
        static final AttachmentType<ShoulderStabState> TYPE=AttachmentRegistry.create(KillerRankingMod.id("shoulder_stab"),
            b->b.initializer(()->READY).persistent(CODEC).copyOnDeath());
    }
    public static void initialize(){Storage.TYPE.identifier();}
    public static ShoulderStabState get(ServerPlayer p){return p.getAttachedOrCreate(Storage.TYPE);}
    public static void set(ServerPlayer p,ShoulderStabState value){p.setAttached(Storage.TYPE,value);}
}
