package dev.killerranking.visual;

/** Keep the caster outside both the standing hitbox and the grounded body's foot end. */
public final class WasabariLayout {
    public static double gap(double ownerWidth,double width,double height,boolean inverted){
        double nativeGap=.85+Math.min(2,height)*.35;
        double boxes=(ownerWidth+width)*.5+.35;
        double grounded=inverted?width*.6+ownerWidth*.5+.25:height*.52+ownerWidth*.5+.20;
        return Math.max(nativeGap,Math.max(boxes,grounded));
    }
    private WasabariLayout(){ }
}
