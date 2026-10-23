package net.coba.wyr;

import net.coba.wyr.gui.BackpackScreen;
import net.coba.wyr.inventory.BackpackMenu;
import net.coba.wyr.inventory.ModMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = WYR.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        IEventBus modEventBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        ClientOnlySetup.init(modEventBus);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(Keybinds.TELEPORT_KEY);
        event.register(Keybinds.PROMPT_LIST_KEY);
        event.register(Keybinds.OPEN_BACKPACK_KEY);
    }

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.BACKPACK_MENU.get(), (BackpackMenu menu, Inventory inv, Component title) -> new BackpackScreen(menu, inv, title));
    }
}