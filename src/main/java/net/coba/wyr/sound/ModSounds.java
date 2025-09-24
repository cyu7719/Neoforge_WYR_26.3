package net.coba.wyr.sound;

import net.coba.wyr.WYR;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, WYR.MODID);

    // Register SoundEvents that match the names in your sounds.json
    public static final DeferredHolder<SoundEvent, SoundEvent> EMERALDS_SIREN =
            registerSoundEvent("emeralds_siren");

    public static final DeferredHolder<SoundEvent, SoundEvent> creeper_hiss =
            registerSoundEvent("tame_hiss");

    public static final DeferredHolder<SoundEvent, SoundEvent> auto_smelt_knock =
            registerSoundEvent("auto_smelt_knock");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(WYR.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}