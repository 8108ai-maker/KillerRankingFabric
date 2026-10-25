package dev.killerranking.client;

import dev.killerranking.combat.CombatRules;
import dev.killerranking.combat.RushRules;
import dev.killerranking.network.RushStatusPayload;
import net.minecraft.network.chat.Component;

public final class ClientRush {
    private static RushStatusPayload value;
    private static int elapsed;
    public static void set(RushStatusPayload status) { value=status;elapsed=0; }
    public static void clear() { value=null;elapsed=0; }
    public static void tick() { if(value!=null&&elapsed<Integer.MAX_VALUE) elapsed++; }
    public static boolean branch() { return value!=null&&value.branch(); }
    public static boolean unlocked() { return value!=null&&value.unlocked(); }
    public static int remaining() { return value==null?0:Math.max(0,value.activeTicks()-elapsed); }
    public static int cooldown() { return value==null?0:Math.max(0,value.cooldownTicks()-elapsed); }
    public static float progress() {
        return !unlocked()?0:remaining()>0?(float)remaining()/RushRules.DURATION:CombatRules.cooldownProgress(cooldown(),RushRules.COOLDOWN);
    }
    public static Component status() {
        if(!unlocked()) return Component.literal("해금 1,000P");
        if(remaining()>0) return Component.literal("연타 "+CombatHud.seconds(remaining()));
        if(cooldown()>0) return Component.literal(CombatHud.seconds(cooldown()));
        return Component.literal(TraitVisuals.enabled()?"준비":"꺼짐");
    }
    private ClientRush() { }
}
