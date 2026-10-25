package dev.killerranking.client;
import dev.killerranking.combat.*;
import dev.killerranking.network.NeungsaStatusPayload;

public final class ClientNeungsaSkill {
    private static NeungsaStatusPayload value;private static int elapsed;
    public static void set(NeungsaStatusPayload status){value=status;elapsed=0;}
    public static void clear(){value=null;elapsed=0;}
    public static void tick(){if(value!=null&&elapsed<Integer.MAX_VALUE)elapsed++;}
    public static boolean branch(){return value!=null&&value.branch();}
    public static boolean unlocked(){return value!=null&&value.unlocked();}
    public static boolean available(){return branch()&&unlocked();}
    public static int remaining(){return value==null?0:Math.max(0,value.remaining()-elapsed);}
    public static int cooldown(){return value==null?0:Math.max(0,value.cooldown()-elapsed);}
    public static float progress(){return CombatRules.cooldownProgress(cooldown(),NeungsaSlashRules.COOLDOWN);}
    public static int iaidoRemaining(){return value==null?0:Math.max(0,value.iaidoRemaining()-elapsed);}
    public static int iaidoCooldown(){return value==null?0:Math.max(0,value.iaidoCooldown()-elapsed);}
    public static int iaidoPhase(){return value==null?0:value.iaidoPhase();}
    public static int iaidoAge(){return value==null?0:Math.min(NeungsaIaidoRules.MAX_HOLD,value.iaidoAge()+elapsed);}
    public static float iaidoProgress(){return iaidoPhase()==NeungsaIaidoRules.CHARGING?Math.clamp(iaidoAge()/(float)NeungsaIaidoRules.CHARGE,0,1):CombatRules.cooldownProgress(iaidoCooldown(),NeungsaIaidoRules.COOLDOWN);}
    public static int echoRemaining(){return value==null?0:Math.max(0,value.echoRemaining()-elapsed);}
    public static int echoCooldown(){return value==null?0:Math.max(0,value.echoCooldown()-elapsed);}
    public static float echoProgress(){return CombatRules.cooldownProgress(echoCooldown(),NeungsaEchoRules.COOLDOWN);}
    private ClientNeungsaSkill(){ }
}
