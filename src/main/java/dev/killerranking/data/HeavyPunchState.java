package dev.killerranking.data;

import com.mojang.serialization.Codec;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.HeavyPunchRules;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

/** Ownership follows RushState. Only this attack's independent cooldown is saved. */
public record HeavyPunchState(long readyAt) {
    public HeavyPunchState {readyAt=Math.max(0,readyAt);}
    public static final Codec<HeavyPunchState> CODEC=Codec.LONG.xmap(HeavyPunchState::new,HeavyPunchState::readyAt);
    private static final class Storage {
        static final AttachmentType<HeavyPunchState> TYPE=AttachmentRegistry.create(KillerRankingMod.id("heavy_punch"),
            b->b.initializer(()->new HeavyPunchState(0)).persistent(CODEC).copyOnDeath());
    }
    public static void initialize(){Storage.TYPE.identifier();}
    public static HeavyPunchState get(ServerPlayer p){return p.getAttachedOrCreate(Storage.TYPE);}
    public static void set(ServerPlayer p,HeavyPunchState s){p.setAttached(Storage.TYPE,s);}
    public int cooldown(long now){return HeavyPunchRules.cooldown(readyAt,now);}
    public HeavyPunchState cast(long now){return cooldown(now)==0?new HeavyPunchState(now+HeavyPunchRules.COOLDOWN):this;}
}
