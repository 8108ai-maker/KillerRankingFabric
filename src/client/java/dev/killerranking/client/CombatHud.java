package dev.killerranking.client;

import dev.killerranking.Trait;
import dev.killerranking.combat.TraitLevels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.Locale;

public final class CombatHud {
	private CombatHud() { }
	public static int traitColor() {
        return 0xFF000000 | TraitVisuals.color(ClientCombatStatus.trait(), ClientDashboard.branch());
	}
	public static String seconds(int ticks) { return String.format(Locale.ROOT, "%.1fs", ticks / 20.0F); }
	public static Component traitName() {
		if (ClientCombatStatus.trait() == Trait.FIGHTING_SPIRIT && ClientDashboard.branch() != dev.killerranking.combat.SpiritBranch.NONE)
            return Component.literal("Lv." + ClientCombatStatus.level() + " " + ClientDashboard.branch().label());
        return Component.literal("Lv." + ClientCombatStatus.level() + " ").append(Component.translatable("trait.killerranking." + ClientCombatStatus.trait().id()));
	}
	public static Component traitDescription() {
		if (ClientCombatStatus.trait() == Trait.FIGHTING_SPIRIT) {
            var branch = ClientDashboard.branch(); var d = ClientDashboard.get(); int level = ClientCombatStatus.level();
            if (branch != dev.killerranking.combat.SpiritBranch.NONE) {
                int move = branch.movement(level, d != null && d.checkmate());
                return Component.literal("공격 +" + branch.attack(level) + "% · 방어 +" + branch.defense(level) + "% / 공속 +" + branch.attackSpeed(level) + "% · 이속 " + (move > 0 ? "+" : "") + move + "%. 켜진 동안 적용. 상세 정보: 스탯창");
            }
            return Component.literal("떡발 / 킹앤킬 중 한 분기를 아래 버튼에서 선택하세요. 선택 전에는 기존 투기 효과가 유지됩니다.");
        }
        String key = "trait.killerranking." + ClientCombatStatus.trait().id() + ".description";
		return switch (ClientCombatStatus.trait()) {
			case KILLING_INTENT -> Component.translatable(key, ClientCombatStatus.power(), seconds(ClientCombatStatus.effectTotalTicks()));
			case FIGHTING_SPIRIT -> Component.translatable(key, ClientCombatStatus.highHealthPercent(), ClientCombatStatus.halfHealthPercent(), ClientCombatStatus.lowHealthPercent(), String.format(Locale.ROOT, "%.2fs", TraitLevels.parryWindow(ClientCombatStatus.level()) / 20.0F));
			case MIXED_MADNESS -> Component.translatable(key, ClientCombatStatus.power(), ClientDashboard.get() == null ? TraitLevels.bonusPercent(5, ClientCombatStatus.level()) : ClientDashboard.get().madnessSpeedPercent());
			case POISON_AURA -> Component.translatable(key, seconds(ClientCombatStatus.effectTotalTicks()), dev.killerranking.combat.PoisonEffects.resistance(ClientCombatStatus.level()), dev.killerranking.combat.PoisonEffects.speed(ClientCombatStatus.level()));
			default -> Component.translatable(key);
		};
	}
	public static Component traitStatus() {
        if (!TraitVisuals.enabled()) return Component.literal("꺼짐 [" + KillerRankingClient.traitKeyLabel().getString() + "]");
		return switch (ClientCombatStatus.trait()) {
			case KILLING_INTENT -> ClientCombatStatus.active() ? Component.literal("+" + ClientCombatStatus.power() + "% " + seconds(ClientCombatStatus.effectTicks())) : Component.translatable("hud.killerranking.on_hit");
			case FIGHTING_SPIRIT -> Component.translatable("hud.killerranking.defense", ClientCombatStatus.power());
			case MIXED_MADNESS -> Component.literal(String.format(Locale.ROOT,"피해 +%.1f%%",ClientCombatStatus.madnessBonus()));
			case POISON_AURA -> Component.translatable("hud.killerranking.poison_duration", seconds(ClientCombatStatus.effectTotalTicks()));
			default -> Component.translatable("hud.killerranking.waiting");
		};
	}
	public static Component parryStatus() {
		return ClientCombatStatus.parryActive() ? Component.translatable("hud.killerranking.parrying") : ClientCombatStatus.parryTicks() > 0
			? Component.literal(seconds(ClientCombatStatus.parryTicks())) : Component.translatable("hud.killerranking.ready");
	}
    public static Component survivalStatus() {
        if (ClientCombatStatus.survivalTicks() > 0) return Component.literal("생존 " + seconds(ClientCombatStatus.survivalTicks()));
        if (ClientCombatStatus.survivalCooldown() > 0) return Component.literal("재사용 " + seconds(ClientCombatStatus.survivalCooldown()));
        return Component.literal(TraitVisuals.enabled() ? "준비" : "꺼짐");
    }
    public static float survivalProgress() {
        return ClientCombatStatus.survivalTicks() > 0 ? (float) ClientCombatStatus.survivalTicks() / dev.killerranking.combat.MadnessSurvival.DURATION
            : dev.killerranking.combat.CombatRules.cooldownProgress(ClientCombatStatus.survivalCooldown(), dev.killerranking.combat.MadnessSurvival.COOLDOWN);
    }
	public static void render(GuiGraphics graphics) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.options.hideGui || client.player.isSpectator() || !client.player.isAlive()) return;
        FixedUi.begin(graphics);
        SkillHud.render(graphics);
        if (!ClientCombatStatus.available() || ClientCombatStatus.trait() == Trait.UNASSIGNED) { FixedUi.end(graphics); return; }
		int x = FixedUi.width() / 2 + 10;
        // Fixed-size bars remain above vanilla food/air bars when their scale changes.
        double vanillaUnits = client.getWindow().getGuiScale() / FixedUi.SCALE;
		int bottom = FixedUi.height() - (int) Math.ceil(39 * vanillaUnits + 13);
		if (client.player.isUnderWater() || client.player.getAirSupply() < client.player.getMaxAirSupply()) bottom -= (int) Math.ceil(10 * vanillaUnits);
		if (client.player.getVehicle() != null) bottom -= (int) Math.ceil(20 * vanillaUnits);
        if (ClientCombatStatus.trait() == Trait.MIXED_MADNESS) {
            label(graphics, Component.literal("미친 고씨"), survivalStatus(), x, bottom - 45, 0xFFFFB181);
            bar(graphics, x, bottom - 37, 81, 5, survivalProgress(), 0xFFFFB181);
        }
		label(graphics, traitName(), traitStatus(), x, bottom - 27, traitColor());
		bar(graphics, x, bottom - 19, 81, 5, ClientCombatStatus.gauge(), traitColor());
		label(graphics, Component.translatable("key.killerranking.parry"), parryStatus(), x, bottom - 9,
			ClientCombatStatus.parryActive() ? 0xFFFFFFFF : 0xFF81D6C5);
		bar(graphics, x, bottom - 1, 81, 5, ClientCombatStatus.parryProgress(), 0xFF81D6C5);
        FixedUi.end(graphics);
	}
	private static void label(GuiGraphics graphics, Component name, Component status, int x, int y, int color) {
		var font = Minecraft.getInstance().font;
		graphics.pose().pushPose();
		graphics.pose().translate(x, y, 0);
		graphics.pose().scale(0.65F, 0.65F, 1);
		String text = status.getString();
		int available = 124 - font.width(text) - 5;
		graphics.drawString(font, font.plainSubstrByWidth(name.getString(), available), 0, 0, color);
		graphics.drawString(font, text, 124 - font.width(text), 0, 0xFFF0F2F7);
		graphics.pose().popPose();
	}
	public static void bar(GuiGraphics graphics, int x, int y, int width, int height, float progress, int color) {
		graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xDD090D16);
		graphics.fill(x, y, x + width, y + height, 0xFF303B4C);
		int fill = Math.round((width - 2) * Math.clamp(progress, 0, 1));
		if (fill > 0) {
			graphics.fill(x + 1, y + 1, x + 1 + fill, y + height - 1, color);
			graphics.fill(x + 1, y + 1, x + 1 + fill, y + 2, 0x66FFFFFF);
		}
	}
}
