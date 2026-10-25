package dev.killerranking.visual;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** A camera rig carries both shoulders, palms and blades through the same rotation. */
public final class FirstPersonCombatRig {
    public static String name(Object name) { return name instanceof String s?s.replace("\"",""):""; }
    public static boolean supports(Object source) {
        String n=name(source);
        if(n.startsWith("parry")||n.equals("neungsa_katana_parry")||n.equals("neungsa_katana_parry_reverse"))return true;
        if(n.startsWith("rook_glove_"))return n.endsWith("right")||n.endsWith("left");
        if(n.startsWith("tteokbal_fist_"))return !n.endsWith("guard");
        if(n.startsWith("pawn6_katana_")||n.startsWith("neungsa_katana_"))
            return n.contains("slash_")||n.endsWith("overhead")||n.endsWith("thrust")||n.equals("neungsa_katana_cleave")||n.equals("neungsa_katana_draw_cleave");
        return n.startsWith("night_bat_")||n.startsWith("gossi_knife_")
            ||n.startsWith("salmosa_dagger_")||n.startsWith("eunbal_hatchet_")||n.startsWith("yuhyeolmoki_saw_");
    }
    public static boolean fittedFist(Object source) {
        String n=name(source);return n.startsWith("tteokbal_fist_")||n.startsWith("rook_glove_")||n.startsWith("parry_tteokbal");
    }
    private static float smooth(float p){p=Math.clamp(p,0,1);return p*p*(3-2*p);}
    public static float weight(Object source,float tick,int end,int stop) {
        String n=name(source);
        if(n.startsWith("parry"))return smooth(tick/6)*(1-smooth((tick-end)/Math.max(1,stop-end)));
        // Draw/carry and fitted wraps already have an authored idle below them.
        if(n.startsWith("neungsa_")||n.startsWith("pawn6_")||fittedFist(n))return 1;
        return smooth(tick/2)*(1-smooth((tick-end)/Math.max(1,stop-end)));
    }
    public static Quaternionf frame(Object source,float tick,int end,int stop) {
        String n=name(source);float p=Math.clamp(tick/Math.max(1,end),0,1);
        float windup=smooth(p/.3f)*(1-smooth((p-.3f)/.28f));
        float follow=smooth((p-.42f)/.2f)*(1-smooth((p-.72f)/.28f));
        float side=n.endsWith("left")||n.endsWith("backhand")?-1:1;
        boolean fist=fittedFist(n),katana=n.contains("katana"),parry=n.contains("parry");
        float pitch=n.startsWith("rook_")?-.42f:fist?-.08f:katana?-.26f:-.18f;
        float yaw=side*(windup*.08f-follow*.06f);
        float roll=side*(windup*.05f-follow*.09f);
        if(parry){pitch=-.12f;yaw=-.08f*windup;roll=.06f*windup;}
        if(n.startsWith("parry")){pitch=0;yaw=0;roll=0;}
        return new Quaternionf().slerp(new Quaternionf().rotationZYX(roll,yaw,pitch),weight(n,tick,end,stop));
    }
    public static Vector3f offset(Object source,float tick,int end,int stop) {
        String n=name(source);float p=Math.clamp(tick/Math.max(1,end),0,1);
        if(n.startsWith("parry")){float w=weight(n,tick,end,stop);float height=n.startsWith("parry_hand")||n.startsWith("parry_tteokbal")?0:-1;return new Vector3f(0,height*w,-3.5f*w);}
        float strike=smooth((p-.30f)/.16f)*(1-smooth((p-.62f)/.38f));
        boolean fist=fittedFist(n),katana=n.contains("katana"),bat=n.startsWith("night_bat_");
        float down=n.startsWith("rook_")?1:fist?4:katana?4:1.5f;
        // Framed at1920x1080: the old4:3-only staging pushed the palms too far away.
        // Bring the complete shoulder/palm/blade rig closer; no limb or item scaling.
        float forward=fist?4.4f:katana?10.4f:bat?7.2f:5.6f;
        // A short forward impulse, then an eased recoil. It never scales a limb.
        float w=weight(n,tick,end,stop);
        return new Vector3f(0,(down+.6f*strike)*w,-(forward+.96f*strike)*w);
    }
    public static Vec3f adjust(Object source,String part,TransformType type,Vec3f value,float tick,int end,int stop) {
        if(part.equals("body")||part.equals("torso"))return type==TransformType.SCALE?value:Vec3f.ZERO;
        if(!part.equals("rightArm")&&!part.equals("leftArm"))return value;
        if(type==TransformType.SCALE) {
            // Preserve the user's shorter fitted fists; discard legacy attack stretch.
            return fittedFist(source)?value:new Vec3f(Math.min(1,value.getX()),Math.min(1,value.getY()),Math.min(1,value.getZ()));
        }
        float w=weight(source,tick,end,stop);if(w==0)return value;
        var q=frame(source,tick,end,stop);
        if(type==TransformType.POSITION) {
            var root=q.transform(new Vector3f(value.getX(),value.getY()-2,value.getZ())).add(offset(source,tick,end,stop));
            return new Vec3f(root.x,root.y+2,root.z);
        }
        if(type==TransformType.ROTATION)return euler(q.mul(new Quaternionf().rotationZYX(value.getZ(),value.getY(),value.getX())));
        return value;
    }
    private static Vec3f euler(Quaternionf q) {
        return new Vec3f((float)Math.atan2(2*(q.w*q.x+q.y*q.z),1-2*(q.x*q.x+q.y*q.y)),
            (float)Math.asin(Math.clamp(2*(q.w*q.y-q.z*q.x),-1,1)),
            (float)Math.atan2(2*(q.w*q.z+q.x*q.y),1-2*(q.y*q.y+q.z*q.z)));
    }
    private FirstPersonCombatRig(){ }
}
