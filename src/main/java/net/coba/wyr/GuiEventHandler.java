// GuiEventHandler.java
package net.coba.wyr;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class GuiEventHandler {

    private static final Map<String, Integer> INTERVAL_MAP = Map.of(
            "Disabled", 0,
            "30s", 30 * 20,
            "1min", 60 * 20,
            "3min", 180 * 20,
            "5min", 300 * 20,
            "10min", 600 * 20
    );
    private static final List<String> INTERVAL_KEYS = List.of("Disabled", "30s", "1min", "3min", "5min", "10min");
    private static String currentInterval = INTERVAL_KEYS.get(0);
    private boolean buttonAdded = false;

    @SubscribeEvent
    public void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        Screen screen = event.getScreen();
        if (screen instanceof CraftingScreen || screen instanceof InventoryScreen) {
            if (ClientEffectCache.hasStackedLogs()) {
                var menuProvider = (net.minecraft.client.gui.screens.inventory.MenuAccess<?>) screen;
                AbstractContainerMenu menu = menuProvider.getMenu();
                if (menu.slots.size() > 1) {
                    Container craftSlots = menu.getSlot(1).container;
                    int axeCount = 0;
                    for (int i = 0; i < craftSlots.getContainerSize(); i++) {
                        if (craftSlots.getItem(i).isCorrectToolForDrops(Blocks.OAK_LOG.defaultBlockState())) axeCount++;
                    }
                    if (axeCount > 1) menu.getSlot(0).set(ItemStack.EMPTY);
                }
            }
            if (ClientEffectCache.hasStrengthII()) {
                var menuProvider = (net.minecraft.client.gui.screens.inventory.MenuAccess<?>) screen;
                AbstractContainerMenu menu = menuProvider.getMenu();

                if (menu.getSlot(0).hasItem() && menu.getSlot(0).getItem().is(Items.SHIELD)) {
                    menu.getSlot(0).set(ItemStack.EMPTY);
                }
            }
        }
    }

    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof CreateWorldScreen) buttonAdded = false;
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
                CycleButton<String> intervalButton = CycleButton.<String>builder(
                                Component::literal, () -> currentInterval
                        )
                        .withValues(INTERVAL_KEYS)
                        .withTooltip(value -> Tooltip.create(Component.literal("Rate at which you get prompted with Would You Rather choices during the gameplay")))
                        .create(
                                cheatsButton.getX(),
                                cheatsButton.getY() + cheatsButton.getHeight() + 8,
                                cheatsButton.getWidth(),
                                cheatsButton.getHeight(),
                                Component.literal("Prompt Interval"),
                                (btn, value) -> {
                                    currentInterval = value;
                                }
                        );

                try {
                    Method addRenderableWidgetMethod = Screen.class.getDeclaredMethod("addRenderableWidget", GuiEventListener.class);
                    addRenderableWidgetMethod.setAccessible(true);
                    addRenderableWidgetMethod.invoke(screen, intervalButton);
                } catch (Exception e) {
                    WYR.LOGGER.error("Could not add custom button to CreateWorldScreen", e);
                }
                buttonAdded = true;
            }
        }
    }

    @SubscribeEvent
    public void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        int intervalTicks = INTERVAL_MAP.getOrDefault(currentInterval, 0);
        ClientPacketDistributor.sendToServer(new PacketSetPromptInterval(intervalTicks));
    }
}