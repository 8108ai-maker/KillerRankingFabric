package dev.killerranking.combat;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import dev.kosmx.playerAnim.core.util.Vec3f;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class NightBatAnimationTest {
    @ParameterizedTest
    @ValueSource(strings={"night_bat_forehand", "night_bat_backhand"})
    void wrappedHandleStaysInThePalmWhileTheWristTurns(String name) throws Exception {
        try(var reader=Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/"+name+".json"))) {
            var animation=new KeyframeAnimationPlayer(AnimationSerializing.deserializeAnimation(reader).getFirst());
            var model=com.google.gson.JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/killerranking/models/item/night_barbed_bat.json"))).getAsJsonObject();
            var display=model.getAsJsonObject("display").getAsJsonObject("thirdperson_righthand");
            float gripY=(1.3f-8)*display.getAsJsonArray("scale").get(1).getAsFloat()+display.getAsJsonArray("translation").get(1).getAsFloat();
            for(int tick=0;tick<=24;tick++) {
                for(float delta:new float[]{0,.5f}) {
                    animation.setupAnim(delta);
                    var rotation=animation.get3DTransform("rightItem",TransformType.ROTATION,delta,Vec3f.ZERO);
                    var position=animation.get3DTransform("rightItem",TransformType.POSITION,delta,Vec3f.ZERO);
                    var grip=new org.joml.Matrix4f().translation(position.getX(),position.getY(),position.getZ())
                        .rotateZ(rotation.getZ()).rotateY(rotation.getY()).rotateX(rotation.getX())
                        .transformPosition(new org.joml.Vector3f(0,gripY,0));
                    assertTrue(grip.distance(0,-2,0)<.5,"Wrapped handle drifts less than half a model pixel between poses");
                }
                animation.tick();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings={"night_bat_forehand", "night_bat_backhand"})
    void barrelCrossesTheTargetInsteadOfStayingUprightAboveTheFist(String name) throws Exception {
        try(var reader=Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/"+name+".json"))) {
            var animation=new KeyframeAnimationPlayer(AnimationSerializing.deserializeAnimation(reader).getFirst());
            org.joml.Vector3f before=null,after=null;
            for(int tick=0;tick<=24;tick++) {
                animation.setupAnim(0);
                var arm=animation.get3DTransform("rightArm",TransformType.ROTATION,0,Vec3f.ZERO);
                var size=animation.get3DTransform("rightArm",TransformType.SCALE,0,new Vec3f(1,1,1));
                var grip=animation.get3DTransform("rightItem",TransformType.ROTATION,0,Vec3f.ZERO);
                // ModelPart Z/Y/X, vanilla held-item X=-90/Y=180, then the grip's Z/Y/X.
                var barrel=new org.joml.Matrix4f().rotateZ(arm.getZ()).rotateY(arm.getY()).rotateX(arm.getX())
                    .scale(size.getX(),size.getY(),size.getZ()).rotateX(-(float)Math.PI/2).rotateY((float)Math.PI)
                    .rotateZ(grip.getZ()).rotateY(grip.getY()).rotateX(grip.getX())
                    .transformDirection(new org.joml.Vector3f(0,1,0)).normalize();
                if(tick==9) before=barrel;
                if(tick==12) after=barrel;
                if(tick==10) {
                    assertTrue(barrel.z<-.9,"At impact the barrel points into the target, not upward");
                    assertTrue(Math.abs(barrel.y)<.2,"Side strike stays nearly horizontal");
                }
                if(tick==24) {
                    assertEquals(0,grip.getX(),.001);assertEquals(0,grip.getY(),.001);assertEquals(0,grip.getZ(),.001);
                }
                animation.tick();
            }
            assertNotNull(before);assertNotNull(after);
            assertTrue(before.x*after.x<-.25,"Barrel sweeps across the front from one side to the other");
        }
    }

    @ParameterizedTest
    @ValueSource(strings={"night_bat_forehand", "night_bat_backhand", "night_bat_slam"})
    void coordinatedWeightShiftSettlesWhileHeadAndWalkingPitchStayFree(String name) throws Exception {
        try(var reader=Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/"+name+".json"))) {
            var animation=new KeyframeAnimationPlayer(AnimationSerializing.deserializeAnimation(reader).getFirst());
            var baseline=new Vec3f(.12f,.23f,.34f);
            float minLength=1, maxLength=1,torsoMotion=0,freeArmMotion=0,stanceMotion=0;
            for(int tick=0;tick<=24;tick++) {
                for(float delta:new float[]{0,.5f}) {
                    animation.setupAnim(delta);
                    for(String part:new String[]{"head"}) {
                        for(var type:new TransformType[]{TransformType.POSITION,TransformType.ROTATION,TransformType.SCALE}) {
                            var value=animation.get3DTransform(part,type,delta,baseline);
                            assertEquals(baseline.getX(),value.getX(),.0001,part);
                            assertEquals(baseline.getY(),value.getY(),.0001,part);
                            assertEquals(baseline.getZ(),value.getZ(),.0001,part);
                        }
                    }
                    var weight=animation.get3DTransform("body",TransformType.POSITION,delta,Vec3f.ZERO);
                    assertTrue(Math.abs(weight.getX())<=.033&&Math.abs(weight.getY())<=.033&&Math.abs(weight.getZ())<=.033,"Weight shift stays within 3.3 cm per axis");
                    var hips=animation.get3DTransform("body",TransformType.ROTATION,delta,Vec3f.ZERO);
                    assertTrue(Math.abs(hips.getX())<.075&&Math.abs(hips.getY())<.175&&Math.abs(hips.getZ())<.025,"Bounded coordinated hip motion");
                    var free=animation.get3DTransform("leftArm",TransformType.ROTATION,delta,Vec3f.ZERO);
                    freeArmMotion=Math.max(freeArmMotion,Math.abs(free.getX()));
                    assertTrue(Math.abs(free.getX())<.45&&Math.abs(free.getY())<.1&&Math.abs(free.getZ())<.15,"Free arm provides a small counterbalance");
                    for(String leg:new String[]{"leftLeg","rightLeg"}) {
                        var pos=animation.get3DTransform(leg,TransformType.POSITION,delta,baseline);
                        assertEquals(baseline.getX(),pos.getX(),.0001);assertEquals(baseline.getY(),pos.getY(),.0001);assertEquals(baseline.getZ(),pos.getZ(),.0001);
                        var turn=animation.get3DTransform(leg,TransformType.ROTATION,delta,baseline);
                        assertEquals(baseline.getX(),turn.getX(),.0001,"Walking pitch is not replaced");
                        assertTrue(Math.abs(turn.getY())<.15&&Math.abs(turn.getZ())<.15,"Small planted foot pivot");
                        stanceMotion=Math.max(stanceMotion,Math.abs(turn.getZ()));
                        var size=animation.get3DTransform(leg,TransformType.SCALE,delta,new Vec3f(1,1,1));
                        assertTrue(size.getY()>=.983&&size.getY()<=1,"Subtle stance compression");
                    }
                    var torso=animation.get3DTransform("torso",TransformType.ROTATION,delta,Vec3f.ZERO);
                    torsoMotion=Math.max(torsoMotion,Math.max(Math.abs(torso.getX()),Math.abs(torso.getY())));
                    assertTrue(Math.abs(torso.getX())<=.20&&Math.abs(torso.getY())<=.425&&Math.abs(torso.getZ())<=.063,"Restrained torso follow-through");
                    var scale=animation.get3DTransform("rightArm",TransformType.SCALE,delta,new Vec3f(1,1,1));
                    minLength=Math.min(minLength,scale.getY());maxLength=Math.max(maxLength,scale.getY());
                    assertTrue(scale.getY()>=.83&&scale.getY()<=1.39,"Bounded limb stretch");
                    assertEquals(1,scale.getX()*scale.getY()*scale.getZ(),.08,"Stretch preserves approximate volume, including interpolation between poses");
                    var pos=animation.get3DTransform("rightArm",TransformType.POSITION,delta,Vec3f.ZERO);
                    assertTrue(Math.abs(pos.getX()+5)<2&&Math.abs(pos.getY()-2)<1&&Math.abs(pos.getZ())<=4.01,"Small shoulder displacement");
                }
                if(tick==24) {
                    var scale=animation.get3DTransform("rightArm",TransformType.SCALE,0,new Vec3f(1,1,1));
                    assertEquals(1,scale.getY(),.001,"No lingering stretched arm");
                    for(String part:new String[]{"body","torso","leftLeg","rightLeg"}) {
                        var rest=animation.get3DTransform(part,TransformType.ROTATION,0,Vec3f.ZERO);
                        assertEquals(0,rest.getX(),.001);assertEquals(0,rest.getY(),.001);assertEquals(0,rest.getZ(),.001);
                    }
                }
                animation.tick();
            }
            assertTrue(minLength<.9&&maxLength>1.3,"Anticipation compression and fast smear both present");
            assertTrue(torsoMotion>.05,"Chest participates in the coordinated weight shift");
            assertTrue(freeArmMotion>.25&&stanceMotion>.015,"The free arm and stance participate with the torso");
            for(int tick=0;tick<20;tick++) animation.tick();
            assertFalse(animation.isActive(),"Attack releases control after settling");
        }
    }
}
