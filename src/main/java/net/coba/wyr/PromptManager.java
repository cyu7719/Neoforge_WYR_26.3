package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.List;

public class PromptManager {

    private static int intervalTicks = 0;
    private static int currentTicks = 0;
    private static boolean isPaused = false;
    private static int timesShown = 0;
    private static final int MAX_TIMES_SHOWN = 20;

    private static List<Choice> lastShownChoices = null;
    private static boolean shouldShowOnRespawn = false;

    public static void setPromptInterval(String intervalValue) {
        intervalTicks = switch (intervalValue) {
            case "30s" -> 30 * 20;
            case "1min" -> 60 * 20;
            case "3min" -> 180 * 20;
            case "5min" -> 300 * 20;
            case "10min" -> 600 * 20;
            default -> 0; // "None"
        };
        currentTicks = 0;
        timesShown = 0;
        isPaused = false;
    }

    public static void resetAndResumeTimer() {
        currentTicks = 0;
        isPaused = false;
        lastShownChoices = null;
        shouldShowOnRespawn = false;
    }

    private void showChoiceScreen() {
        if (Minecraft.getInstance().screen == null) {
            lastShownChoices = ChoiceRegistry.getRandomChoices(2);
            Minecraft.getInstance().setScreen(new WyrScreen(lastShownChoices));
            timesShown++;
            isPaused = true;
        }
    }

    private void showPreviousChoiceScreen() {
        if (Minecraft.getInstance().screen == null && lastShownChoices != null) {
            Minecraft.getInstance().setScreen(new WyrScreen(lastShownChoices));
            isPaused = true;
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if (intervalTicks > 0 && Minecraft.getInstance().level != null && !isPaused && timesShown < MAX_TIMES_SHOWN) {
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
        setPromptInterval("None");
        lastShownChoices = null;
        shouldShowOnRespawn = false;
    }
}