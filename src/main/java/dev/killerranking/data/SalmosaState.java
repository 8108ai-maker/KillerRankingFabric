package dev.killerranking.data;
import com.mojang.serialization.Codec;
import dev.killerranking.KillerRankingMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

/** Purchased ownership is independent of the existing two skill cooldowns. */
public record SalmosaState(boolean unlocked) {
    public static final SalmosaState LOCKED=new SalmosaState(false),OWNED=new SalmosaState(true);
    public static final Codec<SalmosaState> CODEC=Codec.BOOL.xmap(SalmosaState::new,SalmosaState::unlocked);
    private static final class Storage {
        static final AttachmentType<SalmosaState> TYPE=AttachmentRegistry.create(KillerRankingMod.id("salmosa_skill"),
            b->b.initializer(()->LOCKED).persistent(CODEC).copyOnDeath());
    }
    public static void initialize(){Storage.TYPE.identifier();}
    public static SalmosaState get(ServerPlayer p){return p.getAttachedOrCreate(Storage.TYPE);}
    public static void set(ServerPlayer p,SalmosaState value){p.setAttached(Storage.TYPE,value);}
}
