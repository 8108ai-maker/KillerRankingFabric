package dev.killerranking.combat;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import dev.killerranking.data.NeungsaSkillState;
import dev.killerranking.network.*;
import io.netty.buffer.Unpooled;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class NeungsaEchoTest {
    @BeforeAll static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
    @Test void twentyEchoesAreBetweenTheOpeningAndFinishingCuts(){
        var hits=new ArrayList<Integer>();for(int t=-10;t<=120;t++)if(NeungsaEchoRules.hit(t))hits.add(t);
        assertEquals(22,hits.size());assertEquals(6,hits.getFirst());assertEquals(70,hits.getLast());
        for(int i=0;i<20;i++){assertEquals(22+i*2,hits.get(i+1));assertTrue(hits.get(i+1)>NeungsaEchoRules.SHEATHED&&hits.get(i+1)<NeungsaEchoRules.FINAL_START);}
    }
    @Test void oldPurchasesGainCWithoutChangingEitherExistingCooldown(){
        var old=JsonParser.parseString("{\"unlocked\":true,\"readyAt\":800,\"iaidoReadyAt\":950}");
        var state=NeungsaSkillState.CODEC.parse(JsonOps.INSTANCE,old).getOrThrow().echo(700);
        assertEquals(800,state.readyAt());assertEquals(950,state.iaidoReadyAt());assertEquals(1100,state.echoReadyAt());
        assertEquals(state,state.echo(701));assertEquals(400,state.echoCooldown(700));assertEquals(0,state.echoCooldown(1100));
        assertEquals(NeungsaSkillState.LOCKED,NeungsaSkillState.LOCKED.echo(700));
        var json=NeungsaSkillState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();assertEquals(state,NeungsaSkillState.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow());
    }
    @Test void zAndXPreserveRunningCCooldown(){
        var state=new NeungsaSkillState(true,0,0,900).cast(100).iaido(100).owned();
        assertEquals(900,state.echoReadyAt());assertEquals(340,state.readyAt());assertEquals(400,state.iaidoReadyAt());
    }
    @Test void statusAndLateTrackingVisualRoundTrip(){
        var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try{
            var status=new NeungsaStatusPayload(true,true,3,42,5,72,2,8,60,366);NeungsaStatusPayload.CODEC.encode(b,status);assertEquals(status,NeungsaStatusPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
            var v=new NeungsaEchoVisualPayload(UUID.randomUUID(),35,59,-5,62,8,.6f,0,.8f);NeungsaEchoVisualPayload.CODEC.encode(b,v);assertEquals(v,NeungsaEchoVisualPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
            var request=new NeungsaRequestPayload(NeungsaRequestPayload.ECHO);NeungsaRequestPayload.CODEC.encode(b,request);assertEquals(request,NeungsaRequestPayload.CODEC.decode(b));
        }finally{b.release();}
    }
    @Test void originalIsFullySheathedAndMotionlessDuringAllTwentyEchoes()throws Exception{
        for(String suffix:List.of("","_slim")){
            var json=JsonParser.parseString(Files.readString(Path.of("src/client/resources/assets/killerranking/player_animations/neungsa_katana_echo_sequence"+suffix+".json"))).getAsJsonObject();
            var moves=json.getAsJsonObject("emote").getAsJsonArray("moves");JsonObject middle=null;
            for(var v:moves){var m=v.getAsJsonObject();float t=m.get("tick").getAsFloat()/32;
                for(String bone:List.of("rightArm","leftArm","rightLeg","leftLeg","rightItem"))for(String axis:List.of("scaleX","scaleY","scaleZ"))assertEquals(1,m.getAsJsonObject(bone).get(axis).getAsFloat(),.0001);
                assertEquals(-5,m.getAsJsonObject("rightArm").get("x").getAsFloat(),.001);assertEquals(5,m.getAsJsonObject("leftArm").get("x").getAsFloat(),.001);
                if(t>=19&&t<64){assertEquals(1,m.getAsJsonObject("neungsaSheath").get("y").getAsInt());assertEquals(0,m.getAsJsonObject("nightBatTrail").get("y").getAsInt());
                    if(middle==null)middle=m;else for(String bone:List.of("rightArm","leftArm","rightItem","leftItem"))assertEquals(middle.get(bone),m.get(bone));
                }
            }
        }
    }
}
