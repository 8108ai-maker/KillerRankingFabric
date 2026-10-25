package dev.killerranking.combat;
import dev.killerranking.network.*;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RookPacketTest {
    @BeforeAll static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
    @Test void eachPhaseAndCancellationRoundTripWithoutUnreadBytes(){
        for(int phase=0;phase<6;phase++){
            var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
            try{
                var visual=new RookVisualPayload(UUID.randomUUID(),phase==3?null:UUID.randomUUID(),phase,12,15);
                RookVisualPayload.CODEC.encode(b,visual);assertEquals(visual,RookVisualPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
                var status=new RookStatusPayload(true,true,false,phase,32,15,390,phase==2);
                RookStatusPayload.CODEC.encode(b,status);assertEquals(status,RookStatusPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
            }finally{b.release();}
        }
    }
    @Test void allRequestsRoundTripAndInvalidOperationsAreRejected(){
        for(int operation=0;operation<=6;operation++){
            var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
            try{var request=new RookRequestPayload(operation,17);RookRequestPayload.CODEC.encode(b,request);assertEquals(request,RookRequestPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());}finally{b.release();}
        }
        assertThrows(IllegalArgumentException.class,()->new RookRequestPayload(7,0));assertThrows(IllegalArgumentException.class,()->new RookRequestPayload(1,-2));
    }
}
