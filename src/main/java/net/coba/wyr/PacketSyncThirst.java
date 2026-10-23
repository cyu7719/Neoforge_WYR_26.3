package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Packet from Server to Client
public record PacketSyncThirst(int thirstLevel) implements CustomPacketPayload {

    public static final Type<PacketSyncThirst> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "sync_thirst"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncThirst> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            PacketSyncThirst::thirstLevel,
            PacketSyncThirst::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncThirst message, IPayloadContext context) {
        context.enqueueWork(() -> {
            // This runs on the client.
            ThirstHudHandler.setThirst(message.thirstLevel());
        });
    }
}