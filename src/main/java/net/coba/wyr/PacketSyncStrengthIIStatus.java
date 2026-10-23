package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncStrengthIIStatus(boolean hasEffect) implements CustomPacketPayload {

    public static final Type<PacketSyncStrengthIIStatus> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_strength_ii_status"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncStrengthIIStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            PacketSyncStrengthIIStatus::hasEffect,
            PacketSyncStrengthIIStatus::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncStrengthIIStatus message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientEffectCache.setHasStrengthII(message.hasEffect());
        });
    }
}