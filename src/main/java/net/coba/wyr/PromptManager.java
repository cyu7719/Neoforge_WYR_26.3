// PromptManager.java
package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class PromptManager {

    private static List<Choice> lastShownChoices = null;
    private static boolean shouldShowOnRespawn = false;

    // The timer logic has been removed from the client and moved to the server.

    public static void resetAndResumeTimer() {
        // This is called when the client confirms a choice.
        // The server will handle restarting the timer.
        lastShownChoices = null;
        shouldShowOnRespawn = false;
    }

    private void showPreviousChoiceScreen() {
        if (Minecraft.getInstance().screen == null && lastShownChoices != null) {
            Minecraft.getInstance().setScreen(new WyrScreen(lastShownChoices));
        }
    }

    public static void setLastShownChoices(List<Choice> choices) {
        lastShownChoices = choices;
    }

    @SubscribeEvent
    public void onPlayerTick(ClientTickEvent.Pre event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.isDeadOrDying()) {
            if (WyrScreen.isWyrScreenOpen) {
                shouldShowOnRespawn = true;
                Minecraft.getInstance().setScreen(null);
                WyrScreen.isWyrScreenOpen = false;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (shouldShowOnRespawn) {
            showPreviousChoiceScreen();
            shouldShowOnRespawn = false;
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // Clear client-side cache on logout
        lastShownChoices = null;
        shouldShowOnRespawn = false;
    }
}