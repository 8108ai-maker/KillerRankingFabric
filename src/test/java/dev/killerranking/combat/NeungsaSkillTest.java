package dev.killerranking.combat;

import com.mojang.serialization.JsonOps;
import dev.killerranking.data.NeungsaSkillState;
import dev.killerranking.network.*;
import io.netty.buffer.Unpooled;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class NeungsaSkillTest {
    @BeforeAll static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
    @Test void everyWallDirectionCutsOnlyFiveCellsOnItsOriginalPlane(){
        var contact=new BlockPos(-3,62,7);
        for(var face:Direction.values()){
            var cells=NeungsaSlashRules.surface(contact,face,new Vec3(.2,-.5,1));
            assertEquals(5,new HashSet<>(cells).size());assertTrue(cells.contains(contact));
            for(var pos:cells){
                assertEquals(contact.get(face.getAxis()),pos.get(face.getAxis()),face.toString());
                assertEquals(contact.getY(),pos.getY());
                assertTrue(pos.distManhattan(contact)<=2);
            }
        }
    }
    @Test void widthUsesTheLookingDirectionWithoutExtendingReachOrHeight(){
        var start=new Vec3(0,1,0);var forward=new Vec3(0,0,1);
        var flank=new net.minecraft.world.phys.AABB(1.25,0,5,1.85,2,5.6);
        assertTrue(NeungsaSlashRules.entry(start,forward,flank,12).isPresent());
        assertTrue(NeungsaSlashRules.entry(start,forward,flank.move(1,0,0),12).isEmpty());
        assertTrue(NeungsaSlashRules.entry(start,forward,flank.move(0,0,9),12).isEmpty());
        assertTrue(NeungsaSlashRules.entry(start,forward,flank.move(0,4,0),12).isEmpty());
        assertTrue(NeungsaSlashRules.entry(start,forward,flank,4).isEmpty());
        var turned=new Vec3(1,0,1).normalize();
        assertTrue(NeungsaSlashRules.entry(start,turned,new net.minecraft.world.phys.AABB(4,0,4,4.6,2,4.6),12).isPresent());
        assertTrue(NeungsaSlashRules.entry(start,turned,new net.minecraft.world.phys.AABB(6,0,1,6.6,2,1.6),12).isEmpty());
    }
    @Test void downwardCutsChooseTheAcrossBladeDirectionWithoutDiggingDeeper(){
        var contact=new BlockPos(1,-60,2);
        assertEquals(NeungsaSlashRules.surface(contact,Direction.UP,new Vec3(1,-1,0)),
            NeungsaSlashRules.surface(contact,Direction.DOWN,new Vec3(-1,1,0)));
        assertTrue(NeungsaSlashRules.surface(contact,Direction.UP,new Vec3(1,-1,0)).contains(contact.offset(0,0,1)));
        assertTrue(NeungsaSlashRules.surface(contact,Direction.UP,new Vec3(0,-1,1)).contains(contact.offset(1,0,0)));
    }
    @Test void duplicateCastsCannotResetTheCooldownOrCastBeforeThePurchase(){
        assertEquals(NeungsaSkillState.LOCKED,NeungsaSkillState.LOCKED.cast(100));
        var cast=NeungsaSkillState.LOCKED.owned().cast(100);
        assertEquals(240,cast.cooldown(100));assertEquals(239,cast.cooldown(101));
        assertEquals(cast,cast.cast(150));assertEquals(cast,cast.owned());
        assertEquals(0,cast.cooldown(340));assertEquals(580,cast.cast(340).readyAt());
    }
    @Test void ownershipAndAnUnfinishedCooldownSurviveSerialization(){
        for(var state:new NeungsaSkillState[]{NeungsaSkillState.LOCKED,new NeungsaSkillState(true,840)}){
            var encoded=NeungsaSkillState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();
            assertEquals(state,NeungsaSkillState.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow());
        }
        assertEquals(NeungsaSkillState.LOCKED,NeungsaSkillState.CODEC.parse(JsonOps.INSTANCE,new com.google.gson.JsonObject()).getOrThrow());
    }
    @Test void requestStatusAndVisualPacketsRoundTripWithoutLeavingUnreadBytes(){
        var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try{
            for(int op:new int[]{NeungsaRequestPayload.START,NeungsaRequestPayload.UNLOCK,99}){
                var request=new NeungsaRequestPayload(op);NeungsaRequestPayload.CODEC.encode(b,request);
                assertEquals(request,NeungsaRequestPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
            }
            for(boolean branch:new boolean[]{true,false})for(boolean owned:new boolean[]{true,false}){
                var status=new NeungsaStatusPayload(branch,owned,16,237);NeungsaStatusPayload.CODEC.encode(b,status);
                assertEquals(status,NeungsaStatusPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
            }
            for(int remaining:new int[]{0,16}){
                var visual=new NeungsaSlashVisualPayload(UUID.randomUUID(),5,remaining);NeungsaSlashVisualPayload.CODEC.encode(b,visual);
                assertEquals(visual,NeungsaSlashVisualPayload.CODEC.decode(b));assertEquals(0,b.readableBytes());
            }
        }finally{b.release();}
    }
}
