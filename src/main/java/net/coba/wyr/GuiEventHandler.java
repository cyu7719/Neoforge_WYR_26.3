package net.coba.wyr;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
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