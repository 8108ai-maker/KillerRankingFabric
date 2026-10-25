package dev.killerranking.data;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.BishopRules;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.server.level.ServerPlayer;

public record BishopState(boolean unlocked,long readyAt,long barrageReadyAt) {
    public static final BishopState LOCKED=new BishopState(false,0);
    public BishopState(boolean unlocked,long readyAt){this(unlocked,readyAt,0);}
    public BishopState {readyAt=Math.max(0,readyAt);barrageReadyAt=Math.max(0,barrageReadyAt);}
    public static final Codec<BishopState> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.BOOL.optionalFieldOf("unlocked",false).forGetter(BishopState::unlocked),
        Codec.LONG.optionalFieldOf("ready_at",0L).forGetter(BishopState::readyAt),
        Codec.LONG.optionalFieldOf("barrage_ready_at",0L).forGetter(BishopState::barrageReadyAt)).apply(i,BishopState::new));
    private static final class Storage {
        static final AttachmentType<BishopState> TYPE=AttachmentRegistry.create(KillerRankingMod.id("bishop_skill"),b->b.initializer(()->LOCKED).persistent(CODEC).copyOnDeath());
    }
    public static void initialize(){Storage.TYPE.identifier();}
    public static BishopState get(ServerPlayer p){return p.getAttachedOrCreate(Storage.TYPE);}
    public static void set(ServerPlayer p,BishopState s){p.setAttached(Storage.TYPE,s);}
    public int cooldown(long now){return (int)Math.clamp(readyAt-now,0,BishopRules.COOLDOWN);}
    public int barrageCooldown(long now){return (int)Math.clamp(barrageReadyAt-now,0,BishopRules.COOLDOWN);}
    public BishopState owned(boolean owned){return new BishopState(owned,readyAt,barrageReadyAt);}
}
