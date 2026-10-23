package net.coba.wyr.mixin.common;

import net.minecraft.world.entity.player.Inventory;
import net.coba.wyr.util.FurnaceOwnerAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Records the player who opens a furnace menu as that furnace's owner, so
 * fast_smelt_damage stays per-player: whoever last opened the furnace is the
 * one whose effect (and damage) applies to it.
 */
@Mixin(BaseContainerBlockEntity.class)
public abstract class BaseContainerBlockEntityMixin {

    
    @Inject(method = "createMenu(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/inventory/AbstractContainerMenu;",
            at = @At("HEAD"))
    private void wyr$trackOpener(int containerId, Inventory inventory, Player player,
                               CallbackInfoReturnable<AbstractContainerMenu> cir) {
        if ((Object) this instanceof AbstractFurnaceBlockEntity furnace) {
            ((FurnaceOwnerAccess) furnace).wyr$setOwner(player.getUUID());
        }
    }
}
