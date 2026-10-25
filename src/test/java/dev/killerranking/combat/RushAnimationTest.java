package dev.killerranking.combat;

import dev.killerranking.visual.RushBodyShot;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import dev.kosmx.playerAnim.core.util.Vec3f;
import java.nio.file.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RushAnimationTest {
    @Test void strikingBlockArmsActuallyDetachFromTheShouldersWithoutScaling() throws Exception {
        for(String name:new String[]{"rush","rush_echo"}){
            var motion=animation(name);boolean detached=false;
            for(int tick=0;tick<60;tick++){
                motion.setupAnim(0);
                for(String part:new String[]{"rightArm","leftArm"}){
                    var pivot=motion.get3DTransform(part,TransformType.POSITION,0,Vec3f.ZERO);
                    var rotation=motion.get3DTransform(part,TransformType.ROTATION,0,Vec3f.ZERO);
                    if(Math.abs(rotation.getZ())<.08){
                        assertTrue(pivot.getZ()<-6,"Punching arm root moves out in front of the body instead of staying attached");
                        detached=true;
                    }
                    var scale=motion.get3DTransform(part,TransformType.SCALE,0,new Vec3f(1,1,1));
                    assertEquals(1,scale.getX());assertEquals(1,scale.getY());assertEquals(1,scale.getZ());
                }
                motion.tick();
            }
            assertTrue(detached);
        }
    }
    static KeyframeAnimationPlayer animation(String name) throws Exception {
        try(var r=Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/"+name+".json"))){return new KeyframeAnimationPlayer(AnimationSerializing.deserializeAnimation(r).getFirst());}
    }
    static float fold(KeyframeAnimationPlayer p,boolean left,float delta){
        p.setupAnim(delta);return p.get3DTransform(left?"rushLeftForearm":"rushRightForearm",TransformType.ROTATION,delta,Vec3f.ZERO).getX();
    }
    static float roll(KeyframeAnimationPlayer p,boolean left,float delta){
        p.setupAnim(delta);return Math.abs(p.get3DTransform(left?"leftArm":"rightArm",TransformType.ROTATION,delta,Vec3f.ZERO).getZ());
    }
    static Vector3f fist(KeyframeAnimationPlayer p,boolean left,float delta){
        return joint(p,left,10,delta);
    }
    static Vector3f joint(KeyframeAnimationPlayer p,boolean left,float y,float delta){
        p.setupAnim(delta);String arm=left?"leftArm":"rightArm";
        var pos=p.get3DTransform(arm,TransformType.POSITION,delta,Vec3f.ZERO);
        var rot=p.get3DTransform(arm,TransformType.ROTATION,delta,Vec3f.ZERO);
        var hips=p.get3DTransform("body",TransformType.ROTATION,delta,Vec3f.ZERO);
        var tip=RushBodyShot.point(y==4?0:left?1:-1,y,0,fold(p,left,delta));
        return new Matrix4f().rotateY(-hips.getY()).translate(pos.getX(),pos.getY(),pos.getZ()).rotateZYX(rot.getZ(),rot.getY(),rot.getX()).transformPosition(new Vector3f((float)tip.x,(float)tip.y,(float)tip.z));
    }
    @Test void fasterPunchesAlternateEveryThreeTicksAndReleaseImmediately() throws Exception {
        var p=animation("rush");assertTrue(roll(p,true,0)<roll(p,false,0));
        for(int i=0;i<3;i++)p.tick();assertTrue(roll(p,false,0)<roll(p,true,0));
        for(int i=0;i<3;i++)p.tick();assertTrue(roll(p,true,0)<roll(p,false,0));
        for(int i=0;i<110;i++)p.tick();var layer=new ModifierLayer<IAnimation>();layer.setAnimation(p);assertTrue(layer.isActive());layer.setAnimation(null);assertFalse(layer.isActive());
    }
    @Test void rigidArmRetractsBeforeDrivingStraightIntoTheCenter() throws Exception {
        var p=animation("rush");var guard=fist(p,false,0);
        p.tick();var loaded=fist(p,false,0);var hand=fist(p,false,0);
        assertTrue(loaded.z>guard.z+5,"Whole block fist retracts before the straight drive");
        p.tick();var impact=fist(p,false,0);
        assertTrue(hand.z-impact.z>12,"Fist is driven forward, rather than swept sideways");
        assertTrue(Math.abs(impact.x-hand.x)<3,"Side travel is small compared with the forward drive");
        assertTrue(impact.x<0&&impact.x>-2,"Contact lands near the target's center");
        for(boolean left:new boolean[]{false,true}){
            var cycle=animation("rush");
            for(int tick=0;tick<24;tick++){
                for(float delta:new float[]{0,.25f,.5f,.75f})assertTrue(left?fist(cycle,left,delta).x>0:fist(cycle,left,delta).x<0,"Fists stay on their side of the center without crossing");
                cycle.tick();
            }
        }
    }
    @Test void rapidDriveHoldsImpactBeforeSlowerRecovery() throws Exception {
        var p=animation("rush");p.tick();var loaded=fist(p,false,0);
        p.tick();var impact=fist(p,false,0);float drive=loaded.distance(impact);
        assertTrue(drive>6,"Fast drive moves farther than any preparation step");
        for(float delta:new float[]{0,.25f,.5f,.75f})assertEquals(0,impact.distance(fist(p,false,delta)),1e-4,"Contact pose holds for a full tick");
        p.tick();assertEquals(0,impact.distance(fist(p,false,0)),1e-4);
        var previous=impact;
        for(int i=0;i<2;i++){
            p.tick();var next=fist(p,false,0);assertTrue(previous.distance(next)<drive*.6,"Arm recovers more slowly than it strikes");previous=next;
        }
    }
    @Test void bodyShotsKeepRigidArmsAndPlantedFeetWhileTheChestTurns() throws Exception {
        var p=animation("rush");for(int i=0;i<4;i++)p.tick();
        float chestLow=10,chestHigh=-10,tipLow=20,tipHigh=-20;
        var baseline=new Vec3f(.12f,.23f,.34f);
        for(int tick=4;tick<76;tick++){
            for(float delta:new float[]{0,.25f,.5f,.75f}){
                p.setupAnim(delta);
                var chest=p.get3DTransform("torso",TransformType.ROTATION,delta,Vec3f.ZERO);
                var hip=p.get3DTransform("body",TransformType.ROTATION,delta,Vec3f.ZERO);
                chestLow=Math.min(chestLow,chest.getY());chestHigh=Math.max(chestHigh,chest.getY());
                for(boolean left:new boolean[]{false,true}){
                    String arm=left?"leftArm":"rightArm";var scale=p.get3DTransform(arm,TransformType.SCALE,delta,new Vec3f(1,1,1));
                    assertEquals(1,scale.getX(),1e-5);assertEquals(1,scale.getY(),1e-5);assertEquals(1,scale.getZ(),1e-5);
                    assertEquals(0,fold(p,left,delta),1e-6,"No elbow deformation channel");
                    // Use the central axis, not the differently offset fingertip helper.
                    var pos=p.get3DTransform(arm,TransformType.POSITION,delta,Vec3f.ZERO);
                    var rot=p.get3DTransform(arm,TransformType.ROTATION,delta,Vec3f.ZERO);
                    var nativeMatrix=new Matrix4f().translation(pos.getX(),pos.getY(),pos.getZ()).rotateZYX(rot.getZ(),rot.getY(),rot.getX());
                    var start=nativeMatrix.transformPosition(new Vector3f(0,-2,0));var end=nativeMatrix.transformPosition(new Vector3f(0,10,0));
                    assertEquals(12,start.distance(end),1e-5,"Rigid block keeps its full native length");
                    var foot=p.get3DTransform(left?"leftLeg":"rightLeg",TransformType.ROTATION,delta,Vec3f.ZERO);assertEquals(0,foot.getX(),1e-5);assertEquals(0,foot.getY(),1e-5);assertEquals(0,foot.getZ(),1e-5);
                    var walking=new Vec3f(left?-.45f:.45f,.03f,-.02f);
                    var liveFoot=p.get3DTransform(left?"leftLeg":"rightLeg",TransformType.ROTATION,delta,walking);
                    assertEquals(walking.getX(),liveFoot.getX(),1e-5,"Native walking pitch remains free");assertEquals(0,liveFoot.getY(),1e-5);assertEquals(0,liveFoot.getZ(),1e-5);
                    assertEquals(0,hip.getX(),1e-5);assertEquals(0,hip.getY(),1e-5);assertEquals(0,hip.getZ(),1e-5);
                }
                if(tick%6==2&&delta==0){var tip=fist(p,false,0);tipLow=Math.min(tipLow,tip.y);tipHigh=Math.max(tipHigh,tip.y);}
                var head=p.get3DTransform("head",TransformType.ROTATION,delta,baseline);assertEquals(baseline.getY(),head.getY(),1e-5);
            }p.tick();
        }
        assertTrue(chestHigh-chestLow>.5,"Chest turn is retained");
        assertTrue(tipHigh-tipLow>2,"Hits change between ribs and abdomen");
    }
    @Test void torsoTurnsAroundConnectedHipsAndKeepsTheHeadAtTheShoulders() throws Exception {
        for(String name:new String[]{"rush","rush_echo"}){
            var p=animation(name);float low=10,high=-10;
            for(int tick=0;tick<72;tick++){
                for(float delta:new float[]{0,.25f,.5f,.75f}){
                    p.setupAnim(delta);var torso=p.get3DTransform("torso",TransformType.POSITION,delta,Vec3f.ZERO);
                    var rot=p.get3DTransform("torso",TransformType.ROTATION,delta,Vec3f.ZERO);
                    var head=p.get3DTransform("head",TransformType.POSITION,delta,Vec3f.ZERO);
                    assertEquals(torso.getX(),head.getX(),1e-5);assertEquals(torso.getY(),head.getY(),1e-5);assertEquals(torso.getZ(),head.getZ(),1e-5);
                    var waist=new Matrix4f().translation(torso.getX(),torso.getY(),torso.getZ()).rotateZYX(rot.getZ(),rot.getY(),rot.getX()).transformPosition(new Vector3f(0,12,0));
                    assertTrue(waist.distance(new Vector3f(0,12,0))<.3,"Fractional keyframe drift remains small before exact native pivot correction");
                    var pinned=dev.killerranking.visual.RushTorso.pivot(new net.minecraft.world.phys.Vec3(0,12,0),new net.minecraft.world.phys.Vec3(rot.getX(),rot.getY(),rot.getZ()));
                    var exact=new Matrix4f().translation((float)pinned.x,(float)pinned.y,(float)pinned.z).rotateZYX(rot.getZ(),rot.getY(),rot.getX()).transformPosition(new Vector3f(0,12,0));
                    assertEquals(0,exact.distance(new Vector3f(0,12,0)),1e-5,"Rendered torso stays exactly connected through fractional frames");
                    low=Math.min(low,rot.getX());high=Math.max(high,rot.getX());
                }p.tick();
            }
            assertTrue(high-low>.2,"Torso still leans through different body shots");
        }
    }
}
