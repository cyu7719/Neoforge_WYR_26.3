// Corrected PersonalTeleporterItem.java
package net.coba.wyr.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries; // ERROR FIX: Add this import
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey; // ERROR FIX: Add this import
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (player.isCrouching()) {
            // Set position and dimension - only on server side
            if (!level.isClientSide()) {
                tag.putIntArray("TeleportPos", new int[]{player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ()});
                String dimensionKey = level.dimension().identifier().toString();
                tag.putString("Dimension", dimensionKey);
                player.sendSystemMessage(Component.literal("Teleport point set in " + dimensionKey + "!"));
                level.playSound(null, player.blockPosition(), SoundEvents.ENDER_PEARL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

                // Apply the glint effect
                stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }

        } else {
            // Use position
            if (tag.contains("TeleportPos") && tag.contains("Dimension")) {
                int[] posArray = tag.getIntArray("TeleportPos").orElse(new int[0]);
                BlockPos pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
                String dimString = tag.getString("Dimension").orElse("");

                if (player instanceof ServerPlayer serverPlayer && !dimString.isEmpty() && !level.isClientSide()) {
                    // Debug: Print the dimension string to see what format it's in
                    serverPlayer.sendSystemMessage(Component.literal("Debug: Dimension string = '" + dimString + "'"));
                    
                    // Try to parse the dimension - it should be in format like "minecraft:overworld"
                    try {
                        ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, Identifier.tryParse(dimString));
                        ServerLevel targetLevel = serverPlayer.level().getServer().getLevel(levelKey);
                        
                        // Fallback: if dimension lookup failed, try to find it by comparing with current dimension
                        if (targetLevel == null) {
                            serverPlayer.sendSystemMessage(Component.literal("Debug: Primary lookup failed, trying fallback..."));
                            
                            // Try common dimension keys
                            ResourceKey<Level>[] commonDimensions = new ResourceKey[]{
                                Level.OVERWORLD,
                                Level.NETHER,
                                Level.END
                            };
                            
                            for (ResourceKey<Level> dim : commonDimensions) {
                                if (dim.identifier().toString().equals(dimString)) {
                                    targetLevel = serverPlayer.level().getServer().getLevel(dim);
                                    serverPlayer.sendSystemMessage(Component.literal("Debug: Found dimension via fallback: " + dim.identifier()));
                                    break;
                                }
                            }
                            
                            // Last resort: teleport to current dimension if it matches
                            if (targetLevel == null && serverPlayer.level().dimension().identifier().toString().equals(dimString)) {
                                targetLevel = serverPlayer.level();
                                serverPlayer.sendSystemMessage(Component.literal("Debug: Using current dimension as fallback"));
                            }
                        }
                        
                        if (targetLevel != null) {
                            serverPlayer.teleportTo(targetLevel, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, java.util.Set.of(), player.getYRot(), player.getXRot(), false);
                            scrambleInventory(serverPlayer);
                            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

                            // Remove the location and glint after use
                            tag.remove("TeleportPos");
                            tag.remove("Dimension");
                            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
                            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                        } else {
                            serverPlayer.sendSystemMessage(Component.literal("Target dimension not found! Tried: " + dimString));
                        }
                    } catch (Exception e) {
                        serverPlayer.sendSystemMessage(Component.literal("Error parsing dimension: " + dimString));
                    }
                }
            } else {
                if (!level.isClientSide()) {
                    player.sendSystemMessage(Component.literal("No teleport point set. Sneak-right-click to set."));
                }
            }
        }

        return InteractionResult.CONSUME;
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
