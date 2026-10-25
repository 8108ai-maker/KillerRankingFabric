package dev.killerranking.combat;
import dev.killerranking.visual.FirstPersonWeaponPose;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import dev.kosmx.playerAnim.core.util.Vec3f;
import java.nio.file.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
class NeungsaChargeAnimationTest {
    private final java.util.Map<String,dev.kosmx.playerAnim.core.data.KeyframeAnimation> clips=new java.util.HashMap<>();
    private KeyframeAnimationPlayer clip(String name,int tick)throws Exception{
        var data=clips.get(name);
        if(data==null){try(var reader=Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/neungsa_katana_"+name+".json"))){data=AnimationSerializing.deserializeAnimation(reader).getFirst();clips.put(name,data);}}
        var a=new KeyframeAnimationPlayer(data,tick);a.setupAnim(0);return a;
    }
    private KeyframeAnimationPlayer iaido(String name,int tick)throws Exception{return clip("iaido_"+name,tick);}
    private Quaternionf rotation(KeyframeAnimationPlayer a,String part){var r=a.get3DTransform(part,TransformType.ROTATION,0,Vec3f.ZERO);return new Quaternionf().rotationZYX(r.getZ(),r.getY(),r.getX());}
    private Vector3f palm(KeyframeAnimationPlayer a,boolean left,boolean view){
        String part=left?"leftArm":"rightArm";var root=a.get3DTransform(part,TransformType.POSITION,0,Vec3f.ZERO);
        if(view)root=FirstPersonWeaponPose.adjust(a.getData().extraData.get("name"),part,TransformType.POSITION,root,0,0,0);
        return rotation(a,part).transform(new Vector3f(left?1:-1,10,0)).add(root.getX(),root.getY(),root.getZ());
    }
    private Vector3f blade(KeyframeAnimationPlayer a){return rotation(a,"rightArm").mul(rotation(a,"rightItem")).transform(new Vector3f(0,1,0));}
    private Vector3f sayaAxis(KeyframeAnimationPlayer a){return rotation(a,"leftArm").mul(rotation(a,"leftItem")).transform(new Vector3f(0,1,0));}
    private Vector3f sayaOrigin(KeyframeAnimationPlayer a,boolean view){
        float grab=a.get3DTransform("neungsaSaya",TransformType.POSITION,0,Vec3f.ZERO).getY();
        return palm(a,true,view).fma(-grab,sayaAxis(a));
    }
    @ParameterizedTest @ValueSource(strings={"charge","charge_slim","dash","dash_slim"})
    void firstPersonMovesBothShouldersTogetherWithoutChangingTheGripOrCamera(String name)throws Exception{
        for(int tick=0;tick<58*32;tick+=16){
            var a=iaido(name,tick);var n=a.getData().extraData.get("name");assertTrue(FirstPersonWeaponPose.isChargedIaido(n));
            for(String part:new String[]{"body","torso"})for(var type:new TransformType[]{TransformType.POSITION,TransformType.ROTATION}){
                var source=a.get3DTransform(part,type,0,Vec3f.ZERO);
                assertEquals(Vec3f.ZERO,FirstPersonWeaponPose.adjust(n,part,type,source,tick,a.getData().endTick,a.getData().stopTick));
            }
            for(String part:new String[]{"rightArm","leftArm"}){
                var root=a.get3DTransform(part,TransformType.POSITION,0,Vec3f.ZERO);
                var view=FirstPersonWeaponPose.adjust(n,part,TransformType.POSITION,root,tick,0,0);
                var expected=FirstPersonWeaponPose.iaidoViewFrame(n,tick).transform(new Vector3f(root.getX(),root.getY(),root.getZ()));
                expected.add(FirstPersonWeaponPose.iaidoViewSide(n,tick),FirstPersonWeaponPose.iaidoViewDown(n,tick),-FirstPersonWeaponPose.iaidoViewForward(n,tick));
                assertEquals(expected.x,view.getX(),.00001);assertEquals(expected.y,view.getY(),.00001);assertEquals(expected.z,view.getZ(),.00001);
                var nativeArm=a.get3DTransform(part,TransformType.ROTATION,0,Vec3f.ZERO);
                var viewArm=FirstPersonWeaponPose.adjust(n,part,TransformType.ROTATION,nativeArm,tick,0,0);
                var expectedArm=FirstPersonWeaponPose.iaidoViewFrame(n,tick).mul(rotation(a,part));
                var actualArm=new Quaternionf().rotationZYX(viewArm.getZ(),viewArm.getY(),viewArm.getX());
                assertEquals(1,Math.abs(expectedArm.dot(actualArm)),.00001,"Rotate the palm and shoulder with the same view frame");
            }
            for(String part:new String[]{"rightArm","leftArm","rightItem","leftItem"}){
                var scale=a.get3DTransform(part,TransformType.SCALE,0,new Vec3f(1,1,1));assertSame(scale,FirstPersonWeaponPose.adjust(n,part,TransformType.SCALE,scale,tick,0,0));
            }
            for(String part:new String[]{"rightItem","leftItem"})for(var type:new TransformType[]{TransformType.ROTATION,TransformType.SCALE}){
                var source=a.get3DTransform(part,type,0,new Vec3f(1,1,1));assertSame(source,FirstPersonWeaponPose.adjust(n,part,type,source,tick,0,0));
            }
            var arm=a.get3DTransform("rightArm",TransformType.ROTATION,0,Vec3f.ZERO);var grip=a.get3DTransform("rightItem",TransformType.ROTATION,0,Vec3f.ZERO);
            var viewArm=FirstPersonWeaponPose.adjust(n,"rightArm",TransformType.ROTATION,arm,tick,0,0);var viewGrip=FirstPersonWeaponPose.adjust(n,"rightItem",TransformType.ROTATION,grip,tick,0,0);
            var viewBlade=new Quaternionf().rotationZYX(viewArm.getZ(),viewArm.getY(),viewArm.getX()).mul(new Quaternionf().rotationZYX(viewGrip.getZ(),viewGrip.getY(),viewGrip.getX())).transform(new Vector3f(0,1,0));
            assertEquals(1,viewBlade.dot(FirstPersonWeaponPose.iaidoViewFrame(n,tick).transform(blade(a))),.00001,"The blade follows the entire view rig without changing its wrist angle");
            if(name.startsWith("dash")&&tick>=80&&tick<=1488){
                var axis=new Quaternionf().rotationZYX(viewArm.getZ(),viewArm.getY(),viewArm.getX()).transform(new Vector3f(0,1,0));
                assertEquals(1,axis.dot(viewBlade),.00001,"The first-person blade must continue the actual cuboid forearm axis during the cut and two-second hold");
            }
        }
    }
    @ParameterizedTest @ValueSource(strings={"dash","dash_slim"})
    void rotatesArmsAtTheShoulderWithoutStretchingOrMovingTheirRoots(String name)throws Exception{
        float y=name.endsWith("_slim")?2.5f:2;
        for(int tick=0;tick<=58*32;tick++){
            var a=iaido(name,tick);
            for(String part:new String[]{"rightArm","leftArm"}){
                assertEquals(new Vec3f(part.equals("rightArm")?-5:5,y,0),a.get3DTransform(part,TransformType.POSITION,.5f,Vec3f.ZERO),"Keep every subframe on the native shoulder: "+tick);
                var hand=palm(a,part.equals("leftArm"),false);
                assertEquals(Math.sqrt(101),hand.distance(part.equals("rightArm")?-5:5,y,0),.00001,"Native rigid arm length must remain constant");
            }
            for(String part:new String[]{"rightArm","leftArm","rightLeg","leftLeg","rightItem","leftItem"})assertEquals(new Vec3f(1,1,1),a.get3DTransform(part,TransformType.SCALE,.5f,new Vec3f(1,1,1)));
        }
    }
    @ParameterizedTest @ValueSource(strings={"dash","dash_slim"})
    void keepsTheOutwardFollowThroughForFortyTicksBeforeReinserting(String name)throws Exception{
        var end=iaido(name,208);
        for(int tick=208;tick<=1488;tick+=32){
            var held=iaido(name,tick);
            for(String part:new String[]{"rightArm","rightItem","leftArm","leftItem","body","torso","head","rightLeg","leftLeg","neungsaSaya"})for(var type:new TransformType[]{TransformType.POSITION,TransformType.ROTATION})
                assertEquals(end.get3DTransform(part,type,0,Vec3f.ZERO),held.get3DTransform(part,type,0,Vec3f.ZERO),"Hold the finished pose for two seconds: "+part+" / "+tick);
            assertEquals(0,held.get3DTransform("neungsaSheath",TransformType.POSITION,0,Vec3f.ZERO).getY());
            if(tick>208){assertEquals(0,held.get3DTransform("neungsaCut",TransformType.POSITION,0,Vec3f.ZERO).getZ());assertEquals(0,held.get3DTransform("nightBatTrail",TransformType.POSITION,0,Vec3f.ZERO).getY());}
        }
        assertNotEquals(rotation(end,"rightArm"),rotation(iaido(name,48*32),"rightArm"),"Only rotate back after the full hold");
        var fist=palm(end,false,false);var axis=blade(end);
        assertTrue(fist.x<-5&&fist.y<8&&fist.z<-1.5);assertTrue(axis.x<-.8&&axis.z<-.45);
        assertTrue(fist.distance(sayaOrigin(end,false))>8,"Finished fist stays away from the waist-mounted saya");
        var begin=iaido(name,80);assertTrue(palm(begin,false,false).x>0&&blade(begin).x>.8&&fist.x<palm(begin,false,false).x-7,"Open outward from the other side of the torso");
        assertEquals(58*32,end.getData().endTick);assertEquals(20,NeungsaIaidoRules.DURATION,"Visual hold must not extend gameplay lock or parry");
    }
    @ParameterizedTest @ValueSource(strings={"charge","charge_slim"})
    void crouchesWithWideLegsAndTheBladeInsideItsSaya(String name)throws Exception{
        var a=iaido(name,30*32);
        assertTrue(a.get3DTransform("torso",TransformType.ROTATION,0,Vec3f.ZERO).getX()>.4);
        float head=a.get3DTransform("head",TransformType.ROTATION,0,Vec3f.ZERO).getX();assertTrue(head>.1&&head<.3);
        var lower=a.get3DTransform("body",TransformType.POSITION,0,Vec3f.ZERO);assertTrue(lower.getY()<0&&lower.getY()>-.3);
        var right=a.get3DTransform("rightLeg",TransformType.POSITION,0,Vec3f.ZERO);var left=a.get3DTransform("leftLeg",TransformType.POSITION,0,Vec3f.ZERO);
        assertTrue(left.getX()-right.getX()>7&&Math.abs(left.getZ()-right.getZ())>4);assertEquals(1,a.get3DTransform("neungsaSheath",TransformType.POSITION,0,Vec3f.ZERO).getY());
        assertEquals(0,sayaOrigin(a,false).distance(palm(a,false,false)),.00001,"Two palms hold the same sheathed katana");
    }
    @ParameterizedTest @ValueSource(strings={"carry","carry_slim"})
    void returningToCarryCannotPullTheArmsAwayFromTheirShoulders(String name)throws Exception{
        var a=clip(name,0);var dash=iaido(name.endsWith("_slim")?"dash_slim":"dash",58*32);var charge=iaido(name.endsWith("_slim")?"charge_slim":"charge",0);
        for(String part:new String[]{"rightArm","leftArm","rightItem","leftItem"})for(var type:new TransformType[]{TransformType.POSITION,TransformType.ROTATION,TransformType.SCALE}){
            var pose=a.get3DTransform(part,type,0,Vec3f.ZERO);
            for(var other:new Vec3f[]{dash.get3DTransform(part,type,0,Vec3f.ZERO),charge.get3DTransform(part,type,0,Vec3f.ZERO)}){
                assertEquals(pose.getX(),other.getX(),.00001);assertEquals(pose.getY(),other.getY(),.00001);assertEquals(pose.getZ(),other.getZ(),.00001);
            }
        }
    }
    @ParameterizedTest @ValueSource(strings={"dash","dash_slim"})
    void cutKeepsTheBladeLevelAndFixedToTheExtendedForearm(String name)throws Exception{
        var grip=rotation(iaido(name,80),"rightItem");
        for(int tick=80;tick<=1488;tick++){
            var a=iaido(name,tick);assertEquals(1,Math.abs(grip.dot(rotation(a,"rightItem"))),.00001,"No independent wrist spin during the cut or hold");
            var axis=blade(a);var arm=rotation(a,"rightArm").transform(new Vector3f(0,1,0));
            assertEquals(1,arm.dot(axis),.00001,"Blade continues the arm direction");assertTrue(Math.abs(axis.y)<.015&&axis.z<-.49);
            var n=a.getData().extraData.get("name");var nativeRotation=a.get3DTransform("rightArm",TransformType.ROTATION,0,Vec3f.ZERO);
            var viewRotation=FirstPersonWeaponPose.adjust(n,"rightArm",TransformType.ROTATION,nativeRotation,tick,0,0);
            var viewArm=new Quaternionf().rotationZYX(viewRotation.getZ(),viewRotation.getY(),viewRotation.getX());
            var viewBlade=new Quaternionf(viewArm).mul(rotation(a,"rightItem")).transform(new Vector3f(0,1,0));
            assertEquals(1,viewArm.transform(new Vector3f(0,1,0)).dot(viewBlade),.00001,"Every first-person cut subframe keeps the actual rigid cuboid arm axis aligned with the sword");
        }
    }
    @ParameterizedTest @ValueSource(strings={"dash","dash_slim"})
    void entireBladeClearsTheActualPalmHeldSayaBeforeTurning(String name)throws Exception{
        var parser=new com.google.gson.Gson();
        var sword=parser.fromJson(Files.readString(Path.of("src/main/resources/assets/killerranking/models/item/neungsa_katana.json")),com.google.gson.JsonObject.class);
        var saya=parser.fromJson(Files.readString(Path.of("src/main/resources/assets/killerranking/models/item/neungsa_katana_saya.json")),com.google.gson.JsonObject.class);
        double tip=0,mouth=Double.POSITIVE_INFINITY;
        for(var e:sword.getAsJsonArray("elements"))tip=Math.max(tip,(e.getAsJsonObject().getAsJsonArray("to").get(1).getAsDouble()-8)*.9);
        for(var e:saya.getAsJsonArray("elements"))mouth=Math.min(mouth,(e.getAsJsonObject().getAsJsonArray("from").get(1).getAsDouble()-8)*.9);
        var a=iaido(name,48);var axis=blade(a);
        for(boolean view:new boolean[]{false,true}){
            var origin=sayaOrigin(a,view);var fist=palm(a,false,view);float projection=new Vector3f(origin).sub(fist).dot(axis);
            assertTrue(projection+mouth-tip>1,"The actual blade tip clears the mouth before the cut, including first-person");
            assertEquals(0,new Vector3f(origin).sub(fist).cross(axis).length(),.0001,"Extract straight along the sheath axis (PAL float precision)");
        }
        var initial=blade(iaido(name,0));for(int tick=0;tick<=48;tick++)assertEquals(1,initial.dot(blade(iaido(name,tick))),.00001,"No blade turn while still inside the saya");
        assertTrue(blade(iaido(name,80)).dot(blade(iaido(name,208)))<-.45);
        assertEquals(0,iaido(name,8*32).get3DTransform("neungsaSheath",TransformType.POSITION,0,Vec3f.ZERO).getY());
        assertEquals(1,iaido(name,56*32).get3DTransform("neungsaSheath",TransformType.POSITION,0,Vec3f.ZERO).getY());
    }
}
