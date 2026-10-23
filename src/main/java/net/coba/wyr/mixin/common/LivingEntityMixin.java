package net.coba.wyr.mixin.common;

import net.coba.wyr.ChoiceEffects;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * water_walk: lets the player stand on water and lava surfaces by reporting
 * that they can stand on those fluids. This makes the fluid act as solid
 * ground for movement and the sneak edge-check, so shift + direction keys
 * still move the player across the surface.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "canStandOnFluid", at = @At("HEAD"), cancellable = true)
    private void wyr$walkOnFluid(FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) return;
        if (!ChoiceEffects.hasEffect(player, "water_walk")) return;
        if (fluidState.is(FluidTags.WATER) || fluidState.is(FluidTags.LAVA)) {
            cir.setReturnValue(true);
        }
    }
}
