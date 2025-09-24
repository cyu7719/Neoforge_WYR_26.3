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

public class PlayerChoiceTracker {

    private static WyrSavedData getSavedData(ServerPlayer player) {
        return WyrSavedData.get(player.serverLevel());
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
            List<Choice> choices = getChoices(player);
            boolean hasStackedLogs = false;
            for (Choice choice : choices) {
                // MODIFICATION: Call the reapply method to avoid giving items again
                ChoiceEffects.reapplyPersistentEffect(player, choice.getId());
                if ("stacked_logs".equals(choice.getId())) {
                    hasStackedLogs = true;
                }
            }
            PacketDistributor.sendToPlayer(player, new PacketSyncStackedLogsStatus(hasStackedLogs));

            if (isPromptPending(player)) {
                List<Choice> newChoices = ChoiceRegistry.getFilteredRandomChoicesForPlayer(player);
                if (!newChoices.isEmpty()) {
                    PacketDistributor.sendToPlayer(player, new PacketSyncNewChoices(newChoices));
                } else {
                    setPromptPending(player, false);
                }
            }
        }
    }
}