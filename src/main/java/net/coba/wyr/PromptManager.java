package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class PromptManager {

    private static int intervalTicks = 0;
    private static int currentTicks = 0;
    private static boolean isPaused = false;
    // The 20-prompt limit is now handled server-side and is persistent.
    // private static int timesShown = 0;
    // private static final int MAX_TIMES_SHOWN = 20;

    private static List<Choice> lastShownChoices = null;
    private static boolean shouldShowOnRespawn = false;

    public static void setPromptInterval(String intervalValue) {
        intervalTicks = switch (intervalValue) {
            case "30s" -> 30 * 20;
            case "1min" -> 60 * 20;
            case "3min" -> 180 * 20;
            case "5min" -> 300 * 20;
            case "10min" -> 600 * 20;
            default -> 0;
        };
        currentTicks = 0;
        isPaused = false;
        // The client no longer has control over resetting the shown pairs list.
    }

    public static void resetAndResumeTimer() {
        currentTicks = 0;
        isPaused = false;
        lastShownChoices = null;
        shouldShowOnRespawn = false;
    }

    private void showChoiceScreen() {
        if (Minecraft.getInstance().screen == null) {
            PacketDistributor.sendToServer(new PacketRequestNewChoices());
            isPaused = true;
        }
    }

    private void showPreviousChoiceScreen() {
        if (Minecraft.getInstance().screen == null && lastShownChoices != null) {
            Minecraft.getInstance().setScreen(new WyrScreen(lastShownChoices));
            isPaused = true;
        }
    }

    public static void setLastShownChoices(List<Choice> choices) {
        lastShownChoices = choices;
    }


    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        // The server now decides if the player has reached their prompt limit.
        if (intervalTicks > 0 && Minecraft.getInstance().level != null && !isPaused) {
            currentTicks++;
            if (currentTicks >= intervalTicks) {
                showChoiceScreen();
            }
        }
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
        setPromptInterval("Disabled");
        lastShownChoices = null;
        shouldShowOnRespawn = false;
    }
}
