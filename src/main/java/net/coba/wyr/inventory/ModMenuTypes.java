package net.coba.wyr.inventory;

import net.coba.wyr.WYR;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, WYR.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<BackpackMenu>> BACKPACK_MENU =
            MENUS.register("backpack_menu",
                    () -> new MenuType<>(BackpackMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS)
            );

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}