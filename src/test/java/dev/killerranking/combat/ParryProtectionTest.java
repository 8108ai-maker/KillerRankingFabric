package dev.killerranking.combat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParryProtectionTest {
    @Test void protectsImmediatelyUntilExactlySixServerTicksHaveElapsed() {
        var protection = new ParryProtection(); var player = UUID.randomUUID();
        assertFalse(protection.active(player, 100));
        protection.onSuccess(player, 100);
        // The end of the success tick must not deduct a tick from the new window.
        protection.expire(100);
        for (int tick = 100; tick < 106; tick++) assertTrue(protection.active(player, tick));
        assertFalse(protection.active(player, 106));
        protection.expire(106); assertFalse(protection.active(player, 107));
    }
    @Test void incomingDamageChecksDoNotRefreshTheWindowOrProtectOtherPlayers() {
        var protection = new ParryProtection(); var player = UUID.randomUUID(); var other = UUID.randomUUID();
        protection.onSuccess(player, 50);
        for (int hit = 0; hit < 10; hit++) assertTrue(protection.active(player, 55));
        assertFalse(protection.active(other, 55)); assertFalse(protection.active(player, 56));
        protection.onSuccess(player, 70); assertTrue(protection.active(player, 75)); assertFalse(protection.active(player, 76));
    }
    @Test void disconnectRespawnAndShutdownCleanupRemoveProtection() {
        var protection = new ParryProtection(); var first = UUID.randomUUID(); var second = UUID.randomUUID();
        protection.onSuccess(first, 0); protection.onSuccess(second, 0);
        protection.forget(first); assertFalse(protection.active(first, 1)); assertTrue(protection.active(second, 1));
        protection.clear(); assertFalse(protection.active(second, 1));
    }
}
