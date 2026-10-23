package net.coba.wyr.mixin;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.BundleDelimiterPacket;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * WORKAROUND for a NeoForge 26.2.0.3-beta bug: the bundled
 * BundlerInfo$1/BundlerInfo$1$1 classes were incorrectly transformed so that
 * {@code startPacketBundling} and {@code Bundler.addPacket} compare the incoming
 * packet against itself ({@code p == p}) instead of against the captured
 * delimiter ({@code p == packet}). This makes every packet a delimiter and
 * produces empty ClientboundBundlePackets, which prevents ClientboundLoginPacket
 * from ever reaching ClientPacketListener.handleLogin.
 *
 * This mixin replaces createForPacket with a correct implementation that uses
 * reference equality against the real delimiter instance.
 */
@Mixin(BundlerInfo.class)
public interface BundlerInfoFixMixin {

    @Inject(method = "createForPacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener, P extends BundlePacket<? super T>> void wyr$fixCreateForPacket(
            PacketType<P> type,
            Function<Iterable<Packet<? super T>>, P> bundler,
            BundleDelimiterPacket<? super T> packet,
            CallbackInfoReturnable<BundlerInfo> cir) {
        cir.setReturnValue(new BundlerInfo() {
            @Override
            public void unbundlePacket(Packet<?> p, Consumer<Packet<?>> consumer) {
                if (p.type() == type) {
                    P bundle = (P) p;
                    consumer.accept(packet);
                    bundle.subPackets().forEach(consumer);
                    consumer.accept(packet);
                } else {
                    consumer.accept(p);
                }
            }

            @Override
            public void unbundlePacket(Packet<?> bundlePacket, Consumer<Packet<?>> packetSender, ChannelHandlerContext context) {
                if (bundlePacket.type() == type) {
                    P p = (P) bundlePacket;
                    List<Packet<?>> packets = NetworkRegistry.filterGameBundlePackets(context, p.subPackets());
                    if (packets.isEmpty()) {
                        return;
                    }
                    if (packets.size() == 1) {
                        packetSender.accept(packets.get(0));
                        return;
                    }
                    packetSender.accept(packet);
                    packets.forEach(packetSender);
                    packetSender.accept(packet);
                } else {
                    packetSender.accept(bundlePacket);
                }
            }

            @Nullable
            @Override
            public Bundler startPacketBundling(Packet<?> p) {
                return p == packet ? new Bundler() {
                    private final List<Packet<? super T>> bundlePackets = new ArrayList<>();

                    @Nullable
                    @Override
                    public Packet<?> addPacket(Packet<?> p2) {
                        if (p2 == packet) {
                            return bundler.apply(this.bundlePackets);
                        } else if (this.bundlePackets.size() >= BUNDLE_SIZE_LIMIT) {
                            throw new IllegalStateException("Too many packets in a bundle");
                        } else {
                            this.bundlePackets.add((Packet<? super T>) p2);
                            return null;
                        }
                    }
                } : null;
            }
        });
    }
}
