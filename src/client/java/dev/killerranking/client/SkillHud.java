package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.Locale;

/** Three pixel-art slots anchored above the left vanilla health/armor stack. */
public final class SkillHud {
    public static final int SIZE = 24, GAP = 5, WIDTH = SIZE * 3 + GAP * 2;
    public record Slot(String icon, String name, int cooldown, float progress, boolean active, boolean usable, String badge) {
        public boolean empty() { return icon == null; }
    }
    private static final Slot EMPTY = new Slot(null, "", 0, 1, false, false, "");
    private SkillHud() { }

    public static Slot[] slots() {
        Slot[] slots = {EMPTY, EMPTY, EMPTY};
        var p = Minecraft.getInstance().player;
        if (p == null || !ClientCombatStatus.available()) return slots;
        boolean enabled = TraitVisuals.enabled() && !ClientRook.captured();
        if(ClientNeungsaSkill.available()&&(p.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA)||!ClientWasabari.available())){
            slots[0]=new Slot("neungsa_slash","무형참격",ClientNeungsaSkill.cooldown(),ClientNeungsaSkill.progress(),ClientNeungsaSkill.remaining()>0,
                enabled&&p.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA),"");
            slots[1]=new Slot("neungsa_iaido","발도술",ClientNeungsaSkill.iaidoCooldown(),ClientNeungsaSkill.iaidoProgress(),ClientNeungsaSkill.iaidoPhase()!=0,enabled&&p.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA),ClientNeungsaSkill.iaidoPhase()==NeungsaIaidoRules.CHARGING&&ClientNeungsaSkill.iaidoAge()>=NeungsaIaidoRules.CHARGE?"!":"");
            slots[2]=new Slot("neungsa_echo","연속참격",ClientNeungsaSkill.echoCooldown(),ClientNeungsaSkill.echoProgress(),ClientNeungsaSkill.echoRemaining()>0,enabled&&p.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA),"");
        }else if(ClientWasabari.available()){
            slots[0]=new Slot("wasabari","와사바리",ClientWasabari.cooldown(),ClientWasabari.progress(),ClientWasabari.remaining()>0,enabled,"");
            if(ClientKnifePursuit.available())slots[1]=new Slot("knife_pursuit","칼날 추격",ClientKnifePursuit.cooldown(),ClientKnifePursuit.progress(),ClientKnifePursuit.phase()!=0,enabled&&p.getMainHandItem().is(KillerRankingMod.SALMOSA_MID_DAGGER),"");
            if(ClientShoulderStab.available()){
                boolean air=ClientKnifePursuit.deflected()||ClientShoulderStab.phase()==ShoulderStabRules.COUNTER;
                slots[2]=new Slot(air?"air_counter":"shoulder_stab",air?"공중 내려찍기":"어깨 찌르기",ClientShoulderStab.cooldown(),air&&ClientKnifePursuit.deflected()?ClientKnifePursuit.progress():ClientShoulderStab.progress(),ClientShoulderStab.phase()!=0||ClientKnifePursuit.counterReady(),enabled&&p.getMainHandItem().is(KillerRankingMod.SALMOSA_MID_DAGGER)&&(!ClientKnifePursuit.deflected()||ClientKnifePursuit.counterReady()),ClientKnifePursuit.counterReady()&&ClientShoulderStab.cooldown()==0?"C!":"");
            }
        }else if (ClientBishop.branch() && ClientBishop.unlocked()) {
            slots[0] = new Slot("bishop_summon", "신의 부름", ClientBishop.cooldown(), ClientBishop.progress(), ClientBishop.castTicks() > 0, enabled,
                ClientBishop.count() > 0 ? Integer.toString(ClientBishop.count()) : "");
            slots[1] = new Slot("bishop_designation", "상대지정", 0, 1, ClientBishop.hasDesignation(), enabled, "");
            slots[2] = new Slot("bishop_barrage", "형제들의 도움", ClientBishop.barrageCooldown(), ClientBishop.barrageProgress(), ClientBishop.barrageTicks() > 0, enabled, "");
        } else if (ClientCleave.branch() && ClientCleave.unlocked()) {
            boolean weapon = p.getMainHandItem().is(KillerRankingMod.PAWN6_KATANA);
            String charge = ClientCleave.phase() == CleaveRules.CHARGING ? Math.round(ClientCleave.progress() * 100) + "%" : "";
            slots[0] = new Slot("cleave", "일도양단", ClientCleave.cooldown(), ClientCleave.progress(), ClientCleave.phase() != 0, enabled && weapon, charge);
            slots[1] = new Slot("auto_parry", "자동 패링", 0, 1, ClientAutoParry.active(), enabled && weapon, "");
        } else if (ClientRook.branch() && ClientRook.unlocked()) {
            int phase = ClientRook.phase();
            boolean throwing = ClientRook.choice() || phase == RookRules.THROW;
            slots[0] = new Slot(throwing ? "rook_throw" : "rook_grab", throwing ? "던지기" : "잡기", ClientRook.cooldown(), ClientRook.progress(),
                phase == RookRules.AIM || phase == RookRules.CHOICE || phase == RookRules.THROW, enabled, "");
            slots[1] = new Slot("rook_punch", "연속타", 0, 1, phase == RookRules.PUNCH, enabled && (ClientRook.choice() || phase == RookRules.PUNCH), "");
            slots[2] = new Slot("rook_mount", "올라타기", 0, 1, phase == RookRules.MOUNT, enabled && (ClientRook.choice() || phase == RookRules.MOUNT), "");
        } else if (ClientCheckmate.branch() && ClientCheckmate.unlocked()) {
            boolean active = ClientCheckmate.remaining() > 0 || CheckmateVfx.aiming(p.getUUID());
            slots[0] = new Slot("checkmate", "체크 메이트", ClientCheckmate.cooldown(), ClientCheckmate.progress(), active,
                enabled && p.getMainHandItem().is(KillerRankingMod.NIGHT_BARBED_BAT), "");
        } else if (ClientRush.branch() && ClientRush.unlocked()) {
            slots[0] = new Slot("rush", "연타", ClientRush.cooldown(), ClientRush.progress(), ClientRush.remaining() > 0, enabled, "");
            slots[1] = new Slot("heavy_punch", "강타", ClientHeavyPunch.cooldown(), ClientHeavyPunch.progress(), ClientHeavyPunch.remaining()>0,
                enabled && p.getMainHandItem().is(KillerRankingMod.TTEOKBAL_FIST), "");
        } else if (ClientMadnessLeap.available()) {
            slots[0] = new Slot("madness_leap", "은발 사부님의 가르침", ClientMadnessLeap.cooldown(), ClientMadnessLeap.progress(), ClientMadnessLeap.phase() != 0,
                enabled && p.getMainHandItem().is(KillerRankingMod.EUNBAL_HATCHET), "");
        }
        return slots;
    }

    public static int left() {
        var c = Minecraft.getInstance();
        double scale = c.getWindow().getGuiScale() / FixedUi.SCALE;
        int center = (int) Math.round((c.getWindow().getGuiScaledWidth() / 2 - 50) * scale);
        // At a smaller vanilla scale the fixed-size group must stay left of the hunger bars.
        return Math.max(2, Math.min(center - WIDTH / 2, FixedUi.width() / 2 - WIDTH - 4));
    }

    public static int top() {
        var c = Minecraft.getInstance();
        var p = c.player;
        int hearts = (int) Math.ceil((Math.max(p.getHealth(), p.getAttributeValue(Attributes.MAX_HEALTH)) + p.getAbsorptionAmount()) / 20.0);
        int spacing = Math.max(10 - (hearts - 2), 3);
        int vanillaTop = c.getWindow().getGuiScaledHeight() - 39 - (hearts - 1) * spacing - (p.getArmorValue() > 0 ? 10 : 0);
        return Math.max(24, (int) Math.floor(vanillaTop * c.getWindow().getGuiScale() / FixedUi.SCALE) - SIZE - 13);
    }

    /** Called inside FixedUi's transform so every texture pixel stays three physical pixels. */
    public static void render(GuiGraphics g) {
        int x = left(), y = top();
        Slot[] slots = slots();
        Component[] keys = {KillerRankingClient.rushKeyLabel(), KillerRankingClient.secondaryKeyLabel(), KillerRankingClient.rookMountKeyLabel()};
        for (int i = 0; i < slots.length; i++) renderSlot(g, slots[i], keys[i].getString(), x + i * (SIZE + GAP), y);
    }

    private static void renderSlot(GuiGraphics g, Slot slot, String key, int x, int y) {
        String file = slot.empty() ? "empty" : slot.icon();
        ResourceLocation texture = KillerRankingMod.id("textures/gui/skills/" + file + ".png");
        if (!slot.empty() && !slot.usable()) g.setColor(.48F, .48F, .48F, 1);
        g.blit(texture, x, y, 0, 0, SIZE, SIZE, SIZE, SIZE);
        g.setColor(1, 1, 1, 1);
        if (!slot.empty() && slot.cooldown() > 0 && !slot.active()) {
            int height = (int) Math.ceil(SIZE * (1 - Math.clamp(slot.progress(), 0, 1)));
            for (int row = 0; row < height; row++) {
                int min = SIZE, max = -1;
                for (int col = 0; col < SIZE; col++) if (inside(col, row)) { min = Math.min(min, col); max = col; }
                if (max >= min) g.fill(x + min, y + row, x + max + 1, y + row + 1, 0xB5080B12);
            }
            String remaining = slot.cooldown() < 200 ? String.format(Locale.ROOT, "%.1f", slot.cooldown() / 20F) : Integer.toString((int) Math.ceil(slot.cooldown() / 20F));
            small(g, remaining, x + SIZE / 2, y + 8, .75F, 0xFFFFFFFF, SIZE - 2);
        }
        if (!slot.empty() && slot.active()) {
            for (int row = 0; row < SIZE; row++) for (int col = 0; col < SIZE; col++) {
                double radius = (col - 11.5) * (col - 11.5) + (row - 11.5) * (row - 11.5);
                if (radius <= 132.25 && radius >= 110.25) g.fill(x + col, y + row, x + col + 1, y + row + 1, 0xFFFFECAD);
            }
        }
        if (!slot.badge().isEmpty()) small(g, slot.badge(), x + SIZE / 2, y + SIZE - 8, .6F, 0xFFFFFFFF, SIZE - 2);
        if (!slot.empty()) caption(g, slot.name(), x + SIZE / 2, y);
        small(g, key, x + SIZE / 2, y + SIZE + 2, .75F, slot.empty() || !slot.usable() ? 0xFF9A9FA9 : 0xFFFFFFFF, SIZE + GAP - 1);
    }

    private static boolean inside(int x, int y) { return (x - 11.5) * (x - 11.5) + (y - 11.5) * (y - 11.5) <= 132.25; }
    private static void caption(GuiGraphics g, String name, int center, int y) {
        var font = Minecraft.getInstance().font;
        float scale = .55F;
        var lines = font.split(Component.literal(name), (int) ((SIZE + GAP - 1) / scale));
        int top = y - 2 - lines.size() * 7;
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            g.pose().pushPose();
            g.pose().translate(center, top + i * 7, 0);
            g.pose().scale(scale, scale, 1);
            g.drawString(font, line, -font.width(line) / 2, 0, 0xFFE9ECF2, true);
            g.pose().popPose();
        }
    }
    private static void small(GuiGraphics g, String text, int center, int y, float scale, int color, int maxWidth) {
        var font = Minecraft.getInstance().font;
        if (font.width(text) > 0) scale = Math.max(.45F, Math.min(scale, maxWidth / (float) font.width(text)));
        text = font.plainSubstrByWidth(text, (int) (maxWidth / scale));
        g.pose().pushPose();
        g.pose().translate(center, y, 0);
        g.pose().scale(scale, scale, 1);
        g.drawString(font, text, -font.width(text) / 2, 0, color, true);
        g.pose().popPose();
    }
}
