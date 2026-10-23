package net.coba.wyr;

import com.mojang.logging.LogUtils;
import net.coba.wyr.inventory.ModMenuTypes;
import net.coba.wyr.item.ModArmorMaterials;
import net.coba.wyr.item.ModItems;
import net.coba.wyr.sound.ModSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;
import net.minecraft.server.MinecraftServer;

@Mod(WYR.MODID)
public class WYR {
    public static final String MODID = "wouldyourather";

    private static MinecraftServer serverInstance;

    public static final Logger LOGGER = LogUtils.getLogger();

    public WYR(IEventBus modEventBus, ModContainer modContainer) {
        // --- Register logic that runs on BOTH client and server ---
        NeoForge.EVENT_BUS.register(new ChoiceEffects());
        NeoForge.EVENT_BUS.register(new PlayerChoiceTracker());

        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        ModArmorMaterials.register(modEventBus);

        Networking.register(modEventBus);

        ModSounds.register(modEventBus);

        ClientPopupManager.init();
        ClientHangWatchdog.init();

        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
    }

    public static MinecraftServer getServer() {
        return serverInstance;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        WyrCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        serverInstance = event.getServer();
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        serverInstance = null;
    }
}