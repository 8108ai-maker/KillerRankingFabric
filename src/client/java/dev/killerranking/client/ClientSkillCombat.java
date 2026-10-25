package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import java.lang.reflect.Method;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public final class ClientSkillCombat {
    private static Method cancelUpswing = findCancelUpswing();

    public static boolean blocksAttack() {
        var player = Minecraft.getInstance().player;
        if (player == null || !player.isAlive() || player.isSpectator()) return false;
        var id = player.getUUID();
        return ClientNeungsaSkill.echoRemaining()>0||NeungsaEchoVfx.active(id)||ClientNeungsaSkill.iaidoPhase()!=0||ClientNeungsaSkill.iaidoRemaining()>0||NeungsaIaidoVfx.blocksAttack(id)||ClientNeungsaSkill.remaining()>0||NeungsaSlashVfx.active(id)||ClientShoulderStab.phase()!=0||ShoulderStabVfx.active(id)||ClientKnifePursuit.phase()!=0||KnifePursuitVfx.active(id)||WasabariVfx.immobilized(player)||ClientWasabari.remaining()>0||HeavyPunchVfx.locked(player)||ClientHeavyPunch.remaining()>0||ClientBishop.castTicks()>0||ClientBishop.barrageTicks()>0||BishopBarrageVfx.active(id)||ClientAutoParry.active() || ClientRush.remaining() > 0 || RushVfx.active(id)
            || ClientCheckmate.remaining() > 0 || CheckmateVfx.active(id)
            || ClientRook.phase() != 0 || ClientRook.captured() || RookVfx.active(id) || RookVfx.captured(id)
            || ClientMadnessLeap.phase() != 0 || MadnessLeapVfx.active(id)
            || ClientCleave.phase() != 0 || CleaveVfx.active(id)
            || ClientCombatStatus.parryActive();
    }

    /** Use Better Combat's public optional API without requiring that mod on vanilla-combat installations. */
    private static Method findCancelUpswing() {
        if (!FabricLoader.getInstance().isModLoaded("bettercombat")) return null;
        try {
            return Class.forName("net.bettercombat.api.MinecraftClient_BetterCombat").getMethod("cancelUpswing");
        } catch (ReflectiveOperationException e) {
            KillerRankingMod.LOGGER.warn("Better Combat pending-swing cancellation is unavailable; skill attack gates remain active.");
            return null;
        }
    }

    public static void cancelPendingAttack(Minecraft client) {
        if (cancelUpswing == null) return;
        try {
            cancelUpswing.invoke(client);
        } catch (ReflectiveOperationException e) {
            cancelUpswing = null;
            KillerRankingMod.LOGGER.warn("Could not cancel Better Combat's pending swing; skill attack gates remain active.", e);
        }
    }

    private ClientSkillCombat() { }
}
