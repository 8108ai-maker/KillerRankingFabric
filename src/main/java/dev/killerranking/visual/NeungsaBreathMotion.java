package dev.killerranking.visual;

/** Two continuous, curled streams pull inward below the chin rather than orbiting as dots. */
public final class NeungsaBreathMotion {
    public static final int SEGMENTS=32;
    public record Point(float side,float height,float forward) { }
    public static Point point(float u,int side,float time) {
        u=Math.clamp(u,0,1);float v=1-u;
        float x=v*v*v*.88f+3*v*v*u*1.08f+3*v*u*u*.52f;
        float y=v*v*v*.20f-3*v*v*u*.38f-3*v*u*u*.36f;
        float z=v*v*v*.65f+3*v*v*u*.72f+3*v*u*u*.24f;
        float ripple=(float)Math.sin(time*.32f-u*9)*.018f*(float)Math.sin(Math.PI*u);
        return new Point(x*(side<0?-1:1),y+ripple,z);
    }
    public static float width(float u) {
        return .010f+.070f*(float)Math.sin(Math.PI*Math.clamp(u,0,1));
    }
    public static float flow(float u,float time) {
        return .65f+.35f*(float)Math.pow(.5+.5*Math.cos(u*18-time*.9),3);
    }
    private NeungsaBreathMotion(){ }
}
