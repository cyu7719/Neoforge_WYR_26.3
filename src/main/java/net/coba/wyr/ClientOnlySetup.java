package net.coba.wyr;

import net.coba.wyr.gui.BackpackScreen;
import net.coba.wyr.inventory.BackpackMenu;
import net.coba.wyr.inventory.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * This class handles all client-side event registrations.
 * It's initialized from the main mod constructor only when the mod is running on the physical client.
 */
public class ClientOnlySetup {

    /**
     * Registers all client-side event handlers to their respective event buses.
     * @param modEventBus The mod-specific event bus.
     */
    public static void init(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(new GuiEventHandler());
        NeoForge.EVENT_BUS.register(new InputEventHandler());
        NeoForge.EVENT_BUS.register(new HudEventHandler());
        NeoForge.EVENT_BUS.register(new ThirstHudHandler());
        NeoForge.EVENT_BUS.register(new HungerBarHandler());
        NeoForge.EVENT_BUS.register(new BiomeLockHandler());

        NeoForge.EVENT_BUS.register(new ClientEffectsHandler());
        NeoForge.EVENT_BUS.register(new ClientTickHandler());
        NeoForge.EVENT_BUS.register(new ShaderChatHandler());

        NeoForge.EVENT_BUS.register(new ClientPhasingHandler());
        NeoForge.EVENT_BUS.register(new PromptManager());

    }
}