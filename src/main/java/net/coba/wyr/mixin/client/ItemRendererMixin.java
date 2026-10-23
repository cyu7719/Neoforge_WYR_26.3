package net.coba.wyr.mixin.client;

import net.coba.wyr.ClientEffectCache;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemModelResolver.class)
public class ItemRendererMixin {

    @ModifyVariable(
            method = "appendItemLayers",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private ItemStack wyr_scrambleItemModel(ItemStack originalStack) {
        if (ClientEffectCache.hasItemTextureScramble() && !originalStack.isEmpty()) {
            return new ItemStack(ClientEffectCache.getRandomizedItem(originalStack.getItem()), originalStack.getCount());
        }
        return originalStack;
    }
}