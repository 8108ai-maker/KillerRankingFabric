package dev.killerranking.visual;

import dev.killerranking.combat.HeavyPunchRules;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Native pixel-space poses for the two supplied self-wrist-brace reference frames. */
public final class HeavyPunchGrip {
    public record Limb(Vec3 pivot,Vec3 rotation) {
        public Vec3 point(double x,double y,double z){return pivot.add(rotate(new Vec3(x,y,z),rotation));}
        public Vec3 direction(){return rotate(new Vec3(0,1,0),rotation);}
        private Limb mirror(){return new Limb(new Vec3(-pivot.x,pivot.y,pivot.z),new Vec3(rotation.x,-rotation.y,-rotation.z));}
    }
    public record Pose(Limb torso,Limb head,Limb punch,Limb brace,Limb rightLeg,Limb leftLeg) { }
    private static final Vec3 HIP=new Vec3(0,12,0),UPPER_HAND=new Vec3(-3.5,9,-4.8);
    private static final Vec3 PUNCH_SHOULDER=new Vec3(-5,2,0),BRACE_SHOULDER=new Vec3(5,2,0);
    public static Pose pose(float age,boolean leftPunch,boolean firstPerson,float centre){
        double load=smooth(age/24),hit=strike(age),back=smooth((age-46)/8);
        var rotation=new Vec3(.08+.57*load,2.05*load,.05*load).lerp(new Vec3(.72,-.22,.08),hit).scale(1-back);
        var torso=new Limb(RushTorso.pivot(HIP,rotation),rotation);
        var headRotation=new Vec3(rotation.x+.18*load*(1-hit),rotation.y+.18*load*(1-hit),rotation.z)
            .lerp(new Vec3(.92,-.22,.08),hit).scale(1-back);
        var head=new Limb(torso.pivot(),headRotation);
        // Anchor the key poses to the caster, not the victim's distance.
        var loaded=torso.pivot().add(rotate(UPPER_HAND,rotation));
        var raisedRoot=torso.pivot().add(rotate(PUNCH_SHOULDER,rotation)).add(0,-4,-1);
        var extended=new Vec3(.18,.10,-1).normalize();
        var palm=loaded.lerp(raisedRoot.add(extended.scale(10)),hit);
        var punchDirection=rotate(UPPER_HAND.subtract(PUNCH_SHOULDER),rotation).normalize().lerp(extended,hit).normalize();
        var fpOffset=firstPerson?new Vec3(0,3-1.5*hit,-2-Math.max(0,loaded.z+6)*(1-hit)):Vec3.ZERO;
        palm=palm.add(fpOffset).lerp(new Vec3(-3.5,5,-8),back);
        punchDirection=punchDirection.lerp(new Vec3(0,0,-1),back).normalize();
        var punch=arm(palm,punchDirection,-centre);
        var wrist=punch.point(-centre,8.2,0);
        var gripPalm=wrist.add(braceOffset());
        var gripRoot=torso.pivot().add(rotate(BRACE_SHOULDER,rotation)).add(0,2,0).add(fpOffset);
        var gripDirection=gripPalm.subtract(gripRoot).normalize();
        gripPalm=gripPalm.lerp(new Vec3(3.5,5,-8),back);
        gripDirection=gripDirection.lerp(new Vec3(0,0,-1),back).normalize();
        var brace=arm(gripPalm,gripDirection,centre);
        // Plant a wide stance before contact; outward roll keeps the feet apart.
        double stance=load*(1-back),plant=smooth((age-24)/12);
        double step=(1.5+.7*plant)*stance,spread=(.12+.10*plant)*stance;
        var rightLeg=new Limb(new Vec3(-2.5,12,-step),new Vec3(-(.18+.12*plant)*stance,0,spread));
        var leftLeg=new Limb(new Vec3(2.5,12,step),new Vec3((.16+.12*plant)*stance,0,-spread));
        return leftPunch?new Pose(torso.mirror(),head.mirror(),punch.mirror(),brace.mirror(),leftLeg.mirror(),rightLeg.mirror())
            :new Pose(torso,head,punch,brace,rightLeg,leftLeg);
    }
    public static Vec3 braceOffset(){return new Vec3(.85,.85,0);}
    private static Limb arm(Vec3 palm,Vec3 direction,double handX){
        var d=direction.normalize();
        var rotation=new Vec3(Math.atan2(d.z,Math.hypot(d.x,d.y)),0,Math.atan2(-d.x,d.y));
        return new Limb(palm.subtract(rotate(new Vec3(handX,10,0),rotation)),rotation);
    }
    public static Vec3 rotate(Vec3 point,Vec3 rotation){
        var p=new Matrix4f().rotateZYX((float)rotation.z,(float)rotation.y,(float)rotation.x)
            .transformPosition(new Vector3f((float)point.x,(float)point.y,(float)point.z));
        return new Vec3(p.x,p.y,p.z);
    }
    public static float strike(float age){return (float)Math.pow(Math.clamp((age-38)/3,0,1),1.6);}
    private static double smooth(double t){t=Math.clamp(t,0,1);return t*t*(3-2*t);}
    public static float gripWeight(float age){return (float)(Math.clamp(age/6,0,1)*Math.clamp((HeavyPunchRules.RELEASE-age)/2,0,1));}
    private HeavyPunchGrip() { }
}
