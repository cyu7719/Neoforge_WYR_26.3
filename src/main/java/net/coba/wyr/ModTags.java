package net.coba.wyr;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static class Items {
        // Defines the custom tag key for all decoration items
        public static final TagKey<Item> DECORATION_ITEMS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(WYR.MODID, "decoration_items"));
    }
}