// PacketSetPromptInterval.java
package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSetPromptInterval(int intervalTicks) implements CustomPacketPayload {

    public static final Type<PacketSetPromptInterval> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "set_prompt_interval"));
    public static final StreamCodec<FriendlyByteBuf, PacketSetPromptInterval> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            PacketSetPromptInterval::intervalTicks,
            PacketSetPromptInterval::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSetPromptInterval message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                WyrSavedData savedData = WyrSavedData.get((net.minecraft.server.level.ServerLevel) serverPlayer.level());
                int currentSavedInterval = savedData.getPlayerPromptInterval(serverPlayer.getUUID());

                // If the client tries to set the interval to 0 ("Disabled") after a game restart,
                // but the server already has a valid interval saved for this world, ignore the request.
                if (message.intervalTicks() == 0 && currentSavedInterval > 0) {
                    return; // Do nothing, preserving the correct server-side setting.
                }

                // If it's a new world or a legitimate change, update the interval.
                savedData.setPlayerPromptInterval(serverPlayer.getUUID(), message.intervalTicks());

                if (message.intervalTicks() > 0) {
                    long currentTime = serverPlayer.level().getGameTime();
                    // Set the timer only if it's a brand new world (next prompt time isn't already set).
                    // This prevents the timer from resetting if you go to the main menu and back.
                    if (savedData.getNextPromptTime(serverPlayer.getUUID()) == Long.MAX_VALUE) {
                        savedData.setNextPromptTime(serverPlayer.getUUID(), currentTime + message.intervalTicks());
                    }
                } else {
                    // If the user genuinely disables the timer, clear the next prompt time.
                    savedData.setNextPromptTime(serverPlayer.getUUID(), 0);
                }

                // Save is automatic in 1.21.1
            }
        });
    }
}