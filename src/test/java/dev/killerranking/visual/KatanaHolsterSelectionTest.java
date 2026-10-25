package dev.killerranking.visual;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KatanaHolsterSelectionTest {
    private KatanaHolsterSelection.Slots<String> choose(String... items) {
        return KatanaHolsterSelection.select(List.of(items),s->s.contains("katana"),s->s);
    }
    @Test void emptyInventoryHasNoHolsters() {
        var s=choose("minecraft:stick");assertNull(s.waist());assertNull(s.back());
    }
    @Test void neungsaAlwaysTakesWaistAndPawnTakesBackRegardlessOfInventoryOrder() {
        var s=choose("killerranking:pawn6_katana","minecraft:stick","killerranking:neungsa_katana");
        assertEquals("killerranking:neungsa_katana",s.waist());assertEquals("killerranking:pawn6_katana",s.back());
    }
    @Test void duplicateTypesDoNotCoverTheSameBodySlot() {
        var s=choose("killerranking:neungsa_katana","killerranking:neungsa_katana");
        assertEquals("killerranking:neungsa_katana",s.waist());assertNull(s.back());
    }
    @Test void anotherKatanaCanUseTheBackSlot() {
        var s=choose("other:iron_katana","killerranking:neungsa_katana");
        assertEquals("killerranking:neungsa_katana",s.waist());assertEquals("other:iron_katana",s.back());
    }
    @Test void aSingleOtherKatanaUsesWaistAndRemovingItClearsBothSlots() {
        assertEquals("killerranking:pawn6_katana",choose("killerranking:pawn6_katana").waist());
        assertNull(choose().waist());assertNull(choose().back());
    }
}
