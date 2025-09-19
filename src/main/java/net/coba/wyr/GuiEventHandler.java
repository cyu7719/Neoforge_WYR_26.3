package net.coba.wyr;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Handles GUI-related events to inject custom widgets into screens.
 */
public class GuiEventHandler {

    private static final List<String> INTERVAL_VALUES = Arrays.asList("None", "30s", "1min", "3min", "5min", "10min");
    private static String currentInterval = INTERVAL_VALUES.get(0);
    private boolean buttonAdded = false;

    @SubscribeEvent
    public void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        Screen screen = event.getScreen();

        // Check if the screen is either a CraftingScreen (3x3) or InventoryScreen (2x2)
        if (screen instanceof CraftingScreen || screen instanceof InventoryScreen) {
            if (ClientEffectCache.hasStackedLogs()) {
                // Cast to a generic screen type to access the getMenu() method
                var menuProvider = (net.minecraft.client.gui.screens.inventory.MenuAccess<?>) screen;
                AbstractContainerMenu menu = menuProvider.getMenu();

                // In both menus, slot 1 is part of the crafting grid. Its container is the crafting inventory.
                // We check if there are enough slots to prevent crashing on smaller inventories.
                if (menu.slots.size() > 1) {
                    Container craftSlots = menu.getSlot(1).container;
                    int axeCount = 0;

                    // Loop through all slots within that specific crafting container
                    for (int i = 0; i < craftSlots.getContainerSize(); i++) {
                        if (craftSlots.getItem(i).getItem() instanceof AxeItem) {
                            axeCount++;
                        }
                    }

                    // If more than one axe is present, it's a repair. Block the recipe by clearing the output.
                    if (axeCount > 1) {
                        // Slot 0 is always the result slot in both menus.
                        menu.getSlot(0).set(ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof CreateWorldScreen) {
            buttonAdded = false;
        }
    }

    @SubscribeEvent
    public void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof CreateWorldScreen screen && !buttonAdded) {

            AbstractWidget cheatsButton = null;
            for (GuiEventListener child : screen.children()) {
                if (child instanceof AbstractWidget widget) {
                    if (widget.getMessage().getString().contains("Allow Commands")) {
                        cheatsButton = widget;
                        break;
                    }
                }
            }

            if (cheatsButton != null) {
                int buttonX = cheatsButton.getX();
                int verticalSpacing = 8;
                int buttonY = cheatsButton.getY() + cheatsButton.getHeight() + verticalSpacing;
                int buttonWidth = cheatsButton.getWidth();
                int buttonHeight = cheatsButton.getHeight();

                CycleButton<String> intervalButton = CycleButton.<String>builder(
                                (value) -> Component.literal(value)
                        )
                        .withValues(INTERVAL_VALUES)
                        .withInitialValue(currentInterval)
                        .create(
                                buttonX,
                                buttonY,
                                buttonWidth,
                                buttonHeight,
                                Component.literal("Prompt Interval: "),
                                (cycleButton, value) -> {
                                    currentInterval = value;
                                    PromptManager.setPromptInterval(value);
                                }
                        );

                try {
                    Method addRenderableWidgetMethod = Screen.class.getDeclaredMethod("addRenderableWidget", GuiEventListener.class);
                    addRenderableWidgetMethod.setAccessible(true);
                    addRenderableWidgetMethod.invoke(screen, intervalButton);
                } catch (Exception e) {
                    System.err.println("Could not add custom button to CreateWorldScreen via reflection.");
                    e.printStackTrace();
                }

                buttonAdded = true;
            }
        }
    }
}