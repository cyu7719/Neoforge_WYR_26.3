package net.coba.wyr;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * This class handles client-side setup events, like registering keybindings.
 * The annotation ensures this code only runs on the client.
 */
public class ClientSetup {

    /**
     * This method is called during mod setup to register all keybindings.
     * @param event The event fired for key registration.
     */
    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        // We register our teleport key here so it appears in the Controls menu.
        event.register(Keybinds.TELEPORT_KEY);
    }
}