package net.coba.wyr.mixin.client;

import net.coba.wyr.ClientEffectCache;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Mixin(SoundManager.class)
public class SoundManagerMixin {
    private final Random wyr_random = new Random();
    private List<SoundEvent> wyr_soundEvents = null;

    @ModifyVariable(
            method = "play",
            at = @At("HEAD"),
            argsOnly = true
    )
    private SoundInstance wyr_randomizeSound(SoundInstance originalInstance) {
        if (originalInstance == null) {
            return originalInstance;
        }
        Identifier location = originalInstance.getIdentifier();
        if (location == null) {
            return originalInstance;
        }
        String path = location.getPath();

        if (!ClientEffectCache.hasEffect("hero_of_village_random_sounds")) {
            return originalInstance;
        }

        // Get the original SoundEvent from the instance's location
        Optional<SoundEvent> originalSoundOpt = BuiltInRegistries.SOUND_EVENT.getOptional(location);

        // If we can't find the original sound in the registry, don't change it.
        if (originalSoundOpt.isEmpty()) {
            return originalInstance;
        }
        SoundEvent originalSound = originalSoundOpt.get();

        // Get the consistently mapped random sound from our cache
        SoundEvent randomSound = ClientEffectCache.getRandomizedSound(originalSound);

        // Safely get the base volume and pitch using the Accessor interface.
        float volume = 1.0F;
        float pitch = 1.0F;
        if (originalInstance instanceof AbstractSoundInstanceAccessor accessor) {
            volume = accessor.getVolume();
            pitch = accessor.getPitch();
        }

        // Create the new instance with the mapped sound
        return new SimpleSoundInstance(
                BuiltInRegistries.SOUND_EVENT.getResourceKey(randomSound).isPresent() ? 
                    BuiltInRegistries.SOUND_EVENT.getResourceKey(randomSound).get().identifier() : 
                    Identifier.fromNamespaceAndPath("minecraft", randomSound.location().getPath()),
                originalInstance.getSource(),
                volume,
                pitch,
                RandomSource.create(),
                originalInstance.isLooping(),
                originalInstance.getDelay(),
                originalInstance.getAttenuation(),
                originalInstance.getX(),
                originalInstance.getY(),
                originalInstance.getZ(),
                originalInstance.isRelative()
        );
    }
}
