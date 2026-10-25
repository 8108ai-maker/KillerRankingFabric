package dev.killerranking.combat;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.Trait;
import dev.killerranking.data.*;
import dev.killerranking.network.SalmosaStatusPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class SalmosaSkillService {
    public static final int COST=1000;
    public static boolean branch(ServerPlayer p){return PlayerProfiles.get(p).trait()==Trait.POISON_AURA;}
    public static boolean available(ServerPlayer p){return branch(p)&&SalmosaState.get(p).unlocked();}
    public static void unlock(ServerPlayer p){
        if(!branch(p)||!p.isAlive()||p.isSpectator()||SkillCombatLock.blocksAttack(p))return;
        if(SalmosaState.get(p).unlocked()){sync(p);return;}
        var profile=PlayerProfiles.get(p);
        if(profile.pointUnits()<KillerPoints.whole(COST)){dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_DENY,.8f,1f);p.sendSystemMessage(Component.literal("§c살모사 해금에는 1,000포인트가 필요합니다."));sync(p);return;}
        PlayerProfiles.set(p,profile.addPoints(-COST));dev.killerranking.KillerRankingSounds.play(p,dev.killerranking.KillerRankingSounds.UI_PURCHASE,1f,1f);SalmosaState.set(p,SalmosaState.OWNED);
        KillerRankingMod.assignAndSync(p);
        p.sendSystemMessage(Component.literal("§5살모사 해금 완료! 1,000포인트 사용 · Z 와사바리 / X 칼날 추격 / C 어깨 찌르기"));
    }
    public static void sync(ServerPlayer p){
        if(ServerPlayNetworking.canSend(p,SalmosaStatusPayload.TYPE))ServerPlayNetworking.send(p,new SalmosaStatusPayload(branch(p),SalmosaState.get(p).unlocked()));
    }
    private SalmosaSkillService(){ }
}
