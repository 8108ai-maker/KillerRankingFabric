package dev.killerranking.combat;

import net.minecraft.world.phys.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BishopDesignationTest {
    @Test void raySelectsFarTargetsWithoutUsingMeleeReach() {
        for (double distance : new double[]{2, 40, 128, 10000}) {
            var box = new AABB(-.3, 0, distance, .3, 2, distance + .6);
            assertEquals(distance, BishopDesignationTargeting.entry(new Vec3(0, 1, 0), new Vec3(0, 0, 1), box), 1e-5);
            assertEquals(Double.POSITIVE_INFINITY, BishopDesignationTargeting.entry(new Vec3(0, 1, 0), new Vec3(1, 0, 0), box));
        }
    }
    @Test void rayRejectsTargetsBehindAndInvalidDirections() {
        var box = new AABB(-1, -1, -10, 1, 1, -9);
        assertEquals(Double.POSITIVE_INFINITY, BishopDesignationTargeting.entry(Vec3.ZERO, new Vec3(0, 0, 1), box));
        assertEquals(Double.POSITIVE_INFINITY, BishopDesignationTargeting.entry(Vec3.ZERO, Vec3.ZERO, box));
        assertEquals(Double.POSITIVE_INFINITY, BishopDesignationTargeting.entry(Vec3.ZERO, new Vec3(Double.NaN, 0, 1), box));
    }
    @Test void switchingOneCastersTargetDoesNotOverwriteAnotherCastersOrder() {
        var marks = new BishopDesignations(); var a = UUID.randomUUID(); var b = UUID.randomUUID();
        var first = UUID.randomUUID(); var second = UUID.randomUUID();
        marks.set(a, first, "overworld"); marks.set(b, first, "overworld");
        var shared = marks.get(a); assertTrue(marks.used(shared));
        assertEquals(shared, marks.set(a, second, "overworld"));
        assertEquals(first, marks.get(b).target()); assertEquals(second, marks.get(a).target());
        assertTrue(marks.used(shared), "Removing the old effect would cancel another caster's mark");
        marks.remove(b); assertFalse(marks.used(shared)); assertEquals(second, marks.get(a).target());
        assertFalse(marks.used(new BishopDesignations.Mark(second, "nether")));
    }
}
