package dev.killerranking.combat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zoglin;

/** Upright victims lie on their back; creatures with horizontal bodies flip belly-up. */
public final class RookMountPose {
    public static boolean inverted(LivingEntity victim) {
        return victim instanceof Animal && !(victim instanceof Chicken)
            && !(victim instanceof Parrot) && !(victim instanceof Bee)
            || victim instanceof Ravager || victim instanceof Zoglin
            || victim instanceof Creeper || victim instanceof Spider;
    }
    public static double seatHeight(LivingEntity victim) {
        return inverted(victim)?Math.max(.12,victim.getBbHeight()*.55):.12;
    }
    public static double impactHeight(LivingEntity victim) {
        return inverted(victim)?victim.getBbHeight()*.55:.28;
    }
    private RookMountPose() { }
}
