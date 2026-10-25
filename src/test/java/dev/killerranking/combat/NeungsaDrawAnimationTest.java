package dev.killerranking.combat;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import dev.kosmx.playerAnim.core.util.Vec3f;
import java.nio.file.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class NeungsaDrawAnimationTest {
    private final java.util.Map<String,KeyframeAnimation> clips=new java.util.HashMap<>();
    private KeyframeAnimationPlayer player(String name,int tick,float delta) throws Exception {
        if(!clips.containsKey(name)) {
            try(var reader=Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/neungsa_katana_"+name+".json"))) {
                clips.put(name,AnimationSerializing.deserializeAnimation(reader).getFirst());
            }
        }
        var animation=new KeyframeAnimationPlayer(clips.get(name),tick);
        animation.setupAnim(delta);return animation;
    }
    private Quaternionf rotation(KeyframeAnimationPlayer a,String part,float delta) {
        Vec3f r=a.get3DTransform(part,TransformType.ROTATION,delta,Vec3f.ZERO);
        return new Quaternionf().rotationZYX(r.getZ(),r.getY(),r.getX());
    }
    private Quaternionf blade(KeyframeAnimationPlayer a,float delta) {
        return rotation(a,"rightArm",delta).mul(rotation(a,"rightItem",delta));
    }
    private Vector3f palm(KeyframeAnimationPlayer a,float delta) {
        Vec3f p=a.get3DTransform("rightArm",TransformType.POSITION,delta,Vec3f.ZERO);
        return rotation(a,"rightArm",delta).transform(new Vector3f(-1,10,0)).add(p.getX(),p.getY(),p.getZ());
    }
    private void straightPhase(String name,int start,int end) throws Exception {
        var initial=player(name,start,0);Quaternionf basis=blade(initial,0);
        Vector3f origin=palm(initial,0),axis=basis.transform(new Vector3f(0,1,0));
        for(int tick=start;tick<end;tick++)for(float delta:new float[]{0,.25f,.5f,.75f}) {
            var a=player(name,tick,delta);
            assertEquals(1,Math.abs(basis.dot(blade(a,delta))),.00001,"Sword orientation must stay fixed while inside the saya");
            Vector3f travel=palm(a,delta).sub(origin);
            Vector3f across=new Vector3f(travel).sub(new Vector3f(axis).mul(travel.dot(axis)));
            assertTrue(across.length()<.004,"Extract/insert along the saya, never pivot through it: "+across.length());
        }
        assertTrue(palm(player(name,end,0),0).distance(origin)>22,"Clear the whole 21.5-pixel blade before changing direction");
    }
    @ParameterizedTest @ValueSource(strings={"slash_right","slash_left","cleave"})
    void fullyExtractsBeforeTurningForEachIndependentCut(String name) throws Exception {
        straightPhase("draw_"+name,64,160);
    }
    @ParameterizedTest @ValueSource(strings={"slash_right","slash_left","cleave"})
    void alignsBeforeInsertionAndReturnsWithoutAnExtraUnwind(String name) throws Exception {
        straightPhase("sheathe",256,320);
        Quaternionf start=blade(player("draw_"+name,0,0),0),end=blade(player("sheathe",384,0),0);
        assertEquals(1,Math.abs(start.dot(end)),.00001,"The rendered rotation matches the next clip's starting pose");
    }

    @ParameterizedTest @ValueSource(strings={"slash_right","slash_left","cleave"})
    void chainedCutsNeverInsertAndReturnToTheSameDrawnStance(String name) throws Exception {
        for(int tick=0;tick<832;tick++) {
            var a=player(name,tick,.5f);
            assertEquals(0,a.get3DTransform("neungsaSheath",TransformType.POSITION,.5f,Vec3f.ZERO).getY(),.000001);
        }
        assertEquals(1,Math.abs(blade(player(name,0,0),0).dot(blade(player(name,832,0),0))),.00001);
        assertEquals(1,Math.abs(blade(player("draw_"+name,832,0),0).dot(blade(player("drawn_guard",0,0),0))),.00001);
        assertEquals(1,Math.abs(blade(player(name,832,0),0).dot(blade(player("sheathe",0,0),0))),.00001);
    }

    @ParameterizedTest @ValueSource(strings={"slash_right","slash_left","cleave"})
    void armPointsTowardTheHandInsteadOfFlippingWithTheBlade(String name) throws Exception {
        // The cut pivots at the shoulder. Straight extraction/insertion can
        // translate the detached native arm, as allowed by the authored style.
        for(int tick=224;tick<576;tick++)for(float delta:new float[]{0,.25f,.5f,.75f}) {
            var a=player(name,tick,delta);
            Vec3f root=a.get3DTransform("rightArm",TransformType.POSITION,delta,Vec3f.ZERO);
            Vector3f toHand=palm(a,delta).sub(root.getX(),root.getY(),root.getZ()).normalize();
            Vector3f shaft=rotation(a,"rightArm",delta).transform(new Vector3f(0,1,0));
            assertTrue(shaft.dot(toHand)>.97f,"Arm must face the hand rather than reverse with the sword at "+tick+"+"+delta);
            Vec3f scale=a.get3DTransform("rightArm",TransformType.SCALE,delta,new Vec3f(1,1,1));
            assertEquals(1,scale.getX(),.00001);assertEquals(1,scale.getY(),.00001);assertEquals(1,scale.getZ(),.00001);
        }
    }

    @ParameterizedTest @ValueSource(strings={"slash_right","slash_left","cleave","draw_slash_right","draw_slash_left","draw_cleave"})
    void strikingHandStaysInFrontAndClearsTheHead(String name) throws Exception {
        // Independent body-space constraints: the old tangent arc sent the
        // palm behind the torso despite passing the arm's own direction test.
        for(int tick=352;tick<=480;tick++)for(float delta:new float[]{0,.25f,.5f,.75f}) {
            var a=player(name,tick,delta);Vector3f hand=palm(a,delta);
            assertTrue(hand.z<-4,"Striking hand must stay in front of the torso: "+name+" "+tick+" "+hand);
            assertFalse(Math.abs(hand.x)<6&&hand.y<2&&hand.y>-10&&hand.z>-6,
                "Keep the lifted fist outside the head silhouette: "+name+" "+tick+" "+hand);
            Vec3f root=a.get3DTransform("rightArm",TransformType.POSITION,delta,Vec3f.ZERO);
            assertTrue(new Vector3f(root.getX(),root.getY(),root.getZ()).distance(-5,2,0)<8,
                "Only modest rigid limb detachment is allowed during the cut");
        }
    }

    @ParameterizedTest @ValueSource(strings={"guard","drawn_guard","sheathe","slash_right","slash_left","cleave","draw_slash_right","draw_slash_left","draw_cleave"})
    void bladeExitsTheForwardSideOfTheFistInsteadOfReverseGrip(String name) throws Exception {
        var initial=player(name,0,0);
        int end=clips.get(name).endTick;
        for(int tick=0;tick<end;tick++)for(float delta:new float[]{0,.25f,.5f,.75f}) {
            Vector3f direction=rotation(player(name,tick,delta),"rightItem",delta).transform(new Vector3f(0,1,0));
            boolean extended=name.endsWith("slash_right")||name.endsWith("slash_left");
            assertTrue(extended?direction.z<-.05f:direction.z<-.7f,"The blade must exit the forward/thumb side of the fist: "+name+" "+tick+" "+direction);
        }
    }

    @ParameterizedTest @ValueSource(strings={"slash_right","draw_slash_right","slash_left","draw_slash_left"})
    void crossingCutExtendsTheBladeAlongTheForearm(String name) throws Exception {
        for(int tick=224;tick<576;tick++)for(float delta:new float[]{0,.25f,.5f,.75f}) {
            var a=player(name,tick,delta);
            Vector3f arm=rotation(a,"rightArm",delta).transform(new Vector3f(0,1,0));
            Vector3f sword=blade(a,delta).transform(new Vector3f(0,1,0));
            assertTrue(arm.dot(sword)>Math.cos(Math.toRadians(5.1)),
                "Crossing slash must continue the arm's line through the blade: "+name+" "+tick+" dot="+arm.dot(sword));
        }
    }

    @ParameterizedTest @ValueSource(strings={"slash_right","draw_slash_right","slash_left","draw_slash_left"})
    void crossingCutStaysAtChestHeightInsteadOfCuttingDownToTheLegs(String name) throws Exception {
        for(int tick=352;tick<=480;tick++)for(float delta:new float[]{0,.25f,.5f,.75f}) {
            var a=player(name,tick,delta);
            Vector3f axis=blade(a,delta).transform(new Vector3f(0,1,0));
            assertTrue(Math.abs(axis.y)<.105f,"Crossing sweep must remain nearly level: "+name+" "+tick+" "+axis);
            float handY=palm(a,delta).y;
            assertTrue(handY>-1.5f&&handY<5.5f,"Hold the crossing sweep at upper torso height: "+name+" "+tick+" y="+handY);
        }
    }

    @ParameterizedTest @ValueSource(strings={"","draw_"})
    void secondCutKeepsTheFirstCutsReachWithoutResizing(String prefix) throws Exception {
        for(int tick=224;tick<576;tick++)for(float delta:new float[]{0,.25f,.5f,.75f}) {
            float[] reach=new float[2];int i=0;
            for(String name:new String[]{prefix+"slash_right",prefix+"slash_left"}) {
                var a=player(name,tick,delta);
                Vec3f root=a.get3DTransform("rightArm",TransformType.POSITION,delta,Vec3f.ZERO);
                Vector3f tip=blade(a,delta).transform(new Vector3f(0,23.7f*.9f,0)).add(palm(a,delta));
                reach[i++]=tip.distance(root.getX(),root.getY(),root.getZ());
                Vec3f size=a.get3DTransform("rightItem",TransformType.SCALE,delta,new Vec3f(1,1,1));
                assertEquals(1,size.getX(),.00001);assertEquals(1,size.getY(),.00001);assertEquals(1,size.getZ(),.00001);
            }
            assertTrue(reach[0]>30&&reach[1]>30,"Keep the arm and blade extended instead of folding the second cut inwards");
            assertEquals(reach[0],reach[1],.05,"Second-cut reach must match the first at native size");
        }
    }

    @Test void drawnStanceHoldsTheBladeUpAndForward() throws Exception {
        for(int tick=0;tick<32;tick++) {
            Vector3f direction=blade(player("drawn_guard",tick,.5f),.5f).transform(new Vector3f(0,1,0));
            assertTrue(direction.y<-.5f&&direction.z<-.5f,"A drawn katana should point up and forward, not hang below the fist: "+direction);
        }
    }

    @ParameterizedTest @ValueSource(strings={"slash_right","slash_left","cleave","draw_slash_right","draw_slash_left","draw_cleave"})
    void swordAndVisibleFistKeepTheSameGripThroughoutTheAnimation(String name) throws Exception {
        Quaternionf initial=rotation(player(name,0,0),"rightItem",0);
        Quaternionf cutting=rotation(player(name,224,0),"rightItem",0);
        double setupLimit=name.endsWith("slash_right")||name.endsWith("slash_left")?95:30.1;
        for(int tick=0;tick<832;tick++)for(float delta:new float[]{0,.25f,.5f,.75f}) {
            var a=player(name,tick,delta);
            Quaternionf grip=rotation(a,"rightItem",delta);
            assertTrue(Math.abs(initial.dot(grip))>Math.cos(Math.toRadians(setupLimit)/2),
                "Grip setup must stay bounded outside the saya, never an independent half-turn at "+tick+"+"+delta);
            if(tick>=224&&tick<576)assertEquals(1,Math.abs(cutting.dot(grip)),.000001,
                "During the cut the sword must follow the skin fist exactly at "+tick+"+"+delta);
        }
    }
}
