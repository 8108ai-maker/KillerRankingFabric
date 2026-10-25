package dev.killerranking.combat;

import java.util.List;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Two-block squares; the knight route is committed only when the aiming key is released. */
public final class CheckmateBoard {
    public static final double CELL=2, HALF=1, HEIGHT=4;
    public record Offset(int x,int z) { }
    public static final List<Offset> MOVES=List.of(new Offset(1,2),new Offset(-1,2),
        new Offset(2,1),new Offset(2,-1),new Offset(1,-2),new Offset(-1,-2),new Offset(-2,-1),new Offset(-2,1));
    public static Vec3 endpoint(Vec3 origin,Offset move) { return origin.add(move.x()*CELL,0,move.z()*CELL); }
    public static Vec3 corner(Vec3 origin,Vec3 destination) {
        return Math.abs(destination.x-origin.x)>Math.abs(destination.z-origin.z)
            ?new Vec3(destination.x,origin.y,origin.z):new Vec3(origin.x,origin.y,destination.z);
    }
    /** Travel two cells along one axis, then one perpendicular cell, in a single high arc. */
    public static Vec3 position(Vec3 origin,Vec3 destination,double age) {
        double t=Math.clamp((age-CheckmateRules.HOP)/(CheckmateRules.HIT-CheckmateRules.HOP),0,1);
        var turn=corner(origin,destination);
        var horizontal=t<=2.0/3?origin.lerp(turn,t*1.5):turn.lerp(destination,(t-2.0/3)*3);
        return new Vec3(horizontal.x,origin.y+(destination.y-origin.y)*t+4*HEIGHT*t*(1-t),horizontal.z);
    }
    public static boolean occupies(Vec3 tile,AABB entity,double feetY) {
        return entity.maxX>tile.x-HALF&&entity.minX<tile.x+HALF
            &&entity.maxZ>tile.z-HALF&&entity.minZ<tile.z+HALF
            &&feetY>=tile.y-.25&&feetY<=tile.y+1.25;
    }
    /** Ray selection has no preference for occupied squares. Looking elsewhere cancels the cast. */
    public static int select(Vec3 eye,Vec3 look,List<Vec3> tiles) {
        if(look.y>=-1e-6) return -1;
        int selected=-1;double nearest=Double.POSITIVE_INFINITY;
        for(int i=0;i<tiles.size();i++) {
            var tile=tiles.get(i);double distance=(tile.y+.025-eye.y)/look.y;
            if(distance<=0||distance>16||distance>=nearest) continue;
            var hit=eye.add(look.scale(distance));
            if(Math.abs(hit.x-tile.x)<=HALF&&Math.abs(hit.z-tile.z)<=HALF) { selected=i;nearest=distance; }
        }
        return selected;
    }
    private CheckmateBoard() { }
}
