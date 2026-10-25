package dev.killerranking.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.MadnessLeapRules;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

public record MadnessLeapState(long readyAt) {
    public static final MadnessLeapState READY=new MadnessLeapState(0);
    public MadnessLeapState {readyAt=Math.max(0,readyAt);}
    public static final Codec<MadnessLeapState> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.LONG.optionalFieldOf("ready_at",0L).forGetter(MadnessLeapState::readyAt)).apply(i,MadnessLeapState::new));
    public int cooldown(long now){return (int)Math.clamp(readyAt-now,0,MadnessLeapRules.COOLDOWN);}
    public MadnessLeapState cast(long now){return cooldown(now)==0?new MadnessLeapState(now+MadnessLeapRules.COOLDOWN):this;}
    private static final class Storage {
        static final AttachmentType<MadnessLeapState> TYPE=AttachmentRegistry.create(KillerRankingMod.id("madness_leap"),
            b->b.initializer(()->READY).persistent(CODEC).copyOnDeath());
    }
    public static void initialize(){Storage.TYPE.identifier();}
    public static MadnessLeapState get(ServerPlayer p){return p.getAttachedOrCreate(Storage.TYPE);}
    public static void set(ServerPlayer p,MadnessLeapState value){p.setAttached(Storage.TYPE,value);}
}
