package dev.killerranking.client;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.network.AutoParryRequestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ClientAutoParry {
    private static boolean held;
    private static int pulse;
    public static void input(Minecraft client,boolean down){
        boolean usable=client.player!=null&&client.player.isAlive()&&!client.player.isSpectator()&&client.screen==null&&!client.isPaused()
            &&ClientCleave.branch()&&ClientCleave.unlocked()&&TraitVisuals.enabled()
            &&client.player.getMainHandItem().is(KillerRankingMod.PAWN6_KATANA)&&!client.player.isUsingItem()
            &&ClientCleave.phase()==0&&!ClientRook.locked()&&ClientRook.phase()==0&&ClientCheckmate.remaining()==0&&ClientRush.remaining()==0;
        boolean want=down&&usable;
        if(want!=held){held=want;pulse=0;send(want);if(!want&&client.player!=null)AutoParryVfx.stop(client.player.getUUID());}
        else if(held&&++pulse>=5){pulse=0;send(true);}
    }
    private static void send(boolean value){if(ClientPlayNetworking.canSend(AutoParryRequestPayload.TYPE))ClientPlayNetworking.send(new AutoParryRequestPayload(value));}
    public static boolean held(){return held;}
    public static boolean active(){var p=Minecraft.getInstance().player;return p!=null&&(held||AutoParryVfx.active(p.getUUID()));}
    public static Component status(){
        if(active())return Component.literal("자동 방어 중");
        if(!TraitVisuals.enabled())return Component.literal("꺼짐");
        var p=Minecraft.getInstance().player;
        if(p!=null&&!p.getMainHandItem().is(KillerRankingMod.PAWN6_KATANA))return Component.literal("카타나 필요");
        return Component.literal("꾹 눌러 유지");
    }
    public static void clear(){held=false;pulse=0;}
    private ClientAutoParry() { }
}
