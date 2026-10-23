// PacketApplyEffect.java
package net.coba.wyr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketApplyEffect(String effectId) implements CustomPacketPayload {

    public static final Type<PacketApplyEffect> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WYR.MODID, "apply_effect"));

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

                    ChoiceEffects.applyInitialEffect(serverPlayer, message.effectId());

                    ChoiceEffects.syncAllClientEffects(serverPlayer);

                    PlayerChoiceTracker.addChoice(serverPlayer, choice);
                    PlayerChoiceTracker.incrementPromptCount(serverPlayer);
                    PlayerChoiceTracker.setPromptPending(serverPlayer, false);
                    PacketDistributor.sendToPlayer(serverPlayer, new PacketDisplayChoice(choice));

                    WyrSavedData savedData = WyrSavedData.get((net.minecraft.server.level.ServerLevel) serverPlayer.level());
                    int interval = savedData.getPlayerPromptInterval(serverPlayer.getUUID());
                    if (interval > 0) {
                        long nextTime = serverPlayer.level().getGameTime() + interval;
                        savedData.setNextPromptTime(serverPlayer.getUUID(), nextTime);
                    }

                    // Save is automatic in 1.21.1

                    if (message.effectId().equals("choose_spawn_biome_lock")) {
                        PacketDistributor.sendToPlayer(serverPlayer, new PacketOpenBiomeGui());
                    }
                }
            }
        });
    }
}