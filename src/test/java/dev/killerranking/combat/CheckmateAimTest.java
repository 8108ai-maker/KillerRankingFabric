package dev.killerranking.combat;

import dev.killerranking.network.CheckmateConfirmPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CheckmateAimTest {
    @org.junit.jupiter.api.BeforeAll static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
    }
    @Test void lookingAtAnyOfTheEightSquaresSelectsItRatherThanTheEnemyInFront() {
        var origin=new Vec3(.5,-60,.5);var eye=origin.add(0,1.62,0);
        var tiles=CheckmateBoard.MOVES.stream().map(m->CheckmateBoard.endpoint(origin,m)).toList();
        for(int i=0;i<8;i++) {
            var point=tiles.get(i).add(.9,.025,.9);
            assertEquals(i,CheckmateBoard.select(eye,point.subtract(eye).normalize(),tiles));
        }
        assertEquals(-1,CheckmateBoard.select(eye,new Vec3(0,0,1),tiles));
        assertEquals(-1,CheckmateBoard.select(eye,new Vec3(0,1,0),tiles));
        assertEquals(-1,CheckmateBoard.select(eye,new Vec3(0,-1,0),tiles));
    }
    @Test void selectionRespectsHeightAndDoesNotChooseAnUnseenTileBehindTheRay() {
        var eye=new Vec3(0,2,0);var tile=new Vec3(2,1,4);
        assertEquals(0,CheckmateBoard.select(eye,tile.add(0,.025,0).subtract(eye).normalize(),List.of(tile)));
        assertEquals(-1,CheckmateBoard.select(eye,new Vec3(0,-1,-1).normalize(),List.of(tile)));
        assertEquals(2,CheckmateBoard.HALF*2);assertEquals(2,CheckmateBoard.CELL);
    }
    @Test void damageLeavesHalfAHeartAndConsumesAbsorptionWithoutHealingCriticalHealth() {
        assertEquals(19,CheckmateRules.damageToHalfHeart(20,0));
        assertEquals(29,CheckmateRules.damageToHalfHeart(20,10));
        assertEquals(999,CheckmateRules.damageToHalfHeart(1000,0));
        assertEquals(0,CheckmateRules.damageToHalfHeart(1,0));
        assertEquals(0,CheckmateRules.damageToHalfHeart(.5f,10));
    }
    @Test void releaseAndCancelPacketsRoundTripAndLeaveNoUnreadBytes() {
        for(int selected:new int[]{-1,0,7,99}) {
            var buf=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
            try {
                var value=new CheckmateConfirmPayload(selected);CheckmateConfirmPayload.CODEC.encode(buf,value);
                assertEquals(value,CheckmateConfirmPayload.CODEC.decode(buf));assertEquals(0,buf.readableBytes());
            } finally { buf.release(); }
        }
    }
}
