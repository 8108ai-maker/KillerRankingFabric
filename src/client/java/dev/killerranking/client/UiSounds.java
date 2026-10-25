package dev.killerranking.client;

import dev.killerranking.KillerRankingSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;

/** Non-positional UI feedback for the phone and its panels. */
final class UiSounds {
    static void click(SoundManager manager) { manager.play(SimpleSoundInstance.forUI(KillerRankingSounds.UI_CLICK, 1.0F, 0.9F)); }
    static void open() { Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KillerRankingSounds.UI_OPEN, 1.0F, 0.8F)); }
    static void close() { Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KillerRankingSounds.UI_CLOSE, 1.0F, 0.8F)); }
    static void deny() { Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KillerRankingSounds.UI_DENY, 1.0F, 0.8F)); }
    private UiSounds() { }
}
