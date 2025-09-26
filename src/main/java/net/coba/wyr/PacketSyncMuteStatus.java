package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncMuteStatus(boolean isMuted) implements CustomPacketPayload {

    public static final Type<PacketSyncMuteStatus> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WYR.MODID, "sync_mute_status"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncMuteStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            PacketSyncMuteStatus::isMuted,
            PacketSyncMuteStatus::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncMuteStatus message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientEffectCache.setMuted(message.isMuted());
        });
    }
}