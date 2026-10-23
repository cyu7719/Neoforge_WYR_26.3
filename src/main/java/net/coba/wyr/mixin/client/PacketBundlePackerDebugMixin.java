package net.coba.wyr.mixin.client;

import io.netty.channel.ChannelHandlerContext;
import net.coba.wyr.WYR;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBundlePacker;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * TEMPORARY diagnostic: log every packet that reaches the inbound
 * PacketBundlePacker while the client has no player/level. This shows whether
 * the bundle sub-packets (incl. ClientboundLoginPacket) reach the packer or are
 * decoded as delimiters upstream.
 */
@Mixin(PacketBundlePacker.class)
public abstract class PacketBundlePackerDebugMixin {

    private static boolean wyr$dumpedBundlerInfo = false;

    @Inject(method = "decode", at = @At("HEAD"))
    private void wyr$logDecode(ChannelHandlerContext context, Packet<?> packet, List<Object> out, CallbackInfo ci) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player == null && mc.level == null) {
                WYR.LOGGER.error("[WYR-NET] packer.decode " + packet.getClass().getName()
                        + " type=" + packet.type()
                        + " isDelimiter=" + (packet instanceof net.minecraft.network.protocol.BundleDelimiterPacket));
                if (!wyr$dumpedBundlerInfo) {
                    wyr$dumpedBundlerInfo = true;
                    wyr$dumpBundlerInfo();
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * Reflectively dump the bundlerInfo instance: its class and the captured
     * delimiter 'packet' field. This reveals whether BundlerInfo is the vanilla
     * createForPacket anonymous impl and what instance it compares against.
     */
    private void wyr$dumpBundlerInfo() {
        try {
            java.lang.reflect.Field f = PacketBundlePacker.class.getDeclaredField("bundlerInfo");
            f.setAccessible(true);
            Object bi = f.get(this);
            WYR.LOGGER.error("[WYR-NET] bundlerInfo class=" + (bi == null ? "null" : bi.getClass().getName()));
            if (bi != null) {
                for (java.lang.reflect.Field bf : bi.getClass().getDeclaredFields()) {
                    bf.setAccessible(true);
                    Object v = bf.get(bi);
                    WYR.LOGGER.error("[WYR-NET]   bundlerInfo." + bf.getName() + " = "
                            + (v == null ? "null" : v.getClass().getName() + " @" + Integer.toHexString(System.identityHashCode(v))));
                }
            }
        } catch (Throwable t) {
            WYR.LOGGER.error("[WYR-NET] dumpBundlerInfo failed", t);
        }
    }
}
