package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor; // Import this

/**
 * Handles client-side input events, specifically for our custom keybinds.
 */
public class InputEventHandler {

    /**
     * This event fires whenever a key is pressed.
     * @param event The key input event.
     */
    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        // consumeClick() checks if our key was pressed and returns true once per press.
        if (Keybinds.TELEPORT_KEY.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            // Ensure the player exists and has the teleport effect before triggering it.
            if (player != null && ChoiceEffects.hasEffect(player, "ender_teleport")) {
                // INSTEAD of calling the method directly, send a packet to the server.
                PacketDistributor.sendToServer(new PacketTeleport());
            }
        }

        if (Keybinds.PROMPT_LIST_KEY.consumeClick()) {
            // When K is pressed, send a packet to the server asking for the choice list.
            PacketDistributor.sendToServer(new PacketRequestChoiceList());
        }
    }
}