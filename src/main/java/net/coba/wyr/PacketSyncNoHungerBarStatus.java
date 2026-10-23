package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncNoHungerBarStatus(boolean hasEffect) implements CustomPacketPayload {

    public static final Type<PacketSyncNoHungerBarStatus> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_no_hunger_bar_status"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncNoHungerBarStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            PacketSyncNoHungerBarStatus::hasEffect,
            PacketSyncNoHungerBarStatus::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncNoHungerBarStatus message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientEffectCache.setHasNoHungerBar(message.hasEffect());
        });
    }
}