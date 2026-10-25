package dev.killerranking.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.visual.BatTrailHistory;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import java.util.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Records the final item matrix, including wrist, handedness and other animation mods. */
public final class NightBatTrail {
    private record Key(UUID player,boolean left,Item item) { }
    private record Capture(LivingEntity player,boolean left) { }
    private static final Map<Key,BatTrailHistory> HISTORY=new HashMap<>();
    private static final Map<UUID,LivingEntity> OWNERS=new HashMap<>();
    private static final Matrix4f VIEW_INVERSE=new Matrix4f();
    private static ClientLevel world;
    private static Capture capture;
    private static Vec3 camera=Vec3.ZERO;
    private static double time;
    private static float delta;
    private static boolean frameReady;

    public static void register() {
        WorldRenderEvents.START.register(context->{
            if(world!=context.world()) { clear();world=context.world(); }
            VIEW_INVERSE.set(context.positionMatrix()).invert();
            camera=context.camera().getPosition();
            delta=context.tickCounter().getGameTimeDeltaPartialTick(false);
            time=world.getGameTime()+delta;frameReady=true;
        });
        WorldRenderEvents.AFTER_ENTITIES.register(NightBatTrail::render);
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->clear());
        ClientPlayConnectionEvents.JOIN.register((handler,sender,client)->clear());
    }
    public static void begin(LivingEntity entity,ItemStack item,ItemDisplayContext display,boolean left) {
        capture=null;
        if(!frameReady||!supported(item)||!(entity instanceof AbstractClientPlayer||entity instanceof dev.killerranking.entity.BishopFollower)
            ||world!=entity.level()||!entity.isAlive()||entity.isInvisible()||entity.isSpectator()
            ||entity.isUsingItem()||RushVfx.active(entity.getUUID())||entity.position().distanceToSqr(camera)>1024) return;
        if(display!=ItemDisplayContext.THIRD_PERSON_LEFT_HAND&&display!=ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
            &&display!=ItemDisplayContext.FIRST_PERSON_LEFT_HAND&&display!=ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) return;
        capture=new Capture(entity,left);
    }
    public static void end() { capture=null; }
    public static void sample(ItemStack item,PoseStack pose) {
        if(capture==null||!supported(item)||Minecraft.getInstance().isPaused()) return;
        var c=capture;
        Vec3f marker;
        if(c.player() instanceof AbstractClientPlayer player)marker=PlayerAnimationAccess.getPlayerAnimLayer(player).get3DTransform("nightBatTrail",TransformType.POSITION,delta,Vec3f.ZERO);
        else if(c.player() instanceof dev.killerranking.entity.BishopFollower bishop)marker=((BishopFollowerRenderer)Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(bishop)).getModel().trail();
        else marker=Vec3f.ZERO;
        float gate=marker.getY();
        var key=new Key(c.player().getUUID(),c.left(),item.getItem());
        var history=HISTORY.get(key);
        // Every authored strike gate works with native-size arms, including skills and mobs.
        boolean striking=gate>.25f;
        if(item.is(KillerRankingMod.NEUNGSA_KATANA)&&c.player() instanceof AbstractClientPlayer player) {
            var cut=PlayerAnimationAccess.getPlayerAnimLayer(player).get3DTransform("neungsaCut",TransformType.POSITION,delta,Vec3f.ZERO);
            // Include the approach and follow-through of the authored cut, not
            // just its narrow damage window. The visible arc grows with the sword.
            float progress=Math.abs(cut.getX());
            striking=cut.getZ()>.5f&&progress>=8.5f&&progress<=18;
        }
        if(!striking) { if(history!=null) history.resetStroke();return; }
        if(history==null) {
            if(HISTORY.size()>=48) return;
            history=new BatTrailHistory(item.is(KillerRankingMod.NEUNGSA_KATANA));HISTORY.put(key,history);
        }
        // World renders use the world view; first-person renders can use a different
        // model-view. Convert both to stable world coordinates before storing points.
        var matrix=new Matrix4f(VIEW_INVERSE).mul(RenderSystem.getModelViewMatrix()).mul(pose.last().pose());
        Vec3 inner,tip;
        if(item.is(KillerRankingMod.GOSSI_INHERITED_KNIFE)) {
            inner=point(matrix,8/16f,9/16f,8/16f);tip=point(matrix,8/16f,20.5f/16f,8/16f);
        } else if(item.is(KillerRankingMod.SALMOSA_MID_DAGGER)) {
            inner=point(matrix,8/16f,dev.killerranking.visual.SalmosaKnifeGeometry.BLADE_BASE/16,8/16f);tip=point(matrix,8/16f,dev.killerranking.visual.SalmosaKnifeGeometry.TIP/16,8/16f);
        } else if(item.is(KillerRankingMod.EUNBAL_HATCHET)) {
            inner=point(matrix,12/16f,12/16f,8/16f);tip=point(matrix,15.5f/16f,16/16f,8/16f);
        } else if(item.is(KillerRankingMod.NEUNGSA_KATANA)) {
            inner=point(matrix,8.5f/16f,10.6f/16f,8/16f);tip=point(matrix,8.53906f/16f,31.87934f/16f,8/16f);
        } else if(item.is(KillerRankingMod.PAWN6_KATANA)) {
            inner=point(matrix,8/16f,9.4f/16f,8/16f);tip=point(matrix,7.91675f/16f,31.60590f/16f,8/16f);
        } else if(item.is(KillerRankingMod.YUHYEOLMOKI_SAW_KNIFE)) {
            inner=point(matrix,8/16f,6/16f,8/16f);tip=point(matrix,7.17500f/16f,29.89648f/16f,8/16f);
        } else if(item.is(KillerRankingMod.BISHOP_DAGGER)){
            inner=point(matrix,8/16f,8.4f/16f,8/16f);tip=point(matrix,8/16f,25.2f/16f,8/16f);
            // Keep the short dagger model; extend only its anime cutting ribbon.
            var blade=tip.subtract(inner);tip=inner.add(blade.scale(2.4));inner=inner.subtract(blade.scale(.15));
        } else {
            inner=point(matrix,8/16f,15/16f,8/16f);tip=point(matrix,8/16f,30/16f,8/16f);
        }
        if(!Double.isFinite(tip.x+tip.y+tip.z)||tip.distanceToSqr(c.player().position())>36) return;
        var style=TrailStyle.of(item.getItem());
        if(style.tipExtension>0) {
            // Chunky weapons read wider than their model: push the ribbon's outer edge past the tip.
            var blade=tip.subtract(inner);double length=blade.length();
            if(length>1e-4) tip=tip.add(blade.scale(style.tipExtension/length));
        }
        OWNERS.put(c.player().getUUID(),c.player());
        if(c.player() instanceof dev.killerranking.entity.BishopFollower||CheckmateVfx.active(c.player().getUUID())||CleaveVfx.active(c.player().getUUID())||MadnessLeapVfx.active(c.player().getUUID()))
            history.sampleStroke(time,inner,tip,c.player().getPosition(delta),true,(int)Math.ceil(Math.abs(marker.getX())));
        else history.sample(time,inner,tip,c.player().getPosition(delta),true);
    }
    public static void hand(AbstractClientPlayer player,boolean left,PoseStack pose,float center,Vec3f marker){
        if(!frameReady||world!=player.level()||Minecraft.getInstance().isPaused()||player.isInvisible()||player.position().distanceToSqr(camera)>1024)return;
        var key=new Key(player.getUUID(),left,null);var history=HISTORY.get(key);
        if(marker.getY()<=.25){if(history!=null)history.resetStroke();return;}
        if(history==null){if(HISTORY.size()>=48)return;history=new BatTrailHistory();HISTORY.put(key,history);}
        var matrix=new Matrix4f(VIEW_INVERSE).mul(RenderSystem.getModelViewMatrix()).mul(pose.last().pose());
        var inner=point(matrix,center/16,7.5f/16,0);var tip=point(matrix,center/16,11.5f/16,0);
        if(!Double.isFinite(tip.x+tip.y+tip.z)||tip.distanceToSqr(player.position())>36)return;
        OWNERS.put(player.getUUID(),player);history.sampleStroke(time,inner,tip,player.getPosition(delta),true,(int)Math.ceil(Math.abs(marker.getX())));
    }
    private static Vec3 point(Matrix4f matrix,float x,float y,float z) {
        var p=matrix.transformPosition(new Vector3f(x,y,z));return new Vec3(p.x+camera.x,p.y+camera.y,p.z+camera.z);
    }
    private static void render(WorldRenderContext context) {
        if(context.matrixStack()==null||context.consumers()==null) return;
        var stack=context.matrixStack();
        VertexConsumer vertices=null;
        boolean minimal=Minecraft.getInstance().options.particles().get()==ParticleStatus.MINIMAL;
        for(var iterator=HISTORY.entrySet().iterator();iterator.hasNext();) {
            var entry=iterator.next();var p=OWNERS.get(entry.getKey().player());
            if(p==null||p.level()!=world||p.isRemoved()||!p.isAlive()||p.isInvisible()||p.isSpectator()||p.distanceToSqr(camera)>1024) { iterator.remove();continue; }
            var held=(p.getMainArm()==HumanoidArm.LEFT)==entry.getKey().left()?p.getMainHandItem():p.getOffhandItem();
            if(entry.getKey().item()!=null&&(!held.is(entry.getKey().item())||RushVfx.active(p.getUUID()))) { iterator.remove();continue; }
            var ribbons=entry.getValue().visible(time);
            if(ribbons.isEmpty()) { if(entry.getValue().canDiscard(time)) iterator.remove();continue; }
            var style=TrailStyle.of(entry.getKey().item());
            boolean neungsa=style==TrailStyle.NEUNGSA;
            vertices=context.consumers().getBuffer(WeaponTrailRenderType.of(style));
            for(var ribbon:ribbons) {
                double age=Math.max(0,time-ribbon.born());
                int frame=ribbon.frame(time);
                if(frame<0) continue;
                float alpha=style.alpha*(float)(neungsa?Math.max(0,1-age/BatTrailHistory.LIFETIME):Math.min(1,(BatTrailHistory.LIFETIME-age)/1.25));
                var points=ribbon.points();double start=points.getFirst().distance();
                double length=points.getLast().distance()-start;if(length<.001) continue;
                if(neungsa) frame=age<.6?(length<.35?0:length<.9?1:length<1.6?2:3):Math.clamp(4+(int)((age-.6)*4/(BatTrailHistory.LIFETIME-.6)),4,7);
                int step=minimal?2:1;
                for(int i=0;i<points.size()-1;i+=step) {
                    var a=points.get(i);var b=points.get(Math.min(i+step,points.size()-1));
                    float u0=(float)((a.distance()-start)/length),u1=(float)((b.distance()-start)/length);
                    // The live leading edge stays attached to the blade. Once the
                    // cut ends, erase the oldest part first instead of blinking
                    // the entire ribbon out while the sword is still moving.
                    if(neungsa) {
                        float tail=(float)Math.max(0,(age-1)/(BatTrailHistory.LIFETIME-1));
                        if(u1<=tail) continue;
                        if(u0<tail) {
                            double t=(tail-u0)/(u1-u0);
                            a=new BatTrailHistory.Point(a.inner().lerp(b.inner(),t),a.tip().lerp(b.tip(),t),a.distance()+(b.distance()-a.distance())*t);
                            u0=tail;
                        }
                    }
                    quad(stack,vertices,a,b,u0,u1,frame,alpha,neungsa);
                }
            }
        }
        OWNERS.keySet().removeIf(id->HISTORY.keySet().stream().noneMatch(key->key.player().equals(id)));
    }
    private static void quad(PoseStack stack,VertexConsumer v,BatTrailHistory.Point a,BatTrailHistory.Point b,float u0,float u1,int frame,float alpha,boolean neungsa) {
        var pose=stack.last();
        Vec3 ai=a.inner(),at=a.tip(),bi=b.inner(),bt=b.tip();
        Vec3 across=at.subtract(ai),along=bt.subtract(at);
        double areaScale=across.lengthSqr()*along.lengthSqr();
        if(areaScale<1e-16) return;
        Vec3 normal=across.cross(along);
        if(!neungsa&&normal.lengthSqr()<areaScale*.0004) {
            // A straight thrust has no swept blade plane: give its tip path a
            // small camera-facing width instead of an invisible zero-area quad.
            Vec3 direction=bt.subtract(at).normalize();
            Vec3 side=direction.cross(camera.subtract(at)).normalize().scale(.08);
            if(side.lengthSqr()<.00001) return;
            ai=at.subtract(side);at=at.add(side);bi=bt.subtract(side);bt=bt.add(side);
            normal=at.subtract(ai).cross(bt.subtract(at));
        }
        normal=normal.normalize();
        // Eight strip frames occupy consecutive rows in the texture.
        float v0=(frame+.015625f)/BatTrailHistory.FRAMES,v1=(frame+.984375f)/BatTrailHistory.FRAMES;
        var corners=new Vec3[]{ai,at,bt,bi};
        for(int i=0;i<4;i++) {
            Vec3 c=corners[i].subtract(camera);
            v.addVertex(pose.pose(),(float)c.x,(float)c.y,(float)c.z)
                .setColor(1,1,1,neungsa?alpha*(.65f+.35f*(i<2?u0:u1)):alpha).setUv(i<2?u0:u1,i==0||i==3?v1:v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose,(float)normal.x,(float)normal.y,(float)normal.z);
        }
    }
    public static int segmentCount() { return HISTORY.values().stream().flatMap(h->h.visible(time).stream()).mapToInt(r->r.points().size()-1).sum(); }
    public static int segmentCount(UUID entity){return HISTORY.entrySet().stream().filter(e->e.getKey().player().equals(entity)).flatMap(e->e.getValue().visible(time).stream()).mapToInt(r->r.points().size()-1).sum();}
    public static int strokeCount(UUID entity){return HISTORY.entrySet().stream().filter(e->e.getKey().player().equals(entity)).mapToInt(e->e.getValue().visible(time).size()).sum();}
    public static int handSegmentCount(UUID entity,boolean left){return HISTORY.entrySet().stream().filter(e->e.getKey().player().equals(entity)&&e.getKey().item()==null&&e.getKey().left()==left).flatMap(e->e.getValue().visible(time).stream()).mapToInt(r->r.points().size()-1).sum();}
    private static boolean supported(ItemStack item) {
        return item.is(KillerRankingMod.SALMOSA_MID_DAGGER)||item.is(KillerRankingMod.BISHOP_DAGGER)||item.is(KillerRankingMod.NIGHT_BARBED_BAT)||item.is(KillerRankingMod.GOSSI_INHERITED_KNIFE)||item.is(KillerRankingMod.EUNBAL_HATCHET)||item.is(KillerRankingMod.PAWN6_KATANA)||item.is(KillerRankingMod.NEUNGSA_KATANA)||item.is(KillerRankingMod.YUHYEOLMOKI_SAW_KNIFE);
    }
    private static void clear() { HISTORY.clear();OWNERS.clear();SkillArmTrails.clear();capture=null;frameReady=false;world=null; }
    private NightBatTrail() { }
}
