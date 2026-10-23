package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncNoJumpStatus(boolean hasEffect) implements CustomPacketPayload {
    public static final Type<PacketSyncNoJumpStatus> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_no_jump_status"));
    public static final StreamCodec<FriendlyByteBuf, PacketSyncNoJumpStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PacketSyncNoJumpStatus::hasEffect, PacketSyncNoJumpStatus::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketSyncNoJumpStatus message, IPayloadContext context) {
        context.enqueueWork(() -> ClientEffectCache.setHasNoJump(message.hasEffect()));
    }
}