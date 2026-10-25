package dev.killerranking.combat;

import dev.killerranking.KillerRankingMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import java.util.Comparator;

/** The client preview and server confirmation use the same eye ray and range rules. */
public final class KnifePursuitTargeting {
    public static boolean enemy(Player p,LivingEntity e) {
        return e!=p&&e.isAlive()&&!e.isSpectator()&&e.isAttackable()&&!e.isPassenger()
            &&!p.isAlliedTo(e)&&p.canAttack(e)&&!(e instanceof Player other&&(other.isCreative()
            ||p instanceof ServerPlayer server&&!server.canHarmPlayer(other)))
            &&(!(p instanceof ServerPlayer)||!KillerRankingMod.ROOK.locked(e));
    }
    public static LivingEntity selected(Player p) {
        var eye=p.getEyePosition();var end=eye.add(p.getLookAngle().scale(KnifePursuitRules.REACH));
        return p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(KnifePursuitRules.REACH),
            e->enemy(p,e)&&KnifePursuitRules.inRange(p.distanceToSqr(e))&&p.hasLineOfSight(e)
                &&e.getBoundingBox().inflate(.15).clip(eye,end).isPresent())
            .stream().min(Comparator.comparingDouble(e->e.getBoundingBox().inflate(.15).clip(eye,end).orElse(end).distanceToSqr(eye))).orElse(null);
    }
    private KnifePursuitTargeting() { }
}
