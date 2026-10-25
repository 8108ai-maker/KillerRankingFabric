package dev.killerranking.client;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.*;
import dev.killerranking.network.KnifePursuitStatusPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class ClientKnifePursuit {
    private static KnifePursuitStatusPayload value;private static int elapsed;
    public static void set(KnifePursuitStatusPayload status){value=status;elapsed=0;}
    public static void clear(){value=null;elapsed=0;}
    public static void tick(){if(value!=null&&elapsed<Integer.MAX_VALUE)elapsed++;}
    public static boolean available(){return value!=null&&value.available();}
    public static int phase(){return value==null?0:value.phase();}
    public static int age(){return value==null?0:value.age()+elapsed;}
    public static int cooldown(){return value==null?0:Math.max(0,value.cooldown()-elapsed);}
    public static boolean deflected(){return phase()==KnifePursuitRules.DEFLECTED;}
    public static boolean counterReady(){return deflected()&&KnifePursuitRules.counterWindow(age());}
    public static LivingEntity selected(){var p=Minecraft.getInstance().player;return p==null?null:KnifePursuitTargeting.selected(p);}
    public static boolean outlines(Entity e){return phase()==KnifePursuitRules.AIM&&e==selected();}
    public static float progress(){return deflected()?Math.max(0,1-age()/(float)KnifePursuitRules.AIR_DURATION):phase()==KnifePursuitRules.AIM?1:phase()==KnifePursuitRules.CAST?
        Math.max(0,1-age()/(float)KnifePursuitRules.DURATION):CombatRules.cooldownProgress(cooldown(),KnifePursuitRules.COOLDOWN);}
    public static Component status(){
        if(deflected())return Component.literal(counterReady()?"C: 공중에서 칼잡기":age()<KnifePursuitRules.COUNTER_OPEN?"칼이 뜨는 중":"연계 타이밍 지남");
        if(phase()==KnifePursuitRules.AIM)return Component.literal(selected()==null?"12m 안의 상대 지정":"놓으면 칼날 추격");
        if(phase()==KnifePursuitRules.CAST)return Component.literal("칼 투척 · 접근 · 뽑기");
        if(cooldown()>0)return Component.literal(CombatHud.seconds(cooldown()));
        if(!TraitVisuals.enabled())return Component.literal("꺼짐");
        var p=Minecraft.getInstance().player;
        return Component.literal(p!=null&&!p.getMainHandItem().is(KillerRankingMod.SALMOSA_MID_DAGGER)?"중단검 필요":"준비");
    }
    private ClientKnifePursuit() { }
}
