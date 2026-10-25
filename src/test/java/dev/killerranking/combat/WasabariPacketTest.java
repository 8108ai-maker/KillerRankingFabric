package dev.killerranking.combat;
import dev.killerranking.network.*;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class WasabariPacketTest {
    @BeforeAll static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
    @Test void preparationCaptureAndCancellationDecodeWithoutTrailingBytes(){
        for(boolean tripped:new boolean[]{false,true})for(int remaining:new int[]{0,66}){
            var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
            try{var v=new WasabariVisualPayload(UUID.randomUUID(),UUID.randomUUID(),20,remaining,145,tripped);WasabariVisualPayload.CODEC.encode(b,v);assertEquals(v,WasabariVisualPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
                var s=new WasabariStatusPayload(true,remaining,400);WasabariStatusPayload.CODEC.encode(b,s);assertEquals(s,WasabariStatusPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
                WasabariRequestPayload.CODEC.encode(b,new WasabariRequestPayload());assertEquals(new WasabariRequestPayload(),WasabariRequestPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
            }finally{b.release();}
        }
    }
    @Test void malformedTimersAndNonFiniteYawAreBounded(){
        var v=new WasabariVisualPayload(UUID.randomUUID(),UUID.randomUUID(),999,-20,Float.NaN,true);assertEquals(66,v.age());assertEquals(0,v.remaining());assertEquals(0,v.yaw());
        assertEquals(new WasabariStatusPayload(true,66,400),new WasabariStatusPayload(true,999,999));
    }
}
