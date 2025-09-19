package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.Collections;
import java.util.List;

public record PacketRequestNewChoices() implements CustomPacketPayload {

    public static final Type<PacketRequestNewChoices> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WYR.MODID, "request_new_choices"));
    public static final StreamCodec<FriendlyByteBuf, PacketRequestNewChoices> STREAM_CODEC = StreamCodec.unit(new PacketRequestNewChoices());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketRequestNewChoices message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                // Server-side check: Only send choices if the player is under the 20 prompt limit.
                if (PlayerChoiceTracker.getPromptCount(serverPlayer) < 20) {
                    List<Choice> choices = ChoiceRegistry.getFilteredRandomChoicesForPlayer(serverPlayer);
                    PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncNewChoices(choices));
                } else {
                    // If the limit is reached, send an empty list. The client will not open the GUI.
                    PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncNewChoices(Collections.emptyList()));
                }
            }
        });
    }
}