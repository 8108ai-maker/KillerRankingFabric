package dev.killerranking.combat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BishopBarrageMotionTest {
    @Test void cutsBeginAfterHelpersVanishAndCompleteLocalizedSwingsQuickly(){
        assertTrue(BishopBarrageMotion.START>BishopBarrageRules.ECHO_TICKS);
        assertEquals(0,BishopBarrageMotion.head(0));
        assertEquals(1,BishopBarrageMotion.head(3));
        var left=BishopBarrageMotion.point(0,0,0);var right=BishopBarrageMotion.point(0,0,1);
        double span=Math.hypot(right.x()-left.x(),right.y()-left.y());
        assertTrue(span>=1&&span<=2.31);
        assertTrue(BishopBarrageMotion.tail(4)>BishopBarrageMotion.tail(2));
        assertEquals(BishopBarrageMotion.head(6),BishopBarrageMotion.tail(6));
    }
    @Test void movingCutsStayInTheApprovedForwardZoneAndCoverEveryDepth(){
        boolean near=false,middle=false,far=false;
        for(int seed=0;seed<32;seed++)for(int pulse=0;pulse<16;pulse++)for(int slash=0;slash<BishopBarrageMotion.STRIKES;slash++)for(int frame=0;frame<=20;frame++){
            var stroke=BishopBarrageMotion.stroke(seed,pulse,slash);
            float age=frame*.3f,tail=BishopBarrageMotion.tail(age),head=BishopBarrageMotion.head(age);
            assertTrue(tail>=0&&tail<=head&&head<=1);
            var point=stroke.point(head);
            assertTrue(BishopBarrageRules.inZone(point.z(),point.x(),point.y(),0));
            // Include the visible ribbon's thickness rather than only its center line.
            assertTrue(Math.abs(point.x())+.115<=BishopBarrageRules.HALF_WIDTH);
            assertTrue(point.y()+.115<=2.75&&point.y()-.115>=-.5);
            near|=point.z()<2;middle|=point.z()>2&&point.z()<4;far|=point.z()>4;
        }
        assertTrue(near&&middle&&far);
    }
    @Test void eachStrokeStaysStableAcrossFramesButNewCastsAndPulsesChangePlacement(){
        var stroke=BishopBarrageMotion.stroke(1234,17,2);
        for(int frame=0;frame<100;frame++)assertEquals(stroke,BishopBarrageMotion.stroke(1234,17,2));
        assertNotEquals(stroke,BishopBarrageMotion.stroke(1235,17,2));
        assertNotEquals(stroke,BishopBarrageMotion.stroke(1234,18,2));
        assertNotEquals(stroke,BishopBarrageMotion.stroke(1234,25,2));
        assertNotEquals(stroke,BishopBarrageMotion.stroke(1234,17,3));
    }
    @Test void positionsFillTheVolumeInsteadOfRepeatingFixedDepthLanes(){
        var centers=new java.util.HashSet<String>();var directions=new java.util.HashSet<Integer>();
        float minX=99,maxX=-99,minY=99,maxY=-99,minZ=99,maxZ=-99;
        for(int pulse=0;pulse<48;pulse++)for(int slash=0;slash<BishopBarrageMotion.STRIKES;slash++){
            var s=BishopBarrageMotion.stroke(42,pulse,slash);var p=s.point(.5f);
            centers.add(Math.round(p.x()*100)+":"+Math.round(p.y()*100)+":"+Math.round(p.z()*100));
            directions.add((int)Math.floor((Math.atan2(s.sine(),s.cosine())+Math.PI)*4/Math.PI));
            minX=Math.min(minX,p.x());maxX=Math.max(maxX,p.x());minY=Math.min(minY,p.y());maxY=Math.max(maxY,p.y());minZ=Math.min(minZ,p.z());maxZ=Math.max(maxZ,p.z());
        }
        assertTrue(centers.size()>180);
        assertEquals(8,directions.size());
        assertTrue(maxX-minX>2.5&&maxY-minY>1.7&&maxZ-minZ>4);
    }
}
