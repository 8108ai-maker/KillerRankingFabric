package dev.killerranking.visual;

/** Place two detached, upright leg blocks beside the captive during mounting. */
public final class RookLegShape {
    public record Point(double x,double y,double z) { }
    public static double spread(double width){return RookMountLayout.spread(width,2.17/1.8);}
    public static Point deform(double x,double y,double z,boolean left,double spread,double lift) {
        return new Point(x+(left?spread:-spread),y-lift,z+.5);
    }
    public static Point normal(double x,double y,double z,boolean left,double spread) {
        return new Point(x,y,z);
    }
    private RookLegShape() { }
}
