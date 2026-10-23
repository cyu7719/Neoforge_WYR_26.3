package net.coba.wyr.mixin.client;

import net.coba.wyr.WYR;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TEMPORARY diagnostic: log how many sub-packets are passed into a BundlePacket
 * constructor. This shows whether the bundler collected packets (non-empty
 * input) or whether the bundle is constructed empty.
 */
@Mixin(BundlePacket.class)
public abstract class BundlePacketDebugMixin {

    @Inject(method = "<init>", at = @At("HEAD"))
    private static void wyr$logBundleInit(Iterable<?> packets, CallbackInfo ci) {
        try {
            int n = 0;
            StringBuilder names = new StringBuilder();
            for (Object p : packets) {
                n++;
                if (names.length() < 200) names.append(p.getClass().getSimpleName()).append(',');
            }
            WYR.LOGGER.error("[WYR-NET] BundlePacket.<init> inputCount=" + n + " [" + names + "]");
        } catch (Throwable t) {
            WYR.LOGGER.error("[WYR-NET] BundlePacket.<init> log failed", t);
        }
    }
}
