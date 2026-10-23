package net.coba.wyr.mixin.client;

import net.coba.wyr.WYR;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Temporary diagnostic: logs every inbound packet that reaches the client's
 * Connection.channelRead0 while the local player has not been created yet, so
 * we can see whether ClientboundLoginPacket (and the rest of the login burst)
 * is actually delivered to the client during world load.
 */
@Mixin(Connection.class)
public abstract class ConnectionPacketDebugMixin {

    @Inject(method = "channelRead0", at = @At("HEAD"))
    private void wyr$logInbound(io.netty.channel.ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        try {
            // Only log on the client connection before the player exists, to
            // avoid flooding the log during normal gameplay.
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player == null && mc.level == null) {
                WYR.LOGGER.error("[WYR-NET] inbound " + packet.getClass().getName());
            }
        } catch (Throwable ignored) {
        }
    }
}
