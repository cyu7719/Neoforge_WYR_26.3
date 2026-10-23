package net.coba.wyr.item;

import net.coba.wyr.WYR;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, WYR.MODID);

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(WYR.MODID, name));
    }

    public static final DeferredHolder<Item, Item> UNLIMITED_SLICE_OF_PIZZA = ITEMS.register("unlimited_slice_of_pizza",
            () -> new UnlimitedPizzaItem(new Item.Properties().setId(key("unlimited_slice_of_pizza"))
                    .food(new FoodProperties.Builder().nutrition(3).saturationModifier(1.0f).build()).stacksTo(1)));

    // Unlimited_ice_cream_brain_freeze ---
    public static final DeferredHolder<Item, Item> UNLIMITED_ICE_CREAM = ITEMS.register("unlimited_ice_cream",
            () -> new UnlimitedIceCreamItem(new Item.Properties().setId(key("unlimited_ice_cream"))
                    .food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0f).build()).stacksTo(1)));

    // Pocket_teleporter_scramble ---
    public static final DeferredHolder<Item, Item> PERSONAL_TELEPORTER = ITEMS.register("pocket_teleporter",
            () -> new PersonalTeleporterItem(new Item.Properties().setId(key("pocket_teleporter")).stacksTo(1)));

    public static final DeferredHolder<Item, Item> BACKPACK = ITEMS.register("backpack",
            () -> new BackPack(new Item.Properties().setId(key("backpack")).stacksTo(1)));

    public static final DeferredHolder<Item, Item> UNLIMITED_SODA = ITEMS.register("unlimited_soda",
            () -> new UnlimitedSodaItem(new Item.Properties().setId(key("unlimited_soda")).stacksTo(1)
                    .component(DataComponents.CONSUMABLE, net.minecraft.world.item.component.Consumable.builder()
                            .animation(net.minecraft.world.item.ItemUseAnimation.DRINK)
                            .sound(net.minecraft.sounds.SoundEvents.GENERIC_DRINK)
                            .build())));


    // Golem_suit_slow ---
    public static final DeferredHolder<Item, Item> GOLEM_HELMET = ITEMS.register("golem_helmet",
            () -> new Item(new Item.Properties().setId(key("golem_helmet")).stacksTo(1)
                    .durability(ArmorType.HELMET.getDurability(ModArmorMaterials.GOLEM.value().durability()))
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD)
                            .setAsset(ModArmorMaterials.GOLEM.value().assetId())
                            .setEquipSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_NETHERITE)
                            .build())
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ModArmorMaterials.GOLEM.value().createAttributes(ArmorType.HELMET))));

    public static final DeferredHolder<Item, Item> GOLEM_CHESTPLATE = ITEMS.register("golem_chestplate",
            () -> new Item(new Item.Properties().setId(key("golem_chestplate")).stacksTo(1)
                    .durability(ArmorType.CHESTPLATE.getDurability(ModArmorMaterials.GOLEM.value().durability()))
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
                            .setAsset(ModArmorMaterials.GOLEM.value().assetId())
                            .setEquipSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_NETHERITE)
                            .build())
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ModArmorMaterials.GOLEM.value().createAttributes(ArmorType.CHESTPLATE))));

    public static final DeferredHolder<Item, Item> GOLEM_LEGGINGS = ITEMS.register("golem_leggings",
            () -> new Item(new Item.Properties().setId(key("golem_leggings")).stacksTo(1)
                    .durability(ArmorType.LEGGINGS.getDurability(ModArmorMaterials.GOLEM.value().durability()))
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.LEGS)
                            .setAsset(ModArmorMaterials.GOLEM.value().assetId())
                            .setEquipSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_NETHERITE)
                            .build())
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ModArmorMaterials.GOLEM.value().createAttributes(ArmorType.LEGGINGS))));

    public static final DeferredHolder<Item, Item> GOLEM_BOOTS = ITEMS.register("golem_boots",
            () -> new Item(new Item.Properties().setId(key("golem_boots")).stacksTo(1)
                    .durability(ArmorType.BOOTS.getDurability(ModArmorMaterials.GOLEM.value().durability()))
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.FEET)
                            .setAsset(ModArmorMaterials.GOLEM.value().assetId())
                            .setEquipSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_NETHERITE)
                            .build())
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ModArmorMaterials.GOLEM.value().createAttributes(ArmorType.BOOTS))));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}