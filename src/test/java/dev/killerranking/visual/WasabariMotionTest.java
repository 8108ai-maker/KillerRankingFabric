package dev.killerranking.visual;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class WasabariMotionTest {
    @Test void thirdPersonReapStaysLowWithAPlantedSupportFootAndSmoothRecovery(){
        var support=WasabariMotion.thirdPersonPose(0,false).leftLeg().point(0,12,0);
        for(float age=0;age<26;age+=.1f){
            var pose=WasabariMotion.thirdPersonPose(age,false);
            var next=WasabariMotion.thirdPersonPose(age+.1f,false);
            var foot=pose.rightLeg().point(0,12,0);
            assertTrue(foot.y>=20,"ankle-height sweep rather than a high front kick");
            assertTrue(pose.leftLeg().point(0,12,0).distanceTo(support)<.02,"support foot does not skate");
            assertTrue(foot.distanceTo(next.rightLeg().point(0,12,0))<.55,"no frame jumps during the sweep");
            var hip=pose.torso().point(0,12,0);
            assertTrue(pose.rightLeg().pivot().distanceTo(hip.add(-2.5,0,0))<3,"rigid leg remains near the hip");
            assertTrue(pose.leftLeg().pivot().distanceTo(hip.add(2.5,0,0))<.6);
        }
        assertTrue(WasabariMotion.thirdPersonPose(12,false).torso().point(0,12,0).x>1,"weight shifts onto supporting leg");
        assertEquals(WasabariMotion.pose(26,false),WasabariMotion.thirdPersonPose(26,false));
        assertEquals(WasabariMotion.pose(40,true),WasabariMotion.thirdPersonPose(40,true));
    }
    @Test void oppositeHandKeepsHoldingTheSameAnkleThroughoutSixKnifeBeats(){
        var ankle=new Vec3(2.5,7,-17);var shin=new Vec3(2.5,12,-14);
        for(int age=26;age<60;age++){
            var pose=WasabariMotion.hold(WasabariMotion.pose(age,false),age,false,ankle,shin,WasabariMotion.knifeDirection(false),1);
            assertEquals(0,pose.leftArm().point(1,10,0).distanceTo(ankle),1e-4);
        }
    }
    @Test void knifeLoadsHighAndPlungesIntoTheHeldShinOnEachDamageBeat(){
        var ankle=new Vec3(2.5,7,-17);var shin=new Vec3(2.5,12,-14);
        for(int i=0;i<6;i++){
            int age=26+i*6;
            var loaded=WasabariMotion.hold(WasabariMotion.pose(age-2,false),age-2,false,ankle,shin,WasabariMotion.knifeDirection(false),1);
            var strike=WasabariMotion.hold(WasabariMotion.pose(age,false),age,false,ankle,shin,WasabariMotion.knifeDirection(false),1);
            assertEquals(0,strike.rightArm().point(-1,10,0).add(WasabariMotion.knifeDirection(false).scale(WasabariMotion.KNIFE_REACH)).distanceTo(shin),1e-4);
            if(i>0){assertEquals(6,loaded.rightArm().point(-1,10,0).add(WasabariMotion.knifeDirection(false).scale(WasabariMotion.KNIFE_REACH)).distanceTo(shin),1e-4);assertTrue(strike.rightArm().point(-1,10,0).y-loaded.rightArm().point(-1,10,0).y>5);}
        }
    }
    @Test void leftHandAndSlimArmsHoldTheRaisedLeg(){
        assertEquals(0,WasabariMotion.raisedLegPitch(21),1e-5);
        assertEquals(-1.48,WasabariMotion.raisedLegPitch(26),1e-5);
        for(float centre:new float[]{1,.5f}){
            var ankle=new Vec3(-2,8,-16);var shin=new Vec3(-2,12,-14);
            var pose=WasabariMotion.hold(WasabariMotion.pose(32,true),32,true,ankle,shin,WasabariMotion.knifeDirection(true),centre);
            assertEquals(0,pose.rightArm().point(-centre,10,0).distanceTo(ankle),1e-4);
            assertEquals(0,pose.leftArm().point(centre,10,0).add(WasabariMotion.knifeDirection(true).scale(WasabariMotion.KNIFE_REACH)).distanceTo(shin),1e-4);
        }
    }
    @Test void footCocksBehindThenHooksForwardAndBothFeetPlantForStandingStabs(){
        var raised=WasabariMotion.pose(8,false);var wa=WasabariMotion.pose(12,false);var bari=WasabariMotion.pose(20,false);
        assertTrue(raised.rightLeg().point(0,12,0).y<20);assertTrue(raised.rightLeg().point(0,12,0).z>6);
        var planted=wa.rightLeg().point(0,12,0);var pulled=bari.rightLeg().point(0,12,0);
        assertEquals(18,planted.y,1e-4);assertEquals(8.5,planted.z,1e-4);
        assertTrue(pulled.z<planted.z-20);assertEquals(13,pulled.y,1e-4);
        for(float age=0;age<=20;age+=.25f){
            var leg=WasabariMotion.pose(age,false).rightLeg();assertEquals(0,leg.rotation().y);assertTrue(Math.abs(leg.rotation().z)<.3);
            assertTrue(leg.point(0,12,0).distanceTo(WasabariMotion.pose(age+.25f,false).rightLeg().point(0,12,0))<1.8);
        }
        assertTrue(wa.torso().rotation().y>0);assertTrue(bari.torso().rotation().y<0);
        for(int age=26;age<60;age++){var p=WasabariMotion.pose(age,false);assertEquals(12,p.torso().point(0,12,0).y,1e-4);assertEquals(24,p.rightLeg().point(0,12,0).y,1e-4);assertTrue(p.leftLeg().point(0,12,0).y>23.9);assertTrue(p.torso().rotation().x<.31);}
    }
    @Test void sideThrustFollowsTheRenderedLegNormalRatherThanAStandingHitbox(){
        var direction=new Vec3(.48,.88,-.12).normalize();var shin=new Vec3(1,12,-14);var ankle=new Vec3(1,7,-17);
        for(int i=0;i<6;i++){
            int contact=26+i*6;var before=WasabariMotion.hold(WasabariMotion.pose(contact-2,false),contact-2,false,ankle,shin,direction,1);var hit=WasabariMotion.hold(WasabariMotion.pose(contact,false),contact,false,ankle,shin,direction,1);
            var a=before.rightArm().point(-1,10,0);var b=hit.rightArm().point(-1,10,0);
            assertEquals(0,b.add(direction.scale(WasabariMotion.KNIFE_REACH)).distanceTo(shin),1e-4);
            if(i>0){assertTrue(b.x>a.x+2.5);assertTrue(b.y>a.y+5);assertEquals(1,b.subtract(a).normalize().dot(direction),1e-4);}
            assertEquals(0,WasabariMotion.stabWeight(contact-2));assertEquals(1,WasabariMotion.stabWeight(contact));
        }
    }
    @Test void waistRemainsConnectedAndPoseMirrorsForLeftHandedPlayers(){
        for(int age=0;age<=66;age++){
            var pose=WasabariMotion.pose(age,false);var mirror=WasabariMotion.pose(age,true);
            var hip=pose.torso().point(0,12,0);assertEquals(0,hip.add(2.5,0,0).distanceTo(pose.leftLeg().pivot()),1e-4);
            assertEquals(pose.torso().pivot(),pose.head().pivot());
            var right=pose.rightArm().point(-1,10,0);var left=mirror.leftArm().point(1,10,0);
            assertEquals(-right.x,left.x,1e-4);assertEquals(right.y,left.y,1e-4);assertEquals(right.z,left.z,1e-4);
        }
        assertEquals(0,WasabariMotion.pose(66,false).torso().point(0,12,0).distanceTo(new Vec3(0,12,0)),1e-4);
    }
}
