package net.coba.wyr;

import net.coba.wyr.item.BackPack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Packet from Client to Server to request opening the backpack
public record PacketOpenBackpack() implements CustomPacketPayload {
    public static final Type<PacketOpenBackpack> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "open_backpack"));
    public static final StreamCodec<FriendlyByteBuf, PacketOpenBackpack> STREAM_CODEC = StreamCodec.unit(new PacketOpenBackpack());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketOpenBackpack message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                // Find and open the backpack
                BackPack.openBackpack(serverPlayer);
            }
        });
    }
}