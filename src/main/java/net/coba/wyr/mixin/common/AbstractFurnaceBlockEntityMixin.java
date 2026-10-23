package net.coba.wyr.mixin.common;

import net.coba.wyr.ChoiceEffects;
import net.coba.wyr.util.FurnaceOwnerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin implements FurnaceOwnerAccess {

    @Accessor("cookingTotalTime")
    abstract void wyr$setCookingTotalTime(int value);

    @Accessor("cookingTimer")
    abstract int wyr$getCookingProgress();

    /**
     * The player who "owns" this furnace — set when they place the block or
     * open the menu. fast_smelt_damage only applies to the owner's furnace so
     * the effect stays per-player in multiplayer.
     */
    @Unique
    private UUID wyr$ownerUuid;

    @Override
    public UUID wyr$getOwner() {
        return wyr$ownerUuid;
    }

    @Override
    public void wyr$setOwner(UUID uuid) {
        wyr$ownerUuid = uuid;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void wyr$saveOwner(ValueOutput output, CallbackInfo ci) {
        if (wyr$ownerUuid != null) {
            output.putString("WyrOwner", wyr$ownerUuid.toString());
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void wyr$loadOwner(ValueInput input, CallbackInfo ci) {
        wyr$ownerUuid = input.getString("WyrOwner").map(s -> {
            try {
                return UUID.fromString(s);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }).orElse(null);
    }

    /**
     * Resolve the owning player (if online) and check whether they have the
     * fast_smelt_damage effect.
     */
    @Unique
    private static Player wyr$ownerWithEffect(ServerLevel level, AbstractFurnaceBlockEntity blockEntity) {
        UUID owner = ((FurnaceOwnerAccess) blockEntity).wyr$getOwner();
        if (owner == null) return null;
        Player player = level.getPlayerByUUID(owner);
        if (player != null && ChoiceEffects.hasEffect(player, "fast_smelt_damage")) {
            return player;
        }
        return null;
    }

    @Inject(
        method = "serverTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;burn(Lnet/minecraft/core/NonNullList;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
            shift = At.Shift.AFTER
        )
    )
    private static void wyr_onSmeltComplete(ServerLevel level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        // Only the furnace's owner takes the half-heart damage.
        Player owner = wyr$ownerWithEffect(level, blockEntity);
        if (owner != null) {
            owner.hurt(owner.damageSources().magic(), 0.5F);
        }
    }

    /**
     * fast_smelt_damage: items smelt in 1 second (20 ticks) instead of the
     * recipe's normal cook time — but only in a furnace owned by a player who
     * has the effect. We force cookingTotalTime every tick (not just when a
     * recipe starts) so ALL items in the queue smelt fast, not just the first.
     */
    @Inject(method = "serverTick", at = @At("HEAD"))
    private static void wyr_forceFastCook(ServerLevel level, BlockPos pos, BlockState state,
                                          AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        if (wyr$ownerWithEffect(level, blockEntity) == null) return;
        AbstractFurnaceBlockEntityMixin self = (AbstractFurnaceBlockEntityMixin) (Object) blockEntity;
        // Only clamp while actively cooking (progress > 0) so we don't
        // interfere with the idle state.
        if (self.wyr$getCookingProgress() > 0) {
            self.wyr$setCookingTotalTime(20);
        }
    }

}
