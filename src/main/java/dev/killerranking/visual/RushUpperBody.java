package dev.killerranking.visual;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** One native-size upper body on the player's exact position and facing. */
public final class RushUpperBody {
    public static final double SEPARATION=0;
    public static Vec3 origin(Vec3 player,float yaw,float scale){
        return player;
    }
    public static float facing(float yaw){
        return yaw;
    }
    public static Matrix4f root(Vec3 player,float yaw,float scale,Vec3 position,Vec3 rotation){
        var at=origin(player,yaw,scale);float nativeScale=.9375f*scale;
        return new Matrix4f().translation((float)at.x,(float)at.y,(float)at.z)
            .rotateY((float)Math.toRadians(180-facing(yaw)))
            .translate((float)position.x,(float)position.y+.7f,(float)position.z)
            .rotateZYX((float)rotation.z,(float)rotation.y,(float)rotation.x).translate(0,-.7f,0)
            .scale(-nativeScale,-nativeScale,nativeScale).translate(0,-1.501f,0);
    }
    private RushUpperBody(){ }
}
