package dev.killerranking.combat;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.killerranking.visual.FirstPersonWeaponPose;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CleaveAnimationTest {
    @Test void chargingBladeStaysInFrontOfTheHeadAndBothPalmsGripTheHilt()throws Exception{
        var motion=load("charge");
        for(int tick=0;tick<140;tick++){
            for(float delta:new float[]{0,.25f,.5f,.75f}){
                motion.setupAnim(delta);
                var palms=new org.joml.Vector3f[2];org.joml.Matrix4f weapon=null;
                for(int i=0;i<2;i++){
                    String part=i==0?"rightArm":"leftArm";
                    var pos=motion.get3DTransform(part,TransformType.POSITION,delta,Vec3f.ZERO);
                    var rot=motion.get3DTransform(part,TransformType.ROTATION,delta,Vec3f.ZERO);
                    var matrix=new org.joml.Matrix4f().translation(pos.getX(),pos.getY(),pos.getZ()).rotateZYX(rot.getZ(),rot.getY(),rot.getX());
                    palms[i]=matrix.transformPosition(new org.joml.Vector3f(i==0?-1:1,10,0));
                    if(i==0){
                        var item=motion.get3DTransform("rightItem",TransformType.ROTATION,delta,Vec3f.ZERO);
                        weapon=new org.joml.Matrix4f().rotateZYX(rot.getZ(),rot.getY(),rot.getX()).rotateX(-(float)Math.PI/2).rotateY((float)Math.PI).rotateZYX(item.getZ(),item.getY(),item.getX());
                    }
                }
                var direction=weapon.transformDirection(new org.joml.Vector3f(0,1,0));
                var expected=new org.joml.Vector3f(palms[0]).fma(-4.125f,direction);
                assertTrue(expected.distance(palms[1])<.07,"Both hands stay on the same hilt through interpolated frames");
                for(float along=0;along<=24;along+=.5f){
                    var point=new org.joml.Vector3f(palms[0]).fma(along,direction);
                    assertTrue(point.z<-6,"Entire charged blade clears the head's front face, including the tip");
                }
            }
            motion.tick();
        }
    }
    private KeyframeAnimationPlayer load(String name)throws Exception{
        try(var reader=Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/pawn6_katana_cleave_"+name+".json"))){return new KeyframeAnimationPlayer(AnimationSerializing.deserializeAnimation(reader).getFirst());}
    }
    @Test void chargeCanHoldThroughFiveSecondsAndCutAnimationsFinish()throws Exception{
        var charge=load("charge");for(int i=0;i<220;i++)charge.tick();assertTrue(charge.isActive());
        for(String name:new String[]{"dash","full"}){var cut=load(name);for(int i=0;i<30;i++)cut.tick();assertFalse(cut.isActive());}
    }
    @Test void bothHandsStayBoundedAndCameraHeadRemainsFree()throws Exception{
        var baseline=new Vec3f(.17f,.28f,.39f);
        for(String name:new String[]{"charge","dash","full"}){
            var animation=load(name);assertTrue(FirstPersonWeaponPose.isKatana(animation.getData().extraData.get("name")));
            for(int tick=0;tick<=20;tick++){
                animation.setupAnim(.5f);
                for(String hand:new String[]{"rightArm","leftArm"}){
                    var scale=animation.get3DTransform(hand,TransformType.SCALE,.5f,new Vec3f(1,1,1));
                    assertEquals(1,scale.getX(),.00001);assertEquals(1,scale.getY(),.00001);assertEquals(1,scale.getZ(),.00001);
                    var pos=animation.get3DTransform(hand,TransformType.POSITION,.5f,Vec3f.ZERO);assertTrue(Math.abs(pos.getX())<=7&&Math.abs(pos.getZ())<3);
                }
                var head=animation.get3DTransform("head",TransformType.ROTATION,.5f,baseline);
                assertEquals(baseline.getX(),head.getX(),.00001);assertEquals(baseline.getY(),head.getY(),.00001);assertEquals(baseline.getZ(),head.getZ(),.00001);
                animation.tick();
            }
        }
    }
}
