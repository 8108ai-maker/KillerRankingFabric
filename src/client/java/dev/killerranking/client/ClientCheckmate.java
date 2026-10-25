package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.CombatRules;
import dev.killerranking.combat.CheckmateRules;
import dev.killerranking.network.CheckmateStatusPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ClientCheckmate {
    private static CheckmateStatusPayload value;
    private static int elapsed;
    public static void set(CheckmateStatusPayload status) { value=status;elapsed=0; }
    public static void clear() { value=null;elapsed=0; }
    public static void tick() { if(value!=null&&elapsed<Integer.MAX_VALUE) elapsed++; }
    public static boolean branch() { return value!=null&&value.branch(); }
    public static boolean unlocked() { return value!=null&&value.unlocked(); }
    public static boolean rushOwned() { return value!=null&&value.rushOwned(); }
    private static boolean aiming() { var p=Minecraft.getInstance().player;return p!=null&&CheckmateVfx.aiming(p.getUUID()); }
    public static int remaining() { return value==null?0:Math.max(0,value.activeTicks()-elapsed); }
    public static int cooldown() { return value==null?0:Math.max(0,value.cooldownTicks()-elapsed); }
    public static float progress() {
        return !unlocked()?0:aiming()?1:remaining()>0?(float)remaining()/CheckmateRules.DURATION:CombatRules.cooldownProgress(cooldown(),CheckmateRules.COOLDOWN);
    }
    public static Component status() {
        if(!unlocked()) return Component.literal("해금 1,000P");
        if(aiming()) return Component.literal(CheckmateVfx.selectedTile()<0?"칸 선택":"놓으면 도약");
        if(remaining()>0) return Component.literal("내려치기");
        if(cooldown()>0) return Component.literal(CombatHud.seconds(cooldown()));
        if(!TraitVisuals.enabled()) return Component.literal("꺼짐");
        var p=Minecraft.getInstance().player;
        if(p!=null&&!p.getMainHandItem().is(KillerRankingMod.NIGHT_BARBED_BAT)) return Component.literal("방망이 필요");
        return Component.literal("준비");
    }
    private ClientCheckmate() { }
}
