package net.coba.wyr.mixin.client;

import net.coba.wyr.ModDataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow @Final @Mutable
    private static StreamCodec<RegistryFriendlyByteBuf, ItemStack> STREAM_CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void wyr_overwriteStreamCodec(CallbackInfo ci) {
        STREAM_CODEC = ModDataComponents.OVERSIZED_STACK_STREAM_CODEC;
    }
}