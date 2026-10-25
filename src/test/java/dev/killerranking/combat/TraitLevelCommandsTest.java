package dev.killerranking.combat;

import com.mojang.brigadier.CommandDispatcher;
import dev.killerranking.command.TraitLevelCommands;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TraitLevelCommandsTest {
	@BeforeAll static void bootstrap() {
		net.minecraft.SharedConstants.tryDetectVersion();
		net.minecraft.server.Bootstrap.bootStrap();
	}
	private boolean accepts(String input, int permission) {
		var dispatcher = new CommandDispatcher<CommandSourceStack>();
		TraitLevelCommands.register(dispatcher);
		var source = new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, permission, "test", Component.literal("test"), null, null);
		var result = dispatcher.parse(input, source);
		return !result.getReader().canRead() && result.getContext().getLastChild().getCommand() != null;
	}
	@Test void operatorsCanSetOnlyLevelsOneThroughThree() {
		for (int level = 1; level <= 3; level++) assertTrue(accepts("killerranking level set @a " + level, 2));
		assertFalse(accepts("killerranking level set @a 0", 2));
		assertFalse(accepts("killerranking level set @a 4", 2));
	}
	@Test void ordinaryPlayersCannotChangeLevels() {
		assertFalse(accepts("killerranking level set @s 3", 0));
		assertFalse(accepts("killerranking level set @a 3", 1));
	}
	@Test void getLivesUnderLevelAndSupportsSelfOrTargets() {
		assertTrue(accepts("killerranking level get", 2));
		assertTrue(accepts("killerranking level get @a", 2));
		assertFalse(accepts("killerranking get @a", 2));
	}
    @Test void newAdminCommandsRequireOperatorAndAcceptValidChoices() {
        for (String command : new String[] {
            "trait set @a fighting_spirit", "trait set @s killing_intent", "trait set @s mixed_madness", "trait set @a poison_aura",
            "branch set @s tteokbal", "branch set @a king_and_kill", "branch set @s none", "checkmate set @s true", "points add @a 100", "info @a"
        }) {
            assertTrue(accepts("killerranking " + command, 2), command);
            assertFalse(accepts("killerranking " + command, 0), command);
        }
        assertFalse(accepts("killerranking branch set @s unknown", 2));
        assertFalse(accepts("killerranking trait set @s unassigned", 2));
        assertFalse(accepts("killerranking points add @s -1", 2));
    }
    @Test void resetRerollAndHelpWorkWithBothNamesAndRemainOperatorOnly() {
        for (String root : new String[] {"kr", "killerranking"}) {
            for (String suffix : new String[] {"", " help", " parry reset @s", " parry reset @a", " trait reroll @s", " trait reroll @a", " level set @s 3"}) {
                assertTrue(accepts(root + suffix, 2), root + suffix);
                assertFalse(accepts(root + suffix, 0), root + suffix);
                assertFalse(accepts(root + suffix, 1), root + suffix);
            }
            assertFalse(accepts(root + " parry reset", 2));
            assertFalse(accepts(root + " trait reroll", 2));
            assertFalse(accepts(root + " trait reroll @s extra", 2));
        }
    }
    @Test void decimalPointGrantsRemainOperatorOnlyAndBounded() {
        for (String value : new String[]{"0.0000001", "0.0001", "0.001", "0.5", "1000000"}) {
            assertTrue(accepts("kr points add @s " + value, 2));
            assertFalse(accepts("kr points add @s " + value, 0));
        }
        for (String value : new String[]{"0", "-0.001", "0.00000001", "1000001", "NaN", "Infinity"})
            assertFalse(accepts("kr points add @s " + value, 2));
    }
}
