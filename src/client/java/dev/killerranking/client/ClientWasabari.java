package dev.killerranking.client;
import dev.killerranking.combat.WasabariRules;
import dev.killerranking.network.WasabariStatusPayload;
public final class ClientWasabari {
    private static WasabariStatusPayload value;private static int elapsed;
    public static void set(WasabariStatusPayload p){value=p;elapsed=0;}
    public static void clear(){value=null;elapsed=0;}
    public static void tick(){if(value!=null&&elapsed<Integer.MAX_VALUE)elapsed++;}
    public static boolean available(){return value!=null&&value.available();}
    public static int remaining(){return value==null?0:Math.max(0,value.remaining()-elapsed);}
    public static int cooldown(){return value==null?0:Math.max(0,value.cooldown()-elapsed);}
    public static float progress(){return dev.killerranking.combat.CombatRules.cooldownProgress(cooldown(),WasabariRules.COOLDOWN);}
    private ClientWasabari() { }
}
