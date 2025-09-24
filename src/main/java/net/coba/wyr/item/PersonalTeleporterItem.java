// Corrected PersonalTeleporterItem.java
package net.coba.wyr.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries; // ERROR FIX: Add this import
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey; // ERROR FIX: Add this import
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PersonalTeleporterItem extends Item {
    public PersonalTeleporterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (player.isCrouching()) {
            // Set position and dimension
            tag.put("TeleportPos", NbtUtils.writeBlockPos(player.blockPosition()));
            tag.putString("Dimension", level.dimension().location().toString()); // Store dimension ID
            player.displayClientMessage(Component.literal("Teleport point set!"), true);
            level.playSound(null, player.blockPosition(), SoundEvents.ENDER_PEARL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            // Use position
            Optional<BlockPos> posOptional = NbtUtils.readBlockPos(tag, "TeleportPos");
            if (posOptional.isPresent() && tag.contains("Dimension")) {
                BlockPos pos = posOptional.get();
                ResourceLocation dimId = ResourceLocation.parse(tag.getString("Dimension"));

                if (player instanceof ServerPlayer serverPlayer && serverPlayer.getServer() != null) {
                    // ERROR FIX: This is the modern way to get a dimension's key and then the level
                    ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, dimId);
                    ServerLevel targetLevel = serverPlayer.getServer().getLevel(levelKey);

                    if (targetLevel != null) {
                        serverPlayer.teleportTo(targetLevel, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, player.getYRot(), player.getXRot());
                        scrambleInventory(serverPlayer);
                        // Play sound in the original level before teleporting
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            } else {
                player.displayClientMessage(Component.literal("No teleport point set. Sneak-right-click to set."), true);
            }
        }

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return InteractionResultHolder.success(stack);
    }

    private void scrambleInventory(Player player) {
        List<ItemStack> inventoryItems = IntStream.range(0, player.getInventory().getContainerSize())
                .mapToObj(player.getInventory()::getItem)
                .collect(Collectors.toList());

        Collections.shuffle(inventoryItems);

        for (int i = 0; i < inventoryItems.size(); i++) {
            player.getInventory().setItem(i, inventoryItems.get(i));
        }
    }
}