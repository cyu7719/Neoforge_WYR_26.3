package net.coba.wyr.mixin.client;

import net.coba.wyr.WYR;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Temporary diagnostic: logs when ClientPacketListener.handleLogin is entered
 * and when it returns, so we can tell whether the ClientboundLoginPacket ever
 * reaches the client handler during world load.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerDebugMixin {

    @Inject(method = "handleLogin", at = @At("HEAD"))
    private void wyr$logLoginHead(ClientboundLoginPacket packet, CallbackInfo ci) {
        WYR.LOGGER.error("[WYR-NET] handleLogin ENTERED thread=" + Thread.currentThread().getName());
    }

    @Inject(method = "handleLogin", at = @At("RETURN"))
    private void wyr$logLoginReturn(ClientboundLoginPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        WYR.LOGGER.error("[WYR-NET] handleLogin RETURNED mc.player=" + mc.player + " mc.level=" + mc.level);
    }

    private static int wyr$bundleCount = 0;

    @Inject(method = "handleBundlePacket", at = @At("HEAD"))
    private void wyr$logBundle(ClientboundBundlePacket packet, CallbackInfo ci) {
        try {
            boolean hasLogin = false;
            int n = 0;
            StringBuilder names = new StringBuilder();
            for (Packet<? super ClientGamePacketListener> p : packet.subPackets()) {
                n++;
                if (p instanceof ClientboundLoginPacket) hasLogin = true;
                if (names.length() < 300) names.append(p.getClass().getSimpleName()).append(',');
            }
            WYR.LOGGER.error("[WYR-NET] handleBundlePacket #" + (++wyr$bundleCount)
                    + " thread=" + Thread.currentThread().getName()
                    + " subPackets=" + n + " hasLogin=" + hasLogin + " [" + names + "]");
        } catch (Throwable t) {
            WYR.LOGGER.error("[WYR-NET] handleBundlePacket log failed", t);
        }
    }
}
