package dev.killerranking.data;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.CleaveRules;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

public record CleaveState(boolean unlocked,long readyAt) {
    public static final CleaveState LOCKED=new CleaveState(false,0);
    public CleaveState {readyAt=Math.max(0,readyAt);}
    public static final Codec<CleaveState> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.BOOL.optionalFieldOf("unlocked",false).forGetter(CleaveState::unlocked),
        Codec.LONG.optionalFieldOf("ready_at",0L).forGetter(CleaveState::readyAt)).apply(i,CleaveState::new));
    public int cooldown(long now){return (int)Math.clamp(readyAt-now,0,CleaveRules.COOLDOWN);}
    public CleaveState owned(boolean value){return new CleaveState(value,readyAt);}
    public CleaveState cast(long now){return unlocked&&cooldown(now)==0?new CleaveState(true,now+CleaveRules.COOLDOWN):this;}
    private static final class Storage {
        static final AttachmentType<CleaveState> TYPE=AttachmentRegistry.create(KillerRankingMod.id("cleave_skill"),
            b->b.initializer(()->LOCKED).persistent(CODEC).copyOnDeath());
    }
    public static void initialize(){Storage.TYPE.identifier();}
    public static CleaveState get(ServerPlayer p){return p.getAttachedOrCreate(Storage.TYPE);}
    public static void set(ServerPlayer p,CleaveState s){p.setAttached(Storage.TYPE,s);}
}
