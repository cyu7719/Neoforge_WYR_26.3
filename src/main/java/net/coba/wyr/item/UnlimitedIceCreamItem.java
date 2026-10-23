package net.coba.wyr.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class UnlimitedIceCreamItem extends Item {
    public UnlimitedIceCreamItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            // Freeze in place for 10 seconds (200 ticks) with powdered snow effect
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 255));
            player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 200, 255));

            // Heal 6 hunger and saturation
            player.getFoodData().eat(6, 6.0F);
        }
        // Return the original stack to prevent consumption
        return stack;
    }
}