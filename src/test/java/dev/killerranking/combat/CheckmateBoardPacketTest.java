package dev.killerranking.combat;

import dev.killerranking.network.CheckmateVisualPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CheckmateBoardPacketTest {
    @BeforeAll static void bootstrap() { net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap(); }
    @Test void boardAndCancellationRoundTripWithoutUnreadBytes() {
        var origin=new Vec3(.5,-60,.5);
        var tiles=CheckmateBoard.MOVES.stream().map(m->CheckmateBoard.endpoint(origin,m)).toList();
        for(var value:List.of(new CheckmateVisualPayload(UUID.randomUUID(),42,0,origin,tiles,7,false),
                new CheckmateVisualPayload(UUID.randomUUID(),200,0,origin,tiles,-1,true),
                new CheckmateVisualPayload(UUID.randomUUID(),0,0,Vec3.ZERO,List.of(),0,false))) {
            var buf=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
            try { CheckmateVisualPayload.CODEC.encode(buf,value);assertEquals(value,CheckmateVisualPayload.CODEC.decode(buf));assertEquals(0,buf.readableBytes()); }
            finally { buf.release(); }
        }
    }
    @Test void invalidCoordinatesAndMissingDestinationAreRejected() {
        var id=UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,()->new CheckmateVisualPayload(id,42,0,Vec3.ZERO,List.of(),0,false));
        assertThrows(IllegalArgumentException.class,()->new CheckmateVisualPayload(id,42,0,Vec3.ZERO,List.of(Vec3.ZERO),1,false));
        assertThrows(IllegalArgumentException.class,()->new CheckmateVisualPayload(id,42,0,new Vec3(Double.NaN,0,0),List.of(Vec3.ZERO),0,false));
        assertThrows(IllegalArgumentException.class,()->new CheckmateVisualPayload(id,42,0,Vec3.ZERO,List.of(new Vec3(0,Double.POSITIVE_INFINITY,0)),0,false));
    }
    @Test void oversizedIncomingBoardIsRejectedBeforeAllocation() {
        var buf=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try {
            buf.writeUUID(UUID.randomUUID());buf.writeVarInt(42);buf.writeVarInt(0);
            buf.writeDouble(0);buf.writeDouble(0);buf.writeDouble(0);buf.writeVarInt(Integer.MAX_VALUE);
            assertThrows(io.netty.handler.codec.DecoderException.class,()->CheckmateVisualPayload.CODEC.decode(buf));
        } finally { buf.release(); }
    }
}
