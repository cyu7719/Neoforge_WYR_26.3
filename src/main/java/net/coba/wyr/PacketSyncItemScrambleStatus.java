package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncItemScrambleStatus(boolean hasEffect) implements CustomPacketPayload {
    public static final Type<PacketSyncItemScrambleStatus> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_item_scramble_status"));
    public static final StreamCodec<FriendlyByteBuf, PacketSyncItemScrambleStatus> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, PacketSyncItemScrambleStatus::hasEffect, PacketSyncItemScrambleStatus::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketSyncItemScrambleStatus message, IPayloadContext context) {
        context.enqueueWork(() -> ClientEffectCache.setHasItemTextureScramble(message.hasEffect()));
    }
}