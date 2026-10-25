package dev.killerranking.combat;

import java.util.List;
import java.util.OptionalDouble;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

/** Invisible flight stops at the first surface; block removal never continues into its next layer. */
public final class NeungsaSlashRules {
    public static final int COST=1000, COOLDOWN=240, DURATION=16, HIT=5;
    public static final double REACH=12, HALF_WIDTH=1.5, HALF_HEIGHT=.35;
    public static final float DAMAGE=16;
    public static List<BlockPos> surface(BlockPos contact, Direction face, Vec3 direction) {
        boolean alongX=face.getAxis()==Direction.Axis.Z
            || face.getAxis()==Direction.Axis.Y && Math.abs(direction.z)>=Math.abs(direction.x);
        BlockPos side=alongX?new BlockPos(1,0,0):new BlockPos(0,0,1);
        return java.util.stream.IntStream.rangeClosed(-2,2)
            .mapToObj(i->contact.offset(side.getX()*i,0,side.getZ()*i)).toList();
    }
    /** Widen only across the cut: its forward range and height do not grow with the width. */
    public static OptionalDouble entry(Vec3 start,Vec3 forward,AABB bounds,double limit){
        return entry(start,forward,bounds,limit,HALF_WIDTH,HALF_HEIGHT);
    }
    public static OptionalDouble entry(Vec3 start,Vec3 forward,AABB bounds,double limit,double halfWidth,double halfHeight){
        Vec3 side=new Vec3(forward.z,0,-forward.x);
        side=side.lengthSqr()<1.0e-8?new Vec3(1,0,0):side.normalize();
        Vec3 up=forward.cross(side).normalize();
        var relative=bounds.getCenter().subtract(start);
        double near=relative.dot(forward)-support(bounds,forward);
        double far=relative.dot(forward)+support(bounds,forward);
        if(far<0||near>limit+.001
            ||Math.abs(relative.dot(side))>halfWidth+support(bounds,side)
            ||Math.abs(relative.dot(up))>halfHeight+support(bounds,up))return OptionalDouble.empty();
        return OptionalDouble.of(Math.max(0,near));
    }
    private static double support(AABB box,Vec3 axis){
        return (Math.abs(axis.x)*box.getXsize()+Math.abs(axis.y)*box.getYsize()+Math.abs(axis.z)*box.getZsize())*.5;
    }
    public static int cooldown(long readyAt,long now) {
        return (int)Math.clamp(readyAt-now,0,COOLDOWN);
    }
    private NeungsaSlashRules(){ }
}
