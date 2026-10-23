package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatEvent;

public class ShaderChatHandler {

    @SubscribeEvent
    public void onClientChat(ClientChatEvent event) {
        String msg = event.getMessage().trim().toLowerCase();
        if (msg.equals("open shaders")) {
            event.setCanceled(true);
            Minecraft.getInstance().gui.setScreen(new ShaderTestScreen(Component.literal("Shaders")));
        }
    }
}
