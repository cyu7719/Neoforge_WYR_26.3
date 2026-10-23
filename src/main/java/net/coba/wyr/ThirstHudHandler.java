package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public class ThirstHudHandler {

    private static final Identifier THIRST_EMPTY = Identifier.fromNamespaceAndPath(WYR.MODID, "textures/gui/thirst_empty.png");
    private static final Identifier THIRST_HALF = Identifier.fromNamespaceAndPath(WYR.MODID, "textures/gui/thirst_half.png");
    private static final Identifier THIRST_FULL = Identifier.fromNamespaceAndPath(WYR.MODID, "textures/gui/thirst_full.png");

    private static int playerThirst = 20;

    public static void setThirst(int thirst) {
        playerThirst = thirst;
    }

    @SubscribeEvent
    public void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.gui.hud.isHidden()) {
            return;
        }

        if (ChoiceEffects.hasEffect(player, "daily_diamond") && !player.isCreative() && !player.isSpectator()) {
            // Pass the player object to the render method
            renderThirstBar(event.getGuiGraphics(), player);
        }
    }

    private void renderThirstBar(GuiGraphicsExtractor guiGraphics, Player player) {
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int left = screenWidth / 2 + 91;
        int top;

        // --- THE FIX ---
        // Check if the player's air is less than max (i.e., the air bar is showing)
        if (player.getAirSupply() < player.getMaxAirSupply()) {
            // If underwater, move the thirst bar UP to make room for the vanilla air bar.
            top = screenHeight - 59;
        } else {
            // If not underwater, place the thirst bar in its normal spot right above the hunger bar.
            top = screenHeight - 49;
        }

        for (int i = 0; i < 10; ++i) {
            int x = left - i * 8 - 9;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, THIRST_EMPTY, x, top, 0.0F, 0.0F, 9, 9, 9, 9);

            if (playerThirst > 0) {
                if (i * 2 + 1 < playerThirst) {
                    guiGraphics.blit(RenderPipelines.GUI_TEXTURED, THIRST_FULL, x, top, 0.0F, 0.0F, 9, 9, 9, 9);
                } else if (i * 2 + 1 == playerThirst) {
                    guiGraphics.blit(RenderPipelines.GUI_TEXTURED, THIRST_HALF, x, top, 0.0F, 0.0F, 9, 9, 9, 9);
                }
            }
        }
    }
}