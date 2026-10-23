package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncHalfHungerStatus(boolean hasEffect) implements CustomPacketPayload {

    public static final Type<PacketSyncHalfHungerStatus> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_half_hunger_status"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncHalfHungerStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            PacketSyncHalfHungerStatus::hasEffect,
            PacketSyncHalfHungerStatus::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncHalfHungerStatus message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientEffectCache.setHasHalfHunger(message.hasEffect());
        });
    }
}