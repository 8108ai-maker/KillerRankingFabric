package dev.killerranking.client;

import dev.killerranking.KillerRankingMod;
import dev.killerranking.visual.NeungsaDrawState;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import dev.kosmx.playerAnim.minecraftApi.layers.LeftHandedHelperModifier;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Hold the hilt and saya during locomotion; Better Combat still owns every cut. */
public final class NeungsaDrawController {
    private static final net.minecraft.resources.ResourceLocation LAYER = KillerRankingMod.id("neungsa_draw_layer");
    private static final Map<UUID, NeungsaDrawState> STATES = new HashMap<>();
    private static final Map<UUID,Long> DRAW_FINISH = new HashMap<>();
    private static final Map<UUID,String> CLIPS = new HashMap<>();

    public static void register() {
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, stack) -> {
            var layer = new ModifierLayer<IAnimation>();
            layer.addModifierLast(new SpeedModifier(32));
            layer.addModifierLast(new LeftHandedHelperModifier(player));
            stack.addAnimLayer(1900, layer);
            PlayerAnimationAccess.getPlayerAssociatedData(player).set(LAYER, layer);
        });
        ClientPlayConnectionEvents.JOIN.register((h,s,c) -> { STATES.clear(); DRAW_FINISH.clear(); CLIPS.clear(); });
        ClientPlayConnectionEvents.DISCONNECT.register((h,c) -> { STATES.clear(); DRAW_FINISH.clear(); CLIPS.clear(); });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.isPaused()) return;
            STATES.keySet().removeIf(id -> client.level.getPlayerByUUID(id) == null);
            DRAW_FINISH.keySet().removeIf(id -> !STATES.containsKey(id));
            CLIPS.keySet().removeIf(id -> !STATES.containsKey(id));
            for (var player : client.level.players()) {
                var state = STATES.get(player.getUUID());
                if (state == null && player.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA))
                    state = STATES.computeIfAbsent(player.getUUID(), id -> new NeungsaDrawState());
                if (state == null) continue;
                if (!player.isAlive() || !player.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA)) {
                    state.reset(); DRAW_FINISH.remove(player.getUUID()); set(player, null); continue;
                }
                var readyAt = DRAW_FINISH.get(player.getUUID());
                if (readyAt != null && player.tickCount >= readyAt) {
                    DRAW_FINISH.remove(player.getUUID()); set(player,"drawn_guard");
                }
                var before = state.phase();
                boolean continuing = player == client.player && client.screen == null && client.options.keyAttack.isDown();
                var after = state.tick(player.tickCount, continuing || NeungsaHeldBlade.hidden(player));
                if (before != after) set(player, after == NeungsaDrawState.Phase.SHEATHING ? "sheathe" : null);
                // Better Combat fades its idle pose while moving. Keep the
                // existing two-hand hilt/saya grip above locomotion, below
                // attacks/parry. This clip has no body, head or leg channels.
                if (after == NeungsaDrawState.Phase.SHEATHED)
                    set(player, player.getSkin().model() == net.minecraft.client.resources.PlayerSkin.Model.SLIM
                        ? "carry_slim" : "carry");
            }
        });
    }

    public static void iaido(AbstractClientPlayer player){
        STATES.computeIfAbsent(player.getUUID(),id->new NeungsaDrawState()).reset();
        DRAW_FINISH.remove(player.getUUID());set(player,player.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM?"carry_slim":"carry");
    }
    public static void parry(AbstractClientPlayer player){
        interruptIaidoRecovery(player);
        STATES.computeIfAbsent(player.getUUID(),id->new NeungsaDrawState()).attack(player.tickCount,12);
        DRAW_FINISH.remove(player.getUUID());set(player,"drawn_guard");
    }
    public static String attack(AbstractClientPlayer player, String name, float length) {
        if (!name.startsWith("killerranking:neungsa_katana_") || name.endsWith("parry")) return name;
        interruptIaidoRecovery(player);
        var state = STATES.computeIfAbsent(player.getUUID(), id -> new NeungsaDrawState());
        boolean first = state.attack(player.tickCount, length);
        // Keep the sheathed idle under the first fade until extraction clears
        // the scabbard. Installing drawn idle immediately flashes the blade.
        if (first) {
            DRAW_FINISH.put(player.getUUID(),(long)player.tickCount + Math.max(1,(int)Math.ceil(length * 7 / 26)));
            set(player,null);
        } else { DRAW_FINISH.remove(player.getUUID()); set(player,"drawn_guard"); }
        return first ? name.replace("neungsa_katana_", "neungsa_katana_draw_") : name;
    }
    public static void interruptIaidoRecovery(AbstractClientPlayer player){
        if(NeungsaIaidoVfx.interruptRecovery(player.getUUID())){
            STATES.computeIfAbsent(player.getUUID(),id->new NeungsaDrawState()).attack(player.tickCount,0);
            DRAW_FINISH.remove(player.getUUID());set(player,"drawn_guard");
        }
    }
    public static NeungsaDrawState.Phase phase(AbstractClientPlayer player) {
        var state = STATES.get(player.getUUID());
        return state == null ? NeungsaDrawState.Phase.SHEATHED : state.phase();
    }
    @SuppressWarnings("unchecked")
    private static void set(AbstractClientPlayer player, String clip) {
        var data = PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER);
        if (!(data instanceof ModifierLayer<?> raw)) return;
        var layer = (ModifierLayer<IAnimation>)raw;
        if (java.util.Objects.equals(CLIPS.get(player.getUUID()), clip)) return;
        if (clip == null) { layer.setAnimation(null); CLIPS.remove(player.getUUID()); return; }
        var playable = PlayerAnimationRegistry.getAnimation(KillerRankingMod.id("neungsa_katana_" + clip));
        if (playable == null) return;
        var animation = (IAnimation)playable.playAnimation();
        if (animation instanceof KeyframeAnimationPlayer keys) {
            keys.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
            keys.setFirstPersonConfiguration(new FirstPersonConfiguration(true,true,true,false));
        }
        layer.setAnimation(animation);
        CLIPS.put(player.getUUID(), clip);
    }
    private NeungsaDrawController() { }
}
