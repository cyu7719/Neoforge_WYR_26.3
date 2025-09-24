package net.coba.wyr.item;

import net.coba.wyr.WYR;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, WYR.MODID);

    public static final DeferredHolder<Item, Item> UNLIMITED_SLICE_OF_PIZZA = ITEMS.register("unlimited_slice_of_pizza",
            () -> new UnlimitedPizzaItem(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(12).saturationModifier(1.2f).build()).stacksTo(1)));

    // --- EFFECT: unlimited_ice_cream_brain_freeze ---
    public static final DeferredHolder<Item, Item> UNLIMITED_ICE_CREAM = ITEMS.register("unlimited_ice_cream",
            () -> new UnlimitedIceCreamItem(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(14).saturationModifier(0.5f).build()).stacksTo(1)));

    // --- EFFECT: personal_teleporter_scramble ---
    public static final DeferredHolder<Item, Item> PERSONAL_TELEPORTER = ITEMS.register("personal_teleporter",
            () -> new PersonalTeleporterItem(new Item.Properties().stacksTo(1)));


    // --- EFFECT: golem_suit_slow ---
    public static final DeferredHolder<Item, Item> GOLEM_HELMET = ITEMS.register("golem_helmet",
            () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredHolder<Item, Item> GOLEM_CHESTPLATE = ITEMS.register("golem_chestplate",
            () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredHolder<Item, Item> GOLEM_LEGGINGS = ITEMS.register("golem_leggings",
            () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredHolder<Item, Item> GOLEM_BOOTS = ITEMS.register("golem_boots",
            () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}