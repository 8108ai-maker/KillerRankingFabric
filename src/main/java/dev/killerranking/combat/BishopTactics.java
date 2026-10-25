package dev.killerranking.combat;

import net.minecraft.world.phys.Vec3;

/** Bounded pursuit and pattern selection; no teleporting, stat inflation or retreat rush. */
public final class BishopTactics {
    public static int attack(int cycle,int formation,double gap,double escapingSpeed){
        if(gap>1.85||escapingSpeed>.12&&gap>1.25)return BishopRules.STAB_COMBO;
        return Math.floorMod(cycle+formation,3)==1?BishopRules.STAB_COMBO:BishopRules.COMBO;
    }
    public static Vec3 lead(Vec3 target,Vec3 velocity,int ticks){
        var horizontal=new Vec3(velocity.x,0,velocity.z).scale(ticks);
        if(!Double.isFinite(horizontal.lengthSqr()))return target;
        if(horizontal.lengthSqr()>.64)horizontal=horizontal.normalize().scale(.8);
        return target.add(horizontal);
    }
    public static double step(double gap,boolean thrust){
        return Double.isFinite(gap)?Math.clamp((gap-(thrust?1.1:1.25))*.7,0,thrust?.46:.4):0;
    }
    public static Vec3 approach(Vec3 self,Vec3 target,int formation){
        var towards=target.subtract(self);towards=new Vec3(towards.x,0,towards.z);
        if(towards.lengthSqr()<.001)return target;
        towards=towards.normalize();double side=formation%2==0?1:-1;
        // Different entry lanes spread the squad without forcing a detour away from the enemy.
        return target.add(-towards.z*side*.75,0,towards.x*side*.75).subtract(towards.scale(1.15));
    }
    private BishopTactics(){ }
}
