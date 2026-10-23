package net.coba.wyr.mixin.client;

import net.coba.wyr.ClientEffectCache;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Swaps the BlockStateModel (and particle material) returned for each block
 * state while texture_scramble is active. BlockStateModelSet.get() is the
 * single lookup used by chunk rendering (SectionCompiler), block entities,
 * and particles, so this one hook scrambles every block in the world.
 */
@Mixin(BlockStateModelSet.class)
public abstract class BlockStateModelSetMixin {

    @Shadow
    @Final
    private Map<BlockState, BlockStateModel> modelByState;

    @Shadow
    @Final
    private BlockStateModel missingModel;

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void wyr$scrambleModel(BlockState state, CallbackInfoReturnable<BlockStateModel> cir) {
        if (!ClientEffectCache.hasTextureScramble()) return;
        BlockState scrambled = ClientEffectCache.getRandomizedBlockState(state);
        if (scrambled != state) {
            cir.setReturnValue(modelByState.getOrDefault(scrambled, missingModel));
        }
    }

    @Inject(method = "getParticleMaterial", at = @At("HEAD"), cancellable = true)
    private void wyr$scrambleParticle(BlockState state, CallbackInfoReturnable<Material.Baked> cir) {
        if (!ClientEffectCache.hasTextureScramble()) return;
        BlockState scrambled = ClientEffectCache.getRandomizedBlockState(state);
        if (scrambled != state) {
            BlockStateModel model = modelByState.getOrDefault(scrambled, missingModel);
            cir.setReturnValue(model.particleMaterial());
        }
    }
}
