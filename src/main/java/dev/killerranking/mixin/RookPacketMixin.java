package dev.killerranking.mixin;
import dev.killerranking.KillerRankingMod;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Check on the server thread before accepting movement, attacks, block use or item actions. */
@Mixin(ServerGamePacketListenerImpl.class)
abstract class RookPacketMixin {
    @Shadow public ServerPlayer player;
    @Inject(method="handleMovePlayer",at=@At("HEAD"),cancellable=true)
    private void killerranking$grabMove(ServerboundMovePlayerPacket packet,CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet,(ServerGamePacketListenerImpl)(Object)this,player.serverLevel());
        if(KillerRankingMod.ROOK.locked(player)||KillerRankingMod.SHOULDER_STAB.casting(player)) {
            // Keep looking around responsive, while ignoring movement during the grapple.
            if(packet.hasRotation()){
                float yaw=packet.getYRot(player.getYRot()),pitch=packet.getXRot(player.getXRot());
                if(Float.isFinite(yaw)&&Float.isFinite(pitch)){player.setYRot(net.minecraft.util.Mth.wrapDegrees(yaw));player.setXRot(Math.clamp(pitch,-90,90));}
            }
            ci.cancel();
        }
    }
    @Inject(method="handleInteract",at=@At("HEAD"),cancellable=true)
    private void killerranking$grabInteract(ServerboundInteractPacket packet,CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet,(ServerGamePacketListenerImpl)(Object)this,player.serverLevel());
        if(KillerRankingMod.ROOK.locked(player))ci.cancel();
    }
    @Inject(method="handleUseItem",at=@At("HEAD"),cancellable=true)
    private void killerranking$grabUse(ServerboundUseItemPacket packet,CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet,(ServerGamePacketListenerImpl)(Object)this,player.serverLevel());
        if(KillerRankingMod.ROOK.locked(player))ci.cancel();
    }
    @Inject(method="handleUseItemOn",at=@At("HEAD"),cancellable=true)
    private void killerranking$grabBlock(ServerboundUseItemOnPacket packet,CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet,(ServerGamePacketListenerImpl)(Object)this,player.serverLevel());
        if(KillerRankingMod.ROOK.locked(player))ci.cancel();
    }
    @Inject(method="handlePlayerAction",at=@At("HEAD"),cancellable=true)
    private void killerranking$grabAction(ServerboundPlayerActionPacket packet,CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet,(ServerGamePacketListenerImpl)(Object)this,player.serverLevel());
        if(KillerRankingMod.ROOK.locked(player))ci.cancel();
    }
    @Inject(method="handleSetCarriedItem",at=@At("HEAD"),cancellable=true)
    private void killerranking$grabSlot(ServerboundSetCarriedItemPacket packet,CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet,(ServerGamePacketListenerImpl)(Object)this,player.serverLevel());
        if(KillerRankingMod.ROOK.locked(player))ci.cancel();
    }
}
