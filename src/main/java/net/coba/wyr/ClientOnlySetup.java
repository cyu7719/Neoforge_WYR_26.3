package net.coba.wyr;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

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
        modEventBus.addListener((final RegisterKeyMappingsEvent event) -> {
            event.register(Keybinds.TELEPORT_KEY);
            event.register(Keybinds.PROMPT_LIST_KEY);
        });

        NeoForge.EVENT_BUS.register(new GuiEventHandler());
        NeoForge.EVENT_BUS.register(new InputEventHandler());
        NeoForge.EVENT_BUS.register(new HudEventHandler());
        NeoForge.EVENT_BUS.register(new ThirstHudHandler());

        NeoForge.EVENT_BUS.register(new ClientEffectsHandler());
    }
}