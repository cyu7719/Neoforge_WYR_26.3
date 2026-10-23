package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncWaterWalkStatus(boolean hasEffect) implements CustomPacketPayload {

    public static final Type<PacketSyncWaterWalkStatus> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_water_walk_status"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncWaterWalkStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            PacketSyncWaterWalkStatus::hasEffect,
            PacketSyncWaterWalkStatus::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncWaterWalkStatus message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientEffectCache.setHasWaterWalk(message.hasEffect());
        });
    }
}