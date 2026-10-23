package net.coba.wyr.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.coba.wyr.ChoiceEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class ItemMixin {

    /**
     * This Mixin uses @ModifyReturnValue to change the result of the getUseAnimation method.
     */
    @ModifyReturnValue(method = "getUseAnimation", at = @At("RETURN"))
    private ItemUseAnimation wyr_showEatingAnimation(ItemUseAnimation original, ItemStack stack) {
        Player player = Minecraft.getInstance().player;

        if (player != null && ChoiceEffects.hasEffect(player, "eat_anything_hunger_chance") && stack.get(DataComponents.FOOD) == null) {
            return ItemUseAnimation.EAT;
        }

        return original;
    }
}