package dev.killerranking.visual;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;

/** A separate textured ribbon for each actual weapon stroke, sampled in world space. */
public final class BatTrailHistory {
    public static final double LIFETIME=7;
    public static final int FRAMES=8,MAX_STROKES=8,MAX_POINTS=48;
    public record Point(Vec3 inner,Vec3 tip,double distance) { }
    public record Ribbon(List<Point> points,double born) {
        public int frame(double time) { return frameAt(time-born); }
    }
    private static final class Stroke {
        final ArrayList<Point> points=new ArrayList<>();
        final ArrayList<Double> times=new ArrayList<>();
        final double born;
        double updated;
        Stroke(double born) { this.born=born;this.updated=born; }
    }
    private final boolean followBlade;
    public BatTrailHistory() { this(false); }
    /** A live sweep fades from its last blade sample, never from its first frame. */
    public BatTrailHistory(boolean followBlade) { this.followBlade=followBlade; }
    private final ArrayDeque<Stroke> strokes=new ArrayDeque<>();
    private Stroke current;
    private Vec3 lastInner,lastTip,lastOwner;
    private double lastTime=-1;
    private int strokeId=Integer.MIN_VALUE;

    public void sampleStroke(double time,Vec3 inner,Vec3 tip,Vec3 owner,boolean striking,int id){
        if(strokeId!=id){resetStroke();strokeId=id;}
        sample(time,inner,tip,owner,striking,4,6);
    }

    public void sample(double time,Vec3 inner,Vec3 tip,Vec3 owner,boolean striking) {
        sample(time,inner,tip,owner,striking,followBlade?4:1.6,followBlade?6:2.8);
    }
    private void sample(double time,Vec3 inner,Vec3 tip,Vec3 owner,boolean striking,double maxGap,double maxTravel) {
        prune(time);
        if(!striking) { resetStroke();return; }
        if(!finite(inner)||!finite(tip)||!finite(owner)||!Double.isFinite(time)) { resetStroke();return; }
        if(lastTime>=0&&time-lastTime<.12) return;
        if(lastTip!=null) {
            double gap=time-lastTime;
            double travel=tip.subtract(lastTip).subtract(owner.subtract(lastOwner)).length();
            if(followBlade&&gap>0&&gap<=maxGap&&travel<.025&&owner.distanceTo(lastOwner)<1.5) return;
            if(gap<=maxGap&&gap>0&&travel>=.025&&travel<maxTravel&&owner.distanceTo(lastOwner)<1.5) {
                if(current==null) {
                    current=new Stroke(time);
                    current.points.add(new Point(lastInner,lastTip,0));current.times.add(lastTime);strokes.addLast(current);
                    while(strokes.size()>MAX_STROKES) strokes.removeFirst();
                }
                var previous=current.points.getLast();
                current.points.add(new Point(inner,tip,previous.distance()+lastTip.distanceTo(tip)));
                current.times.add(time);current.updated=time;
                if(current.points.size()>MAX_POINTS) { current.points.removeFirst();current.times.removeFirst(); }
            } else current=null;
        }
        lastTime=time;lastInner=inner;lastTip=tip;lastOwner=owner;
    }
    public List<Ribbon> visible(double time) {
        prune(time);
        return strokes.stream().map(s->new Ribbon(followBlade?BladeSweepCurve.smooth(s.points):WeaponTrailCurve.smooth(s.points),followBlade?s.updated:s.born)).toList();
    }
    public boolean empty() { return strokes.isEmpty(); }
    public boolean canDiscard(double time) { return strokes.isEmpty()&&(lastTime<0||time-lastTime>2); }
    public void resetStroke() { current=null;lastTime=-1;lastTip=lastInner=lastOwner=null; }
    public static int frameAt(double age) {
        if(age<0||age>=LIFETIME||!Double.isFinite(age)) return -1;
        return Math.min(FRAMES-1,(int)(age*FRAMES/LIFETIME));
    }
    private static boolean finite(Vec3 p) { return Double.isFinite(p.x+p.y+p.z); }
    private void prune(double time) {
        if(lastTime>time) { strokes.clear();resetStroke(); }
        strokes.removeIf(s->time-(followBlade?s.updated:s.born)>=LIFETIME||time<s.born);
        if(followBlade) for(var stroke:strokes) {
            // Keep one trailing window rather than an ever-growing full circle.
            while(stroke.points.size()>2&&time-stroke.times.get(1)>=LIFETIME) {
                stroke.points.removeFirst();stroke.times.removeFirst();
            }
        }
        if(current!=null&&!strokes.contains(current)) current=null;
    }
}
