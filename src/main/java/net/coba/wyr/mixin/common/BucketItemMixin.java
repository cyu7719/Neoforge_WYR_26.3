package net.coba.wyr.mixin.common;

import net.coba.wyr.ChoiceEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Allows water to be placed in the Nether while the nether_water_fast_lava
 * effect is active. Vanilla evaporates water in ultraWarm dimensions; this
 * mixin bypasses that check for affected players.
 */
@Mixin(BucketItem.class)
public abstract class BucketItemMixin {

    /**
     * Handle the whole use interaction for water buckets in ultraWarm
     * dimensions so the player can actually place water. This runs before
     * vanilla's evaporation logic and places the block directly.
     */
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void wyr$netherWaterUse(Level level, Player player, InteractionHand hand,
                                    CallbackInfoReturnable<InteractionResult> cir) {
        if (!ChoiceEffects.hasEffect(player, "nether_water_fast_lava")) return;
        // Only bypass evaporation in the Nether (WATER_EVAPORATES is a
        // positional attribute, so getDimensionValue() throws for it).
        if (level.dimension() != Level.NETHER) return;

        BucketItem self = (BucketItem) (Object) this;
        if (self.content != Fluids.WATER) return;

        BlockHitResult hit = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos target = hit.getBlockPos().relative(hit.getDirection());
        BlockState targetState = level.getBlockState(target);
        if (!(targetState.isAir() || targetState.canBeReplaced(Fluids.WATER))) {
            // Try the clicked position itself (e.g. replaceable plants).
            if (level.getBlockState(hit.getBlockPos()).canBeReplaced(Fluids.WATER)) {
                target = hit.getBlockPos();
            } else {
                return;
            }
        }

        if (!level.isClientSide()) {
            level.setBlock(target, Fluids.WATER.defaultFluidState().createLegacyBlock(), 11);
            // Play the nether evaporation fizz so it sounds like placing water
            // in the nether, not the normal overworld bucket-empty sound.
            level.playSound(null, target, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.5F,
                    2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F);
            if (!player.isCreative()) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            }
        }
        cir.setReturnValue(InteractionResult.SUCCESS);
    }

    @Inject(method = "emptyContents", at = @At("HEAD"), cancellable = true)
    private void wyr$allowNetherWater(LivingEntity entity, Level level, BlockPos pos, BlockHitResult hitResult,
                                      CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof Player player)) return;
        if (!ChoiceEffects.hasEffect(player, "nether_water_fast_lava")) return;
        if (level.dimension() != Level.NETHER) return;

        BucketItem self = (BucketItem) (Object) this;
        if (self.content != Fluids.WATER) return;

        // Determine the position the water should go into. emptyContents is
        // called with the target pos, but fall back to the block adjacent to
        // the clicked face if that pos isn't replaceable.
        BlockPos target = pos;
        BlockState current = level.getBlockState(target);
        if (!(current.canBeReplaced(Fluids.WATER) || current.isAir()) && hitResult != null) {
            BlockPos adj = hitResult.getBlockPos().relative(hitResult.getDirection());
            BlockState adjState = level.getBlockState(adj);
            if (adjState.canBeReplaced(Fluids.WATER) || adjState.isAir()) {
                target = adj;
                current = adjState;
            }
        }

        if (!(current.canBeReplaced(Fluids.WATER) || current.isAir())) return;

        // Tell both sides the empty succeeded so the client doesn't play the
        // evaporation fizz; place the actual water block on the server.
        cir.setReturnValue(true);
        if (!level.isClientSide()) {
            level.setBlock(target, Fluids.WATER.defaultFluidState().createLegacyBlock(), 11);
        }
    }
}
