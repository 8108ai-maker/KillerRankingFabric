package dev.killerranking.mission;

import dev.killerranking.data.PlayerProfiles;
import dev.killerranking.data.KillerPoints;
import dev.killerranking.network.ProfilePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayList;

public final class RotatingMissions {
    public static final net.fabricmc.fabric.api.attachment.v1.AttachmentType<MissionBoard> TYPE = net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry.create(
        dev.killerranking.KillerRankingMod.id("mission_board"), b -> b.initializer(() -> new MissionBoard(-1, java.util.List.of())).persistent(MissionBoard.CODEC).copyOnDeath());
    public static void initialize() { }
    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        for (var p : server.getPlayerList().getPlayers()) update(p);
    }
    public static MissionBoard update(ServerPlayer player) {
        var world = player.server.overworld();
        var board = player.getAttachedOrCreate(TYPE);
        long now = world.getDayTime();
        if (board.needsRotation(now)) {
            long cycle = MissionBoard.cycleAt(now);
            long seed = world.getSeed() ^ player.getUUID().getMostSignificantBits() ^ player.getUUID().getLeastSignificantBits() ^ (cycle * 0x9e3779b97f4a7c15L);
            board = new MissionBoard(cycle, MissionCatalog.choose(seed).stream().map(d -> new MissionBoard.Entry(d.id(), d.read(player), 0, false)).toList());
            player.setAttached(TYPE, board);
            player.sendSystemMessage(Component.literal("§b미션 10개가 갱신되었습니다! 휴대폰 → 미션에서 확인하세요."));
            return board;
        }
        var entries = new ArrayList<MissionBoard.Entry>();
        long rewards = 0;
        for (var e : board.entries()) {
            var d = MissionCatalog.find(e.id());
            if (d == null) { entries.add(e); continue; }
            var next = e.advance(d.read(player), d.goal(), !player.isCreative() && !player.isSpectator());
            if (!e.paid() && next.paid()) {
                next = next.withPaidReward(d.rewardUnits());
                rewards += d.rewardUnits();
                dev.killerranking.KillerRankingSounds.play(player, dev.killerranking.KillerRankingSounds.MISSION_COMPLETE, 1.0F, 1.0F);
                player.sendSystemMessage(Component.literal("§6미션 완료: " + d.title() + " / +" + KillerPoints.format(d.rewardUnits()) + " P"));
            }
            entries.add(next);
        }
        var next = new MissionBoard(board.cycle(), entries);
        if (!next.equals(board)) player.setAttached(TYPE, next);
        if (rewards > 0) {
            var profile = PlayerProfiles.get(player).addPointUnits(rewards);
            PlayerProfiles.set(player, profile);
            ServerPlayNetworking.send(player, ProfilePayload.from(profile));
        }
        return next;
    }
    private RotatingMissions() { }
}
