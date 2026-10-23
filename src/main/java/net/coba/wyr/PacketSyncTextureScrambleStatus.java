package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncTextureScrambleStatus(boolean hasEffect) implements CustomPacketPayload {

    public static final Type<PacketSyncTextureScrambleStatus> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_texture_scramble_status"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncTextureScrambleStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            PacketSyncTextureScrambleStatus::hasEffect,
            PacketSyncTextureScrambleStatus::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncTextureScrambleStatus message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientEffectCache.setHasTextureScramble(message.hasEffect());
        });
    }
}