package dev.killerranking.client;

import dev.killerranking.combat.BishopBarrageMotion;
import dev.killerranking.network.NeungsaCutEffectPayload;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** The same animated ribbons as Brothers' Help, fitted to the victim rather than the whole damage zone. */
public final class NeungsaHitCutVfx {
    private static final List<Cut> ACTIVE=new ArrayList<>();
    private static int segments;
    private static final class Cut {
        final LivingEntity target;
        final BishopBarrageMotion.Stroke[] strokes=new BishopBarrageMotion.Stroke[2];
        int age;
        Cut(LivingEntity target){
            this.target=target;long seed=ThreadLocalRandom.current().nextLong();
            for(int i=0;i<strokes.length;i++){
                var source=BishopBarrageMotion.stroke(seed,0,i);
                // Center each moving ribbon on the struck torso; retain Bishop's shape and staggered sweep.
                strokes[i]=new BishopBarrageMotion.Stroke(0,0,0,source.cosine(),source.sine(),source.halfLength(),source.bend(),source.depthTilt()*.15f,source.depthBend()*.15f);
            }
        }
    }
    public static void register(){
        WorldRenderEvents.AFTER_ENTITIES.register(NeungsaHitCutVfx::render);
        ClientTickEvents.END_CLIENT_TICK.register(c->{
            if(c.level==null){clear();return;}if(c.isPaused())return;
            ACTIVE.removeIf(cut->++cut.age>=8||cut.target.isRemoved()||cut.target.level()!=c.level);
        });
    }
    public static void emit(NeungsaCutEffectPayload p){
        var c=Minecraft.getInstance();if(c.level==null)return;
        var e=c.level.getEntity(p.entity());if(e instanceof LivingEntity target&&target.getUUID().equals(p.uuid()))ACTIVE.add(new Cut(target));
    }
    public static boolean active(UUID target){return ACTIVE.stream().anyMatch(c->c.target.getUUID().equals(target));}
    public static int segments(){return segments;}
    public static void clear(){ACTIVE.clear();segments=0;}
    private static void render(WorldRenderContext context){
        segments=0;if(context.matrixStack()==null||context.consumers()==null)return;
        var camera=context.camera().getPosition();float partial=context.tickCounter().getGameTimeDeltaPartialTick(false);
        var vertices=context.consumers().getBuffer(WeaponTrailRenderType.LAYER);
        for(var cut:ACTIVE){
            var target=cut.target;if(target.isRemoved()||target.distanceToSqr(camera)>4096)continue;
            var center=new Vec3(target.xOld+(target.getX()-target.xOld)*partial,target.yOld+(target.getY()-target.yOld)*partial+target.getBbHeight()*.56,target.zOld+(target.getZ()-target.zOld)*partial);
            var at=center.add(camera.subtract(center).normalize().scale(target.getBbWidth()*.65+.08));
            var stack=context.matrixStack();stack.pushPose();stack.translate(at.x-camera.x,at.y-camera.y,at.z-camera.z);
            stack.mulPose(context.camera().rotation());
            // Bishop's bright inner ribbon uses negative local depth. With a camera-facing plane,
            // flip that axis so the inner ribbon is nearer and blends over the dark outer ribbon.
            float size=Math.clamp(target.getBbHeight()*.42f,.45f,1.6f);stack.scale(size,size,-size);
            for(int i=0;i<cut.strokes.length;i++)segments+=BishopBarrageVfx.renderStroke(stack,vertices,cut.strokes[i],cut.age+partial-i*.8f);
            stack.popPose();
        }
    }
    private NeungsaHitCutVfx(){ }
}
