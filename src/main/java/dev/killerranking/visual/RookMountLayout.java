package dev.killerranking.visual;

/** Model placement for a mounted scene, independent of standing entity hitboxes. */
public final class RookMountLayout {
    public static double surface(double height,double width,boolean inverted) {
        return inverted?Math.max(.18,height*.55):.28+Math.min(.9,width)*.2;
    }
    public static double rootOffset(double height,double width,boolean inverted,double scale) {
        return surface(height,width,inverted)-.75*scale;
    }
    public static double legLift(double height,double width,boolean inverted,double scale) {
        return (.03-rootOffset(height,width,inverted,scale))*16/scale;
    }
    public static double spread(double width,double scale) {
        return Math.clamp(width*8/scale+.3,4.25,14);
    }
    private RookMountLayout() { }
}
