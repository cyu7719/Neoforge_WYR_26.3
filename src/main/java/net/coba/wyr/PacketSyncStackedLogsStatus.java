package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Packet from Server to Client to sync the status of the stacked_logs effect
public record PacketSyncStackedLogsStatus(boolean hasEffect) implements CustomPacketPayload {

    public static final Type<PacketSyncStackedLogsStatus> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WYR.MODID, "sync_stacked_logs_status"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncStackedLogsStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            PacketSyncStackedLogsStatus::hasEffect,
            PacketSyncStackedLogsStatus::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncStackedLogsStatus message, IPayloadContext context) {
        context.enqueueWork(() -> {
            // This runs on the client. Update the client-side tracker.
            ClientEffectCache.setHasStackedLogs(message.hasEffect());
        });
    }
}