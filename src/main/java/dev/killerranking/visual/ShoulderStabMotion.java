package dev.killerranking.visual;
import dev.killerranking.combat.ShoulderStabRules;
import net.minecraft.world.phys.Vec3;
import dev.killerranking.visual.HeavyPunchGrip.Limb;
import static dev.killerranking.visual.HeavyPunchGrip.rotate;

/** Rigid block limbs, a high reverse-grip plunge and a planted wide stance. */
public final class ShoulderStabMotion {
    public static float strike(float age){return (float)Math.pow(Math.clamp((age-6)/2,0,1),1.8);}
    public static WasabariMotion.Pose pose(float age,boolean left){
        double load=smooth(age/4),hit=strike(age),recover=smooth((age-16)/8),weight=1-recover;
        var rotation=new Vec3(.03+.12*hit,-.30*load+.48*hit,.07*load-.10*hit).scale(weight);
        var hip=new Vec3(0,12,0);var torso=new Limb(RushTorso.pivot(hip,rotation),rotation);
        var head=new Limb(torso.pivot(),new Vec3(.04+.16*hit,rotation.y*.6,rotation.z*.4));
        var palm=new Vec3(-5,12,0).lerp(new Vec3(-6,-8,2),load).lerp(new Vec3(-3,-1,-10),hit).lerp(new Vec3(-5,12,0),recover);
        var shoulder=torso.point(-5,2,0);var right=arm(palm,palm.subtract(shoulder),-1);
        var braceDir=new Vec3(.15,.90,-.4).normalize();var other=arm(torso.point(5,2,0).add(braceDir.scale(10)),braceDir,1);
        double stance=load*weight;
        var rleg=new Limb(new Vec3(-2.5,12,-1.5*stance),new Vec3(-.14*stance,0,.13*stance));
        var lleg=new Limb(new Vec3(2.5,12,1.5*stance),new Vec3(.14*stance,0,-.13*stance));
        return left?new WasabariMotion.Pose(mirror(torso),mirror(head),mirror(other),mirror(right),mirror(lleg),mirror(rleg))
            :new WasabariMotion.Pose(torso,head,right,other,rleg,lleg);
    }
    public static WasabariMotion.Pose contact(float age,boolean left,Vec3 wound,Vec3 direction,float centre){
        return contact(age,left,wound,direction,centre,false);
    }
    public static WasabariMotion.Pose contact(float age,boolean left,Vec3 wound,Vec3 direction,float centre,boolean firstPerson){
        var base=pose(age,left);direction=direction.normalize();
        double withdrawal=smooth((age-ShoulderStabRules.WITHDRAW)/3);
        var palm=wound.subtract(direction.scale(SalmosaKnifeGeometry.SKILL_REACH+6*(1-strike(age))+5*withdrawal));
        // The rigid upper arm sits outside the first-person camera. The palm and
        // actual dagger tip still land at the same world-space shoulder contact.
        var shoulder=base.torso().point(left?(firstPerson?18:5):-(firstPerson?18:5),firstPerson?8:2,firstPerson?4:0);
        var knife=arm(palm,palm.subtract(shoulder),left?centre:-centre);
        double recovery=smooth((age-17)/7);
        var rest=left?base.leftArm():base.rightArm();
        knife=new Limb(knife.pivot().lerp(rest.pivot(),recovery),knife.rotation().lerp(rest.rotation(),recovery));
        return new WasabariMotion.Pose(base.torso(),base.head(),left?base.rightArm():knife,left?knife:base.leftArm(),base.rightLeg(),base.leftLeg());
    }
    public static Limb arm(Vec3 palm,Vec3 direction,double centre){
        var d=direction.normalize();var rotation=new Vec3(Math.atan2(d.z,Math.hypot(d.x,d.y)),0,Math.atan2(-d.x,d.y));
        return new Limb(palm.subtract(rotate(new Vec3(centre,10,0),rotation)),rotation);
    }
    public static WasabariMotion.Pose counterPose(float age,boolean left){
        float sample=age<6?age*4/6:age<10?4+(age-6)/2:age<12?6+age-10:age<20?8+age-12:16+(age-20)*8/6;
        var base=pose(sample,left);double airborne=smooth(age/4)*(1-smooth((age-10)/2));
        var right=new Limb(base.rightLeg().pivot(),base.rightLeg().rotation().add(-.72*airborne,0,0));
        var other=new Limb(base.leftLeg().pivot(),base.leftLeg().rotation().add(.54*airborne,0,0));
        return new WasabariMotion.Pose(base.torso(),base.head(),base.rightArm(),base.leftArm(),left?other:right,left?right:other);
    }
    public static WasabariMotion.Pose counterContact(float age,boolean left,Vec3 wound,Vec3 direction,Vec3 airCatch,float centre,boolean firstPerson){
        var base=counterPose(age,left);var rest=left?base.leftArm():base.rightArm();direction=direction.normalize();
        var contact=wound.subtract(direction.scale(SalmosaKnifeGeometry.SKILL_REACH));
        var palm=age<6?rest.point(left?centre:-centre,10,0).lerp(airCatch,smooth((age-4)/2))
            :airCatch.lerp(contact,Math.pow(Math.clamp((age-10)/2,0,1),1.8));
        palm=palm.subtract(direction.scale(5*smooth((age-20)/3)));
        var shoulder=base.torso().point(left?(firstPerson?18:5):-(firstPerson?18:5),firstPerson?8:2,firstPerson?4:0);
        var hand=arm(palm,palm.subtract(shoulder),left?centre:-centre);
        double recovery=smooth((age-23)/3);hand=new Limb(hand.pivot().lerp(rest.pivot(),recovery),hand.rotation().lerp(rest.rotation(),recovery));
        return new WasabariMotion.Pose(base.torso(),base.head(),left?base.rightArm():hand,left?hand:base.leftArm(),base.rightLeg(),base.leftLeg());
    }
    private static Limb mirror(Limb limb){return new Limb(new Vec3(-limb.pivot().x,limb.pivot().y,limb.pivot().z),new Vec3(limb.rotation().x,-limb.rotation().y,-limb.rotation().z));}
    private static double smooth(double t){t=Math.clamp(t,0,1);return t*t*(3-2*t);}
    private ShoulderStabMotion(){ }
}
