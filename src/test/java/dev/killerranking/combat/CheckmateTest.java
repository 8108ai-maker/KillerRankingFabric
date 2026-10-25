package dev.killerranking.combat;

import com.mojang.serialization.JsonOps;
import dev.killerranking.data.CheckmateState;
import dev.killerranking.data.SpiritState;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import dev.kosmx.playerAnim.core.util.Vec3f;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CheckmateTest {
    @Test void cooldownSurvivesRevocationRegrantAndSaveLoad() {
        var state=CheckmateState.LOCKED.owned(true).cast(100);
        assertEquals(400,state.cooldown(100));assertSame(state,state.cast(101));
        var revoked=state.owned(false);assertSame(revoked,revoked.cast(150));
        var restored=revoked.owned(true);assertEquals(300,restored.cooldown(200));
        var json=CheckmateState.CODEC.encodeStart(JsonOps.INSTANCE,restored).getOrThrow();
        assertEquals(restored,CheckmateState.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow());
        assertEquals(1,restored.cooldown(499));assertEquals(0,restored.cooldown(500));
        assertEquals(400,restored.cast(500).cooldown(500));
    }
    @Test void oldPlayerDataHasSafeDefaultsAndKeepsOldOwnershipFlag() {
        assertEquals(CheckmateState.LOCKED,CheckmateState.CODEC.parse(JsonOps.INSTANCE,new com.google.gson.JsonObject()).getOrThrow());
        var old=com.google.gson.JsonParser.parseString("{\"branch\":\"king_and_kill\",\"checkmate\":true}");
        var spirit=SpiritState.CODEC.parse(JsonOps.INSTANCE,old).getOrThrow();
        assertTrue(spirit.checkmate());assertEquals(SpiritBranch.KING_AND_KILL,spirit.branch());
        assertEquals(0,new CheckmateState(true,-10).readyAt());
    }
    @Test void strikeOnlyIncludesEntitiesOccupyingTheLandingSquare() {
        var tile=new Vec3(2,-60,3.5);
        assertTrue(CheckmateBoard.occupies(tile,new AABB(1.6,-60,3.1,2.4,-58,3.9),-60));
        assertFalse(CheckmateBoard.occupies(tile,new AABB(.1,-60,2.2,.9,-58,3),-60));
        assertFalse(CheckmateBoard.occupies(tile,new AABB(1.6,-57,3.1,2.4,-55,3.9),-57));
        assertFalse(CheckmateBoard.occupies(tile,new AABB(1.6,-61,3.1,2.4,-59,3.9),-61));
    }
    @Test void eightDistinctKnightSquaresUseTwoThenOneCells() {
        assertEquals(8,CheckmateBoard.MOVES.stream().distinct().count());
        for(var move:CheckmateBoard.MOVES) {
            assertEquals(3,Math.abs(move.x())+Math.abs(move.z()));
            assertEquals(2,Math.max(Math.abs(move.x()),Math.abs(move.z())));
            var end=CheckmateBoard.endpoint(Vec3.ZERO,move);
            assertEquals(20,end.lengthSqr(),1e-9);
        }
    }
    @Test void leapRisesFourBlocksAndFollowsLInsteadOfDiagonal() {
        var origin=new Vec3(.5,-60,.5);var end=new Vec3(2,-60,3.5);
        assertEquals(origin,CheckmateBoard.position(origin,end,CheckmateRules.HOP));
        assertEquals(end,CheckmateBoard.position(origin,end,CheckmateRules.HIT));
        var apex=CheckmateBoard.position(origin,end,(CheckmateRules.HOP+CheckmateRules.HIT)/2.0);
        assertEquals(-56,apex.y);assertEquals(.5,apex.x);assertEquals(2.75,apex.z);
        var turn=CheckmateBoard.position(origin,end,CheckmateRules.HOP+(CheckmateRules.HIT-CheckmateRules.HOP)*2.0/3);
        assertEquals(.5,turn.x,1e-9);assertEquals(3.5,turn.z,1e-9);
        assertEquals(origin,CheckmateBoard.position(origin,end,-1));
        assertEquals(end,CheckmateBoard.position(origin,end,100));
        var higher=end.add(0,1,0);
        assertEquals(higher,CheckmateBoard.position(origin,higher,CheckmateRules.HIT));
    }
    @Test void overheadStrikeHasShortSmearThenSettlesWithoutMovingHead() throws Exception {
        try(var reader=Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/checkmate.json"))) {
            var anim=new KeyframeAnimationPlayer(AnimationSerializing.deserializeAnimation(reader).getFirst());
            float previous=0,sweep=0,maxScale=0,minScale=2,maxBody=0;
            for(int t=0;t<=CheckmateRules.DURATION;t++) {
                anim.setupAnim(0);
                var rot=anim.get3DTransform("rightArm",TransformType.ROTATION,0,Vec3f.ZERO);
                var size=anim.get3DTransform("rightArm",TransformType.SCALE,0,new Vec3f(1,1,1));
                maxScale=Math.max(maxScale,size.getY());minScale=Math.min(minScale,size.getY());
                if(t>CheckmateRules.DIVE&&t<=CheckmateRules.HIT) sweep+=(float)Math.atan2(Math.sin(rot.getX()-previous),Math.cos(rot.getX()-previous));
                previous=rot.getX();
                var head=anim.get3DTransform("head",TransformType.ROTATION,0,Vec3f.ZERO);
                assertEquals(0,head.getX());assertEquals(0,head.getY());assertEquals(0,head.getZ());
                var body=anim.get3DTransform("body",TransformType.POSITION,0,Vec3f.ZERO);
                maxBody=Math.max(maxBody,Math.abs(body.getY()));
                if(t>=40) assertEquals(1,size.getY(),.001);
                anim.tick();
            }
            assertTrue(sweep>4,"The bat crosses from behind/above the shoulder into the downstroke");
            assertTrue(maxScale>1.2&&maxScale<1.3);assertTrue(minScale<.95);
            assertTrue(maxBody>0&&maxBody<.06,"Landing compression remains small");
        }
    }
}
