package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncRenderDistanceStatus(boolean hasEffect) implements CustomPacketPayload {
    public static final Type<PacketSyncRenderDistanceStatus> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_render_distance_status"));
    public static final StreamCodec<FriendlyByteBuf, PacketSyncRenderDistanceStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PacketSyncRenderDistanceStatus::hasEffect, PacketSyncRenderDistanceStatus::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketSyncRenderDistanceStatus message, IPayloadContext context) {
        context.enqueueWork(() -> ClientEffectCache.setHasLowRenderDistance(message.hasEffect()));
    }
}