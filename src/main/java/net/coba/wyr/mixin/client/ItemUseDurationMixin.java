package net.coba.wyr.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.coba.wyr.ChoiceEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class ItemUseDurationMixin {

    /**
     * This Mixin uses @ModifyReturnValue to change the result of the getUseDuration method.
     * The use duration determines how long the eating animation plays.
     */
    @ModifyReturnValue(method = "getUseDuration", at = @At("RETURN"))
    private int wyr_setEatingSpeed(int original, ItemStack stack) {
        Player player = Minecraft.getInstance().player;

        if (player != null && ChoiceEffects.hasEffect(player, "eat_anything_hunger_chance") && stack.get(DataComponents.FOOD) == null) {
            return 32;
        }

        return original;
    }
}