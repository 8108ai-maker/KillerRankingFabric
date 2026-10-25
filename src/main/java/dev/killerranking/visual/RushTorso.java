package dev.killerranking.visual;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Place a rigid native torso so its lower centre stays on the hip joint. */
public final class RushTorso {
    public static Vec3 pivot(Vec3 hip,Vec3 rotation){
        var bottom=new Matrix4f().rotateZYX((float)rotation.z,(float)rotation.y,(float)rotation.x)
            .transformPosition(new Vector3f(0,12,0));
        return hip.subtract(bottom.x,bottom.y,bottom.z);
    }
    private RushTorso(){ }
}
