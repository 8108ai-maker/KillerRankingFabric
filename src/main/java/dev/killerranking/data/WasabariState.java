package dev.killerranking.data;

import com.mojang.serialization.Codec;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.WasabariRules;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

/** Independent cooldown; purchased ownership is stored in SalmosaState. */
public record WasabariState(long readyAt) {
    public WasabariState {readyAt=Math.max(0,readyAt);}
    public static final Codec<WasabariState> CODEC=Codec.LONG.xmap(WasabariState::new,WasabariState::readyAt);
    private static final class Storage {
        static final AttachmentType<WasabariState> TYPE=AttachmentRegistry.create(KillerRankingMod.id("wasabari"),b->b.initializer(()->new WasabariState(0)).persistent(CODEC).copyOnDeath());
    }
    public static void initialize(){Storage.TYPE.identifier();}
    public static WasabariState get(ServerPlayer p){return p.getAttachedOrCreate(Storage.TYPE);}
    public static void set(ServerPlayer p,WasabariState value){p.setAttached(Storage.TYPE,value);}
    public int cooldown(long now){return WasabariRules.cooldown(readyAt,now);}
    public WasabariState cast(long now){return cooldown(now)==0?new WasabariState(now+WasabariRules.COOLDOWN):this;}
}
