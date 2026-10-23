package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncShaderStatus(boolean hasEffect) implements CustomPacketPayload {

    public static final Type<PacketSyncShaderStatus> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_shader_status"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncShaderStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            PacketSyncShaderStatus::hasEffect,
            PacketSyncShaderStatus::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncShaderStatus message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientEffectCache.setHasRandomShader(message.hasEffect());
        });
    }
}