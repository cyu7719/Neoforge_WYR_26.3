package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncCrosshairSeed(long seed) implements CustomPacketPayload {
    public static final Type<PacketSyncCrosshairSeed> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_crosshair_seed"));
    public static final StreamCodec<FriendlyByteBuf, PacketSyncCrosshairSeed> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_LONG, PacketSyncCrosshairSeed::seed, PacketSyncCrosshairSeed::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketSyncCrosshairSeed message, IPayloadContext context) {
        context.enqueueWork(() -> ClientEffectCache.setCrosshairSeed(message.seed()));
    }
}