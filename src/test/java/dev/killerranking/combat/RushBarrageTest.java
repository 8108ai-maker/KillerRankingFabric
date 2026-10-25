package dev.killerranking.combat;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import org.junit.jupiter.api.Test;
import static dev.killerranking.combat.RushAnimationTest.*;
import static org.junit.jupiter.api.Assertions.*;

class RushBarrageTest {
    @Test void echoUsesItsOwnNineTickCycleBesideSixTickPlayerCycle() throws Exception {
        var player=animation("rush");var echo=animation("rush_echo");
        float initialPlayer=roll(player,false,0),initialEcho=roll(echo,false,0);
        for(int i=0;i<6;i++){player.tick();echo.tick();}
        assertEquals(initialPlayer,roll(player,false,0),1e-5);assertTrue(Math.abs(initialEcho-roll(echo,false,0))>.1);
        for(int i=0;i<3;i++){player.tick();echo.tick();}
        assertEquals(initialEcho,roll(echo,false,0),1e-5);assertTrue(Math.abs(initialPlayer-roll(player,false,0))>.1);
    }
    @Test void separateEchoBeatCanOverlapAndFallBetweenPlayerContacts() throws Exception {
        var player=animation("rush");var echo=animation("rush_echo");boolean between=false,together=false;
        for(int i=0;i<72;i++){
            boolean playerHit=Math.min(roll(player,false,0),roll(player,true,0))<.1;
            boolean echoHit=Math.min(roll(echo,false,0),roll(echo,true,0))<.1;
            between|=echoHit&&!playerHit;together|=echoHit&&playerHit;
            player.tick();echo.tick();
        }
        assertTrue(between,"Echo can strike between player hits");assertTrue(together,"Separate rhythms also meet on some hits");
    }
    @Test void echoArmsHaveNativeSizeAndNoElbowBending() throws Exception {
        var echo=animation("rush_echo");
        for(int tick=0;tick<72;tick++){
            for(float delta:new float[]{0,.25f,.5f,.75f})for(boolean left:new boolean[]{false,true}){
                echo.setupAnim(delta);var scale=echo.get3DTransform(left?"leftArm":"rightArm",TransformType.SCALE,delta,new Vec3f(1,1,1));
                assertEquals(1,scale.getX(),1e-5);assertEquals(1,scale.getY(),1e-5);assertEquals(1,scale.getZ(),1e-5);
                assertEquals(0,fold(echo,left,delta),1e-6);
            }echo.tick();
        }
    }
    @Test void upperBodyBeatsAreIndependentWhileBothRootsStayAnchored() throws Exception {
        var echo=animation("rush_echo");var player=animation("rush");boolean differentPunches=false,differentChest=false;
        for(int tick=0;tick<72;tick++){
            for(float delta:new float[]{0,.25f,.5f,.75f}){
                echo.setupAnim(delta);player.setupAnim(delta);
                for(var motion:new dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer[]{echo,player})for(var type:new TransformType[]{TransformType.POSITION,TransformType.ROTATION}){
                    var root=motion.get3DTransform("body",type,delta,Vec3f.ZERO);
                    assertEquals(0,root.getX(),1e-5);assertEquals(0,root.getY(),1e-5);assertEquals(0,root.getZ(),1e-5);
                }
                var torso=echo.get3DTransform("torso",TransformType.POSITION,delta,Vec3f.ZERO);
                var head=echo.get3DTransform("head",TransformType.POSITION,delta,Vec3f.ZERO);
                assertEquals(torso.getY(),head.getY(),1e-5);assertEquals(torso.getZ(),head.getZ(),1e-5);
                var a=echo.get3DTransform("torso",TransformType.ROTATION,delta,Vec3f.ZERO);
                var b=player.get3DTransform("torso",TransformType.ROTATION,delta,Vec3f.ZERO);
                differentChest|=Math.abs(a.getY()-b.getY())>.25;
                differentPunches|=fist(player,false,delta).distance(fist(echo,false,delta))>4;
            }
            echo.tick();player.tick();
        }
        assertTrue(differentPunches);assertTrue(differentChest,"Position matching must not synchronize torso poses or beats");
    }
}
