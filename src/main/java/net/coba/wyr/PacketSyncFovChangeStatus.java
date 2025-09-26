package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncFovChangeStatus(boolean hasEffect) implements CustomPacketPayload {
    public static final Type<PacketSyncFovChangeStatus> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WYR.MODID, "sync_fov_change_status"));
    public static final StreamCodec<FriendlyByteBuf, PacketSyncFovChangeStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PacketSyncFovChangeStatus::hasEffect, PacketSyncFovChangeStatus::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketSyncFovChangeStatus message, IPayloadContext context) {
        context.enqueueWork(() -> ClientEffectCache.setHasFovChange(message.hasEffect()));
    }
}