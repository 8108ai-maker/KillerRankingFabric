package dev.killerranking.client;

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;

/** Scopes view poses to the camera player; remote players may render during the same pass. */
public final class FirstPersonWeaponView {
    private static boolean rendering;
    public static void begin(AbstractClientPlayer player) {
        var client=Minecraft.getInstance();
        rendering=player==client.getCameraEntity()&&client.options.getCameraType().isFirstPerson()
            &&FirstPersonMode.isFirstPersonPass();
    }
    public static boolean active() { return rendering; }
    public static void end() { rendering=false; }
    private FirstPersonWeaponView() { }
}
