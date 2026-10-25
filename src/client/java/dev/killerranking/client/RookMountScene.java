package dev.killerranking.client;

import dev.killerranking.combat.RookMountPose;
import dev.killerranking.visual.RookMountLayout;
import java.util.UUID;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Hide the standing caster and draw its animated skin in a separate grounded scene. */
public final class RookMountScene {
    private static UUID rendering;
    private static long hidden,frames;

    public static boolean hideOriginal(AbstractClientPlayer player) {
        boolean hide=!player.getUUID().equals(rendering)&&RookVfx.mountVictim(player.getUUID())!=null;
        if(hide)hidden++;
        return hide;
    }
    public static Vec3 position(AbstractClientPlayer player,LivingEntity victim,float delta) {
        var center=victim.getPosition(delta);
        var away=new Vec3(player.getX()-victim.getX(),0,player.getZ()-victim.getZ());
        if(away.lengthSqr()<.0001){var look=player.getLookAngle();away=new Vec3(-look.x,0,-look.z);}
        double root=RookMountLayout.rootOffset(victim.getBbHeight(),victim.getBbWidth(),RookMountPose.inverted(victim),player.getScale());
        return center.add(away.normalize().scale(.15)).add(0,root,0);
    }
    public static void render(WorldRenderContext context) {
        var c=Minecraft.getInstance();
        if(c.level==null||context.matrixStack()==null||context.consumers()==null)return;
        float delta=context.tickCounter().getGameTimeDeltaPartialTick(false);
        var camera=context.camera().getPosition();var stack=context.matrixStack();
        for(var player:c.level.players()) {
            var victim=RookVfx.mountVictim(player.getUUID());
            if(victim==null||player.position().distanceToSqr(camera)>4096)continue;
            var at=position(player,victim,delta);
            var renderer=(PlayerRenderer)c.getEntityRenderDispatcher().getRenderer(player);
            stack.pushPose();stack.translate(at.x-camera.x,at.y-camera.y,at.z-camera.z);
            rendering=player.getUUID();
            try {
                renderer.render(player,player.getYRot(),delta,stack,context.consumers(),c.getEntityRenderDispatcher().getPackedLightCoords(player,delta));
                frames++;
            } finally {rendering=null;stack.popPose();}
        }
    }
    public static long renderedFrames(){return frames;}
    public static long hiddenFrames(){return hidden;}
    private RookMountScene() { }
}
