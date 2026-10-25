package dev.killerranking;

import dev.killerranking.combat.ParryService;
import dev.killerranking.combat.MadnessSurvivalService;
import dev.killerranking.data.MadnessSurvivalState;
import dev.killerranking.combat.SpiritBranch;
import dev.killerranking.data.SpiritState;
import dev.killerranking.network.BranchRequestPayload;
import dev.killerranking.network.DashboardPayload;
import dev.killerranking.network.DashboardService;
import dev.killerranking.mission.RotatingMissions;
import dev.killerranking.combat.CombatRules;
import dev.killerranking.combat.TraitLevels;
import dev.killerranking.command.TraitLevelCommands;
import dev.killerranking.combat.CombatStatusService;
import dev.killerranking.network.CombatStatusPayload;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.tags.DamageTypeTags;
import dev.killerranking.config.KillerRankingConfig;
import dev.killerranking.data.PlayerProfile;
import dev.killerranking.data.PlayerProfiles;
import dev.killerranking.item.KillerPhoneItem;
import dev.killerranking.item.StarterPhoneService;
import dev.killerranking.network.OpenPhonePayload;
import dev.killerranking.network.ParryEffectPayload;
import dev.killerranking.network.ParryRequestPayload;
import dev.killerranking.network.ProfilePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KillerRankingMod implements ModInitializer {
	public static final String MOD_ID = "killerranking";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    /** Kept for older call sites; the catalog lives in {@link KillerRankingSounds}. */
    public static final net.minecraft.sounds.SoundEvent KNIFE_PURSUIT_BLINK=KillerRankingSounds.KNIFE_PURSUIT_BLINK;
	public static final Item PHONE = new KillerPhoneItem(new Item.Properties());
    public static final Item NIGHT_BARBED_BAT = new net.minecraft.world.item.SwordItem(
        net.minecraft.world.item.Tiers.IRON, new Item.Properties()
            .attributes(net.minecraft.world.item.SwordItem.createAttributes(net.minecraft.world.item.Tiers.IRON, 5, -2.8F))
            .rarity(net.minecraft.world.item.Rarity.UNCOMMON));
    public static final Item GOSSI_INHERITED_KNIFE = new net.minecraft.world.item.SwordItem(
        net.minecraft.world.item.Tiers.IRON, new Item.Properties()
            .attributes(net.minecraft.world.item.SwordItem.createAttributes(net.minecraft.world.item.Tiers.IRON, 3, -1.6F))
            .rarity(net.minecraft.world.item.Rarity.UNCOMMON));
    public static final Item EUNBAL_HATCHET = new net.minecraft.world.item.AxeItem(
        net.minecraft.world.item.Tiers.IRON, new Item.Properties()
            .attributes(net.minecraft.world.item.AxeItem.createAttributes(net.minecraft.world.item.Tiers.IRON, 6, -2.8F))
            .rarity(net.minecraft.world.item.Rarity.UNCOMMON));
    public static final Item ROOK_KNUCKLE_GLOVES = new net.minecraft.world.item.SwordItem(
        net.minecraft.world.item.Tiers.IRON, new Item.Properties()
            .attributes(net.minecraft.world.item.SwordItem.createAttributes(net.minecraft.world.item.Tiers.IRON, 4, -3.0F))
            .rarity(net.minecraft.world.item.Rarity.UNCOMMON));
    public static final Item TTEOKBAL_FIST = new dev.killerranking.item.BandagedFistItem();
    public static final Item NEUNGSA_KATANA = new net.minecraft.world.item.SwordItem(
        net.minecraft.world.item.Tiers.DIAMOND, new Item.Properties()
            .attributes(net.minecraft.world.item.SwordItem.createAttributes(net.minecraft.world.item.Tiers.DIAMOND, 4, -2.2F))
            .rarity(net.minecraft.world.item.Rarity.UNCOMMON));
    public static final Item PAWN6_KATANA = new net.minecraft.world.item.SwordItem(
        net.minecraft.world.item.Tiers.DIAMOND, new Item.Properties()
            .attributes(net.minecraft.world.item.SwordItem.createAttributes(net.minecraft.world.item.Tiers.DIAMOND, 4, -2.2F))
            .rarity(net.minecraft.world.item.Rarity.UNCOMMON));
    public static final Item YUHYEOLMOKI_SAW_KNIFE = new net.minecraft.world.item.SwordItem(
        net.minecraft.world.item.Tiers.IRON, new Item.Properties()
            .attributes(net.minecraft.world.item.SwordItem.createAttributes(net.minecraft.world.item.Tiers.IRON, 6, -2.4F))
            .rarity(net.minecraft.world.item.Rarity.UNCOMMON));
    public static final Item SALMOSA_MID_DAGGER = new net.minecraft.world.item.SwordItem(
        net.minecraft.world.item.Tiers.IRON, new Item.Properties()
            .attributes(net.minecraft.world.item.SwordItem.createAttributes(net.minecraft.world.item.Tiers.IRON, 4, -1.8F))
            .rarity(net.minecraft.world.item.Rarity.UNCOMMON));
    public static final net.minecraft.core.particles.SimpleParticleType KILLING_AURA = net.fabricmc.fabric.api.particle.v1.FabricParticleTypes.simple();
    public static final net.minecraft.core.particles.SimpleParticleType POISON_AURA = net.fabricmc.fabric.api.particle.v1.FabricParticleTypes.simple();
    public static final net.minecraft.core.particles.SimpleParticleType POISON_FLAME = net.fabricmc.fabric.api.particle.v1.FabricParticleTypes.simple();
    public static final net.minecraft.core.particles.SimpleParticleType SPIRIT_AURA = net.fabricmc.fabric.api.particle.v1.FabricParticleTypes.simple();
    public static final net.minecraft.core.particles.SimpleParticleType NEUNGSA_CUT = net.fabricmc.fabric.api.particle.v1.FabricParticleTypes.simple();
    public static final net.minecraft.core.particles.SimpleParticleType BISHOP_SLASH = net.fabricmc.fabric.api.particle.v1.FabricParticleTypes.simple();
    public static final net.minecraft.core.particles.SimpleParticleType SLASH_SHARD = net.fabricmc.fabric.api.particle.v1.FabricParticleTypes.simple(true);
    public static final dev.killerranking.combat.TraitVisualService TRAIT_VISUALS = new dev.killerranking.combat.TraitVisualService();
	public static final dev.killerranking.combat.HuntingMarkService HUNTING_MARKS = new dev.killerranking.combat.HuntingMarkService();
	public static final ParryService PARRY = new ParryService();
	public static final CombatStatusService COMBAT_STATUS = new CombatStatusService();
    public static final dev.killerranking.combat.ShoulderStabService SHOULDER_STAB=new dev.killerranking.combat.ShoulderStabService();
    public static final dev.killerranking.combat.KnifePursuitService KNIFE_PURSUIT=new dev.killerranking.combat.KnifePursuitService();
    public static final dev.killerranking.combat.NeungsaSkillService NEUNGSA_SKILL=new dev.killerranking.combat.NeungsaSkillService();
    public static final dev.killerranking.combat.WasabariService WASABARI=new dev.killerranking.combat.WasabariService();
    public static net.minecraft.core.Holder.Reference<net.minecraft.world.effect.MobEffect> HEMORRHAGE;
    public static final dev.killerranking.combat.HeavyPunchService HEAVY_PUNCH=new dev.killerranking.combat.HeavyPunchService();
    public static final dev.killerranking.combat.RushService RUSH = new dev.killerranking.combat.RushService();
    public static final dev.killerranking.combat.CheckmateService CHECKMATE = new dev.killerranking.combat.CheckmateService();
	
	public static final dev.killerranking.combat.RookService ROOK = new dev.killerranking.combat.RookService();
	public static final dev.killerranking.combat.CleaveService CLEAVE = new dev.killerranking.combat.CleaveService();
	public static final dev.killerranking.combat.MadnessLeapService MADNESS_LEAP = new dev.killerranking.combat.MadnessLeapService();
	public static final dev.killerranking.combat.BishopService BISHOP=new dev.killerranking.combat.BishopService();
    public static final dev.killerranking.combat.BishopBarrageService BISHOP_BARRAGE=new dev.killerranking.combat.BishopBarrageService();
    public static net.minecraft.core.Holder.Reference<net.minecraft.world.effect.MobEffect> BISHOP_DESIGNATION;
    public static final Item BISHOP_DAGGER=new net.minecraft.world.item.SwordItem(net.minecraft.world.item.Tiers.IRON,
        new Item.Properties().attributes(net.minecraft.world.item.SwordItem.createAttributes(net.minecraft.world.item.Tiers.IRON,2,-2.0f)).rarity(net.minecraft.world.item.Rarity.UNCOMMON));
    public static final net.minecraft.world.entity.EntityType<dev.killerranking.entity.BishopFollower> BISHOP_FOLLOWER=net.minecraft.world.entity.EntityType.Builder
        .<dev.killerranking.entity.BishopFollower>of(dev.killerranking.entity.BishopFollower::new,net.minecraft.world.entity.MobCategory.CREATURE).sized(.6f,1.85f).clientTrackingRange(10).updateInterval(2).build("killerranking:bishop_follower");
    public static KillerRankingConfig CONFIG;

	@Override
	public void onInitialize() {
        KillerRankingSounds.register();
		CONFIG = KillerRankingConfig.load();
        SpiritState.initialize();
        dev.killerranking.data.BishopState.initialize();
        dev.killerranking.data.TraitActivation.initialize();
        MadnessSurvivalState.initialize();
        dev.killerranking.data.MadnessLeapState.initialize();
        dev.killerranking.data.RushState.initialize();
        dev.killerranking.data.HeavyPunchState.initialize();
        dev.killerranking.data.WasabariState.initialize();
        dev.killerranking.data.KnifePursuitState.initialize();
        dev.killerranking.data.SalmosaState.initialize();
        dev.killerranking.data.NeungsaSkillState.initialize();
        dev.killerranking.data.ShoulderStabState.initialize();
        dev.killerranking.data.CheckmateState.initialize();
        dev.killerranking.data.RookState.initialize();
        dev.killerranking.data.CleaveState.initialize();
        dev.killerranking.data.MoraleState.initialize();
        StarterPhoneService.initialize();
        RotatingMissions.initialize();
        // Register persistent attachments before any player data is deserialized.
        PlayerProfiles.PROFILE.identifier();
		Registry.register(BuiltInRegistries.ITEM, id("phone"), PHONE);
        Registry.register(BuiltInRegistries.ITEM, id("night_barbed_bat"), NIGHT_BARBED_BAT);
        Registry.register(BuiltInRegistries.ITEM, id("gossi_inherited_knife"), GOSSI_INHERITED_KNIFE);
        Registry.register(BuiltInRegistries.ITEM, id("eunbal_hatchet"), EUNBAL_HATCHET);
        Registry.register(BuiltInRegistries.ITEM, id("rook_knuckle_gloves"), ROOK_KNUCKLE_GLOVES);
        Registry.register(BuiltInRegistries.ITEM, id("tteokbal_fist"), TTEOKBAL_FIST);
        Registry.register(BuiltInRegistries.ITEM, id("pawn6_katana"), PAWN6_KATANA);
        Registry.register(BuiltInRegistries.ITEM, id("neungsa_katana"), NEUNGSA_KATANA);
        Registry.register(BuiltInRegistries.ITEM, id("yuhyeolmoki_saw_knife"), YUHYEOLMOKI_SAW_KNIFE);
        Registry.register(BuiltInRegistries.ITEM, id("salmosa_mid_dagger"), SALMOSA_MID_DAGGER);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, id("killing_aura"), KILLING_AURA);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, id("poison_aura"), POISON_AURA);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, id("poison_flame"), POISON_FLAME);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, id("tteokbal_aura"), SPIRIT_AURA);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, id("bishop_slash"), BISHOP_SLASH);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE,id("neungsa_cut"),NEUNGSA_CUT);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE,id("slash_shard"),SLASH_SHARD);
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> entries.accept(PHONE));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.accept(NIGHT_BARBED_BAT);
            entries.accept(GOSSI_INHERITED_KNIFE);
            entries.accept(EUNBAL_HATCHET);
            entries.accept(ROOK_KNUCKLE_GLOVES);
            entries.accept(TTEOKBAL_FIST);
            entries.accept(PAWN6_KATANA);
            entries.accept(NEUNGSA_KATANA);
            entries.accept(YUHYEOLMOKI_SAW_KNIFE);
            entries.accept(SALMOSA_MID_DAGGER);
        });
		Registry.register(BuiltInRegistries.ENTITY_TYPE,id("bishop_follower"),BISHOP_FOLLOWER);
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(BISHOP_FOLLOWER,dev.killerranking.entity.BishopFollower.attributes());
        Registry.register(BuiltInRegistries.ITEM,id("bishop_dagger"),BISHOP_DAGGER);
        BISHOP_DESIGNATION=Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT,id("bishop_designation"),new dev.killerranking.combat.BishopDesignationEffect());
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries->entries.accept(BISHOP_DAGGER));
        HEMORRHAGE=Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT,id("hemorrhage"),new dev.killerranking.combat.HemorrhageEffect());
        registerNetworking();
		registerGameplay();
		TraitLevelCommands.register();
		LOGGER.info("Killer Ranking initialized. Mixed Madness weight: {}", CONFIG.mixedMadnessWeight);
	}

	private static void registerNetworking() {
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.SalmosaUnlockPayload.TYPE,dev.killerranking.network.SalmosaUnlockPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.SalmosaStatusPayload.TYPE,dev.killerranking.network.SalmosaStatusPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.SalmosaUnlockPayload.TYPE,(payload,context)->dev.killerranking.combat.SalmosaSkillService.unlock(context.player()));
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.NeungsaRequestPayload.TYPE,dev.killerranking.network.NeungsaRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.NeungsaStatusPayload.TYPE,dev.killerranking.network.NeungsaStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.NeungsaSlashVisualPayload.TYPE,dev.killerranking.network.NeungsaSlashVisualPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.NeungsaEchoVisualPayload.TYPE,dev.killerranking.network.NeungsaEchoVisualPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.NeungsaIaidoVisualPayload.TYPE,dev.killerranking.network.NeungsaIaidoVisualPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.NeungsaCutEffectPayload.TYPE,dev.killerranking.network.NeungsaCutEffectPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.NeungsaRequestPayload.TYPE,(payload,context)->NEUNGSA_SKILL.request(context.player(),payload));
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.WasabariRequestPayload.TYPE,dev.killerranking.network.WasabariRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.WasabariStatusPayload.TYPE,dev.killerranking.network.WasabariStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.WasabariVisualPayload.TYPE,dev.killerranking.network.WasabariVisualPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.WasabariRequestPayload.TYPE,(payload,context)->WASABARI.start(context.player()));
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.ShoulderStabRequestPayload.TYPE,dev.killerranking.network.ShoulderStabRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.ShoulderStabStatusPayload.TYPE,dev.killerranking.network.ShoulderStabStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.ShoulderStabVisualPayload.TYPE,dev.killerranking.network.ShoulderStabVisualPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.ShoulderStabRequestPayload.TYPE,(payload,context)->SHOULDER_STAB.request(context.player(),payload));
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.KnifePursuitRequestPayload.TYPE,dev.killerranking.network.KnifePursuitRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.KnifePursuitStatusPayload.TYPE,dev.killerranking.network.KnifePursuitStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.KnifePursuitVisualPayload.TYPE,dev.killerranking.network.KnifePursuitVisualPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.KnifePursuitRequestPayload.TYPE,(payload,context)->KNIFE_PURSUIT.request(context.player(),payload));
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.HeavyPunchRequestPayload.TYPE,dev.killerranking.network.HeavyPunchRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.HeavyPunchStatusPayload.TYPE,dev.killerranking.network.HeavyPunchStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.HeavyPunchVisualPayload.TYPE,dev.killerranking.network.HeavyPunchVisualPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.HeavyPunchRequestPayload.TYPE,(payload,context)->HEAVY_PUNCH.start(context.player()));
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.BishopRequestPayload.TYPE,dev.killerranking.network.BishopRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.BishopStatusPayload.TYPE,dev.killerranking.network.BishopStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.BishopBarrageVisualPayload.TYPE,dev.killerranking.network.BishopBarrageVisualPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.BishopRequestPayload.TYPE,(payload,context)->BISHOP.request(context.player(),payload));
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.MadnessLeapRequestPayload.TYPE, dev.killerranking.network.MadnessLeapRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.MadnessLeapStatusPayload.TYPE, dev.killerranking.network.MadnessLeapStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.MadnessLeapVisualPayload.TYPE, dev.killerranking.network.MadnessLeapVisualPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.MadnessLeapRequestPayload.TYPE, (payload, context) -> MADNESS_LEAP.request(context.player(), payload));
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.CleaveRequestPayload.TYPE, dev.killerranking.network.CleaveRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.AutoParryRequestPayload.TYPE, dev.killerranking.network.AutoParryRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.AutoParryStatusPayload.TYPE, dev.killerranking.network.AutoParryStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.AutoParryEffectPayload.TYPE, dev.killerranking.network.AutoParryEffectPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.AutoParryRequestPayload.TYPE, (payload, context) -> PARRY.holdAutomatic(context.player(), payload.held()));
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.CleaveStatusPayload.TYPE, dev.killerranking.network.CleaveStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.CleaveVisualPayload.TYPE, dev.killerranking.network.CleaveVisualPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.CleaveRequestPayload.TYPE, (payload, context) -> CLEAVE.request(context.player(), payload));
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.RookRequestPayload.TYPE, dev.killerranking.network.RookRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.RookStatusPayload.TYPE, dev.killerranking.network.RookStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.RookVisualPayload.TYPE, dev.killerranking.network.RookVisualPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.RookRequestPayload.TYPE, (payload, context) -> ROOK.request(context.player(), payload));
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.CheckmateRequestPayload.TYPE, dev.killerranking.network.CheckmateRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.CheckmateConfirmPayload.TYPE, dev.killerranking.network.CheckmateConfirmPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.CheckmateUnlockPayload.TYPE, dev.killerranking.network.CheckmateUnlockPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.CheckmateStatusPayload.TYPE, dev.killerranking.network.CheckmateStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.CheckmateVisualPayload.TYPE, dev.killerranking.network.CheckmateVisualPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.CheckmateRequestPayload.TYPE, (payload, context) -> CHECKMATE.start(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.CheckmateConfirmPayload.TYPE, (payload, context) -> CHECKMATE.confirm(context.player(), payload.selected()));
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.CheckmateUnlockPayload.TYPE, (payload, context) -> CHECKMATE.unlock(context.player()));
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.RushRequestPayload.TYPE, dev.killerranking.network.RushRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.RushUnlockPayload.TYPE, dev.killerranking.network.RushUnlockPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.RushStatusPayload.TYPE, dev.killerranking.network.RushStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.RushVisualPayload.TYPE, dev.killerranking.network.RushVisualPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.RushRequestPayload.TYPE, (payload, context) -> RUSH.start(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.RushUnlockPayload.TYPE, (payload, context) -> RUSH.unlock(context.player()));
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.TraitVisualPayload.TYPE, dev.killerranking.network.TraitVisualPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(dev.killerranking.network.TraitTogglePayload.TYPE, dev.killerranking.network.TraitTogglePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(dev.killerranking.network.TraitTogglePayload.TYPE, (payload, context) -> TRAIT_VISUALS.toggle(context.player()));
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.HuntingMarkPayload.TYPE, dev.killerranking.network.HuntingMarkPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DashboardPayload.TYPE, DashboardPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BranchRequestPayload.TYPE, BranchRequestPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BranchRequestPayload.TYPE, (payload, context) -> {
            var player = context.player();
            var branch = SpiritBranch.fromId(payload.branch());
            if (branch == SpiritBranch.NONE || !player.isAlive() || player.isSpectator()
                || PlayerProfiles.get(player).trait() != Trait.FIGHTING_SPIRIT || SpiritState.get(player).branch() != SpiritBranch.NONE) return;
            SpiritState.set(player, new SpiritState(branch, false));
            player.sendSystemMessage(Component.literal("§b" + branch.label() + " 선택 완료!"));
            assignAndSync(player);
        });
		PayloadTypeRegistry.playC2S().register(ParryRequestPayload.TYPE, ParryRequestPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(OpenPhonePayload.TYPE, OpenPhonePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ParryEffectPayload.TYPE, ParryEffectPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.ParryImpactPayload.TYPE, dev.killerranking.network.ParryImpactPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.WeaponHitPayload.TYPE, dev.killerranking.network.WeaponHitPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ProfilePayload.TYPE, ProfilePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(CombatStatusPayload.TYPE, CombatStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.killerranking.network.KatanaHolstersPayload.TYPE, dev.killerranking.network.KatanaHolstersPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ParryRequestPayload.TYPE, (payload, context) -> PARRY.tryStart(context.player()));
	}

	private static void registerGameplay() {
        dev.killerranking.visual.KatanaHolsters.register();
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity,level)->{
            if(entity instanceof LivingEntity living)BISHOP.cleanOrphanEffect(living);
        });
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_UNLOAD.register((entity,level)->{
            if(entity instanceof LivingEntity living){NEUNGSA_SKILL.forget(living);SHOULDER_STAB.forget(living);KNIFE_PURSUIT.forget(living);WASABARI.forget(living);BISHOP.targetRemoved(living);HEAVY_PUNCH.forget(living);}
        });
        net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents.START_TRACKING.register((entity, viewer) -> {
            if (entity instanceof ServerPlayer player) { NEUNGSA_SKILL.send(player,viewer);SHOULDER_STAB.send(player,viewer);KNIFE_PURSUIT.send(player,viewer);WASABARI.send(player,viewer);HEAVY_PUNCH.send(player,viewer); TRAIT_VISUALS.send(player, viewer); BISHOP.send(player,viewer); RUSH.send(player, viewer); CHECKMATE.send(player, viewer); ROOK.send(player, viewer); CLEAVE.send(player, viewer); MADNESS_LEAP.send(player, viewer); PARRY.sendAutomatic(player, viewer); }
        });
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            MadnessSurvivalService.cancel(handler.player);
            assignAndSync(handler.player);
            StarterPhoneService.onJoin(handler.player);
        });
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            NEUNGSA_SKILL.forget(handler.player);SHOULDER_STAB.forget(handler.player);KNIFE_PURSUIT.forget(handler.player);WASABARI.forget(handler.player);HEAVY_PUNCH.forget(handler.player); BISHOP.forget(handler.player); RUSH.forget(handler.player); CHECKMATE.forget(handler.player); ROOK.forget(handler.player); CLEAVE.forget(handler.player); MADNESS_LEAP.forget(handler.player);
            MadnessSurvivalService.cancel(handler.player);
			TRAIT_VISUALS.forget(handler.player.getUUID());
			HUNTING_MARKS.clear(handler.player); PARRY.forget(handler.player.getUUID());
			COMBAT_STATUS.forget(handler.player.getUUID());
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) dev.killerranking.data.MoraleState.set(newPlayer, 1);
            NEUNGSA_SKILL.forget(oldPlayer);NEUNGSA_SKILL.forget(newPlayer);SHOULDER_STAB.forget(oldPlayer);KNIFE_PURSUIT.forget(oldPlayer);SHOULDER_STAB.forget(newPlayer);KNIFE_PURSUIT.forget(newPlayer);WASABARI.forget(oldPlayer);WASABARI.forget(newPlayer);HEAVY_PUNCH.forget(oldPlayer); HEAVY_PUNCH.forget(newPlayer); BISHOP.forget(oldPlayer); BISHOP.forget(newPlayer); RUSH.forget(newPlayer); CHECKMATE.forget(newPlayer); ROOK.forget(oldPlayer); CLEAVE.forget(newPlayer); MADNESS_LEAP.forget(oldPlayer);
            MadnessSurvivalService.cancel(newPlayer);
			TRAIT_VISUALS.forget(newPlayer.getUUID());
			HUNTING_MARKS.clear(newPlayer); PARRY.forget(newPlayer.getUUID());
			COMBAT_STATUS.forget(newPlayer.getUUID());
			assignAndSync(newPlayer);
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> { NEUNGSA_SKILL.clear();SHOULDER_STAB.clear();KNIFE_PURSUIT.clear();WASABARI.clear();HEAVY_PUNCH.clear(); BISHOP.clear(server); PARRY.clear(); COMBAT_STATUS.clear(); HUNTING_MARKS.clear(); TRAIT_VISUALS.clear(); RUSH.clear(); CHECKMATE.clear(); ROOK.clear(); CLEAVE.clear(); MADNESS_LEAP.clear(); });
		ServerTickEvents.END_SERVER_TICK.register(server -> { NEUNGSA_SKILL.tick(server);SHOULDER_STAB.tick(server);KNIFE_PURSUIT.tick(server);WASABARI.tick(server);HEAVY_PUNCH.tick(server); BISHOP_BARRAGE.tick(server); BISHOP.tick(server); PARRY.tick(server); RUSH.tick(server); CHECKMATE.tick(server); ROOK.tick(server); CLEAVE.tick(server); MADNESS_LEAP.tick(server); MadnessSurvivalService.tick(server); dev.killerranking.combat.TraitExperienceService.tick(server); COMBAT_STATUS.tick(server); HUNTING_MARKS.tick(server); TRAIT_VISUALS.tick(server); RotatingMissions.tick(server); DashboardService.tick(server); StarterPhoneService.tick(server); });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!dev.killerranking.combat.SkillCombatLock.allowsDamage(entity, source)) return false;
            // Every eligible melee hit reaches automatic parry, including hits inside its success protection.
            if (!dev.killerranking.combat.BleedingDamage.current(entity) && !source.is(DamageTypeTags.IS_PROJECTILE) && source.getDirectEntity() == source.getEntity()
                && entity instanceof ServerPlayer defender && source.getEntity() instanceof LivingEntity attacker && attacker != defender
                && (PARRY.automatic(defender)||NEUNGSA_SKILL.iaidoActive(defender)) && PARRY.tryParry(defender, attacker)) return false;
            if (entity instanceof ServerPlayer player && PARRY.isProtected(player)) return false;
			if (!dev.killerranking.combat.BleedingDamage.current(entity) && !source.is(DamageTypeTags.IS_PROJECTILE) && source.getDirectEntity() == source.getEntity()
					&& entity instanceof ServerPlayer defender && source.getEntity() instanceof LivingEntity attacker && attacker != defender) {
				return !PARRY.tryParry(defender, attacker);
			}
			return true;
		});
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            NEUNGSA_SKILL.forget(victim);SHOULDER_STAB.forget(victim);KNIFE_PURSUIT.forget(victim);WASABARI.forget(victim);HEAVY_PUNCH.forget(victim); BISHOP.targetRemoved(victim);
            ROOK.forget(victim);
            if (!dev.killerranking.combat.BleedingDamage.current(victim)) dev.killerranking.combat.TraitExperienceService.hit(victim, source);
            if (victim instanceof ServerPlayer dead) { BISHOP.forget(dead); PARRY.stopAutomatic(dead); HUNTING_MARKS.clear(dead); MadnessSurvivalService.cancel(dead); RUSH.forget(dead); CHECKMATE.forget(dead); CLEAVE.forget(dead); MADNESS_LEAP.forget(dead); }
        });
		ServerLivingEntityEvents.AFTER_DAMAGE.register((victim, source, baseDamage, damageTaken, blocked) -> {
            dev.killerranking.combat.WeaponHitService.afterDamage(victim, source, damageTaken, blocked);
			if (blocked || damageTaken <= 0 || dev.killerranking.combat.BleedingDamage.current(victim)) return;
            dev.killerranking.combat.TraitExperienceService.hit(victim, source);
            if (!(source.getEntity() instanceof ServerPlayer attacker) || !dev.killerranking.data.TraitActivation.enabled(attacker)) return;
			var profile = PlayerProfiles.get(attacker);
			Trait trait = profile.trait();
			if (trait == Trait.KILLING_INTENT) {
				COMBAT_STATUS.onKillingIntentHit(attacker);
			}
			if (trait == Trait.POISON_AURA && attacker != victim) {
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, TraitLevels.poisonTicks(profile.traitLevel()), 0, true, true));
                HUNTING_MARKS.onHit(attacker, victim);
            }
		});

	}

	public static void assignAndSync(ServerPlayer player) {
		PlayerProfile before = PlayerProfiles.get(player);
		PlayerProfile profile = PlayerProfiles.ensureTrait(player, CONFIG);
        BISHOP.refresh(player); BISHOP.sync(player); MadnessSurvivalService.refresh(player);
        NEUNGSA_SKILL.refresh(player);NEUNGSA_SKILL.sync(player);dev.killerranking.combat.SalmosaSkillService.sync(player);
        SHOULDER_STAB.refresh(player);SHOULDER_STAB.sync(player);KNIFE_PURSUIT.refresh(player);KNIFE_PURSUIT.sync(player);WASABARI.refresh(player);WASABARI.sync(player);HEAVY_PUNCH.refresh(player); HEAVY_PUNCH.sync(player); RUSH.refresh(player);
        CHECKMATE.refresh(player); CHECKMATE.sync(player);
        ROOK.refresh(player); ROOK.sync(player); CLEAVE.refresh(player); CLEAVE.sync(player); MADNESS_LEAP.refresh(player); MADNESS_LEAP.sync(player);
        PARRY.refreshAutomatic(player); PARRY.sendAutomatic(player, player);
        RUSH.sync(player);
        if (profile.trait() != Trait.POISON_AURA || !dev.killerranking.data.TraitActivation.enabled(player)) HUNTING_MARKS.clear(player);
		if (before.trait() == Trait.UNASSIGNED) player.sendSystemMessage(Component.literal("§a고유 전투 특성 배정: " + profile.trait().koreanName()));
		ServerPlayNetworking.send(player, ProfilePayload.from(profile));
		COMBAT_STATUS.sync(player);
        TRAIT_VISUALS.sync(player);
        RotatingMissions.update(player);
        DashboardService.sync(player);
	}

	public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
}
