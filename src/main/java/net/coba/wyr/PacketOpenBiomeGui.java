package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketOpenBiomeGui() implements CustomPacketPayload {

    public static final Type<PacketOpenBiomeGui> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "open_biome_gui"));
    public static final StreamCodec<FriendlyByteBuf, PacketOpenBiomeGui> STREAM_CODEC = StreamCodec.unit(new PacketOpenBiomeGui());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketOpenBiomeGui message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandlers.handleOpenBiomeGui(message);
        });
    }
}