package net.coba.wyr.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class UnlimitedIceCreamItem extends Item {
    public UnlimitedIceCreamItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            // Apply brain freeze effects
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 255)); // 10 seconds, cannot move
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 200, 128)); // Negative jump boost to prevent jumping
            player.hurt(player.damageSources().freeze(), 2.0F);

            // Manually apply food stats
            FoodProperties foodProperties = this.getFoodProperties(stack, player);
            if (foodProperties != null) {
                player.getFoodData().eat(foodProperties.nutrition(), foodProperties.saturation());
            }

            // You would send a packet to the client here to trigger the powder snow overlay effect
            // PacketDistributor.sendToPlayer((ServerPlayer) player, new PacketApplyBrainFreezeOverlay());
        }
        // Return the original stack to prevent consumption
        return stack;
    }
}