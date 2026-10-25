package dev.killerranking.combat;
import dev.killerranking.entity.BishopFollower;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;

public final class BishopTargets {
    public static boolean allowed(ServerPlayer owner,LivingEntity target){
        if(owner==null||target==null||target==owner||!target.isAlive()||target.isRemoved()||target.isSpectator()||!target.isAttackable()
            ||owner.level()!=target.level()||owner.isAlliedTo(target)||!owner.canAttack(target))return false;
        if(target instanceof ServerPlayer p&&(!owner.server.isPvpAllowed()||p.isCreative()||!owner.canHarmPlayer(p)))return false;
        if(target instanceof TamableAnimal pet&&pet.isOwnedBy(owner))return false;
        if(target instanceof BishopFollower follower){
            var other=follower.owner();
            if(owner.getUUID().equals(follower.ownerId())||other==null||!allowed(owner,other))return false;
        }
        return true;
    }
    private BishopTargets() { }
}
