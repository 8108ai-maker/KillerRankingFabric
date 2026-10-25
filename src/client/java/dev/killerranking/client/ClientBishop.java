package dev.killerranking.client;
import dev.killerranking.combat.BishopRules;
import dev.killerranking.network.BishopStatusPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import java.util.*;

public final class ClientBishop {
    private record Snapshot(BishopStatusPayload value,int tick) { }
    private static final Map<UUID,Snapshot> PLAYERS=new HashMap<>();private static int clock;
    public static void set(BishopStatusPayload value){PLAYERS.put(value.player(),new Snapshot(value,clock));}
    public static void clear(){PLAYERS.clear();clock=0;BishopBarrageVfx.clear();}
    public static void tick(){clock++;PLAYERS.values().removeIf(s->clock-s.tick()>50);}
    private static Snapshot own(){var p=Minecraft.getInstance().player;return p==null?null:PLAYERS.get(p.getUUID());}
    public static boolean branch(){var s=own();return s!=null&&s.value().branch();}
    public static boolean unlocked(){var s=own();return s!=null&&s.value().unlocked();}
    public static boolean otherOwned(){var s=own();return s!=null&&s.value().otherOwned();}
    public static boolean enabled(UUID player){var s=PLAYERS.get(player);return s!=null&&s.value().enabled();}
    public static int castTicks(){var s=own();return s==null?0:Math.max(0,s.value().castTicks()-(clock-s.tick()));}
    public static int count(){var s=own();return s==null?0:s.value().count();}
    public static int remaining(){var s=own();return s==null?0:Math.max(0,s.value().remaining()-(clock-s.tick()));}
    public static int cooldown(){var s=own();return s==null?0:Math.max(0,s.value().cooldown()-(clock-s.tick()));}
    public static int barrageTicks(){var s=own();return s==null?0:Math.max(0,s.value().barrageTicks()-(clock-s.tick()));}
    public static int barrageCooldown(){var s=own();return s==null?0:Math.max(0,s.value().barrageCooldown()-(clock-s.tick()));}
    public static boolean hasDesignation(){var s=own();return s!=null&&!s.value().targetName().isEmpty();}
    public static Component designationStatus(){var s=own();String name=s==null?"":s.value().targetName();return Component.literal(!TraitVisuals.enabled()?"꺼짐":name.isEmpty()?"대상 없음":name.length()>10?name.substring(0,10)+"…":name);}
    public static float barrageProgress(){return barrageTicks()>0?barrageTicks()/(float)dev.killerranking.combat.BishopBarrageRules.DURATION:Math.clamp(1-barrageCooldown()/(float)BishopRules.cooldown(ClientCombatStatus.level()),0,1);}
    public static Component barrageStatus(){return Component.literal(barrageTicks()>0?"베기 · "+CombatHud.seconds(barrageTicks()):barrageCooldown()>0?CombatHud.seconds(barrageCooldown()):TraitVisuals.enabled()?"준비":"꺼짐");}
    public static float progress(){return Math.clamp(1-cooldown()/(float)BishopRules.cooldown(ClientCombatStatus.level()),0,1);}
    public static Component status(){return Component.literal(castTicks()>0?"소환 중":count()>0?count()+"명 · "+(cooldown()>0?CombatHud.seconds(cooldown()):"준비"):cooldown()>0?CombatHud.seconds(cooldown()):TraitVisuals.enabled()?"준비":"꺼짐");}
    private ClientBishop() { }
}
