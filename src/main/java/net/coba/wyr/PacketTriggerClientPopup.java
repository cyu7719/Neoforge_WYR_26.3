package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Simple trigger packet to tell client to open popup without any data
public record PacketTriggerClientPopup() implements CustomPacketPayload {
    public static final Type<PacketTriggerClientPopup> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "trigger_client_popup"));

    public static final StreamCodec<FriendlyByteBuf, PacketTriggerClientPopup> STREAM_CODEC = StreamCodec.unit(new PacketTriggerClientPopup());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketTriggerClientPopup message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandlers.handleTriggerClientPopup(message);
        });
    }
}
