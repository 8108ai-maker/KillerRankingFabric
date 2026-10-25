package dev.killerranking.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

/** Trace loaded entities, independent of the player's normal interaction reach. */
public final class BishopDesignationTargeting {
    public static double entry(Vec3 eye, Vec3 direction, AABB box) {
        if (!Double.isFinite(direction.lengthSqr()) || direction.lengthSqr() < 1e-12) return Double.POSITIVE_INFINITY;
        var ray = direction.normalize();
        if (box.contains(eye)) return 0;
        double reach = eye.distanceTo(box.getCenter()) + box.getSize() * 2 + 1;
        return box.clip(eye, eye.add(ray.scale(reach))).map(p -> p.distanceTo(eye)).orElse(Double.POSITIVE_INFINITY);
    }
    public static LivingEntity selected(ServerPlayer owner) {
        var eye = owner.getEyePosition(); var ray = owner.getLookAngle();
        LivingEntity selected = null; double nearest = Double.POSITIVE_INFINITY;
        for (var entity : owner.serverLevel().getAllEntities()) {
            if (!(entity instanceof LivingEntity target) || target == owner || !target.isAlive()
                    || target.isSpectator() || !target.isPickable()) continue;
            // The caster's escort formation must not block its own targeting ray.
            if(target instanceof dev.killerranking.entity.BishopFollower follower&&owner.getUUID().equals(follower.ownerId()))continue;
            double distance = entry(eye, ray, target.getBoundingBox().inflate(.1));
            if (distance < nearest) { nearest = distance; selected = target; }
        }
        // An ally in front blocks selection of an enemy behind it, just like a wall.
        if (!BishopTargets.allowed(owner, selected)) return null;
        if (!loadedPath(owner, eye, ray, nearest)) return null;
        var hit = owner.serverLevel().clip(new ClipContext(eye, eye.add(ray.scale(nearest)),
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        return hit.getType() == HitResult.Type.MISS ? selected : null;
    }
    private static boolean loadedPath(ServerPlayer owner,Vec3 eye,Vec3 ray,double distance){
        int x=(int)Math.floor(eye.x/16),z=(int)Math.floor(eye.z/16);
        int sx=ray.x>=0?1:-1,sz=ray.z>=0?1:-1;
        double tx=Math.abs(ray.x)<1e-12?Double.POSITIVE_INFINITY:(((sx>0?x+1:x)*16)-eye.x)/ray.x;
        double tz=Math.abs(ray.z)<1e-12?Double.POSITIVE_INFINITY:(((sz>0?z+1:z)*16)-eye.z)/ray.z;
        double dx=Math.abs(ray.x)<1e-12?Double.POSITIVE_INFINITY:16/Math.abs(ray.x);
        double dz=Math.abs(ray.z)<1e-12?Double.POSITIVE_INFINITY:16/Math.abs(ray.z);
        while(true){
            if(!owner.serverLevel().hasChunk(x,z))return false;
            if(Math.min(tx,tz)>distance)return true;
            if(tx<tz){x+=sx;tx+=dx;}else if(tz<tx){z+=sz;tz+=dz;}else{x+=sx;z+=sz;tx+=dx;tz+=dz;}
        }
    }
    private BishopDesignationTargeting() { }
}
