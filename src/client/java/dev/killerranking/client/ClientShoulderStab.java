package dev.killerranking.client;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.*;
import dev.killerranking.network.ShoulderStabStatusPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class ClientShoulderStab {
    private static ShoulderStabStatusPayload value;private static int elapsed;
    public static void set(ShoulderStabStatusPayload status){value=status;elapsed=0;}
    public static void clear(){value=null;elapsed=0;}
    public static void tick(){if(value!=null&&elapsed<Integer.MAX_VALUE)elapsed++;}
    public static boolean available(){return value!=null&&value.available();}
    public static int phase(){return value==null?0:value.phase();}
    public static int age(){return value==null?0:value.age()+elapsed;}
    public static int cooldown(){return value==null?0:Math.max(0,value.cooldown()-elapsed);}
    public static LivingEntity selected(){var p=Minecraft.getInstance().player;return p==null?null:ShoulderStabTargeting.selected(p);}
    public static boolean outlines(Entity e){return phase()==ShoulderStabRules.AIM&&e==selected();}
    public static float progress(){return phase()==ShoulderStabRules.AIM?1:phase()>=ShoulderStabRules.CAST?
        Math.max(0,1-age()/(float)(phase()==ShoulderStabRules.COUNTER?ShoulderStabRules.COUNTER_DURATION:ShoulderStabRules.DURATION)):CombatRules.cooldownProgress(cooldown(),ShoulderStabRules.COOLDOWN);}
    public static Component status(){
        if(phase()==ShoulderStabRules.COUNTER)return Component.literal("공중 칼잡기 · 내려찍기");
        if(phase()==ShoulderStabRules.AIM)return Component.literal(selected()==null?"12m 안의 상대 지정":"놓으면 어깨 찌르기");
        if(phase()==ShoulderStabRules.CAST)return Component.literal("순간 접근 · 어깨 찌르기");
        if(cooldown()>0)return Component.literal(CombatHud.seconds(cooldown()));
        if(!TraitVisuals.enabled())return Component.literal("꺼짐");
        var p=Minecraft.getInstance().player;
        return Component.literal(p!=null&&!p.getMainHandItem().is(KillerRankingMod.SALMOSA_MID_DAGGER)?"중단검 필요":"준비");
    }
    private ClientShoulderStab() { }
}
