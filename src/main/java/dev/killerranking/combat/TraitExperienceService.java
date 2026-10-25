package dev.killerranking.combat;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.Trait;
import dev.killerranking.data.PlayerProfile;
import dev.killerranking.data.PlayerProfiles;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class TraitExperienceService {
    public static int required(int level) {
        return TraitExperience.required(level, KillerRankingMod.CONFIG.traitLevel2Xp, KillerRankingMod.CONFIG.traitLevel3Xp);
    }
    private static boolean eligible(ServerPlayer p) { return dev.killerranking.data.TraitActivation.enabled(p) && !p.isSpectator() && !p.isCreative(); }
    public static void tick(MinecraftServer server) {
        for (var player : server.getPlayerList().getPlayers()) {
            if (!eligible(player) || !player.isAlive()) continue;
            var before = PlayerProfiles.get(player);
            boolean active = before.trait() == Trait.KILLING_INTENT ? KillerRankingMod.COMBAT_STATUS.killingIntentActive(player)
                : before.trait() == Trait.MIXED_MADNESS && CombatRules.madnessActive(CombatRules.healthRatio(player.getHealth(), player.getMaxHealth()));
            apply(player, before, TraitExperience.activeTick(before, active, KillerRankingMod.CONFIG.traitLevel2Xp, KillerRankingMod.CONFIG.traitLevel3Xp));
        }
    }
    public static void hit(LivingEntity victim, DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker == victim) return;
        if (attacker instanceof ServerPlayer player && player.isAlive()) awardHit(player);
        else if(attacker instanceof dev.killerranking.entity.BishopFollower follower&&follower.owner()!=null&&follower.owner().isAlive())awardHit(follower.owner());
        // A lethal received hit counts too; copyOnDeath preserves the resulting XP.
        if (victim instanceof ServerPlayer player) awardHit(player);
    }
    private static void awardHit(ServerPlayer player) {
        if (!eligible(player)) return;
        var before = PlayerProfiles.get(player);
        if (TraitExperience.passive(before.trait())) apply(player, before,
            TraitExperience.add(before, 1, KillerRankingMod.CONFIG.traitLevel2Xp, KillerRankingMod.CONFIG.traitLevel3Xp));
    }
    private static void apply(ServerPlayer player, PlayerProfile before, PlayerProfile after) {
        if (before.equals(after)) return;
        PlayerProfiles.set(player, after);
        if (before.traitLevel() != after.traitLevel()) {
            KillerRankingMod.assignAndSync(player);
            if (after.traitLevel() > before.traitLevel()) dev.killerranking.KillerRankingSounds.play(player, dev.killerranking.KillerRankingSounds.LEVEL_UP, 1.0F, 1.0F);
            player.sendSystemMessage(Component.literal("§a특성 레벨 상승! " + after.trait().koreanName() + " Lv." + after.traitLevel()), true);
        }
    }
    private TraitExperienceService() { }
}
