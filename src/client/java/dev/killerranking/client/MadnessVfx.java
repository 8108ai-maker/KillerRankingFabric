package dev.killerranking.client;

import dev.killerranking.Trait;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.world.entity.player.Player;
import java.util.*;

/** Mixed Madness uses the same server-owned introduction timer as the trait toggle. */
public final class MadnessVfx {
    public static boolean active(UUID id) { return TraitVisuals.trait(id) == Trait.MIXED_MADNESS && TraitVisuals.intro(id) > 0; }
    private static List<Player> visible(Minecraft client) {
        var camera=client.getCameraEntity();
        if (client.level==null || camera==null) return List.of();
        return client.level.players().stream().filter(p -> active(p.getUUID()) && !KillingIntentVfx.hiddenForCamera(p) && p.distanceToSqr(camera)<=1024)
            .sorted(Comparator.comparingDouble(p -> p.distanceToSqr(camera))).limit(4).map(p -> (Player)p).toList();
    }
    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (context.matrixStack()==null || context.consumers()==null) return;
            float delta=context.tickCounter().getGameTimeDeltaPartialTick(false);
            for(var player:visible(Minecraft.getInstance())) {
                float left=TraitVisuals.intro(player.getUUID())-delta;
                float alpha=Math.min(1,(100-left)/8f)*Math.min(1,left/8f);
                MadnessGlyph.render(context,player,delta,Math.max(0,alpha),100-left);
            }
        });
    }
    public static void tick(Minecraft client) {
        var sprites=KillingIntentVfx.sprites();
        if (client.level==null || client.isPaused() || sprites==null) return;
        var setting=client.options.particles().get();
        int count=setting==ParticleStatus.MINIMAL?1:setting==ParticleStatus.DECREASED?4:8;
        for(var player:visible(client)) {
            for(int i=0;i<count;i++) client.particleEngine.add(new MadnessAuraParticle(client.level,player,sprites,false));
            if(setting!=ParticleStatus.MINIMAL) client.particleEngine.add(new MadnessAuraParticle(client.level,player,sprites,true));
        }
    }
    private MadnessVfx() { }
}
