package net.coba.wyr.mixin.client;

import net.coba.wyr.ClientEffectCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ModelBlockRenderer.class)
public class BlockRenderDispatcherMixin {

    @Unique
    private BlockState wyr$scrambledState;

    // Capture the scrambled state first so the model handler can resolve the
    // matching model. getRandomizedBlockState is deterministic (cached), so the
    // same original state always maps to the same scrambled state.
    @ModifyVariable(
            method = "tesselateBlock",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private BlockState wyr_scrambleBlockState(BlockState originalState) {
        if (ClientEffectCache.hasTextureScramble()) {
            wyr$scrambledState = ClientEffectCache.getRandomizedBlockState(originalState);
            return wyr$scrambledState;
        }
        wyr$scrambledState = null;
        return originalState;
    }

    // The caller already resolved the BlockStateModel for the ORIGINAL state, so
    // we must swap it for the scrambled state's model or the texture won't change.
    @ModifyVariable(
            method = "tesselateBlock",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private BlockStateModel wyr_scrambleBlockModel(BlockStateModel originalModel) {
        if (wyr$scrambledState != null) {
            return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(wyr$scrambledState);
        }
        return originalModel;
    }
}