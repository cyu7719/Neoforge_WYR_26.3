package net.coba.wyr.loot;

import com.mojang.serialization.MapCodec;
import net.coba.wyr.WYR;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder; // Make sure this import is present and not commented out

public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, WYR.MODID);

    // --- THIS IS THE CORRECTED LINE ---
    // Replaced 'var' with the full, explicit DeferredHolder type
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>> CUSTOM_BLOCK_DROPS =
            LOOT_MODIFIER_SERIALIZERS.register("custom_block_drops", () -> CustomBlockLootModifier.CODEC);

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(eventBus);
    }
}