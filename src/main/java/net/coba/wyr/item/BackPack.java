package net.coba.wyr.item;

import net.coba.wyr.ModDataComponents;
import net.coba.wyr.inventory.BackpackMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BackPack extends Item {
    public BackPack(Properties pProperties) {
        // Apply the custom data component with an empty default value
        super(pProperties.component(ModDataComponents.BACKPACK_CONTENTS.get(), ItemContainerContents.EMPTY));
    }

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            ItemStack stack = pPlayer.getItemInHand(pUsedHand);
            serverPlayer.openMenu(new BackpackMenuProvider(stack));
        }
        return pLevel.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    // Helper method to open the backpack from anywhere (like a keybind)
    public static void openBackpack(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BackPack) {
                player.openMenu(new BackpackMenuProvider(stack));
                return; // Open the first backpack found
            }
        }
    }

    private static class BackpackMenuProvider implements MenuProvider {
        private final ItemStack backpackStack;

        public BackpackMenuProvider(ItemStack stack) {
            this.backpackStack = stack;
        }

        @Override
        public Component getDisplayName() {
            return this.backpackStack.getHoverName();
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
            return new BackpackMenu(pContainerId, pPlayerInventory, this.backpackStack);
        }
    }
}