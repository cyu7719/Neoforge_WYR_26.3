package net.coba.wyr.mixin.client;

import net.coba.wyr.ClientEffectCache;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/ClientInput;tick()V",
                    shift = At.Shift.AFTER
            )
    )
    private void wyr_disableJump(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer)(Object)this;
        boolean noJump = ClientEffectCache.hasNoJump();
        boolean noSprint = ClientEffectCache.hasEffect("resistance_ii");
        if (noJump || noSprint) {
            net.minecraft.world.entity.player.Input oldInput = player.input.keyPresses;
            player.input.keyPresses = new net.minecraft.world.entity.player.Input(
                    oldInput.forward(),
                    oldInput.backward(),
                    oldInput.left(),
                    oldInput.right(),
                    noJump ? false : oldInput.jump(),   // jump
                    oldInput.shift(),
                    noSprint ? false : oldInput.sprint() // sprint
            );
            // Also force-stop any sprint already in progress so the player
            // can't keep sprinting (matches the "no hunger bar" behaviour).
            if (noSprint && player.isSprinting()) {
                player.setSprinting(false);
            }
        }
    }
}