package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketApplyEffect(String effectId) implements CustomPacketPayload {

    public static final Type<PacketApplyEffect> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WYR.MODID, "apply_effect"));

    public static final StreamCodec<FriendlyByteBuf, PacketApplyEffect> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            PacketApplyEffect::effectId,
            PacketApplyEffect::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketApplyEffect message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                Choice choice = ChoiceRegistry.getChoiceById(message.effectId());
                if (choice != null) {
                    ChoiceEffects.applyEffect(serverPlayer, message.effectId());
                    PlayerChoiceTracker.addChoice(serverPlayer, choice);
                    // Increment the player's persistent prompt counter.
                    PlayerChoiceTracker.incrementPromptCount(serverPlayer);
                }
            }
        });
    }
}