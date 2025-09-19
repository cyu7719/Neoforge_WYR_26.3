package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent; // <-- CORRECTED IMPORT

/**
 * Handles rendering custom text on the in-game HUD (Heads-Up Display).
 * This version uses the modern RenderGuiEvent for NeoForge 1.21.1+.
 */
public class HudEventHandler {

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        // Don't render if the player is null, the GUI is hidden (F1), or they don't have the effect
        if (player == null || mc.options.hideGui || !ChoiceEffects.hasEffect(player, "ender_teleport")) {
            return;
        }

        // Get all the necessary rendering components from the event and Minecraft instance
        GuiGraphics guiGraphics = event.getGuiGraphics();
        Font font = mc.font;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // This logic remains the same: check cooldown and create the right text
        long cooldownEndTime = ChoiceEffects.getTeleportCooldown(player);
        long currentTime = System.currentTimeMillis();
        Component textToRender;

        if (currentTime < cooldownEndTime) {
            long remainingMillis = cooldownEndTime - currentTime;
            int remainingSeconds = (int) Math.ceil(remainingMillis / 1000.0);
            textToRender = Component.literal("Teleport Cooldown: " + remainingSeconds + "s");
        } else {
            Component keyName = Keybinds.TELEPORT_KEY.getTranslatedKeyMessage();
            textToRender = Component.literal("Press ").append(keyName).append(" to Teleport");
        }

        // Positioning logic also remains the same
        int hotbarWidth = 182;
        int textWidth = font.width(textToRender);
        int x = (screenWidth - hotbarWidth) / 2 - textWidth - 5;
        int y = screenHeight - font.lineHeight - 3;

        // Draw the text with a shadow
        guiGraphics.drawString(font, textToRender, x, y, 0xFFFFFF, true);
    }
}