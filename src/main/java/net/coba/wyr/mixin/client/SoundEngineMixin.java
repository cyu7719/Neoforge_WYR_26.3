package net.coba.wyr.mixin.client;

import net.coba.wyr.ClientEffectCache;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {

    private static boolean wyr$isMuted() {
        return ClientEffectCache.isMuted() || ClientEffectCache.hasEffect("fast_swim_muted");
    }

    @Inject(method = "calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F", at = @At("HEAD"), cancellable = true)
    private void wyr_muteSound(net.minecraft.client.resources.sounds.SoundInstance soundInstance, CallbackInfoReturnable<Float> cir) {
        if (wyr$isMuted()) {
            cir.setReturnValue(0.0F);
        }
    }

    /**
     * Mute ALL sounds (including music/ambient that may not respect volume 0)
     * by refusing to start them at the engine entry point.
     */
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void wyr_skipSound(net.minecraft.client.resources.sounds.SoundInstance soundInstance,
                               CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (wyr$isMuted()) {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
        }
    }
}