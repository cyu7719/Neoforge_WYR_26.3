package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncRandomCrosshairStatus(boolean hasEffect) implements CustomPacketPayload {
    public static final Type<PacketSyncRandomCrosshairStatus> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_random_crosshair_status"));
    public static final StreamCodec<FriendlyByteBuf, PacketSyncRandomCrosshairStatus> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, PacketSyncRandomCrosshairStatus::hasEffect, PacketSyncRandomCrosshairStatus::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketSyncRandomCrosshairStatus message, IPayloadContext context) {
        context.enqueueWork(() -> ClientEffectCache.setHasRandomCrosshair(message.hasEffect()));
    }
}