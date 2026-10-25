package dev.killerranking.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import dev.killerranking.data.KillerPoints;
import com.mojang.brigadier.CommandDispatcher;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.TraitLevels;
import dev.killerranking.data.PlayerProfiles;
import dev.killerranking.data.SpiritState;
import dev.killerranking.combat.SpiritBranch;
import dev.killerranking.Trait;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.Collection;
import java.util.List;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class TraitLevelCommands {
	private TraitLevelCommands() { }
	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) -> register(dispatcher));
	}
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		var level = literal("level");
		level.then(literal("set").then(argument("players", EntityArgument.players())
			.then(argument("level", IntegerArgumentType.integer(1, TraitLevels.MAX))
				.executes(context -> set(context.getSource(), EntityArgument.getPlayers(context, "players"), IntegerArgumentType.getInteger(context, "level"))))));
		level.then(literal("get")
			.executes(context -> get(context.getSource(), List.of(context.getSource().getPlayerOrException())))
			.then(argument("players", EntityArgument.players()).executes(context -> get(context.getSource(), EntityArgument.getPlayers(context, "players")))));
        var root = literal("killerranking").requires(source -> source.hasPermission(2))
            .executes(c -> help(c.getSource())).then(literal("help").executes(c -> help(c.getSource()))).then(level);
        var traits = argument("players", EntityArgument.players());
        for (var trait : Trait.values()) {
            if (trait == Trait.UNASSIGNED) continue;
            traits.then(literal(trait.id()).executes(c -> changeTrait(c.getSource(), EntityArgument.getPlayers(c, "players"), trait)));
        }
        root.then(literal("trait").then(literal("set").then(traits)));
        root.then(literal("trait").then(literal("reroll").then(argument("players", EntityArgument.players()).executes(c -> {
            var players = EntityArgument.getPlayers(c, "players");
            for (var player : players) {
                var profile = PlayerProfiles.reroll(player, KillerRankingMod.CONFIG);
                SpiritState.set(player, new SpiritState(SpiritBranch.NONE, false));
                KillerRankingMod.COMBAT_STATUS.forget(player.getUUID());
                KillerRankingMod.assignAndSync(player);
                player.sendSystemMessage(Component.literal("§a특성 재추첨: " + profile.trait().koreanName() + " Lv." + profile.traitLevel()));
                c.getSource().sendSuccess(() -> Component.literal(player.getScoreboardName() + ": " + profile.trait().koreanName() + " Lv." + profile.traitLevel()), true);
            }
            return players.size();
        }))));
        root.then(literal("parry").then(literal("reset").then(argument("players", EntityArgument.players()).executes(c -> {
            var players = EntityArgument.getPlayers(c, "players");
            for (var player : players) {
                KillerRankingMod.PARRY.resetCooldown(player);
                player.sendSystemMessage(Component.literal("§b패링 쿨타임이 초기화되었습니다."));
            }
            c.getSource().sendSuccess(() -> Component.literal(players.size() + "명의 패링 쿨타임을 초기화했습니다."), true);
            return players.size();
        }))));
        var branches = argument("players", EntityArgument.players());
        for (var branch : SpiritBranch.values()) branches.then(literal(branch.id()).executes(c -> {
            var players = EntityArgument.getPlayers(c, "players");
            for (var player : players) {
                PlayerProfiles.set(player, PlayerProfiles.get(player).withTrait(Trait.FIGHTING_SPIRIT));
                SpiritState.set(player, new SpiritState(branch, false));
                KillerRankingMod.assignAndSync(player);
            }
            c.getSource().sendSuccess(() -> Component.literal(players.size() + "명: " + branch.label()), true);
            return players.size();
        }));
        root.then(literal("branch").then(literal("set").then(branches)));
        root.then(literal("checkmate").then(literal("set").then(argument("players", EntityArgument.players())
            .then(argument("owned", BoolArgumentType.bool()).executes(c -> {
                int count = 0;
                for (var player : EntityArgument.getPlayers(c, "players")) {
                    if (PlayerProfiles.get(player).trait() != Trait.FIGHTING_SPIRIT || SpiritState.get(player).branch() != SpiritBranch.KING_AND_KILL) continue;
                    KillerRankingMod.CHECKMATE.setOwned(player, BoolArgumentType.getBool(c, "owned")); count++;
                }
                int changed = count;
                c.getSource().sendSuccess(() -> Component.literal(changed + "명의 체크 메이트 보유 상태를 변경했습니다. (킹앤킬 분기 전용)"), true);
                return changed;
            })))));
        root.then(literal("rook").then(literal("set").then(argument("players",EntityArgument.players()).then(argument("owned",BoolArgumentType.bool()).executes(c->{
            int count=0;for(var player:EntityArgument.getPlayers(c,"players"))if(dev.killerranking.combat.RookService.branch(player)){KillerRankingMod.ROOK.setOwned(player,BoolArgumentType.getBool(c,"owned"));count++;}
            int n=count;c.getSource().sendSuccess(()->Component.literal(n+"명의 잡기 보유 상태를 변경했습니다."),true);return n;
        })))));
        root.then(literal("cleave").then(literal("set").then(argument("players",EntityArgument.players()).then(argument("owned",BoolArgumentType.bool()).executes(c->{
            int count=0;for(var player:EntityArgument.getPlayers(c,"players"))if(dev.killerranking.combat.CleaveService.branch(player)){KillerRankingMod.CLEAVE.setOwned(player,BoolArgumentType.getBool(c,"owned"));count++;}
            int n=count;c.getSource().sendSuccess(()->Component.literal(n+"명의 일도양단 보유 상태를 변경했습니다."),true);return n;
        })))));
        root.then(literal("cleave").then(literal("reset").then(argument("players",EntityArgument.players()).executes(c->{
            var players=EntityArgument.getPlayers(c,"players");for(var player:players){KillerRankingMod.CLEAVE.cancel(player);dev.killerranking.data.CleaveState.set(player,new dev.killerranking.data.CleaveState(dev.killerranking.data.CleaveState.get(player).unlocked(),0));KillerRankingMod.CLEAVE.sync(player);}
            c.getSource().sendSuccess(()->Component.literal(players.size()+"명의 일도양단 쿨타임을 초기화했습니다."),true);return players.size();
        }))));
        root.then(literal("rook").then(literal("reset").then(argument("players",EntityArgument.players()).executes(c->{
            var players=EntityArgument.getPlayers(c,"players");for(var player:players){KillerRankingMod.ROOK.cancel(player);dev.killerranking.data.RookState.set(player,new dev.killerranking.data.RookState(dev.killerranking.data.RookState.get(player).unlocked(),0));KillerRankingMod.ROOK.sync(player);}
            c.getSource().sendSuccess(()->Component.literal(players.size()+"명의 잡기 쿨타임을 초기화했습니다."),true);return players.size();
        }))));
        root.then(literal("bishop").then(literal("set").then(argument("players",EntityArgument.players()).then(argument("owned",BoolArgumentType.bool()).executes(c->{
            int count=0;for(var player:EntityArgument.getPlayers(c,"players"))if(dev.killerranking.combat.BishopService.branch(player)){KillerRankingMod.BISHOP.setOwned(player,BoolArgumentType.getBool(c,"owned"));count++;}
            int n=count;c.getSource().sendSuccess(()->Component.literal(n+"명의 비숍 보유 상태를 변경했습니다."),true);return n;
        })))));
        root.then(literal("bishop").then(literal("reset").then(argument("players",EntityArgument.players()).executes(c->{
            var players=EntityArgument.getPlayers(c,"players");for(var player:players){KillerRankingMod.BISHOP.dismiss(player);dev.killerranking.data.BishopState.set(player,new dev.killerranking.data.BishopState(dev.killerranking.data.BishopState.get(player).unlocked(),0));KillerRankingMod.BISHOP.sync(player);}
            c.getSource().sendSuccess(()->Component.literal(players.size()+"명의 비숍 소환·쿨타임을 초기화했습니다."),true);return players.size();
        }))));
        root.then(literal("points").then(literal("add").then(argument("players", EntityArgument.players())
            .then(argument("amount", DoubleArgumentType.doubleArg(0.0000001, 1000000)).executes(c -> {
                var players = EntityArgument.getPlayers(c, "players");
                long amount = KillerPoints.fromDecimal(DoubleArgumentType.getDouble(c, "amount"));
                for (var player : players) {
                    PlayerProfiles.set(player, PlayerProfiles.get(player).addPointUnits(amount));
                    KillerRankingMod.assignAndSync(player);
                }
                c.getSource().sendSuccess(() -> Component.literal(players.size() + "명에게 " + KillerPoints.format(amount) + " P 지급"), true);
                return players.size();
            })))));
        root.then(literal("info").then(argument("players", EntityArgument.players()).executes(c -> get(c.getSource(), EntityArgument.getPlayers(c, "players")))));
        var rootNode = dispatcher.register(root);
        dispatcher.register(literal("kr").requires(source -> source.hasPermission(2))
            .executes(c -> help(c.getSource())).redirect(rootNode));
	}
    private static int help(CommandSourceStack source) {
        for (String line : new String[] {
            "§b킬러랭킹 관리자 명령어 (§f/kr§b로도 사용 가능)",
            "§f/kr trait set <대상> <특성> §7— 특성 지정",
            "§f/kr trait reroll <대상> §7— 특성 재추첨 (레벨·포인트 유지)",
            "§f/kr parry reset <대상> §7— 패링 쿨타임 초기화",
            "§7특성: killing_intent(살기), fighting_spirit(투기), poison_aura(독기), mixed_madness(광기)",
            "§f/kr branch set <대상> <분기> §7— 투기 분기 지정",
            "§7분기: tteokbal(떡발), king_and_kill(킹앤킬), none(선택 초기화)",
            "§f/kr level set <대상> <1~3> §7— 레벨 지정",
            "§f/kr level get [대상] §7— 레벨 확인",
            "§f/kr points add <대상> <수량> §7— 포인트 지급",
            "§f/kr checkmate set <대상> <true|false> §7— 체크 메이트 설정",
            "§f/kr rook set <대상> <true|false> §7— 잡기 설정 (다른 투기 스킬 해제)",
            "§f/kr rook reset <대상> §7— 잡기 취소·쿨타임 초기화",
            "§f/kr cleave set <대상> <true|false> §7— 일도양단 설정 (다른 투기 스킬 해제)",
            "§f/kr cleave reset <대상> §7— 일도양단 취소·쿨타임 초기화",
            "§f/kr bishop set <대상> <true|false> §7— 비숍 설정 (다른 투기 스킬 해제)",
            "§f/kr bishop reset <대상> §7— 부하 해산·쿨타임 초기화",
            "§f/kr info <대상> §7— 특성·분기·레벨·포인트 확인",
            "§7대상: 플레이어 이름, @s(자신), @a(전체). OP 권한 레벨 2 이상 필요."
        }) source.sendSuccess(() -> Component.literal(line), false);
        return 1;
    }
    private static int changeTrait(CommandSourceStack source, Collection<ServerPlayer> players, Trait trait) {
        for (var player : players) {
            PlayerProfiles.set(player, PlayerProfiles.get(player).withTrait(trait));
            SpiritState.set(player, new SpiritState(SpiritBranch.NONE, false));
            KillerRankingMod.assignAndSync(player);
            player.sendSystemMessage(Component.literal("§a전투 특성 변경: " + trait.koreanName() + (trait == Trait.FIGHTING_SPIRIT ? " / 휴대폰 전투 탭에서 분기를 선택하세요." : "")));
        }
        source.sendSuccess(() -> Component.literal(players.size() + "명의 특성을 " + trait.koreanName() + "(으)로 변경했습니다."), true);
        return players.size();
    }
	private static int set(CommandSourceStack source, Collection<ServerPlayer> players, int level) {
		for (ServerPlayer player : players) {
			var profile = PlayerProfiles.ensureTrait(player, KillerRankingMod.CONFIG).withLevel(level);
			PlayerProfiles.set(player, profile);
			KillerRankingMod.assignAndSync(player);
			player.sendSystemMessage(Component.literal("§a전투 특성: " + profile.trait().koreanName() + " Lv." + level + " / 3"));
		}
		source.sendSuccess(() -> Component.literal(players.size() + "명의 전투 특성 레벨을 " + level + "로 설정했습니다."), true);
		return players.size();
	}
	private static int get(CommandSourceStack source, Collection<ServerPlayer> players) {
		for (ServerPlayer player : players) {
			var profile = PlayerProfiles.get(player);
			source.sendSuccess(() -> Component.literal(player.getScoreboardName() + ": " + profile.trait().koreanName() + " Lv." + profile.traitLevel() + " / 3 / " + SpiritState.get(player).branch().label() + " / " + KillerPoints.format(profile.pointUnits()) + " P / XP " + profile.traitXp() + " / " + dev.killerranking.combat.TraitExperienceService.required(profile.traitLevel())), false);
		}
		return players.size();
	}
}
