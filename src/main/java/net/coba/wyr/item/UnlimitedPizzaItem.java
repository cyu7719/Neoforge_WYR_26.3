package net.coba.wyr.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class UnlimitedPizzaItem extends Item {

    public UnlimitedPizzaItem(Properties properties) {
        super(properties);
    }

    /**
     * This method is called when the player finishes eating the food.
     * We now manually apply the food effects and return the stack
     * without calling the superclass method, which would consume the item.
     */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            // Restore 3 hunger and saturation
            player.getFoodData().eat(3, 3.0F);
        }
        // Return the original stack to prevent it from being consumed.
        return stack;
    }
}