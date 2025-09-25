package net.coba.wyr;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static class Items {
        // Define the custom tag key for decoration items
        public static final TagKey<Item> DECORATION_ITEMS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(WYR.MODID, "decoration_items"));
    }
}