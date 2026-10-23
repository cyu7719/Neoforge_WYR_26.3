// PlayerChoiceTracker.java
package net.coba.wyr;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.coba.wyr.ClientEffectCache.hasRandomSounds;

public class PlayerChoiceTracker {

    private static WyrSavedData getSavedData(ServerPlayer player) {
        return WyrSavedData.get((net.minecraft.server.level.ServerLevel) player.level());
    }

    public static void addChoice(ServerPlayer player, Choice choice) {
        getSavedData(player).addPlayerChoice(player.getUUID(), choice.getId());
    }

    public static void removeChoice(ServerPlayer player, String choiceId) {
        getSavedData(player).removePlayerChoice(player.getUUID(), choiceId);
    }

    public static List<Choice> getChoices(ServerPlayer player) {
        return getSavedData(player).getPlayerChoices(player.getUUID()).stream()
                .map(ChoiceRegistry::getChoiceById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static void clearChoices(ServerPlayer player) {
        getSavedData(player).clearPlayerChoices(player.getUUID());
    }

    public static int getPromptCount(ServerPlayer player) {
        return getSavedData(player).getPlayerPromptCount(player.getUUID());
    }

    public static void incrementPromptCount(ServerPlayer player) {
        getSavedData(player).incrementPlayerPromptCount(player.getUUID());
    }

    public static boolean isPromptPending(ServerPlayer player) {
        return getSavedData(player).isPromptPending(player.getUUID());
    }

    public static void setPromptPending(ServerPlayer player, boolean pending) {
        getSavedData(player).setPromptPending(player.getUUID(), pending);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WYR.LOGGER.info("Player logged in: {} - reapplying effects and syncing", player.getName().getString());
            // Get all saved choices for the player
            List<Choice> choices = getChoices(player);

            // Re-apply all persistent server-side effects. This updates the in-memory state.
            for (Choice choice : choices) {
                ChoiceEffects.applyEffect(player, choice.getId(), false);
            }

            ChoiceEffects.syncAllClientEffects(player);

            // Handle any pending prompts.
            if (isPromptPending(player)) {
                List<Choice> newChoices = ChoiceRegistry.getFilteredRandomChoicesForPlayer(player);
                if (!newChoices.isEmpty()) {
                    // Convert Choice objects to choice IDs for the packet
                    List<String> choiceIds = newChoices.stream()
                        .map(Choice::getId)
                        .toList();
                    PacketDistributor.sendToPlayer(player, new PacketSyncNewChoices(choiceIds));
                } else {
                    setPromptPending(player, false);
                }
            }
        }
    }
}