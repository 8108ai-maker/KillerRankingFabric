package dev.killerranking.combat;
import java.util.SplittableRandom;

/** Each cast scatters stable, short blade strokes throughout the damage zone. */
public final class BishopBarrageMotion {
    public static final int START=BishopBarrageRules.ECHO_TICKS+1, SPACING=2, LIFE=6, STRIKES=4;
    public static final float SWEEP=2.5f;
    public record Point(float x,float y,float z) { }
    public record Stroke(float x,float y,float z,float cosine,float sine,float halfLength,float bend,float depthTilt,float depthBend) {
        public Point point(float progress) {
            float p=Math.clamp(progress,0,1),along=(p*2-1)*halfLength,arc=(float)Math.sin(Math.PI*p);
            return new Point(x+cosine*along-sine*bend*arc,y+sine*along+cosine*bend*arc,z+depthTilt*(p-.5f)+depthBend*arc);
        }
    }
    public static float head(float age){return Math.clamp(age/SWEEP,0,1);}
    public static float tail(float age){
        return Math.clamp(Math.max(head(age)-.85f,(age-SWEEP)/(LIFE-SWEEP)),0,1);
    }
    public static Point point(int pulse,int slash,float progress){
        return stroke(0,pulse,slash).point(progress);
    }
    public static Stroke stroke(long castSeed,int pulse,int slash) {
        // The stroke's seed is independent of partial tick and network refreshes: random placement never jitters mid-swing.
        var random=new SplittableRandom(castSeed^((long)pulse+1)*0x9E3779B97F4A7C15L^((long)slash+1)*0xD1B54A32D192ED03L);
        float angle=(float)random.nextDouble(0,Math.PI*2),cos=(float)Math.cos(angle),sin=(float)Math.sin(angle);
        float bend=(float)random.nextDouble(-.16,.16),half=(float)random.nextDouble(.55,1.15);
        half=Math.min(half,(1.08f-Math.abs(cos*bend))/Math.max(.001f,Math.abs(sin)));
        float extentX=Math.abs(cos)*half+Math.abs(sin*bend),extentY=Math.abs(sin)*half+Math.abs(cos*bend);
        float x=(float)random.nextDouble(-1.84+extentX,1.84-extentX);
        float y=(float)random.nextDouble(.15+extentY,2.60-extentY);
        float depthTilt=(float)random.nextDouble(-.65,.65),depthBend=(float)random.nextDouble(-.12,.12);
        float extentZ=Math.abs(depthTilt)*.5f+Math.abs(depthBend);
        float z=(float)random.nextDouble(.55+extentZ,5.85-extentZ);
        return new Stroke(x,y,z,cos,sin,half,bend,depthTilt,depthBend);
    }
    private BishopBarrageMotion() { }
}
