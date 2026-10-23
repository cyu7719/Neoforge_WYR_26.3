package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Handles custom hunger bar rendering for choice effects that modify hunger display.
 */
public class HungerBarHandler {
    
    private static final Identifier GUI_ICONS = Identifier.withDefaultNamespace("textures/gui/icons.png");
    
    // Food icon sprites (1.21.x HUD sprites)
    private static final Identifier FOOD_EMPTY = Identifier.withDefaultNamespace("hud/food_empty");
    private static final Identifier FOOD_HALF = Identifier.withDefaultNamespace("hud/food_half");
    private static final Identifier FOOD_FULL = Identifier.withDefaultNamespace("hud/food_full");
    private static final Identifier FOOD_EMPTY_HUNGER = Identifier.withDefaultNamespace("hud/food_empty_hunger");
    private static final Identifier FOOD_HALF_HUNGER = Identifier.withDefaultNamespace("hud/food_half_hunger");
    private static final Identifier FOOD_FULL_HUNGER = Identifier.withDefaultNamespace("hud/food_full_hunger");

    @SubscribeEvent
    public void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        // When the half-hunger effect is active, replace the vanilla 10-slot food
        // bar with a 5-slot bar so no empty drumsticks are shown.
        if (!VanillaGuiLayers.FOOD_LEVEL.equals(event.getName())) return;
        if (!ClientEffectCache.hasHalfHunger()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        // Creative/spectator players have no hunger bar in vanilla — keep it hidden.
        if (player.isCreative() || player.isSpectator()) return;

        event.setCanceled(true);
        renderCustomHungerBar(event.getGuiGraphics(), player);
    }

    private void renderCustomHungerBar(net.minecraft.client.gui.GuiGraphicsExtractor guiGraphics, Player player) {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int top = screenHeight - 39;
        int foodLevel = player.getFoodData().getFoodLevel();
        boolean hasHungerEffect = player.hasEffect(MobEffects.HUNGER);

        Identifier empty = hasHungerEffect ? FOOD_EMPTY_HUNGER : FOOD_EMPTY;
        Identifier half = hasHungerEffect ? FOOD_HALF_HUNGER : FOOD_HALF;
        Identifier full = hasHungerEffect ? FOOD_FULL_HUNGER : FOOD_FULL;

        // Render 5 slots (right to left), each representing 2 food points (max 10).
        for (int i = 0; i < 5; i++) {
            int x = screenWidth / 2 + 82 - i * 8;
            guiGraphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, empty, x, top, 9, 9);

            int slotIndex = 4 - i;
            int requiredLevel = slotIndex * 2 + 1;

            if (foodLevel >= requiredLevel + 1) {
                guiGraphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, full, x, top, 9, 9);
            } else if (foodLevel == requiredLevel) {
                guiGraphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, half, x, top, 9, 9);
            }
        }
    }
}
