package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncRandomSoundsStatus(boolean hasEffect) implements CustomPacketPayload {

    public static final Type<PacketSyncRandomSoundsStatus> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_random_sounds_status"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncRandomSoundsStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            PacketSyncRandomSoundsStatus::hasEffect,
            PacketSyncRandomSoundsStatus::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncRandomSoundsStatus message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientEffectCache.setHasRandomSounds(message.hasEffect());
        });
    }
}