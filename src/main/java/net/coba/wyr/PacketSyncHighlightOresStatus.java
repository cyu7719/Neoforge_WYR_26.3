package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncHighlightOresStatus(boolean hasEffect) implements CustomPacketPayload {

    public static final Type<PacketSyncHighlightOresStatus> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_highlight_ores_status"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncHighlightOresStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            PacketSyncHighlightOresStatus::hasEffect,
            PacketSyncHighlightOresStatus::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncHighlightOresStatus message, IPayloadContext context) {
        context.enqueueWork(() -> ClientEffectCache.setHasHighlightOres(message.hasEffect()));
    }
}