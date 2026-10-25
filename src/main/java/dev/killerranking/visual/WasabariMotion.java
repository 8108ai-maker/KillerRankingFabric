package dev.killerranking.visual;
import dev.killerranking.combat.WasabariRules;
import net.minecraft.world.phys.Vec3;
import static dev.killerranking.visual.HeavyPunchGrip.rotate;
import dev.killerranking.visual.HeavyPunchGrip.Limb;

/** Rigid native limbs: cock the foot back, hook forward, then stand and plunge into the held leg. */
public final class WasabariMotion {
    public static final double KNIFE_REACH=SalmosaKnifeGeometry.SKILL_REACH;
    public static final double KNIFE_LIFT=6;
    public record Pose(Limb torso,Limb head,Limb rightArm,Limb leftArm,Limb rightLeg,Limb leftLeg) { }
    public static double grabWeight(float age){return smooth((age-21)/5);}
    public static float raisedLegPitch(float age){return (float)(-1.48*grabWeight(age));}
    /** Keep the opposite palm around the lifted ankle while the knife hand retracts and stabs the same shin. */
    public static Pose hold(Pose base,float age,boolean left,Vec3 ankle,Vec3 shin,Vec3 knifeDirection,float centre){
        knifeDirection=knifeDirection.normalize();
        var knifePalm=shin.subtract(knifeDirection.scale(KNIFE_REACH+KNIFE_LIFT*(1-stabWeight(age))));
        var knifeShoulder=base.torso().point(left?5:-5,2,0);
        var knife=arm(knifePalm,knifePalm.subtract(knifeShoulder).normalize(),left?centre:-centre);
        var braceShoulder=base.torso().point(left?-5:5,2,0);
        var brace=arm(ankle,ankle.subtract(braceShoulder).normalize(),left?-centre:centre);
        double weight=grabWeight(age);
        var right=blend(base.rightArm(),left?brace:knife,weight);
        var other=blend(base.leftArm(),left?knife:brace,weight);
        return new Pose(base.torso(),base.head(),right,other,base.rightLeg(),base.leftLeg());
    }
    private static Limb blend(Limb a,Limb b,double weight){return new Limb(a.pivot().lerp(b.pivot(),weight),a.rotation().lerp(b.rotation(),weight));}
    public static Vec3 knifeDirection(boolean left){return new Vec3(left?-.48:.48,.88,0).normalize();}
    /** Load behind the support leg, kick/hook through on Bari, then plant both feet. */
    public static Vec3 hookFoot(float age){
        if(age<=12)return new Vec3(-2.5,24,0).lerp(new Vec3(-3.3,18,8.5),smooth(age/12));
        if(age<=15)return new Vec3(-3.3,18,8.5);
        if(age<=20)return new Vec3(-3.3,18,8.5).lerp(new Vec3(-2.2,13,-13.5),smooth((age-15)/5));
        return new Vec3(-2.2,13,-13.5).lerp(new Vec3(-3.2,24,-2),smooth((age-20)/6));
    }
    /** Third-person low reap: weight on the planted leg, a small outside arc, then pull across the ankle. */
    public static Pose thirdPersonPose(float age,boolean left){
        if(age>=26)return pose(age,left);
        var base=pose(age,false);
        double load=smooth(age/10)*(1-smooth((age-14)/6));
        double sweep=smooth((age-14)/6)*(1-smooth((age-21)/5));
        var hip=new Vec3(1.35*load+.65*sweep,12+.30*load+.20*sweep,.50*load-.75*sweep);
        var rot=base.torso().rotation().add(-.14*load+.08*sweep,.03*load-.05*sweep,-.10*load+.035*sweep);
        var torso=new Limb(RushTorso.pivot(hip,rot),rot);
        var head=new Limb(torso.pivot(),new Vec3(base.head().rotation().x-.07*load+.05*sweep,rot.y*.65,0));
        var shoulderShift=torso.point(-5,2,0).subtract(base.torso().point(-5,2,0));
        var right=new Limb(base.rightArm().pivot().add(shoulderShift),base.rightArm().rotation());
        // Counterbalance forward instead of throwing the free arm above the head.
        var braceDir=new Vec3(.08,.97,-.23).lerp(new Vec3(.14,.40,-.91),Math.max(load,sweep)*.85).normalize();
        var other=arm(torso.point(5,2,0).add(braceDir.scale(10)),braceDir,1);
        var frontHip=hip.add(-2.5,0,0);
        var rightLeg=leg(thirdPersonFoot(age),frontHip);
        var leftLeg=leg(new Vec3(3.16,23.98,0),hip.add(2.5,0,0));
        if(left)return new Pose(mirror(torso),mirror(head),mirror(other),mirror(right),mirror(leftLeg),mirror(rightLeg));
        return new Pose(torso,head,right,other,rightLeg,leftLeg);
    }
    private static Vec3 thirdPersonFoot(float age){
        var rest=new Vec3(-2.5,24,0);var load=new Vec3(-3.1,20.5,7.5);
        var outside=new Vec3(-4.8,21,-2.5);var contact=new Vec3(-2.3,20.5,-10);
        var follow=new Vec3(-.8,22,-7);var plant=new Vec3(-3.2,24,-2);
        if(age<=12)return rest.lerp(load,smooth(age/12));
        if(age<=14)return load;
        if(age<=17)return load.lerp(outside,smooth((age-14)/3));
        if(age<=20)return outside.lerp(contact,smooth((age-17)/3));
        if(age<=22)return contact.lerp(follow,smooth((age-20)/2));
        return follow.lerp(plant,smooth((age-22)/4));
    }
    private static Limb leg(Vec3 foot,Vec3 hip){
        var d=foot.subtract(hip).normalize();
        var rot=new Vec3(Math.atan2(d.z,Math.hypot(d.x,d.y)),0,Math.atan2(-d.x,d.y));
        return new Limb(foot.subtract(rotate(new Vec3(0,12,0),rot)),rot);
    }
    public static Pose pose(float age,boolean left){
        double hold=smooth((age-21)/5)*(1-smooth((age-60)/6));
        double load=smooth(age/12)*(1-smooth((age-15)/5));
        double kick=smooth((age-15)/5)*(1-smooth((age-20)/6)),jab=stabWeight(age);
        double twist=.16*load-.19*kick+.10*jab*hold;
        var hip=new Vec3(.8*load,12-.4*load,-.7*load);
        var rot=new Vec3(.04+.16*hold+.10*jab*hold,.0+twist,-.05*load);
        var torso=new Limb(RushTorso.pivot(hip,rot),rot);
        var head=new Limb(torso.pivot(),new Vec3(.05+.18*hold,twist*.65,0));
        var shoulder=torso.point(-5,2,0);
        var aim=new Vec3(-.40,-.88,-.26).normalize();
        var punchDir=new Vec3(.06,.97,-.23).lerp(aim,hold).normalize();
        var palm=shoulder.add(punchDir.scale(10));
        var right=arm(palm,punchDir,-1);
        var braceDir=new Vec3(.08,.97,-.23).lerp(new Vec3(.16,-.98,-.12),Math.max(load,kick)).normalize();
        var other=arm(torso.point(5,2,0).add(braceDir.scale(10)),braceDir,1);
        var frontHip=hip.add(-2.5,0,0);
        var restingFoot=frontHip.add(-.7,12,-2);
        var foot=hookFoot(age).lerp(restingFoot,smooth((age-60)/6));
        var footDirection=foot.subtract(frontHip).normalize();
        var footRot=new Vec3(Math.atan2(footDirection.z,Math.hypot(footDirection.x,footDirection.y)),0,Math.atan2(-footDirection.x,footDirection.y));
        var rightLeg=new Limb(foot.subtract(rotate(new Vec3(0,12,0),footRot)),footRot);
        var leftLeg=new Limb(hip.add(2.5,0,0),new Vec3(.025*load,0,-.055));
        if(left)return new Pose(mirror(torso),mirror(head),mirror(other),mirror(right),mirror(leftLeg),mirror(rightLeg));
        return new Pose(torso,head,right,other,rightLeg,leftLeg);
    }
    private static Limb arm(Vec3 palm,Vec3 direction,double x){var d=direction.normalize();var r=new Vec3(Math.atan2(d.z,Math.hypot(d.x,d.y)),0,Math.atan2(-d.x,d.y));return new Limb(palm.subtract(rotate(new Vec3(x,10,0),r)),r);}
    private static Limb mirror(Limb p){return new Limb(new Vec3(-p.pivot().x,p.pivot().y,p.pivot().z),new Vec3(p.rotation().x,-p.rotation().y,-p.rotation().z));}
    public static double stabWeight(double age){for(int i=0;i<WasabariRules.STABS;i++){double t=age-(WasabariRules.FIRST_STAB+i*WasabariRules.STAB_INTERVAL);if(t>=-2&&t<3)return t<0?smooth((t+2)/2):1-smooth(t/3);}return 0;}
    public static double pulse(double age,double beat,double width){return Math.max(0,1-Math.abs(age-beat)/width);}
    private static double smooth(double value){value=Math.clamp(value,0,1);return value*value*(3-2*value);}
    private WasabariMotion() { }
}
