package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Simple trigger packet to tell client to open popup without complex Choice serialization
public record PacketTriggerPopup() implements CustomPacketPayload {
    public static final Type<PacketTriggerPopup> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "trigger_popup"));

    public static final StreamCodec<FriendlyByteBuf, PacketTriggerPopup> STREAM_CODEC = StreamCodec.unit(new PacketTriggerPopup());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketTriggerPopup message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandlers.handleTriggerPopup(message);
        });
    }
}
