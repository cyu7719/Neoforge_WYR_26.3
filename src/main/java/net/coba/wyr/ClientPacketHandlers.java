package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ClientPacketHandlers {

    public static void handleOpenBiomeGui(PacketOpenBiomeGui message) {
        Minecraft.getInstance().gui.setScreen(new BiomeSelectionScreen());
    }

    public static void handleSyncChoiceList(PacketSyncChoiceList message) {
        Minecraft.getInstance().gui.setScreen(new PromptListScreen(message.choices()));
    }



    public static void handleSyncNewChoices(PacketSyncNewChoices message) {
        if (!message.choiceIds().isEmpty()) {
            // Convert choice IDs back to Choice objects
            List<Choice> choices = new ArrayList<>();
            for (String choiceId : message.choiceIds()) {
                Choice choice = ChoiceRegistry.getChoiceById(choiceId);
                if (choice != null) {
                    choices.add(choice);
                }
            }

            if (choices.size() >= 2) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level == null || mc.player == null) {
                    // Still loading the world - defer the screen to a client tick.
                    ClientPopupManager.queuePopup(choices);
                } else {
                    mc.gui.setScreen(new WyrScreen(choices));
                }
            }
        }
    }

    public static void handleTriggerPopup(PacketTriggerPopup message) {
        // Open popup with simple random choices for now
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            // Use a simple approach - get any 2 random choices from the registry
            List<Choice> allChoices = new ArrayList<>(ChoiceRegistry.getAllChoices());
            allChoices.removeIf(choice -> choice == null || choice.getId() == null);

            if (allChoices.size() >= 2) {
                Collections.shuffle(allChoices);
                List<Choice> selectedChoices = new ArrayList<>();
                selectedChoices.add(allChoices.get(0));
                selectedChoices.add(allChoices.get(1));
                if (mc.level == null) {
                    ClientPopupManager.queuePopup(selectedChoices);
                } else {
                    mc.gui.setScreen(new WyrScreen(selectedChoices));
                }
            }
        }
    }

    public static void handleTriggerClientPopup(PacketTriggerClientPopup message) {
        // Trigger the client-side popup manager
        ClientPopupManager.triggerPopup();
    }
    public static void handlePlayCustomSound(PacketPlayCustomSound message) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        Optional<SoundEvent> soundEvent = BuiltInRegistries.SOUND_EVENT.getOptional(message.soundId());

        soundEvent.ifPresent(event -> player.level().playSound(
                player,
                player.blockPosition(),
                event,
                SoundSource.PLAYERS,
                message.volume(),
                message.pitch()
        ));
    }
}