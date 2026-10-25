package dev.killerranking.client;
import dev.killerranking.combat.RookRules;
import dev.killerranking.network.RookStatusPayload;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class ClientRook {
    private static RookStatusPayload value;private static int elapsed;
    public static void set(RookStatusPayload status){value=status;elapsed=0;}
    public static void clear(){value=null;elapsed=0;}
    public static void tick(){if(value!=null&&elapsed<Integer.MAX_VALUE)elapsed++;}
    public static boolean branch(){return value!=null&&value.branch();}
    public static boolean unlocked(){return value!=null&&value.unlocked();}
    public static boolean otherOwned(){return value!=null&&value.otherOwned();}
    public static int phase(){return value==null?0:value.phase();}
    public static boolean choice(){return phase()==RookRules.CHOICE;}
    public static boolean locked(){var p=Minecraft.getInstance().player;return p!=null&&(WasabariVfx.immobilized(p)||ClientWasabari.remaining()>0||HeavyPunchVfx.locked(p))||value!=null&&(value.captured()||phase()>=RookRules.CHOICE);}
    public static boolean captured(){var p=Minecraft.getInstance().player;return p!=null&&(WasabariVfx.captured(p.getUUID())||HeavyPunchVfx.captured(p))||value!=null&&value.captured();}
    public static int remaining(){return value==null?0:Math.max(0,value.remaining()-elapsed);}
    public static int cooldown(){return value==null?0:Math.max(0,value.cooldown()-elapsed);}
    public static float progress(){return phase()>0?(float)remaining()/RookRules.duration(phase()):dev.killerranking.combat.CombatRules.cooldownProgress(cooldown(),RookRules.COOLDOWN);}
    public static Entity target(){var c=Minecraft.getInstance();return c.level==null||value==null?null:c.level.getEntity(value.target());}
    public static LivingEntity selected(){
        var c=Minecraft.getInstance();var p=c.player;if(p==null||c.level==null)return null;
        var eye=p.getEyePosition();var end=eye.add(p.getLookAngle().scale(RookRules.REACH));
        return c.level.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(RookRules.REACH),e->e!=p&&e.isAlive()&&!e.isSpectator()&&e.isAttackable()&&!e.isPassenger()&&!e.isVehicle()
            &&!p.isAlliedTo(e)&&p.canAttack(e)&&!(e instanceof Player other&&other.isCreative())&&!RookVfx.captured(e.getUUID())
            &&p.distanceToSqr(e)<=4&&p.hasLineOfSight(e)&&e.getBoundingBox().inflate(.12).clip(eye,end).isPresent())
            .stream().min(Comparator.comparingDouble(e->e.getBoundingBox().inflate(.12).clip(eye,end).orElse(end).distanceToSqr(eye))).orElse(null);
    }
    public static boolean outlines(Entity e){return phase()==RookRules.AIM&&e==selected();}
    public static Component status(){
        if(phase()==RookRules.AIM)return Component.literal(selected()==null?"2m 안에서 지정":"놓으면 잡기");
        if(choice())return Component.literal("선택 "+CombatHud.seconds(remaining()));
        if(phase()>RookRules.CHOICE)return Component.literal(switch(phase()){case RookRules.THROW->"던지기";case RookRules.PUNCH->"주먹 연속타";default->"올라타기";});
        if(cooldown()>0)return Component.literal(CombatHud.seconds(cooldown()));
        return Component.literal(!TraitVisuals.enabled()?"꺼짐":unlocked()?"준비":"해금 1,000P");
    }
    private ClientRook(){}
}
