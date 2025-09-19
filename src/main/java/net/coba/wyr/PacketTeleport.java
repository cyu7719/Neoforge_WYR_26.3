package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// The packet now implements ICustomPacketPayload
public record PacketTeleport() implements CustomPacketPayload {

    // The unique ID for this packet type
    public static final Type<PacketTeleport> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WYR.MODID, "teleport"));

    // The new StreamCodec handles writing and reading the packet data from the network buffer.
    // For a packet with no fields, we use .unit(), which handles an empty payload.
    public static final StreamCodec<FriendlyByteBuf, PacketTeleport> STREAM_CODEC = StreamCodec.unit(new PacketTeleport());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // This is the handler that runs on the server when the packet is received.
    public static void handle(PacketTeleport message, IPayloadContext context) {
        // We use context.enqueueWork() to ensure our code runs on the server's main thread.
        context.enqueueWork(() -> {
            // Check if the player is a ServerPlayer (it will be here) and has the effect.
            if (context.player() instanceof ServerPlayer serverPlayer && ChoiceEffects.hasEffect(serverPlayer, "ender_teleport")) {
                // Now, call your existing logic from the correct (server-side) context.
                ChoiceEffects.handleEnderTeleport(serverPlayer);
            }
        });
    }
}