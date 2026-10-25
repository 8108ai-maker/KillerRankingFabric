package dev.killerranking.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.killerranking.visual.CombatCameraMotion;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

/** Camera feedback never writes entity rotation, movement, aim or options. */
public final class CombatCameraFeedback {
    private record Pulse(double born,float power,float side,boolean parry){ }
    private static final ArrayList<Pulse> PULSES=new ArrayList<>();
    private static ClientLevel world;
    private static long parries,fovFrames,shakeFrames;
    private static float minimumFov=1,maximumAngle;
    private static void prepare(Minecraft c){if(world!=c.level){clear();world=c.level;}}
    public static void parry(java.util.UUID owner){
        var c=Minecraft.getInstance();if(c.player==null||c.level==null||!c.player.getUUID().equals(owner))return;
        prepare(c);add(new Pulse(world.getGameTime(),1,1,true));parries++;
    }
    private static float hitSide=1;
    /** A lighter, alternating-side kick for ordinary weapon contacts; heavy families and crits push harder. */
    public static void hit(dev.killerranking.combat.WeaponFamily family,boolean crit){
        var c=Minecraft.getInstance();if(c.player==null||c.level==null)return;
        prepare(c);hitSide=-hitSide;
        float power=switch(family){case BLUNT,HATCHET->.62f;case KNUCKLE->.55f;case SAW->.5f;default->.4f;};
        if(crit)power*=1.35f;
        add(new Pulse(world.getGameTime(),power,hitSide,false));
    }
    private static void add(Pulse p){if(PULSES.size()>=8)PULSES.removeFirst();PULSES.add(p);}
    public static void tick(Minecraft c){prepare(c);if(world==null||c.isPaused())return;PULSES.removeIf(p->world.getGameTime()-p.born>=8);}
    public static CombatCameraMotion.Pose sample(float delta){
        var c=Minecraft.getInstance();prepare(c);
        if(world==null||c.player==null||c.getCameraEntity()!=c.player||!c.options.getCameraType().isFirstPerson()||c.screen!=null||!c.player.isAlive())return CombatCameraMotion.Pose.REST;
        float pitch=0,yaw=0,roll=0,x=0,y=0,fov=1;
        double time=world.getGameTime()+delta;
        for(var p:PULSES){var v=CombatCameraMotion.sample((float)(time-p.born),p.power(),p.side(),p.parry());pitch+=v.pitch();yaw+=v.yaw();roll+=v.roll();x+=v.x();y+=v.y();fov+=v.fov()-1;}
        return new CombatCameraMotion.Pose(Math.clamp(pitch,-1.6f,1.6f),Math.clamp(yaw,-1.2f,1.2f),Math.clamp(roll,-.9f,.9f),Math.clamp(x,-.008f,.008f),Math.clamp(y,-.01f,.01f),Math.clamp(fov,.90f,1.025f));
    }
    public static double fov(double original,float delta){
        var p=sample(delta);if(p.fov()!=1){fovFrames++;minimumFov=Math.min(minimumFov,p.fov());}return original*p.fov();
    }
    public static void shake(PoseStack stack,float delta){
        var p=sample(delta);if(p.equals(CombatCameraMotion.Pose.REST))return;
        stack.translate(p.x(),p.y(),0);stack.mulPose(Axis.XP.rotationDegrees(p.pitch()));stack.mulPose(Axis.YP.rotationDegrees(p.yaw()));stack.mulPose(Axis.ZP.rotationDegrees(p.roll()));
        shakeFrames++;maximumAngle=Math.max(maximumAngle,Math.max(Math.abs(p.pitch()),Math.max(Math.abs(p.yaw()),Math.abs(p.roll()))));
    }
    public static long parries(){return parries;}
    public static long fovFrames(){return fovFrames;}public static long shakeFrames(){return shakeFrames;}
    public static float minimumFov(){return minimumFov;}public static float maximumAngle(){return maximumAngle;}
    public static int activeCount(){return PULSES.size();}
    public static void clear(){PULSES.clear();world=null;parries=fovFrames=shakeFrames=0;minimumFov=1;maximumAngle=0;}
    private CombatCameraFeedback(){ }
}
