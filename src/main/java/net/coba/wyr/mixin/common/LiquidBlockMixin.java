package net.coba.wyr.mixin.common;

import net.coba.wyr.ChoiceEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LiquidBlock.class)
public class LiquidBlockMixin {

    @Inject(
        method = "tick",
        at = @At("HEAD"),
        cancellable = true
    )
    private void wyr_preventWaterEvaporationTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        // Check if this is water in the Nether
        if (state.is(net.minecraft.world.level.block.Blocks.WATER) && level.dimension() == Level.NETHER) {
            // Find nearby player with the effect
            for (Player player : level.getEntitiesOfClass(Player.class, new net.minecraft.world.phys.AABB(pos).inflate(16))) {
                if (ChoiceEffects.hasEffect(player, "nether_water_fast_lava")) {
                    // Cancel the server tick to prevent evaporation
                    ci.cancel();
                    return;
                }
            }
        }
    }
}