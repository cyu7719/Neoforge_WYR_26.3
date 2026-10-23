package net.coba.wyr.mixin.client;

import com.mojang.logging.LogUtils;
import net.coba.wyr.ChoiceEffects;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

    // ++ ADD THIS LOGGER ++
    private static final Logger LOGGER = LogUtils.getLogger();

    @Shadow public abstract Player getPlayerOwner();

    @Redirect(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"
            )
    )
    private boolean wyr_redirectAllFluidChecks(FluidState instance, TagKey<Fluid> tag) {
        // ++ ADD THIS DEBUG MESSAGE ++
        LOGGER.info("[WYR DEBUG] FishingHook Mixin Fired! Game is checking for tag: {}", tag.location());

        Player player = this.getPlayerOwner();

        if (player != null && ChoiceEffects.hasEffect(player, "lava_fishing_rod") && instance.is(FluidTags.LAVA)) {
            // ++ ADD THIS DEBUG MESSAGE ++
            LOGGER.info("[WYR DEBUG] Player has effect and bobber is in lava. Overriding check...");

            if (tag == FluidTags.WATER) {
                LOGGER.info("[WYR DEBUG] -> Game asked for WATER, returning TRUE");
                return true;
            }
            if (tag == FluidTags.LAVA) {
                LOGGER.info("[WYR DEBUG] -> Game asked for LAVA, returning FALSE");
                return false;
            }
        }

        // Let the original check run.
        return instance.is(tag);
    }
}