package dev.killerranking.combat;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import dev.kosmx.playerAnim.core.util.Vec3f;
import java.nio.file.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class InheritedWeaponAnimationTest {
    @ParameterizedTest
    @ValueSource(strings={"eunbal_hatchet_forehand","eunbal_hatchet_backhand","eunbal_hatchet_cleave"})
    void cuttingEdgeLeadsEachStrikeForEitherMainHand(String name) throws Exception {
        for(boolean left:new boolean[]{false,true}) {
            try(var reader=Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/"+name+".json"))) {
                var a=new KeyframeAnimationPlayer(AnimationSerializing.deserializeAnimation(reader).getFirst());
                org.joml.Vector3f before=null,after=null,edge=null;
                for(int tick=0;tick<=12;tick++) {
                    a.setupAnim(0);
                    var arm=a.get3DTransform("rightArm",TransformType.ROTATION,0,Vec3f.ZERO);
                    var item=a.get3DTransform("rightItem",TransformType.ROTATION,0,Vec3f.ZERO);
                    float sign=left?-1:1;
                    var matrix=new org.joml.Matrix4f().rotateZ(sign*arm.getZ()).rotateY(sign*arm.getY()).rotateX(arm.getX())
                        .rotateX(-(float)Math.PI/2).rotateY((float)Math.PI)
                        .rotateZ(sign*item.getZ()).rotateY(sign*item.getY()).rotateX(item.getX());
                    matrix.rotateY((float)Math.PI/2);
                    if(tick==9) before=matrix.transformDirection(new org.joml.Vector3f(0,1,0));
                    if(tick==12) after=matrix.transformDirection(new org.joml.Vector3f(0,1,0));
                    if(tick==10) edge=matrix.transformDirection(new org.joml.Vector3f(1,0,0)).normalize();
                    a.tick();
                }
                assertTrue(after.sub(before).normalize().dot(edge)>.7,"Cutting edge points along the sweep, not the poll or flat cheek; left="+left);
            }
        }
    }
    @ParameterizedTest
    @ValueSource(strings={"gossi_knife_slash_right","gossi_knife_slash_left","gossi_knife_stab","eunbal_hatchet_forehand","eunbal_hatchet_backhand","eunbal_hatchet_cleave"})
    void gripStaysInPalmAndMotionReturnsControlToWalking(String name) throws Exception {
        boolean knife=name.startsWith("gossi");
        var model=com.google.gson.JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/killerranking/models/item/"+(knife?"gossi_inherited_knife":"eunbal_hatchet")+".json"))).getAsJsonObject();
        var display=model.getAsJsonObject("display").getAsJsonObject("thirdperson_righthand");
        var rot=display.getAsJsonArray("rotation");var offset=display.getAsJsonArray("translation");
        var modelGrip=new org.joml.Matrix4f().translation(offset.get(0).getAsFloat(),offset.get(1).getAsFloat(),offset.get(2).getAsFloat())
            .rotateXYZ((float)Math.toRadians(rot.get(0).getAsFloat()),(float)Math.toRadians(rot.get(1).getAsFloat()),(float)Math.toRadians(rot.get(2).getAsFloat()))
            .scale(display.getAsJsonArray("scale").get(1).getAsFloat()).transformPosition(new org.joml.Vector3f(0,(knife?4:2)-8,0));
        try(var reader=Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/"+name+".json"))) {
            var a=new KeyframeAnimationPlayer(AnimationSerializing.deserializeAnimation(reader).getFirst());
            for(int tick=0;tick<=24;tick++) {
                for(float dt:new float[]{0,.5f}) {
                    a.setupAnim(dt);
                    var r=a.get3DTransform("rightItem",TransformType.ROTATION,dt,Vec3f.ZERO);
                    var p=a.get3DTransform("rightItem",TransformType.POSITION,dt,Vec3f.ZERO);
                    var grip=new org.joml.Matrix4f().translation(p.getX(),p.getY(),p.getZ()).rotateZ(r.getZ()).rotateY(r.getY()).rotateX(r.getX()).transformPosition(new org.joml.Vector3f(modelGrip));
                    assertTrue(grip.distance(0,-2,0)<.5,"Handle remains in palm between poses");
                    var size=a.get3DTransform("rightArm",TransformType.SCALE,dt,new Vec3f(1,1,1));
                    assertTrue(size.getY()>=.90&&size.getY()<=1.23,"Short, bounded limb smear");
                    var shoulder=a.get3DTransform("rightArm",TransformType.POSITION,dt,Vec3f.ZERO);
                    assertTrue(Math.abs(shoulder.getZ())<=1.3,"No detached shoulder behind back");
                    var baseline=new Vec3f(.12f,.23f,.34f);
                    for(var type:new TransformType[]{TransformType.POSITION,TransformType.ROTATION,TransformType.SCALE}) {
                        var head=a.get3DTransform("head",type,dt,baseline);
                        assertEquals(baseline.getX(),head.getX(),.0001);assertEquals(baseline.getY(),head.getY(),.0001);assertEquals(baseline.getZ(),head.getZ(),.0001);
                    }
                    for(String leg:new String[]{"leftLeg","rightLeg"})
                        assertEquals(baseline.getX(),a.get3DTransform(leg,TransformType.ROTATION,dt,baseline).getX(),.0001,"Walking stays free");
                    if(name.equals("gossi_knife_stab")&&tick==10&&dt==0) {
                        var arm=a.get3DTransform("rightArm",TransformType.ROTATION,dt,Vec3f.ZERO);
                        var blade=new org.joml.Matrix4f().rotateZ(arm.getZ()).rotateY(arm.getY()).rotateX(arm.getX()).rotateX(-(float)Math.PI/2).rotateY((float)Math.PI).rotateZ(r.getZ()).rotateY(r.getY()).rotateX(r.getX()).transformDirection(new org.joml.Vector3f(0,1,0));
                        assertTrue(blade.z<-.99&&Math.abs(blade.y)<.01,"Knife point faces target at stab impact");
                    }
                    if(tick==24) assertEquals(1,size.getY(),.001,"No lingering arm stretch");
                }
                a.tick();
            }
            for(int i=0;i<20;i++) a.tick();assertFalse(a.isActive());
        }
    }
}
