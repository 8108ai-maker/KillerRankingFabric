package dev.killerranking.visual;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import java.util.Set;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Compact view-only poses; attack clocks, wrists and third-person animation stay intact. */
public final class FirstPersonWeaponPose {
    public static final float NEUNGSA_VIEW_DOWN=1.5f;
    public static final float NEUNGSA_VIEW_FORWARD=8.8f;
    public static final float NEUNGSA_IAIDO_VIEW_FORWARD=16;
    private static final Set<String> ATTACKS=Set.of(
        "night_bat_forehand","night_bat_backhand","night_bat_slam",
        "gossi_knife_slash_right","gossi_knife_slash_left","gossi_knife_stab",
        "salmosa_dagger_forehand","salmosa_dagger_backhand","salmosa_dagger_stab",
        "eunbal_hatchet_forehand","eunbal_hatchet_backhand","eunbal_hatchet_cleave",
        "yuhyeolmoki_saw_forehand","yuhyeolmoki_saw_backhand","yuhyeolmoki_saw_cleave");
    private static final Set<String> FISTS=Set.of("tteokbal_fist_guard","tteokbal_fist_right",
        "tteokbal_fist_left","tteokbal_fist_cross");

    public static boolean supports(Object name) {
        if(!(name instanceof String s)) return false;
        // PlayerAnimator's legacy JSON reader retains quotes in animation names.
        if(s.length()>1&&s.startsWith("\"")&&s.endsWith("\"")) s=s.substring(1,s.length()-1);
        return FirstPersonCombatRig.supports(s)||s.equals("salmosa_knife_pursuit")||s.equals("madness_leap")||ATTACKS.contains(s)||isKatana(s)||isTteokbalFist(s);
    }

    public static boolean isKatana(Object name) {
        return name instanceof String s && (s.replace("\"", "").startsWith("pawn6_katana_")
            || s.replace("\"", "").startsWith("neungsa_katana_"));
    }

    public static boolean isTteokbalFist(Object name) {
        return name instanceof String s&&FISTS.contains(s.replace("\"", ""));
    }

    public static Vec3f adjust(Object name,String part,TransformType type,Vec3f value,float tick,int end,int stop) {
        if(FirstPersonCombatRig.supports(name))return FirstPersonCombatRig.adjust(name,part,type,value,tick,end,stop);
        if(isChargedIaido(name)) {
            // Put both anchored shoulders in one camera rig. Rotate that whole
            // rig, never the wrist independently of the forearm or its sword.
            if(part.equals("body")||part.equals("torso"))
                return type==TransformType.SCALE?value:Vec3f.ZERO;
            if(part.equals("rightArm")||part.equals("leftArm")) {
                if(type==TransformType.POSITION) {
                    var root=iaidoViewFrame(name,tick).transform(new Vector3f(value.getX(),value.getY(),value.getZ()));
                    return new Vec3f(root.x+iaidoViewSide(name,tick),root.y+iaidoViewDown(name,tick),root.z-iaidoViewForward(name,tick));
                }
                if(type==TransformType.ROTATION) return iaidoViewRotation(name,value,tick);
            }
            return value;
        }
        if(name instanceof String s&&s.replace("\"", "").equals("madness_leap")) {
            // Aerial turns are visible from outside; the camera and view arms retain a stable upright frame.
            if(part.equals("body")||part.equals("torso"))return type==TransformType.SCALE?new Vec3f(1,1,1):Vec3f.ZERO;
            if(type==TransformType.POSITION&&(part.equals("rightArm")||part.equals("leftArm")))
                return new Vec3f(value.getX(),value.getY()+4.5f,value.getZ()-5);
            return value;
        }
        if(isTteokbalFist(name)) {
            // Translate the skin arm and its attached wrap together. Keeping
            // rotation and scale intact retains the fitted fist and punch recoil.
            if(type==TransformType.POSITION&&(part.equals("rightArm")||part.equals("leftArm")))
                return new Vec3f(value.getX(),value.getY()+3.5f,value.getZ()-4.4f);
            return value;
        }
        // A shared translation keeps the second palm attached to the hilt.
        if(isKatana(name)) {
            boolean neungsa = name instanceof String s && s.replace("\"", "").startsWith("neungsa_katana_");
            if(type==TransformType.POSITION&&(part.equals("rightArm")||part.equals("leftArm")))
                return new Vec3f(value.getX(),value.getY()+(neungsa?NEUNGSA_VIEW_DOWN:6),value.getZ()-(neungsa?NEUNGSA_VIEW_FORWARD:8));
            return value;
        }
        float weight=smooth(tick/2f)*(1-smooth((tick-end)/Math.max(1f,stop-end)));
        if(weight<=0) return value;
        float x=value.getX(),y=value.getY(),z=value.getZ();
        boolean bat=name instanceof String s&&s.contains("night_bat_");
        Vec3f target=value;
        if(part.equals("rightArm")) {
            target=switch(type) {
                // Forward and below eye level, with a shorter shoulder excursion.
                case POSITION -> new Vec3f((bat?-9f:-6f)+(x+5)*.55f,3.6f+(y-2)*.55f,(bat?-6.2f:-3.2f)+z*.4f);
                case ROTATION -> new Vec3f(-.95f+(x+.95f)*.72f,y*.65f,z*.65f);
                case SCALE -> new Vec3f(1+(x-1)*.35f,1+(y-1)*.35f,1+(z-1)*.35f);
                default -> value;
            };
        } else if(part.equals("leftArm")) {
            target=switch(type) {
                case POSITION -> new Vec3f(5+(x-5)*.4f,3.6f+(y-2)*.4f,-1.5f+z*.4f);
                case ROTATION -> new Vec3f(x*.4f,y*.4f,z*.4f);
                default -> value;
            };
        } else if(part.equals("body")||part.equals("torso")) {
            if(type==TransformType.ROTATION||type==TransformType.POSITION&&part.equals("body"))
                target=new Vec3f(x*.25f,y*.25f,z*.25f);
        }
        return target==value?value:new Vec3f(x+(target.getX()-x)*weight,y+(target.getY()-y)*weight,z+(target.getZ()-z)*weight);
    }
    private static float smooth(float t) { t=Math.max(0,Math.min(1,t));return t*t*(3-2*t); }
    public static boolean isChargedIaido(Object name) {
        if(!(name instanceof String s))return false;
        s=s.replace("\"", "");
        return s.startsWith("neungsa_katana_iaido_charge")||s.startsWith("neungsa_katana_iaido_dash");
    }
    private static float iaidoViewWeight(Object name,float tick){
        if(!(name instanceof String s)||!s.replace("\"", "").startsWith("neungsa_katana_iaido_dash"))return 0;
        float t=tick/32;
        return t<=1.5f?0:t<=2.5f?smooth(t-1.5f):t<=NeungsaIaidoDrawMotion.HOLD_END?1:t<=NeungsaIaidoDrawMotion.ALIGN_END?1-smooth((t-NeungsaIaidoDrawMotion.HOLD_END)/4):0;
    }
    public static float iaidoViewDown(Object name,float tick){return NEUNGSA_VIEW_DOWN+12.5f*iaidoViewWeight(name,tick);}
    public static float iaidoViewSide(Object name,float tick){return 4*iaidoViewWeight(name,tick);}
    public static float iaidoViewForward(Object name,float tick){return NEUNGSA_IAIDO_VIEW_FORWARD+8*iaidoViewWeight(name,tick);}
    public static Quaternionf iaidoViewFrame(Object name,float tick){
        return new Quaternionf().slerp(new Quaternionf().rotationZ(.2f).rotateX(-.9f).rotateY(-.4f),iaidoViewWeight(name,tick));
    }
    private static Vec3f iaidoViewRotation(Object name,Vec3f arm,float tick){
        float weight=iaidoViewWeight(name,tick);
        if(weight==0)return arm;
        var adapted=iaidoViewFrame(name,tick).mul(new Quaternionf().rotationZYX(arm.getZ(),arm.getY(),arm.getX()));
        float x=(float)Math.atan2(2*(adapted.w*adapted.x+adapted.y*adapted.z),1-2*(adapted.x*adapted.x+adapted.y*adapted.y));
        float y=(float)Math.asin(Math.clamp(2*(adapted.w*adapted.y-adapted.z*adapted.x),-1,1));
        float z=(float)Math.atan2(2*(adapted.w*adapted.z+adapted.x*adapted.y),1-2*(adapted.y*adapted.y+adapted.z*adapted.z));
        return new Vec3f(x,y,z);
    }
    private FirstPersonWeaponPose() { }
}
