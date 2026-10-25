package dev.killerranking.combat;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import dev.kosmx.playerAnim.core.util.Vec3f;
import java.nio.file.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BishopAnimationTest {
    private static KeyframeAnimationPlayer clip(String name,int tick,float partial)throws Exception{
        try(var reader=Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/"+name+".json"))){
            var a=new KeyframeAnimationPlayer(AnimationSerializing.deserializeAnimation(reader).getFirst(),tick);a.setupAnim(partial);return a;
        }
    }
    @ParameterizedTest @ValueSource(strings={"bishop_stab","bishop_cut","bishop_leap","bishop_sprint","bishop_combo","bishop_stab_combo","bishop_dash_cut","bishop_dash_leap","bishop_backstep"})
    void motionPreservesNativeLimbSizeAndInterpolatesEveryFrame(String name)throws Exception{
        for(int t=0;t<=28;t++)for(float delta:new float[]{0,.5f}){
            var a=clip(name,t,delta);
            for(var part:new String[]{"rightArm","leftArm","rightLeg","leftLeg"}){
                var scale=a.get3DTransform(part,TransformType.SCALE,delta,new Vec3f(1,1,1));
                assertEquals(1,scale.getX(),.001);assertEquals(1,scale.getY(),.001);assertEquals(1,scale.getZ(),.001);
                var r=a.get3DTransform(part,TransformType.ROTATION,delta,Vec3f.ZERO);
                assertTrue(Float.isFinite(r.getX()+r.getY()+r.getZ()));
            }
        }
    }
    @Test void comboAlternatesBladeSweepsAndReturnsToRest()throws Exception{
        for(int i=0;i<6;i++){
            int hit=4+i*4;var a=clip("bishop_combo",hit,0);
            float yaw=a.get3DTransform("torso",TransformType.ROTATION,0,Vec3f.ZERO).getY();
            assertTrue(i%2==0?yaw<-.2:yaw>.2,"Every damage frame completes the next crossing cut");
            var marker=a.get3DTransform("nightBatTrail",TransformType.POSITION,0,Vec3f.ZERO);
            assertTrue(marker.getY()>.9);assertEquals(i+1,marker.getX(),.001);
        }
        assertEquals(0,clip("bishop_combo",28,0).get3DTransform("torso",TransformType.ROTATION,0,Vec3f.ZERO).getY(),.001);
    }
    @Test void daggerPointsForwardAtTheActualStabDamageFrame()throws Exception{
        var a=clip("bishop_stab",BishopRules.hitTick(1),0);
        var arm=a.get3DTransform("rightArm",TransformType.ROTATION,0,Vec3f.ZERO);
        var item=a.get3DTransform("rightItem",TransformType.ROTATION,0,Vec3f.ZERO);
        var direction=new org.joml.Matrix4f().rotateZ(arm.getZ()).rotateY(arm.getY()).rotateX(arm.getX())
            .rotateX(-(float)Math.PI/2).rotateY((float)Math.PI).rotateZ(item.getZ()).rotateY(item.getY()).rotateX(item.getX())
            .transformDirection(new org.joml.Vector3f(0,1,0));
        assertTrue(direction.z<-.94&&Math.abs(direction.y)<.02,"Blade points into the enemy instead of upward during a stab");
        var load=clip("bishop_stab",5,0);var recover=clip("bishop_stab",18,0);
        assertTrue(load.get3DTransform("torso",TransformType.ROTATION,0,Vec3f.ZERO).getY()>.2);
        assertEquals(0,recover.get3DTransform("torso",TransformType.ROTATION,0,Vec3f.ZERO).getY(),.001);
    }
    @Test void eachRepeatedStabPointsForwardAndRetractsBetweenContacts()throws Exception{
        for(int i=0;i<6;i++){
            int tick=5+i*5;var a=clip("bishop_stab_combo",tick,0);
            var arm=a.get3DTransform("rightArm",TransformType.ROTATION,0,Vec3f.ZERO);
            var item=a.get3DTransform("rightItem",TransformType.ROTATION,0,Vec3f.ZERO);
            var direction=new org.joml.Matrix4f().rotateZ(arm.getZ()).rotateY(arm.getY()).rotateX(arm.getX())
                .rotateX(-(float)Math.PI/2).rotateY((float)Math.PI).rotateZ(item.getZ()).rotateY(item.getY()).rotateX(item.getX())
                .transformDirection(new org.joml.Vector3f(0,1,0));
            assertTrue(direction.z<-.98&&Math.abs(direction.x)<.05&&Math.abs(direction.y)<.05,"Straight forward contact, not another side slash");
            var marker=a.get3DTransform("nightBatTrail",TransformType.POSITION,0,Vec3f.ZERO);assertEquals(i+1,marker.getX(),.001);assertEquals(1,marker.getY(),.001);
            var retract=clip("bishop_stab_combo",tick+1,0).get3DTransform("rightArm",TransformType.POSITION,0,Vec3f.ZERO);
            assertTrue(retract.getZ()>a.get3DTransform("rightArm",TransformType.POSITION,0,Vec3f.ZERO).getZ()+2);
        }
        assertEquals(0,clip("bishop_stab_combo",34,0).get3DTransform("nightBatTrail",TransformType.POSITION,0,Vec3f.ZERO).getY(),.001);
    }
}
