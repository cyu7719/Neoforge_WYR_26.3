package net.coba.wyr.mixin.common;

import net.coba.wyr.ChoiceEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets players sneak-walk across fluid surfaces while water_walk is active.
 * Vanilla's sneak edge-detection (maybeBackOffFromEdge) sees no solid block at
 * the edge of a fluid and stops movement; this skips that backoff when the
 * block below is water/lava so sneaking behaves like walking on land.
 */
@Mixin(Player.class)
public abstract class EntityMixin {

    @Inject(method = "maybeBackOffFromEdge", at = @At("HEAD"), cancellable = true)
    private void wyr$allowSneakOnFluid(Vec3 movement, net.minecraft.world.entity.MoverType moverType,
                                       CallbackInfoReturnable<Vec3> cir) {
        Player player = (Player) (Object) this;
        if (!ChoiceEffects.hasEffect(player, "water_walk")) return;
        if (!player.isShiftKeyDown()) return;

        // Only skip the edge backoff when the player is actually over a fluid
        // they can stand on — otherwise sneaking off real cliffs still works.
        Level level = player.level();
        BlockPos below = player.blockPosition().below();
        if (level.getFluidState(below).is(FluidTags.WATER)
                || level.getFluidState(below).is(FluidTags.LAVA)
                || level.getFluidState(player.blockPosition()).is(FluidTags.WATER)
                || level.getFluidState(player.blockPosition()).is(FluidTags.LAVA)) {
            cir.setReturnValue(movement);
        }
    }
}
