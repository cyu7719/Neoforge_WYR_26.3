package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

        if (player == null || mc.gui.hud.isHidden()) {
            return;
        }

        // --- Random-food countdown timer (right of the last hotbar slot) ---
        if (ChoiceEffects.hasEffect(player, "random_food_no_jump")) {
            int seconds = ClientEffectCache.getFoodTimerSeconds();
            if (seconds >= 0) {
                GuiGraphicsExtractor gg = event.getGuiGraphics();
                Font f = mc.font;
                int sw = mc.getWindow().getGuiScaledWidth();
                int sh = mc.getWindow().getGuiScaledHeight();
                Component timer = Component.literal("Next food in: " + seconds + "s");
                int hotbarWidth = 182;
                int tx = (sw + hotbarWidth) / 2 + 6; // just right of the last hotbar slot
                int ty = sh - f.lineHeight - 3;
                gg.text(f, timer, tx, ty, 0xFFFFAA00, true);
            }
        }

        // --- Random-shader countdown timer (right of the last hotbar slot) ---
        if (ChoiceEffects.hasEffect(player, "no_hunger_random_shader")) {
            int seconds = ClientEffectCache.getShaderTimerSeconds();
            if (seconds >= 0) {
                GuiGraphicsExtractor gg = event.getGuiGraphics();
                Font f = mc.font;
                int sw = mc.getWindow().getGuiScaledWidth();
                int sh = mc.getWindow().getGuiScaledHeight();
                Component timer = Component.literal("Next shader in: " + seconds + "s");
                int hotbarWidth = 182;
                int tx = (sw + hotbarWidth) / 2 + 6; // just right of the last hotbar slot
                int ty = sh - f.lineHeight - 3; // bottom of the screen
                gg.text(f, timer, tx, ty, 0xFF55FFFF, true);
            }
        }

        // Don't render the teleport hint if they don't have the effect
        if (!ChoiceEffects.hasEffect(player, "ender_teleport")) {
            return;
        }

        // Get all the necessary rendering components from the event and Minecraft instance
        GuiGraphicsExtractor guiGraphics = event.getGuiGraphics();
        Font font = mc.font;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // This logic remains the same: check cooldown and create the right text
        long cooldownEndTime = ChoiceEffects.getTeleportCooldown(player);
        long currentTime = System.currentTimeMillis();
        Component textToRender;

        Component keyName = Keybinds.TELEPORT_KEY.getTranslatedKeyMessage();
        String keyStr = keyName.getString();

        if (currentTime < cooldownEndTime) {
            long remainingMillis = cooldownEndTime - currentTime;
            int remainingSeconds = (int) Math.ceil(remainingMillis / 1000.0);
            textToRender = Component.literal("use " + keyStr + " to teleport - " + remainingSeconds + "s");
        } else {
            textToRender = Component.literal("use " + keyStr + " to teleport");
        }

        // Positioning logic also remains the same
        int hotbarWidth = 182;
        int textWidth = font.width(textToRender);
        int x = (screenWidth - hotbarWidth) / 2 - textWidth - 5;
        int y = screenHeight - font.lineHeight - 3;

        // Draw the text with a shadow
        guiGraphics.text(font, textToRender, x, y, 0xFFFFFFFF, true);
    }
}