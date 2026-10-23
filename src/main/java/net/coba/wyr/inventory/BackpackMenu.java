package net.coba.wyr.inventory;

import net.coba.wyr.ModDataComponents;
import net.coba.wyr.item.BackPack;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;

public class BackpackMenu extends AbstractContainerMenu {
    private static final int ROWS = 6;
    private static final int COLUMNS = 9;
    private final ItemStack backpackStack;
    private final Container backpackInventory;

    // Standard constructor for standard MenuType registration
    public BackpackMenu(int pContainerId, Inventory playerInventory) {
        this(pContainerId, playerInventory, findBackpackInPlayerInv(playerInventory.player));
    }

    // This is the main constructor for the server and client
    public BackpackMenu(int pContainerId, Inventory playerInventory, ItemStack backpackStack) {
        super(ModMenuTypes.BACKPACK_MENU.get(), pContainerId);
        this.backpackStack = backpackStack;
        int containerSize = ROWS * COLUMNS;

        // Create a temporary list to load the items into
        NonNullList<ItemStack> items = NonNullList.withSize(containerSize, ItemStack.EMPTY);
        // Load items from the backpack's custom data component
        ItemContainerContents contents = this.backpackStack.get(ModDataComponents.BACKPACK_CONTENTS.get());
        if (contents != null) {
            contents.copyInto(items);
        }

        // Create a standard, stable SimpleContainer from the item list
        this.backpackInventory = new SimpleContainer(items.toArray(new ItemStack[0]));

        // Backpack Slots (using the standard vanilla Slot class)
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLUMNS; ++col) {
                this.addSlot(new Slot(this.backpackInventory, col + row * COLUMNS, 8 + col * 18, 18 + row * 18));
            }
        }

        // Player Inventory & Hotbar Slots
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 198));
        }
    }

    private static ItemStack findBackpackInPlayerInv(Player player) {
        if (player.getMainHandItem().getItem() instanceof BackPack) return player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof BackPack) return player.getOffhandItem();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BackPack) return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();
            int backpackSlotCount = ROWS * COLUMNS;
            if (pIndex < backpackSlotCount) {
                if (!this.moveItemStackTo(slotStack, backpackSlotCount, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(slotStack, 0, backpackSlotCount, false)) return ItemStack.EMPTY;
            }
            if (slotStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        List<ItemStack> itemsToSave = new ArrayList<>();
        for (int i = 0; i < this.backpackInventory.getContainerSize(); i++) {
            itemsToSave.add(this.backpackInventory.getItem(i));
        }
        this.backpackStack.set(ModDataComponents.BACKPACK_CONTENTS.get(), ItemContainerContents.fromItems(itemsToSave));
    }
}