package dev.killerranking.combat;
import dev.killerranking.visual.*;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import dev.kosmx.playerAnim.core.util.Vec3f;
import java.nio.file.*;
import org.joml.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
class FirstPersonWeaponPoseTest {
 @ParameterizedTest
 @ValueSource(strings={"parry_reverse","neungsa_katana_parry_reverse","parry_hand","parry_hand_reverse","parry_tteokbal","parry_tteokbal_reverse"})
 void oppositeGuardsKeepTheSameHandAndWeaponRig(String name)throws Exception {
  palmsAndTheirWeaponsShareOneRigidCameraTransform(name);
 }
 @ParameterizedTest
 @ValueSource(strings={"night_bat_forehand","night_bat_backhand","night_bat_slam","gossi_knife_slash_right","gossi_knife_slash_left","gossi_knife_stab","salmosa_dagger_forehand","salmosa_dagger_backhand","salmosa_dagger_stab","eunbal_hatchet_forehand","eunbal_hatchet_backhand","eunbal_hatchet_cleave","yuhyeolmoki_saw_forehand","yuhyeolmoki_saw_backhand","yuhyeolmoki_saw_cleave","rook_glove_right","rook_glove_left","tteokbal_fist_right","tteokbal_fist_left","tteokbal_fist_cross","pawn6_katana_slash_right","pawn6_katana_slash_left","pawn6_katana_thrust","neungsa_katana_slash_right","neungsa_katana_slash_left","neungsa_katana_cleave","neungsa_katana_draw_slash_right","neungsa_katana_draw_slash_left","neungsa_katana_draw_cleave","parry","neungsa_katana_parry"})
 void palmsAndTheirWeaponsShareOneRigidCameraTransform(String name)throws Exception {
  try(var reader=Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/"+name+".json"))){
   var data=AnimationSerializing.deserializeAnimation(reader).getFirst();
   assertTrue(FirstPersonWeaponPose.supports(data.extraData.get("name")));
   var player=new KeyframeAnimationPlayer(data);
   for(int tick=0;tick<=data.stopTick;tick++){
    player.setupAnim(0);
    for(String arm:new String[]{"rightArm","leftArm"}){
     var root=player.get3DTransform(arm,TransformType.POSITION,0,Vec3f.ZERO);
     var rot=player.get3DTransform(arm,TransformType.ROTATION,0,Vec3f.ZERO);
     var viewRoot=FirstPersonWeaponPose.adjust(name,arm,TransformType.POSITION,root,tick,data.endTick,data.stopTick);
     var viewRot=FirstPersonWeaponPose.adjust(name,arm,TransformType.ROTATION,rot,tick,data.endTick,data.stopTick);
     var q=new Quaternionf().rotationZYX(rot.getZ(),rot.getY(),rot.getX());
     var viewQ=new Quaternionf().rotationZYX(viewRot.getZ(),viewRot.getY(),viewRot.getX());
     // The actual palm offset and full frame, rather than a shoulder-to-hand line.
     var local=new Vector3f(arm.equals("rightArm")?-1:1,10,0);
     var nativePalm=q.transform(new Vector3f(local)).add(root.getX(),root.getY()-2,root.getZ());
     var actual=viewQ.transform(new Vector3f(local)).add(viewRoot.getX(),viewRoot.getY()-2,viewRoot.getZ());
     var expected=FirstPersonCombatRig.frame(name,tick,data.endTick,data.stopTick).transform(nativePalm).add(FirstPersonCombatRig.offset(name,tick,data.endTick,data.stopTick));
     assertEquals(0,actual.distance(expected),.002,"Palm detached: "+name+" "+tick);
     var size=player.get3DTransform(arm,TransformType.SCALE,0,new Vec3f(1,1,1));
     var adjusted=FirstPersonWeaponPose.adjust(name,arm,TransformType.SCALE,size,tick,data.endTick,data.stopTick);
     if(FirstPersonCombatRig.fittedFist(name))assertSame(size,adjusted,"Keep chosen fist proportions");
     else assertTrue(adjusted.getX()<=1&&adjusted.getY()<=1&&adjusted.getZ()<=1,"No arm enlargement");
    }
    for(var type:TransformType.values())for(String part:new String[]{"rightItem","leftItem","nightBatTrail","neungsaCut","neungsaSheath","head"}){
     var source=player.get3DTransform(part,type,0,type==TransformType.SCALE?new Vec3f(1,1,1):Vec3f.ZERO);
     assertSame(source,FirstPersonWeaponPose.adjust(name,part,type,source,tick,data.endTick,data.stopTick),"Keep wrist, saya, strike gates and camera");
    }
    player.tick();
   }
  }
 }
 @ParameterizedTest
 @ValueSource(strings={"pawn6_katana_parry","pawn6_katana_parry_reverse","pawn6_katana_auto_parry","pawn6_katana_auto_parry_reverse","pawn6_katana_echo_horizontal","pawn6_katana_echo_cross_guard"})
 void pawnSixParryNeverUsesNewRig(String name){
  assertFalse(FirstPersonCombatRig.supports(name));
  var arm=new Vec3f(-1.4f,.4f,.6f);
  assertSame(arm,FirstPersonWeaponPose.adjust(name,"rightArm",TransformType.ROTATION,arm,4,12,12));
  var view=FirstPersonWeaponPose.adjust(name,"rightArm",TransformType.POSITION,new Vec3f(-5,2,0),4,12,12);
  assertEquals(-5,view.getX());assertEquals(8,view.getY());assertEquals(-8,view.getZ());
 }
}

