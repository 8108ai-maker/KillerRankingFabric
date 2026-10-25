package dev.killerranking.visual;

/** Render-only impact curve. Angles are degrees; translations are metres. */
public final class CombatCameraMotion {
    public record Pose(float pitch,float yaw,float roll,float x,float y,float fov) {
        public static final Pose REST=new Pose(0,0,0,0,0,1);
    }
    private static float smooth(float p){p=Math.clamp(p,0,1);return p*p*(3-2*p);}
    public static Pose sample(float age,float power,float side,boolean parry){
        float duration=parry?8:7;
        if(!Float.isFinite(age)||!Float.isFinite(power)||!Float.isFinite(side)||age<=0||age>=duration)return Pose.REST;
        power=Math.clamp(power,0,1.5f);side=side<0?-1:1;
        float kick=smooth(age/.65f)*(1-smooth((age-.65f)/2.1f));
        float rebound=smooth((age-1.3f)/1.2f)*(1-smooth((age-2.5f)/2.2f));
        float settle=smooth(age/.4f)*(1-smooth(age/duration))*(float)Math.exp(-age*.36);
        float fine=(float)Math.sin(age*7.2f)*settle;
        float strength=power*(parry?1.3f:1);
        // One fast focus, a small outward rebound, then exact resting projection.
        float fov=1-strength*(parry?.075f:.038f)*kick+strength*.012f*rebound;
        return new Pose(strength*(-.72f*kick+.27f*rebound+.17f*fine),
            strength*side*(.38f*kick-.21f*rebound+.12f*fine),
            strength*side*(.28f*kick-.16f*rebound+.14f*fine),
            strength*side*.0028f*fine,strength*(.003f*kick+.0017f*fine),fov);
    }
    private CombatCameraMotion(){ }
}
