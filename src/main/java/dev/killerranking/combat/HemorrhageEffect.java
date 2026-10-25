package dev.killerranking.combat;
import dev.killerranking.KillerRankingMod;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/** Vanilla saves, expires and cures the debuff. Its pulses do not depend on an active cast. */
public final class HemorrhageEffect extends MobEffect {
    public HemorrhageEffect(){super(MobEffectCategory.HARMFUL,0xBA123C);}
    @Override public boolean shouldApplyEffectTickThisTick(int remaining,int amplifier){return WasabariRules.bleedPulse(remaining);}
    @Override public boolean applyEffectTick(LivingEntity victim,int amplifier){
        if(victim.level() instanceof ServerLevel level&&victim.isAlive()
            &&BleedingDamage.hit(KillerRankingMod.WASABARI.bleeder(victim),victim,WasabariRules.BLEED_DAMAGE))
            level.sendParticles(new DustParticleOptions(new Vector3f(.78f,.025f,.055f),.8f),victim.getX(),victim.getY()+.25,victim.getZ(),6,.16,.08,.16,.02);
        return true;
    }
}
