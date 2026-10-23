package net.coba.wyr.mixin.common;

import net.coba.wyr.ChoiceEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.item.crafting.RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(
        method = "getRecipeFor",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void wyr_modifySmeltingTime(CallbackInfoReturnable<?> cir) {
        // This mixin might not be the right approach
        // We'll handle smelting speed differently
    }
}
