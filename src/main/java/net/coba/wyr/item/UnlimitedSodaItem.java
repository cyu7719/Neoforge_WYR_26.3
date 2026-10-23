package net.coba.wyr.item;

import net.coba.wyr.PacketSyncThirst;
import net.coba.wyr.WyrSavedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class UnlimitedSodaItem extends Item {

    public UnlimitedSodaItem(Properties pProperties) {
        super(pProperties);
    }

    // This method is called when the player finishes drinking.
    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (pLivingEntity instanceof Player player) {
            // Restore 0.5 hunger (1 hunger point)
            player.getFoodData().eat(1, 0.5F);

            // This logic only runs on the server to handle thirst
            if (!pLevel.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                WyrSavedData savedData = WyrSavedData.get((net.minecraft.server.level.ServerLevel) serverPlayer.level());

                // Get current thirst, add 5, and make sure it doesn't go over the max of 20.
                int currentThirst = savedData.getPlayerThirst(player.getUUID());
                int newThirst = Math.min(20, currentThirst + 5);
                savedData.setPlayerThirst(player.getUUID(), newThirst);

                PacketDistributor.sendToPlayer(serverPlayer, new net.coba.wyr.PacketSyncThirst(newThirst));
            }
        }
        // Return the original stack to prevent consumption
        return pStack;
    }

    // This sets how long the drinking animation lasts (32 ticks is the same as a water bottle).
    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity p_344588_) {
        return 32;
    }

    // This sets the animation to "drinking".
    @Override
    public ItemUseAnimation getUseAnimation(ItemStack pStack) {
        return ItemUseAnimation.DRINK;
    }

    // This makes the drinking sound play when you start drinking.
    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK.value();
    }

    // This is called when the player right-clicks with the item.
    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        return ItemUtils.startUsingInstantly(pLevel, pPlayer, pUsedHand);
    }
}