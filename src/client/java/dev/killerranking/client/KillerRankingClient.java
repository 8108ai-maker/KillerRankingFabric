package dev.killerranking.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.network.OpenPhonePayload;
import dev.killerranking.network.ParryEffectPayload;
import dev.killerranking.network.ParryRequestPayload;
import dev.killerranking.network.ProfilePayload;
import dev.killerranking.network.CombatStatusPayload;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.minecraftApi.layers.LeftHandedHelperModifier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.lwjgl.glfw.GLFW;

public final class KillerRankingClient implements ClientModInitializer {
	private static boolean choiceOffered;
    private static boolean checkmateHeld,rookHeld,cleaveHeld,madnessHeld,knifeHeld,shoulderHeld,neungsaHeld,zWasDown,xWasDown,cWasDown;
    private static final KeyMapping ROOK_PUNCH_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.killerranking.rook_punch", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "category.killerranking"));
    private static final KeyMapping ROOK_MOUNT_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.killerranking.rook_mount", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.killerranking"));
    private static final KeyMapping TRAIT_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        "key.killerranking.trait_toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.killerranking"));
    private static final KeyMapping STATS_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        "key.killerranking.stats", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, "category.killerranking"));
    private static final ResourceLocation PARRY_LAYER = KillerRankingMod.id("parry_layer");
    private static final ResourceLocation PARRY_SPEED = KillerRankingMod.id("parry_speed");
    private static final KeyMapping RUSH_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        "key.killerranking.rush", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "category.killerranking"));
	private static final ResourceLocation PARRY_ANIMATION = KillerRankingMod.id("parry");
	private static final ResourceLocation PARRY_REVERSE_ANIMATION = KillerRankingMod.id("parry_reverse");
	private static final ResourceLocation NEUNGSA_PARRY_ANIMATION = KillerRankingMod.id("neungsa_katana_parry");
	private static final ResourceLocation NEUNGSA_PARRY_REVERSE_ANIMATION = KillerRankingMod.id("neungsa_katana_parry_reverse");
	private static final ResourceLocation KATANA_PARRY_ANIMATION = KillerRankingMod.id("pawn6_katana_parry");
	private static final ResourceLocation KATANA_PARRY_REVERSE_ANIMATION = KillerRankingMod.id("pawn6_katana_parry_reverse");

	private static final KeyMapping PARRY_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
		"key.killerranking.parry", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.killerranking"));

	@Override
	public void onInitializeClient() {
        ParryImpactVfx.register();
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.ParryImpactPayload.TYPE,(p,c)->c.client().execute(()->ParryImpactVfx.accept(p)));
        WeaponHitVfx.register();
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.WeaponHitPayload.TYPE,(p,c)->c.client().execute(()->WeaponHitVfx.accept(p)));
        NeungsaKatanaVisuals.register();
        NeungsaSlashVfx.register();
        NeungsaIaidoVfx.register();NeungsaEchoVfx.register();
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.NeungsaEchoVisualPayload.TYPE,(v,c)->c.client().execute(()->NeungsaEchoVfx.accept(v)));
        NeungsaHitCutVfx.register();
        net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.getInstance().register(KillerRankingMod.NEUNGSA_CUT,NeungsaCutParticle.Provider::new);
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.NeungsaIaidoVisualPayload.TYPE,(v,c)->c.client().execute(()->NeungsaIaidoVfx.accept(v)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.NeungsaCutEffectPayload.TYPE,(v,c)->c.client().execute(()->NeungsaCutParticle.emit(v)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.NeungsaStatusPayload.TYPE,(payload,context)->context.client().execute(()->ClientNeungsaSkill.set(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.NeungsaSlashVisualPayload.TYPE,(payload,context)->context.client().execute(()->NeungsaSlashVfx.accept(payload)));
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(BishopModel.LAYER,BishopModel::layer);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(BishopMaskLayer.LAYER,BishopMaskLayer::layer);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(KillerRankingMod.BISHOP_FOLLOWER,BishopFollowerRenderer::new);
        net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.getInstance().register(KillerRankingMod.BISHOP_SLASH,BishopSlashParticle.Provider::new);
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.BishopStatusPayload.TYPE,(payload,context)->context.client().execute(()->ClientBishop.set(payload)));
        BishopBarrageVfx.register();
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.BishopBarrageVisualPayload.TYPE,(payload,context)->context.client().execute(()->BishopBarrageVfx.accept(payload)));
        KillingIntentVfx.register();
        PoisonVfx.register();
        SpiritVfx.register();
        MadnessVfx.register();
        ShoulderStabVfx.register();
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.ShoulderStabStatusPayload.TYPE,(payload,context)->context.client().execute(()->ClientShoulderStab.set(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.ShoulderStabVisualPayload.TYPE,(payload,context)->context.client().execute(()->ShoulderStabVfx.accept(payload)));
        KnifePursuitVfx.register();
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.KnifePursuitStatusPayload.TYPE,(payload,context)->context.client().execute(()->ClientKnifePursuit.set(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.KnifePursuitVisualPayload.TYPE,(payload,context)->context.client().execute(()->KnifePursuitVfx.accept(payload)));
        WasabariVfx.register();
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.SalmosaStatusPayload.TYPE,(payload,context)->context.client().execute(()->ClientSalmosa.set(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.WasabariStatusPayload.TYPE,(payload,context)->context.client().execute(()->ClientWasabari.set(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.WasabariVisualPayload.TYPE,(payload,context)->context.client().execute(()->WasabariVfx.accept(payload)));
        HeavyPunchVfx.register(); RushVfx.register(); CheckmateVfx.register(); RookVfx.register(); CleaveVfx.register(); MadnessLeapVfx.register();
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.MadnessLeapStatusPayload.TYPE, (payload, context) -> context.client().execute(() -> ClientMadnessLeap.set(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.MadnessLeapVisualPayload.TYPE, (payload, context) -> context.client().execute(() -> MadnessLeapVfx.accept(payload))); AutoParryVfx.register();
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.CleaveStatusPayload.TYPE, (payload, context) -> context.client().execute(() -> ClientCleave.set(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.CleaveVisualPayload.TYPE, (payload, context) -> context.client().execute(() -> CleaveVfx.accept(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.RookStatusPayload.TYPE, (payload, context) -> context.client().execute(() -> ClientRook.set(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.RookVisualPayload.TYPE, (payload, context) -> context.client().execute(() -> RookVfx.accept(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.AutoParryStatusPayload.TYPE, (payload, context) -> context.client().execute(() -> AutoParryVfx.accept(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.AutoParryEffectPayload.TYPE, (payload, context) -> context.client().execute(() -> AutoParryVfx.accept(payload)));
        NightBatTrail.register();
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.CheckmateStatusPayload.TYPE, (payload, context) -> context.client().execute(() -> ClientCheckmate.set(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.CheckmateVisualPayload.TYPE, (payload, context) -> context.client().execute(() -> CheckmateVfx.accept(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.HeavyPunchStatusPayload.TYPE,(payload,context)->context.client().execute(()->ClientHeavyPunch.set(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.HeavyPunchVisualPayload.TYPE,(payload,context)->context.client().execute(()->HeavyPunchVfx.accept(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.RushStatusPayload.TYPE, (payload, context) -> context.client().execute(() -> ClientRush.set(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.RushVisualPayload.TYPE, (payload, context) -> context.client().execute(() -> RushVfx.accept(payload.player(), payload.remaining(), payload.age())));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.TraitVisualPayload.TYPE, (payload, context) -> context.client().execute(() -> TraitVisuals.accept(payload)));
        ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.HuntingMarkPayload.TYPE, (payload, context) -> context.client().execute(() -> HuntingMarkHud.accept(payload)));
		PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, stack) -> {
			ModifierLayer<IAnimation> layer = new ModifierLayer<>();
			// Better Combat's attack layer is 2000. Parry briefly overrides both arms.
			layer.addModifierLast(new LeftHandedHelperModifier(player));
			stack.addAnimLayer(3000, layer);
			PlayerAnimationAccess.getPlayerAssociatedData(player).set(PARRY_LAYER, layer);
		});
		ClientPlayNetworking.registerGlobalReceiver(dev.killerranking.network.DashboardPayload.TYPE, (payload, context) -> context.client().execute(() -> {
            ClientDashboard.set(dev.killerranking.network.DashboardData.JSON.fromJson(payload.json(), dev.killerranking.network.DashboardData.class));
            if (!ClientDashboard.needsChoice()) choiceOffered = false;
        }));
        ClientPlayNetworking.registerGlobalReceiver(ProfilePayload.TYPE, (payload, context) -> context.client().execute(() -> ClientProfile.set(payload)));
		ClientPlayNetworking.registerGlobalReceiver(CombatStatusPayload.TYPE, (payload, context) -> context.client().execute(() -> ClientCombatStatus.set(payload)));
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> { CombatCameraFeedback.clear();ParryImpactVfx.clear();WeaponHitVfx.clear();ClientNeungsaSkill.clear();NeungsaSlashVfx.clear();NeungsaEchoVfx.clear();NeungsaIaidoVfx.clear();ClientShoulderStab.clear();ShoulderStabVfx.clear();ClientSalmosa.clear(); ClientBishop.clear(); ClientAutoParry.clear(); AutoParryVfx.clear(); ClientMadnessLeap.clear(); MadnessLeapVfx.clear(); ClientCleave.clear(); CleaveVfx.clear(); ClientRook.clear(); RookVfx.clear(); ClientKnifePursuit.clear();KnifePursuitVfx.clear();ClientWasabari.clear();WasabariVfx.clear();ClientHeavyPunch.clear(); HeavyPunchVfx.clear(); ClientRush.clear(); RushVfx.clear(); ClientCheckmate.clear(); CheckmateVfx.clear(); ClientProfile.reset(); ClientCombatStatus.reset(); ClientDashboard.reset(); TraitVisuals.clear(); KillingIntentVfx.clear(); PoisonVfx.clear(); SpiritVfx.clear(); HuntingMarkHud.clear(); choiceOffered = false; checkmateHeld=false; rookHeld=false; cleaveHeld=false; madnessHeld=false; knifeHeld=false; shoulderHeld=false; neungsaHeld=false; zWasDown=false; xWasDown=false; cWasDown=false; });
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> { CombatCameraFeedback.clear();ParryImpactVfx.clear();WeaponHitVfx.clear();ClientNeungsaSkill.clear();NeungsaSlashVfx.clear();NeungsaEchoVfx.clear();NeungsaIaidoVfx.clear();ClientShoulderStab.clear();ShoulderStabVfx.clear();ClientSalmosa.clear(); ClientBishop.clear(); ClientAutoParry.clear(); AutoParryVfx.clear(); ClientMadnessLeap.clear(); MadnessLeapVfx.clear(); ClientCleave.clear(); CleaveVfx.clear(); ClientRook.clear(); RookVfx.clear(); ClientKnifePursuit.clear();KnifePursuitVfx.clear();ClientWasabari.clear();WasabariVfx.clear();ClientHeavyPunch.clear(); HeavyPunchVfx.clear(); ClientRush.clear(); RushVfx.clear(); ClientCheckmate.clear(); CheckmateVfx.clear(); ClientProfile.reset(); ClientCombatStatus.reset(); ClientDashboard.reset(); TraitVisuals.clear(); KillingIntentVfx.clear(); PoisonVfx.clear(); SpiritVfx.clear(); HuntingMarkHud.clear(); choiceOffered = false; checkmateHeld=false; rookHeld=false; cleaveHeld=false; madnessHeld=false; knifeHeld=false; shoulderHeld=false; neungsaHeld=false; zWasDown=false; xWasDown=false; cWasDown=false; });
		HudRenderCallback.EVENT.register((graphics, tickCounter) -> { CombatHud.render(graphics); HuntingMarkHud.render(graphics); RookHud.render(graphics); });
		ClientPlayNetworking.registerGlobalReceiver(OpenPhonePayload.TYPE, (payload, context) -> context.client().execute(() -> context.client().setScreen(new PhoneScreen())));
		ClientPlayNetworking.registerGlobalReceiver(ParryEffectPayload.TYPE, (payload, context) -> context.client().execute(() -> {
			if (context.client().level == null) return;
			Entity entity = context.client().level.getPlayerByUUID(payload.playerId());
			if (entity instanceof AbstractClientPlayer player) playParryAnimation(player, payload.reversed());
		}));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ParryImpactVfx.tick(client);WeaponHitVfx.tick(client);
            CombatCameraFeedback.tick(client);
            BishopBarrageVfx.tick(client);
            TraitVisuals.tick(client); KillingIntentVfx.tick(client); PoisonVfx.tick(client); SpiritVfx.tick(client); MadnessVfx.tick(client); ShoulderStabVfx.tick(client); KnifePursuitVfx.tick(client);WasabariVfx.tick(client);HeavyPunchVfx.tick(client); RushVfx.tick(client); CheckmateVfx.tick(client); RookVfx.tick(client); CleaveVfx.tick(client); MadnessLeapVfx.tick(client); AutoParryVfx.tick(client); HuntingMarkHud.tick(client);
			if (!client.isPaused()) { ClientBishop.tick(); ClientCombatStatus.tick(); ClientNeungsaSkill.tick();ClientShoulderStab.tick(); ClientKnifePursuit.tick();ClientWasabari.tick();ClientHeavyPunch.tick(); ClientRush.tick(); ClientCheckmate.tick(); ClientRook.tick(); ClientCleave.tick(); ClientMadnessLeap.tick(); }
            boolean usable=client.player!=null&&client.player.isAlive()&&!client.player.isSpectator()
                &&client.screen==null&&!client.isPaused();
            ClientAutoParry.input(client,ROOK_PUNCH_KEY.isDown());
            boolean down=RUSH_KEY.isDown();
            boolean madness=ClientMadnessLeap.available()&&TraitVisuals.enabled()&&client.player!=null&&client.player.getMainHandItem().is(KillerRankingMod.EUNBAL_HATCHET);
            if(madnessHeld&&(!down||!usable||!madness)){
                var target=ClientMadnessLeap.selected();
                madnessRequest(usable&&madness&&!down?dev.killerranking.network.MadnessLeapRequestPayload.RELEASE:dev.killerranking.network.MadnessLeapRequestPayload.CANCEL,target==null?-1:target.getId());
                madnessHeld=false;
            }
            boolean rook=ClientRook.branch()&&ClientRook.unlocked();
            boolean cleave=ClientCleave.branch()&&ClientCleave.unlocked();
            if(cleaveHeld&&(!down||!usable||!cleave)){
                cleaveRequest(usable&&cleave&&!down?dev.killerranking.network.CleaveRequestPayload.RELEASE:dev.killerranking.network.CleaveRequestPayload.CANCEL);
                cleaveHeld=false;
            }
            if(rookHeld&&(!down||!usable||!rook)) {
                var target=ClientRook.selected();
                rookRequest(usable&&rook&&!down?dev.killerranking.network.RookRequestPayload.CONFIRM:dev.killerranking.network.RookRequestPayload.CANCEL,target==null?-1:target.getId());
                rookHeld=false;
            }
            if(checkmateHeld&&(!down||!usable||!ClientCheckmate.branch()||rook)) {
                if(ClientPlayNetworking.canSend(dev.killerranking.network.CheckmateConfirmPayload.TYPE))
                    ClientPlayNetworking.send(new dev.killerranking.network.CheckmateConfirmPayload(usable&&ClientCheckmate.branch()&&!rook&&!down?CheckmateVfx.selectedTile():-1));
                checkmateHeld=false;
            }
            if(down&&!zWasDown&&usable&&!ClientRook.captured()&&!ClientAutoParry.active()) {
                if(ClientNeungsaSkill.available()&&client.player.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA)){
                    if(TraitVisuals.enabled()&&!ClientSkillCombat.blocksAttack()&&ClientNeungsaSkill.cooldown()==0&&ClientPlayNetworking.canSend(dev.killerranking.network.NeungsaRequestPayload.TYPE)){
                        ClientSkillCombat.cancelPendingAttack(client);ClientPlayNetworking.send(new dev.killerranking.network.NeungsaRequestPayload(dev.killerranking.network.NeungsaRequestPayload.START));
                    }
                }else if(ClientWasabari.available()){
                    if(TraitVisuals.enabled()&&!ClientSkillCombat.blocksAttack()&&ClientWasabari.cooldown()==0&&ClientPlayNetworking.canSend(dev.killerranking.network.WasabariRequestPayload.TYPE)){
                        ClientSkillCombat.cancelPendingAttack(client);ClientPlayNetworking.send(new dev.killerranking.network.WasabariRequestPayload());
                    }
                }else if(ClientBishop.branch()&&ClientBishop.unlocked()){
                    if(ClientBishop.castTicks()==0&&TraitVisuals.enabled()&&ClientPlayNetworking.canSend(dev.killerranking.network.BishopRequestPayload.TYPE)){
                        ClientSkillCombat.cancelPendingAttack(client);ClientPlayNetworking.send(new dev.killerranking.network.BishopRequestPayload(dev.killerranking.network.BishopRequestPayload.SUMMON));
                    }
                }else if(madness){
                    if(ClientMadnessLeap.phase()==0){ClientSkillCombat.cancelPendingAttack(client);madnessRequest(dev.killerranking.network.MadnessLeapRequestPayload.START,-1);madnessHeld=true;}
                }else if(cleave){
                    if(ClientCleave.phase()==0){cleaveRequest(dev.killerranking.network.CleaveRequestPayload.START);cleaveHeld=true;}
                }else if(rook) {
                    if(ClientRook.choice())rookRequest(dev.killerranking.network.RookRequestPayload.THROW,-1);
                    else if(ClientRook.phase()==0){rookRequest(dev.killerranking.network.RookRequestPayload.START,-1);rookHeld=true;}
                }else if(ClientCheckmate.branch()&&ClientPlayNetworking.canSend(dev.killerranking.network.CheckmateRequestPayload.TYPE)) {
                    ClientPlayNetworking.send(new dev.killerranking.network.CheckmateRequestPayload());checkmateHeld=true;
                }
            }
            while(RUSH_KEY.consumeClick()) {
                if(usable&&!ClientRook.locked()&&ClientRush.branch()&&!ClientCheckmate.branch()&&ClientPlayNetworking.canSend(dev.killerranking.network.RushRequestPayload.TYPE))
                    ClientPlayNetworking.send(new dev.killerranking.network.RushRequestPayload());
            }
            boolean xDown=ROOK_PUNCH_KEY.isDown();
            boolean neungsa=usable&&ClientNeungsaSkill.available()&&TraitVisuals.enabled()&&client.player.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA);
            if(neungsaHeld&&(!xDown||!neungsa)){
                if(ClientPlayNetworking.canSend(dev.killerranking.network.NeungsaRequestPayload.TYPE))ClientPlayNetworking.send(new dev.killerranking.network.NeungsaRequestPayload(neungsa&&!xDown?dev.killerranking.network.NeungsaRequestPayload.IAIDO_RELEASE:dev.killerranking.network.NeungsaRequestPayload.IAIDO_CANCEL));
                neungsaHeld=false;
            }
            if(xDown&&!xWasDown&&usable&&ClientNeungsaSkill.available()&&TraitVisuals.enabled()&&!ClientSkillCombat.blocksAttack()
                &&ClientNeungsaSkill.iaidoCooldown()==0&&client.player.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA)
                &&ClientPlayNetworking.canSend(dev.killerranking.network.NeungsaRequestPayload.TYPE)){
                ClientSkillCombat.cancelPendingAttack(client);ClientPlayNetworking.send(new dev.killerranking.network.NeungsaRequestPayload(dev.killerranking.network.NeungsaRequestPayload.IAIDO));
                neungsaHeld=true;
            }
            boolean knife=ClientKnifePursuit.available()&&TraitVisuals.enabled()&&client.player!=null&&client.player.getMainHandItem().is(KillerRankingMod.SALMOSA_MID_DAGGER);
            if(knifeHeld&&(!xDown||!usable||!knife)){
                var target=ClientKnifePursuit.selected();
                knifeRequest(usable&&knife&&!xDown?dev.killerranking.network.KnifePursuitRequestPayload.RELEASE:dev.killerranking.network.KnifePursuitRequestPayload.CANCEL,target==null?-1:target.getId());
                knifeHeld=false;
            }
            if(xDown&&!xWasDown&&usable&&knife&&!ClientSkillCombat.blocksAttack()&&ClientKnifePursuit.cooldown()==0){
                ClientSkillCombat.cancelPendingAttack(client);knifeRequest(dev.killerranking.network.KnifePursuitRequestPayload.START,-1);knifeHeld=true;
            }
            if(xDown&&!xWasDown&&usable&&ClientHeavyPunch.available()&&TraitVisuals.enabled()&&!ClientSkillCombat.blocksAttack()
                &&ClientHeavyPunch.cooldown()==0&&client.player.getMainHandItem().is(KillerRankingMod.TTEOKBAL_FIST)
                &&ClientPlayNetworking.canSend(dev.killerranking.network.HeavyPunchRequestPayload.TYPE)){
                ClientSkillCombat.cancelPendingAttack(client);ClientPlayNetworking.send(new dev.killerranking.network.HeavyPunchRequestPayload());
            }
            if(xDown&&!xWasDown&&usable&&ClientBishop.branch()&&ClientBishop.unlocked()&&TraitVisuals.enabled()&&!ClientRook.captured()
                    &&ClientPlayNetworking.canSend(dev.killerranking.network.BishopRequestPayload.TYPE)){
                    ClientPlayNetworking.send(new dev.killerranking.network.BishopRequestPayload(dev.killerranking.network.BishopRequestPayload.DESIGNATE));
            }
            while(ROOK_PUNCH_KEY.consumeClick())if(usable&&ClientRook.choice())rookRequest(dev.killerranking.network.RookRequestPayload.PUNCH,-1);
            xWasDown=xDown;
            boolean cDown=ROOK_MOUNT_KEY.isDown();
            if(cDown&&!cWasDown&&neungsa&&!ClientSkillCombat.blocksAttack()&&ClientNeungsaSkill.echoCooldown()==0&&ClientPlayNetworking.canSend(dev.killerranking.network.NeungsaRequestPayload.TYPE)){
                ClientSkillCombat.cancelPendingAttack(client);ClientPlayNetworking.send(new dev.killerranking.network.NeungsaRequestPayload(dev.killerranking.network.NeungsaRequestPayload.ECHO));
            }
            boolean shoulder=ClientShoulderStab.available()&&TraitVisuals.enabled()&&client.player!=null&&client.player.getMainHandItem().is(KillerRankingMod.SALMOSA_MID_DAGGER);
            if(shoulderHeld&&(!cDown||!usable||!shoulder)){
                var target=ClientShoulderStab.selected();
                if(ClientPlayNetworking.canSend(dev.killerranking.network.ShoulderStabRequestPayload.TYPE))ClientPlayNetworking.send(new dev.killerranking.network.ShoulderStabRequestPayload(usable&&shoulder&&!cDown?dev.killerranking.network.ShoulderStabRequestPayload.RELEASE:dev.killerranking.network.ShoulderStabRequestPayload.CANCEL,target==null?-1:target.getId()));
                shoulderHeld=false;
            }
            if(cDown&&!cWasDown&&usable&&shoulder&&ClientKnifePursuit.deflected()&&ClientShoulderStab.cooldown()==0&&ClientPlayNetworking.canSend(dev.killerranking.network.ShoulderStabRequestPayload.TYPE)){
                ClientSkillCombat.cancelPendingAttack(client);ClientPlayNetworking.send(new dev.killerranking.network.ShoulderStabRequestPayload(dev.killerranking.network.ShoulderStabRequestPayload.COUNTER,-1));
            }
            if(cDown&&!cWasDown&&usable&&shoulder&&!ClientKnifePursuit.deflected()&&!ClientSkillCombat.blocksAttack()&&ClientShoulderStab.cooldown()==0&&ClientPlayNetworking.canSend(dev.killerranking.network.ShoulderStabRequestPayload.TYPE)){
                ClientSkillCombat.cancelPendingAttack(client);ClientPlayNetworking.send(new dev.killerranking.network.ShoulderStabRequestPayload(dev.killerranking.network.ShoulderStabRequestPayload.START,-1));shoulderHeld=true;
            }

            if(cDown&&!cWasDown&&usable&&ClientBishop.branch()&&ClientBishop.unlocked()&&TraitVisuals.enabled()&&!ClientSkillCombat.blocksAttack()
                        &&ClientBishop.barrageCooldown()==0&&ClientPlayNetworking.canSend(dev.killerranking.network.BishopRequestPayload.TYPE)){
                    ClientSkillCombat.cancelPendingAttack(client);ClientPlayNetworking.send(new dev.killerranking.network.BishopRequestPayload(dev.killerranking.network.BishopRequestPayload.BARRAGE));
            }
            while(ROOK_MOUNT_KEY.consumeClick())if(usable&&ClientRook.choice())rookRequest(dev.killerranking.network.RookRequestPayload.MOUNT,-1);
            cWasDown=cDown;
            zWasDown=down;
            while (TRAIT_KEY.consumeClick()) {
                if (client.player != null && client.player.isAlive() && !client.player.isSpectator() && client.screen == null
                        && ClientPlayNetworking.canSend(dev.killerranking.network.TraitTogglePayload.TYPE))
                    ClientPlayNetworking.send(new dev.killerranking.network.TraitTogglePayload());
            }
            while (STATS_KEY.consumeClick()) {
                if (client.player != null && client.screen == null) client.setScreen(new StatsScreen());
                else if (client.screen instanceof StatsScreen) client.setScreen(null);
            }
            if (!choiceOffered && ClientDashboard.needsChoice() && client.player != null && client.player.isAlive() && !client.player.isSpectator() && client.screen == null) {
                choiceOffered = true; client.setScreen(new BranchChoiceScreen(null));
            }
			while (PARRY_KEY.consumeClick()) {
				if (client.player != null && client.player.isAlive() && !client.player.isSpectator() && client.screen == null
						&& !ClientAutoParry.active() && ClientPlayNetworking.canSend(ParryRequestPayload.TYPE)) ClientPlayNetworking.send(new ParryRequestPayload());
			}
		});
	}

	@SuppressWarnings("unchecked")
	private static void playParryAnimation(AbstractClientPlayer player, boolean reversed) {
		IAnimation animationLayer = PlayerAnimationAccess.getPlayerAssociatedData(player).get(PARRY_LAYER);
		if (!(animationLayer instanceof ModifierLayer<?> layer)) return;
		boolean neungsa = player.getMainHandItem().is(KillerRankingMod.NEUNGSA_KATANA);
		boolean katana = neungsa || player.getMainHandItem().is(KillerRankingMod.PAWN6_KATANA);
		String ordinary=player.getMainHandItem().is(KillerRankingMod.TTEOKBAL_FIST)?"parry_tteokbal"
            :player.getMainHandItem().isEmpty()||player.getMainHandItem().is(KillerRankingMod.ROOK_KNUCKLE_GLOVES)?"parry_hand":"parry";
		IPlayable animation = PlayerAnimationRegistry.getAnimation(neungsa ? (reversed?NEUNGSA_PARRY_REVERSE_ANIMATION:NEUNGSA_PARRY_ANIMATION) : katana
            ? (reversed ? KATANA_PARRY_REVERSE_ANIMATION : KATANA_PARRY_ANIMATION) : KillerRankingMod.id(ordinary+(reversed?"_reverse":"")));
		if (animation == null && katana) {
			animation = PlayerAnimationRegistry.getAnimation(PARRY_ANIMATION);
			katana = false;
		}
		if (animation == null) return;
		var speed=PlayerAnimationAccess.getPlayerAssociatedData(player).get(PARRY_SPEED);
		if(speed instanceof dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier)layer.removeModifier(0);
		if(!katana){
            var tempo=new dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier(3);
            layer.addModifierBefore(tempo);PlayerAnimationAccess.getPlayerAssociatedData(player).set(PARRY_SPEED,tempo);
        }else PlayerAnimationAccess.getPlayerAssociatedData(player).set(PARRY_SPEED,null);
		if (neungsa) NeungsaDrawController.parry(player);
		IAnimation playable = (IAnimation) animation.playAnimation();
		if (playable instanceof KeyframeAnimationPlayer keyframes) {
			keyframes.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
			// Canonical right-hand pose; LeftHandedHelperModifier mirrors it for left-handed players.
			keyframes.setFirstPersonConfiguration(new FirstPersonConfiguration(true, true, true, !katana));
		}
		((ModifierLayer<IAnimation>) layer).setAnimation(playable);
	}
    public static boolean rookChoiceKey(InputConstants.Key key) {
        var client=net.minecraft.client.Minecraft.getInstance();
        if(!ClientRook.choice()||client.player==null||!client.player.isAlive()
            ||client.player.isSpectator()||client.screen!=null||client.isPaused())return false;
        if(key.equals(KeyBindingHelper.getBoundKeyOf(ROOK_PUNCH_KEY))) {
            rookRequest(dev.killerranking.network.RookRequestPayload.PUNCH,-1);return true;
        }
        if(key.equals(KeyBindingHelper.getBoundKeyOf(ROOK_MOUNT_KEY))) {
            rookRequest(dev.killerranking.network.RookRequestPayload.MOUNT,-1);return true;
        }
        return false;
    }
	private static void knifeRequest(int operation,int target){
        if(ClientPlayNetworking.canSend(dev.killerranking.network.KnifePursuitRequestPayload.TYPE))ClientPlayNetworking.send(new dev.killerranking.network.KnifePursuitRequestPayload(operation,target));
    }
    private static void madnessRequest(int operation,int target){
        if(ClientPlayNetworking.canSend(dev.killerranking.network.MadnessLeapRequestPayload.TYPE))ClientPlayNetworking.send(new dev.killerranking.network.MadnessLeapRequestPayload(operation,target));
    }
    private static void cleaveRequest(int operation){
        if(ClientPlayNetworking.canSend(dev.killerranking.network.CleaveRequestPayload.TYPE))ClientPlayNetworking.send(new dev.killerranking.network.CleaveRequestPayload(operation));
    }
    private static void rookRequest(int operation,int target) {
        if(ClientPlayNetworking.canSend(dev.killerranking.network.RookRequestPayload.TYPE))ClientPlayNetworking.send(new dev.killerranking.network.RookRequestPayload(operation,target));
    }
    public static Component secondaryKeyLabel(){return ROOK_PUNCH_KEY.getTranslatedKeyMessage();}
    /** X/C also have vanilla toolbar bindings, so shared skill keys track physical press/release directly. */
    public static void sharedSkillKey(InputConstants.Key key,boolean down){
        if(KeyBindingHelper.getBoundKeyOf(ROOK_PUNCH_KEY).equals(key))ROOK_PUNCH_KEY.setDown(down);
        if(KeyBindingHelper.getBoundKeyOf(ROOK_MOUNT_KEY).equals(key))ROOK_MOUNT_KEY.setDown(down);
    }
    public static Component bishopBarrageKeyLabel(){return ROOK_MOUNT_KEY.getTranslatedKeyMessage();}
    public static Component rookPunchKeyLabel(){return ROOK_PUNCH_KEY.getTranslatedKeyMessage();}
    public static Component rookMountKeyLabel(){return ROOK_MOUNT_KEY.getTranslatedKeyMessage();}
    public static Component traitKeyLabel() { return TRAIT_KEY.getTranslatedKeyMessage(); }
    public static Component rushKeyLabel() { return RUSH_KEY.getTranslatedKeyMessage(); }
	public static Component parryKeyLabel() { return PARRY_KEY.getTranslatedKeyMessage(); }
}


