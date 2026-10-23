package net.coba.wyr.mixin.common;

import net.coba.wyr.ChoiceEffects;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    @Shadow public final NonNullList<Slot> slots = NonNullList.create();

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void wyr_preventTotemInteract(int slotId, int button, ContainerInput containerInput, Player player, CallbackInfo ci) {
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                if (customData != null && customData.copyTag().getBooleanOr("wyr_locked_totem", false)) {
                    if (ChoiceEffects.hasEffect(player, "daily_totem_no_armor")) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}
