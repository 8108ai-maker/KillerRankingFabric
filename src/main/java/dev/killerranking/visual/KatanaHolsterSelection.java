package dev.killerranking.visual;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/** One waist slot and one back slot; inventory duplicates never overlap. */
public final class KatanaHolsterSelection {
    public record Slots<T>(T waist, T back) { }
    public static <T> Slots<T> select(List<T> inventory, Predicate<T> katana, Function<T,String> kind) {
        List<T> swords = inventory.stream().filter(katana).toList();
        T waist = swords.stream().filter(s -> kind.apply(s).equals("killerranking:neungsa_katana"))
            .findFirst().orElse(swords.isEmpty() ? null : swords.getFirst());
        if (waist == null) return new Slots<>(null,null);
        String first = kind.apply(waist);
        T back = swords.stream().filter(s -> !kind.apply(s).equals(first))
            .filter(s -> kind.apply(s).equals("killerranking:pawn6_katana"))
            .findFirst().orElseGet(() -> swords.stream().filter(s -> !kind.apply(s).equals(first)).findFirst().orElse(null));
        return new Slots<>(waist,back);
    }
    private KatanaHolsterSelection() { }
}
