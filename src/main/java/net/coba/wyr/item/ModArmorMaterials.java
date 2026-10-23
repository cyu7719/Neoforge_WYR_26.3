package net.coba.wyr.item;

import net.coba.wyr.WYR;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.IEventBus;

import java.util.EnumMap;

public class ModArmorMaterials {

    // Register the GOLEM material using Holder.direct since ArmorMaterials are no longer registered via DeferredRegister in 26.1
    public static final Holder<ArmorMaterial> GOLEM = Holder.direct(createGolemMaterial());

    private static ArmorMaterial createGolemMaterial() {
        EnumMap<ArmorType, Integer> protection = new EnumMap<>(ArmorType.class);
        protection.put(ArmorType.BOOTS, 3);
        protection.put(ArmorType.LEGGINGS, 6);
        protection.put(ArmorType.CHESTPLATE, 8);
        protection.put(ArmorType.HELMET, 3);
        protection.put(ArmorType.BODY, 0);

        return new ArmorMaterial(37, protection, 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F,
                ItemTags.REPAIRS_IRON_ARMOR,
                ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(WYR.MODID, "golem")));
    }

    public static void register(IEventBus eventBus) {
        // Empty in 26.1/1.21.4 since ArmorMaterials are no longer registered
    }
}
