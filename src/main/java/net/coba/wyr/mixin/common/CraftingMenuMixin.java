package net.coba.wyr.mixin.common;

import net.coba.wyr.ChoiceEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents the "repair two damaged items into one" recipe from producing a
 * repaired mace in the crafting grid while the mace_max_damage_weak effect is
 * active. The result slot is cleared before the player can take it.
 */
@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

    @Inject(method = "slotChangedCraftingGrid", at = @At("TAIL"))
    private static void wyr$blockMaceRepair(AbstractContainerMenu menu, ServerLevel level, Player player,
                                            CraftingContainer craftSlots, ResultContainer resultSlots,
                                            RecipeHolder<CraftingRecipe> recipe, CallbackInfo ci) {
        if (!ChoiceEffects.hasEffect(player, "mace_max_damage_weak")) return;

        ItemStack result = resultSlots.getItem(0);
        if (!result.is(Items.MACE)) return;

        // Count maces in the crafting grid - the repair recipe uses exactly 2.
        int maceCount = 0;
        for (int i = 0; i < craftSlots.getContainerSize(); i++) {
            if (craftSlots.getItem(i).is(Items.MACE)) {
                maceCount++;
            }
        }
        if (maceCount >= 2) {
            resultSlots.setItem(0, ItemStack.EMPTY);
        }
    }
}
