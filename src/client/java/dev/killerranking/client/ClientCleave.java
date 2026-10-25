package dev.killerranking.client;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.CleaveRules;
import dev.killerranking.network.CleaveStatusPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
public final class ClientCleave {
    private static CleaveStatusPayload value;private static int elapsed;
    public static void set(CleaveStatusPayload p){value=p;elapsed=0;}
    public static void clear(){value=null;elapsed=0;}
    public static void tick(){if(value!=null&&elapsed<Integer.MAX_VALUE)elapsed++;}
    public static boolean branch(){return value!=null&&value.branch();}
    public static boolean unlocked(){return value!=null&&value.unlocked();}
    public static boolean otherOwned(){return value!=null&&value.otherOwned();}
    public static int cooldown(){return value==null?0:Math.max(0,value.cooldown()-elapsed);}
    public static int phase(){return value==null?0:value.phase();}
    public static int age(){return value==null?0:value.age()+elapsed;}
    public static float progress(){return !unlocked()?0:phase()==CleaveRules.CHARGING?Math.min(1,age()/(float)CleaveRules.CHARGE):phase()==CleaveRules.SLASHING?Math.max(0,1-age()/(float)CleaveRules.CAST):1-cooldown()/(float)CleaveRules.COOLDOWN;}
    public static Component status(){
        if(phase()==CleaveRules.CHARGING)return Component.literal(age()>=CleaveRules.CHARGE?"완충 · 놓으면 제자리":"충전 "+CombatHud.seconds(Math.min(age(),CleaveRules.CHARGE)));
        if(phase()==CleaveRules.SLASHING)return Component.literal("베기");
        if(cooldown()>0)return Component.literal(CombatHud.seconds(cooldown()));
        if(!TraitVisuals.enabled())return Component.literal("꺼짐");
        var p=Minecraft.getInstance().player;if(p!=null&&!p.getMainHandItem().is(KillerRankingMod.PAWN6_KATANA))return Component.literal("카타나 필요");
        return Component.literal("준비");
    }
    private ClientCleave() { }
}
