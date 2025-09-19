package net.coba.wyr.item;

import net.coba.wyr.WYR;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, WYR.MODID);

    // Cooked Beef: 8 hunger, 0.8 saturation modifier
    // 1.5x Hunger = 12
    // 1.5x Saturation Modifier = 1.2
    public static final DeferredHolder<Item, Item> UNLIMITED_SLICE_OF_PIZZA = ITEMS.register("unlimited_slice_of_pizza",
            () -> new UnlimitedPizzaItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(12)
                            .saturationModifier(1.2f)
                            .build())
                    .stacksTo(1) // Can't be stacked
            ));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}