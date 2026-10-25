package dev.killerranking.combat;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import dev.kosmx.playerAnim.core.util.Vec3f;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SkillTrailAnimationTest {
    private static Vec3f marker(String clip,String bone,int tick)throws Exception{
        try(var r=Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/"+clip+".json"))){
            var p=new KeyframeAnimationPlayer(AnimationSerializing.deserializeAnimation(r).getFirst(),tick);p.setupAnim(0);
            return p.get3DTransform(bone,TransformType.POSITION,0,Vec3f.ZERO);
        }
    }
    @Test void bladeSkillsHaveDirectionalGatesAtTheirRealImpactFrames()throws Exception{
        for(var clip:new String[]{"checkmate","madness_leap","pawn6_katana_cleave_full","pawn6_katana_cleave_dash"}){
            int hit=switch(clip){case "checkmate"->CheckmateRules.HIT;case "madness_leap"->MadnessLeapRules.HIT;case "pawn6_katana_cleave_full"->CleaveRules.FULL_HIT;default->5;};
            assertTrue(marker(clip,"nightBatTrail",hit).getY()>.9,clip);
            assertEquals(0,marker(clip,"nightBatTrail",0).getY(),.001,"Aiming/charging does not leave attack trails");
        }
    }
    @Test void rookUsesOnlyTheStrikingHandAndAlternatesMountedContacts()throws Exception{
        for(int hit:new int[]{3,9,15,21,27}){
            assertTrue(marker("rook_punch","rightSkillTrail",hit).getY()>.9);
            assertEquals(0,marker("rook_punch","leftSkillTrail",hit).getY(),.001);
        }
        for(int i=0;i<6;i++){
            int hit=RookRules.MOUNT_FIRST_HIT+i*RookRules.MOUNT_HIT_INTERVAL;
            assertTrue(marker("rook_mount",i%2==0?"rightSkillTrail":"leftSkillTrail",hit).getY()>.9);
            assertEquals(0,marker("rook_mount",i%2==0?"leftSkillTrail":"rightSkillTrail",hit).getY(),.001);
        }
    }
}
