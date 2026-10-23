package net.coba.wyr.mixin.client;

import net.coba.wyr.ChoiceEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {

    @Final private Player player;
    @Shadow @Final private Container enchantSlots;

    @Inject(method = "getEnchantmentList", at = @At("RETURN"), cancellable = true)
    private void wyr_injectPerfectEnchants(CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        ItemStack stack = this.enchantSlots.getItem(0);

        if (this.player != null && !stack.isEmpty() && ChoiceEffects.hasEffect(this.player, "perfect_enchants_destroy_chance")) {

            List<Holder<Enchantment>> possible = this.player.level().holderLookup(Registries.ENCHANTMENT).listElementIds()
                    .map(id -> this.player.level().holderLookup(Registries.ENCHANTMENT).get(id))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(holder -> holder.is(EnchantmentTags.IN_ENCHANTING_TABLE) && holder.value().canEnchant(stack))
                    .collect(Collectors.toList());

            if (possible.isEmpty()) {
                return;
            }

            Collections.shuffle(possible);
            List<EnchantmentInstance> perfectOffers = new ArrayList<>();
            Holder<Enchantment> enchantHolder = possible.get(0);

            perfectOffers.add(new EnchantmentInstance(enchantHolder, enchantHolder.value().getMaxLevel()));

            cir.setReturnValue(perfectOffers);
        }
    }
}