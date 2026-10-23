package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientPopupManager {
    private static int checkTimer = 0;
    private static final int CHECK_INTERVAL = 20; // Check every 20 ticks (1 second)
    private static boolean popupTriggered = false;
    private static long lastPopupTime = 0;
    // Choices queued while the client is still loading the world.
    private static List<Choice> pendingChoices = null;

    public static void init() {
        NeoForge.EVENT_BUS.addListener(ClientPopupManager::onClientTick);
    }

    public static void triggerPopup() {
        popupTriggered = true;
        lastPopupTime = System.currentTimeMillis();
        System.out.println("ClientPopupManager: Popup triggered by server");
    }

    /**
     * Queue a WyrScreen to open once the client has finished loading the world.
     * Calling Minecraft.setScreen() while the level is still null can stall the
     * render thread during the login/loading transition.
     */
    public static void queuePopup(List<Choice> choices) {
        if (choices == null || choices.size() < 2) return;
        pendingChoices = new ArrayList<>(choices);
        popupTriggered = true;
        lastPopupTime = System.currentTimeMillis();
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gui.screen() != null) {
            return;
        }

        checkTimer++;
        if (checkTimer >= CHECK_INTERVAL) {
            checkTimer = 0;
            checkForPendingPopup();
        }
    }

    private static void checkForPendingPopup() {
        Minecraft mc = Minecraft.getInstance();
        // Check if popup was triggered and we should open it
        if (popupTriggered && mc.player != null && mc.level != null && mc.gui.screen() == null) {
            // Check if enough time has passed since trigger (to avoid immediate opening)
            long timeSinceTrigger = System.currentTimeMillis() - lastPopupTime;
            if (timeSinceTrigger >= 500) { // Wait 500ms after trigger
                System.out.println("ClientPopupManager: Opening popup after trigger");
                if (pendingChoices != null) {
                    openPopup(pendingChoices);
                    pendingChoices = null;
                } else {
                    openRandomPopup();
                }
                popupTriggered = false;
            }
        }
    }

    private static void openPopup(List<Choice> selectedChoices) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gui.screen() != null) {
            return;
        }
        System.out.println("ClientPopupManager: Opening WyrScreen with choices:");
        for (Choice choice : selectedChoices) {
            System.out.println("  - " + choice.getId() + ": " + choice.positive() + " / " + choice.negative());
        }
        mc.gui.setScreen(new WyrScreen(selectedChoices));
    }

    private static void openRandomPopup() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gui.screen() != null) {
            return;
        }

        // Get random choices for the popup
        List<Choice> allChoices = new ArrayList<>(ChoiceRegistry.getAllChoices());
        allChoices.removeIf(choice -> choice == null || choice.getId() == null);

        if (allChoices.size() >= 2) {
            Collections.shuffle(allChoices);
            List<Choice> selectedChoices = new ArrayList<>();
            selectedChoices.add(allChoices.get(0));
            selectedChoices.add(allChoices.get(1));

            openPopup(selectedChoices);
        }
    }
}
