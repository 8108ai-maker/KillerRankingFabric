package dev.killerranking.visual;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RookLegShapeTest {
    private static double distance(RookLegShape.Point a,RookLegShape.Point b) {
        return Math.sqrt(Math.pow(a.x()-b.x(),2)+Math.pow(a.y()-b.y(),2)+Math.pow(a.z()-b.z(),2));
    }
    @Test void detachedLegBlocksStayUprightWithNativeDimensionsAndClearTheTorso() {
        for(double width:new double[]{.3,.6,.9,2,100})for(boolean left:new boolean[]{false,true}) {
            double spread=RookLegShape.spread(width);
            var hip=RookLegShape.deform(0,0,0,left,spread,3);
            var foot=RookLegShape.deform(0,12,0,left,spread,3);
            assertEquals(12,distance(hip,foot),1e-9);
            assertEquals(hip.x(),foot.x());assertEquals(hip.z(),foot.z());
            // Native hip center is 1.9 pixels from the body center; the body half-width is 4.
            assertTrue(1.9+Math.abs(hip.x())-2>4,"The inner face must be separate from the torso");
            assertTrue(hip.y()<0);assertTrue(foot.y()>0);
            for(double y=0;y<=12;y+=.25) {
                var p=RookLegShape.deform(0,y,0,left,spread,3);
                assertEquals(hip.x(),p.x(),1e-9);
                assertEquals(hip.y()+y,p.y(),1e-9);
                assertEquals(hip.z(),p.z(),1e-9);
                assertEquals(4,distance(RookLegShape.deform(-2,y,0,left,spread,3),RookLegShape.deform(2,y,0,left,spread,3)),1e-9);
                assertEquals(4,distance(RookLegShape.deform(0,y,-2,left,spread,3),RookLegShape.deform(0,y,2,left,spread,3)),1e-9);
            }
        }
    }
    @Test void straddlingIsSymmetricAndEachFlatFaceRetainsItsPerpendicularNormal() {
        for(double width:new double[]{.6,.9,2}) {
            double spread=RookLegShape.spread(width);
            for(double y:new double[]{0,6,12}) {
                var l=RookLegShape.deform(2,y,1,true,spread,3);
                var r=RookLegShape.deform(-2,y,1,false,spread,3);
                assertEquals(-l.x(),r.x(),1e-9);assertEquals(l.y(),r.y(),1e-9);assertEquals(l.z(),r.z(),1e-9);
            }
            for(boolean left:new boolean[]{false,true})for(double[] axis:new double[][]{{1,0,0},{0,1,0},{0,0,1}}) {
                var n=RookLegShape.normal(axis[0],axis[1],axis[2],left,spread);
                assertEquals(1,n.x()*n.x()+n.y()*n.y()+n.z()*n.z(),1e-9);
                for(double[] tangent:new double[][]{{1,0,0},{0,1,0},{0,0,1}})if(axis[0]*tangent[0]+axis[1]*tangent[1]+axis[2]*tangent[2]==0) {
                    var p=RookLegShape.deform(tangent[0],tangent[1],tangent[2],left,spread,3);
                    var origin=RookLegShape.deform(0,0,0,left,spread,3);
                    assertEquals(0,n.x()*(p.x()-origin.x())+n.y()*(p.y()-origin.y())+n.z()*(p.z()-origin.z()),1e-9);
                }
            }
        }
    }
}
