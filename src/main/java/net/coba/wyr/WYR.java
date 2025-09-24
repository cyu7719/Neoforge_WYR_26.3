package net.coba.wyr;

import com.mojang.logging.LogUtils;
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
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import net.coba.wyr.loot.ModLootModifiers;
import net.minecraft.server.MinecraftServer;

@Mod(WYR.MODID)
public class WYR {
    public static final String MODID = "wouldyourather";

    private static MinecraftServer serverInstance;

    public static final Logger LOGGER = LogUtils.getLogger();

    public WYR(IEventBus modEventBus, ModContainer modContainer) {
        // --- Register logic that runs on BOTH client and server ---
        NeoForge.EVENT_BUS.register(new PromptManager());
        NeoForge.EVENT_BUS.register(new ChoiceEffects());
        NeoForge.EVENT_BUS.register(new PlayerChoiceTracker());

        ModItems.register(modEventBus);

        ModLootModifiers.register(modEventBus);

        Networking.register(modEventBus);

        ModSounds.register(modEventBus);

        // Register the main class itself for server events
        NeoForge.EVENT_BUS.register(this);

        // Add listeners for common setup events
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        if (FMLLoader.getDist().isClient()) {
            ClientOnlySetup.init(modEventBus);
            NeoForge.EVENT_BUS.register(new ClientPhasingHandler());
        }
    }

    public static MinecraftServer getServer() {
        return serverInstance;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        WyrCommands.register(event.getServer().getCommands().getDispatcher());
    }
}