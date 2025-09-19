package net.coba.wyr;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class ChoiceEffects {
    private static final Map<UUID, Set<String>> activeEffects = new HashMap<>();
    private static final Map<UUID, Integer> playerTickCounters = new HashMap<>();
    private static final Map<UUID, Long> teleportCooldowns = new HashMap<>();
    private static final Map<UUID, Integer> fishCatchCount = new HashMap<>();
    private static final Map<UUID, Long> dailyDiamondCooldown = new HashMap<>();

    private static final ResourceLocation INSTA_HEAL_HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(WYR.MODID, "insta_heal_health_modifier");
    private static final ResourceLocation EXTRA_HEARTS_HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(WYR.MODID, "extra_hearts_health_modifier");

    private static Set<String> getActiveEffects(Player player) {
        return activeEffects.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
    }

    public static boolean hasEffect(Player player, String effectId) {
        return getActiveEffects(player).contains(effectId);
    }

    public static long getTeleportCooldown(Player player) {
        return teleportCooldowns.getOrDefault(player.getUUID(), 0L);
    }

    public static void applyEffect(Player player, String effectId) {
        Set<String> playerEffects = getActiveEffects(player);
        if (playerEffects.contains(effectId)) return;
        playerEffects.add(effectId);

        if (player instanceof ServerPlayer serverPlayer) {
            // --- ADD THIS BLOCK ---
            if ("stacked_logs".equals(effectId)) {
                PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncStackedLogsStatus(true));
            }

            AttributeInstance maxHealth = serverPlayer.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                if ("insta_heal".equals(effectId)) {
                    maxHealth.addPermanentModifier(new AttributeModifier(INSTA_HEAL_HEALTH_MODIFIER_ID, -14.0, AttributeModifier.Operation.ADD_VALUE));
                } else if ("extra_hearts".equals(effectId)) {
                    maxHealth.addPermanentModifier(new AttributeModifier(EXTRA_HEARTS_HEALTH_MODIFIER_ID, 20.0, AttributeModifier.Operation.ADD_VALUE));
                }
            }
            if ("creative_flight".equals(effectId)) {
                serverPlayer.getAbilities().mayfly = true;
                serverPlayer.onUpdateAbilities();
            }
            if ("strength_ii".equals(effectId)) {
                serverPlayer.getServer().getRecipeManager().byKey(ResourceLocation.withDefaultNamespace("shield"))
                        .ifPresent(recipe -> serverPlayer.resetRecipes(List.of(recipe)));
            }
        }
    }

    public static void removeEffect(Player player, String effectId) {
        getActiveEffects(player).remove(effectId);

        if (player instanceof ServerPlayer serverPlayer) {
            switch (effectId) {
                case "insta_heal":
                case "extra_hearts":
                    AttributeInstance maxHealth = serverPlayer.getAttribute(Attributes.MAX_HEALTH);
                    if (maxHealth != null) {
                        maxHealth.removeModifier(INSTA_HEAL_HEALTH_MODIFIER_ID);
                        maxHealth.removeModifier(EXTRA_HEARTS_HEALTH_MODIFIER_ID);
                    }
                    if (serverPlayer.getHealth() > serverPlayer.getMaxHealth()) {
                        serverPlayer.setHealth(serverPlayer.getMaxHealth());
                    }
                    break;
                case "creative_flight":
                    if (!serverPlayer.isCreative()) {
                        serverPlayer.getAbilities().mayfly = false;
                        serverPlayer.getAbilities().flying = false;
                        serverPlayer.onUpdateAbilities();
                    }
                    break;
                case "strength_ii":
                    serverPlayer.getServer().getRecipeManager().byKey(ResourceLocation.withDefaultNamespace("shield"))
                            .ifPresent(recipe -> serverPlayer.awardRecipes(List.of(recipe)));
                    break;
            }
        }
    }

    public static void clearAllEffects(Player player) {
        UUID playerUUID = player.getUUID();
        Set<String> playerEffects = getActiveEffects(player);

        // --- ADD THIS BLOCK ---
        if (player instanceof ServerPlayer serverPlayer && playerEffects.contains("stacked_logs")) {
            PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncStackedLogsStatus(false));
        }

        if (player instanceof ServerPlayer serverPlayer) {
            PlayerChoiceTracker.clearChoices(serverPlayer);
        }

        activeEffects.remove(playerUUID);
        playerTickCounters.remove(playerUUID);
        teleportCooldowns.remove(playerUUID);
        fishCatchCount.remove(playerUUID);
        dailyDiamondCooldown.remove(playerUUID);

        if (player instanceof ServerPlayer serverPlayer) {
            AttributeInstance maxHealth = serverPlayer.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.removeModifier(INSTA_HEAL_HEALTH_MODIFIER_ID);
                maxHealth.removeModifier(EXTRA_HEARTS_HEALTH_MODIFIER_ID);
            }
            if (serverPlayer.getHealth() > serverPlayer.getMaxHealth()) {
                serverPlayer.setHealth(serverPlayer.getMaxHealth());
            }
            if (!serverPlayer.isCreative()) {
                serverPlayer.getAbilities().mayfly = false;
                serverPlayer.getAbilities().flying = false;
                serverPlayer.onUpdateAbilities();
            }
            if (playerEffects.contains("strength_ii")) {
                serverPlayer.getServer().getRecipeManager().byKey(ResourceLocation.withDefaultNamespace("shield"))
                        .ifPresent(recipe -> serverPlayer.awardRecipes(List.of(recipe)));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerUUID = event.getEntity().getUUID();
        activeEffects.remove(playerUUID);
        playerTickCounters.remove(playerUUID);
        teleportCooldowns.remove(playerUUID);
        fishCatchCount.remove(playerUUID);
        dailyDiamondCooldown.remove(playerUUID);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.level().isClientSide()) return;

            Set<String> playerEffects = getActiveEffects(player);
            if (playerEffects.isEmpty()) return;

            if (hasEffect(player, "daily_diamond")) {
                long currentTime = player.level().getGameTime();
                long lastTime = dailyDiamondCooldown.getOrDefault(player.getUUID(), 0L);
                if (currentTime > lastTime) {
                    if (player.level().getDayTime() % 24000 < 100) {
                        player.getInventory().add(new ItemStack(Items.DIAMOND_BLOCK));
                        dailyDiamondCooldown.put(player.getUUID(), currentTime + 23000);
                    }
                }
            }

            if (hasEffect(player, "daily_diamond")) {
                WyrSavedData savedData = WyrSavedData.get((ServerLevel) player.level());
                int currentThirst = savedData.getPlayerThirst(player.getUUID());
                float currentExhaustion = savedData.getPlayerThirstExhaustion(player.getUUID());

                if (currentExhaustion > 4.0F) {
                    currentExhaustion -= 4.0F;
                    if (currentThirst > 0) {
                        savedData.setPlayerThirst(player.getUUID(), currentThirst - 1);
                    }
                }

                if (currentThirst <= 0 && player.tickCount % 80 == 0) {
                    player.hurt(player.damageSources().starve(), 1.0F);
                }

                if (player.tickCount % 20 == 0) {
                    PacketDistributor.sendToPlayer(player, new PacketSyncThirst(currentThirst));
                }
                savedData.setPlayerThirstExhaustion(player.getUUID(), currentExhaustion);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        Set<String> playerEffects = getActiveEffects(player);
        if (playerEffects.isEmpty()) return;
        int ticks = playerTickCounters.getOrDefault(player.getUUID(), 0);

        for (String effectId : playerEffects) {
            switch (effectId) {
                case "phasing":
                    break;
                case "creative_flight":
                    if (player.fallDistance > 0 && !player.getAbilities().flying) {
                        player.getAbilities().mayfly = false;
                    } else {
                        player.getAbilities().mayfly = true;
                    }
                    player.onUpdateAbilities();
                    if (player.getAbilities().flying) {
                        if (ticks % 200 == 0 && ticks != 0) {
                            List<ItemStack> allItems = new ArrayList<>();
                            allItems.addAll(player.getInventory().items);
                            allItems.addAll(player.getInventory().armor);
                            allItems.addAll(player.getInventory().offhand);
                            List<ItemStack> possibleItemsToDelete = allItems.stream()
                                    .filter(stack -> !stack.isEmpty())
                                    .toList();
                            if (!possibleItemsToDelete.isEmpty()) {
                                ItemStack stackToRemoveFrom = possibleItemsToDelete.get(player.getRandom().nextInt(possibleItemsToDelete.size()));
                                stackToRemoveFrom.shrink(1);
                                player.getInventory().setChanged();
                            }
                        }
                    }
                    break;
                case "speed_iv":
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2, 3, true, false));
                    if (player.getFoodData().getFoodLevel() > 0) {
                        player.getFoodData().addExhaustion(0.015f * 3);
                    }
                    break;
                case "water_breathing":
                    player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 2, 0, true, false));
                    if (!player.getInventory().getArmor(3).isEmpty()) {
                        player.getInventory().add(player.getInventory().armor.set(3, ItemStack.EMPTY));
                    }
                    break;
                case "haste_ii":
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 2, 1, true, false));
                    break;
                case "high_jump":
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 2, 3, true, false));
                    break;
                case "strength_ii":
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2, 1, true, false));
                    for (int i = 0; i < player.getInventory().items.size(); i++) {
                        ItemStack stack = player.getInventory().items.get(i);
                        if (stack.getItem() instanceof ShieldItem) {
                            ItemStack toDrop = stack.copy();
                            player.getInventory().items.set(i, ItemStack.EMPTY);
                            player.drop(toDrop, true);
                        }
                    }
                    ItemStack offhandStack = player.getOffhandItem();
                    if (offhandStack.getItem() instanceof ShieldItem) {
                        player.getInventory().offhand.set(0, ItemStack.EMPTY);
                        player.drop(offhandStack.copy(), true);
                    }
                    if (player.containerMenu.getCarried().getItem() instanceof ShieldItem) {
                        ItemStack carriedStack = player.containerMenu.getCarried().copy();
                        player.containerMenu.setCarried(ItemStack.EMPTY);
                        player.drop(carriedStack, true);
                    }
                    break;
                case "night_vision":
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false));
                    if (ticks % 20 == 0) {
                        player.level().getEntitiesOfClass(EnderMan.class, player.getBoundingBox().inflate(32)).stream()
                                .filter(enderman -> enderman.getTarget() == null)
                                .forEach(enderman -> enderman.setTarget(player));
                    }
                    break;
                case "extra_hearts":
                    if (!player.getInventory().getArmor(2).isEmpty()) {
                        player.getInventory().add(player.getInventory().armor.set(2, ItemStack.EMPTY));
                    }
                    break;
                case "water_walk":
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 0, true, false));
                    if (player.getAbilities().flying || player.getDeltaMovement().y > 0.0D) {
                        break;
                    }
                    AABB box = player.getBoundingBox();
                    BlockPos corner1 = BlockPos.containing(box.minX, box.minY - 0.5D, box.minZ);
                    BlockPos corner2 = BlockPos.containing(box.maxX, box.minY, box.maxZ);
                    double highestFluidSurface = Double.NEGATIVE_INFINITY;
                    boolean foundFluid = false;
                    for (BlockPos pos : BlockPos.betweenClosed(corner1, corner2)) {
                        FluidState fluidState = player.level().getFluidState(pos);
                        if (!fluidState.isEmpty()) {
                            foundFluid = true;
                            double surfaceY = (double) pos.getY() + fluidState.getHeight(player.level(), pos);
                            if (surfaceY > highestFluidSurface) {
                                highestFluidSurface = surfaceY;
                            }
                        }
                    }
                    if (foundFluid) {
                        Vec3 motion = player.getDeltaMovement();
                        if (box.minY <= highestFluidSurface && motion.y <= 0) {
                            player.setPos(player.getX(), highestFluidSurface, player.getZ());
                            if (player.isCrouching()) {
                                player.setOnGround(false);
                                if (player.zza == 0.0F && player.xxa == 0.0F) {
                                    player.setDeltaMovement(motion.x() * 0.7D, 0.0D, motion.z() * 0.7D);
                                } else {
                                    player.setDeltaMovement(motion.x(), 0.0D, motion.z());
                                }
                            } else {
                                player.setDeltaMovement(motion.with(Direction.Axis.Y, 0.0D));
                                player.setOnGround(true);
                            }
                        }
                    }
                    break;
                case "dolphin_grace":
                    if (player.isInWater()) {
                        player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 2, 0, true, false));
                    } else if (!player.isCreative() && !player.isSpectator() && ticks % 20 == 0) {
                        player.hurt(player.damageSources().drown(), 2.0F);
                    }
                    break;
                case "wall_climb":
                    if (player.horizontalCollision) {
                        Vec3 motion = player.getDeltaMovement();
                        if (motion.y() > 0) {
                            player.setDeltaMovement(motion.x(), 0.2D, motion.z());
                        } else {
                            double newY = -0.15;
                            if (player.isCrouching()) {
                                newY = -0.3;
                            }
                            player.setDeltaMovement(motion.x(), newY, motion.z());
                        }
                    }
                    break;
                case "triple_damage":
                    if (!player.getInventory().getArmor(0).isEmpty()) {
                        player.getInventory().add(player.getInventory().armor.set(0, ItemStack.EMPTY));
                    }
                    break;
                case "resistance_ii":
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2, 1, true, false));
                    if (player.isSprinting()) {
                        player.setSprinting(false);
                    }
                    break;
            }
        }
        playerTickCounters.put(player.getUUID(), ticks + 1);

        if (hasEffect(player, "stacked_logs")) {
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof AxeItem) {
                    CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    if (!tag.getBoolean("WyrNerfed")) {
                        stack.setDamageValue(stack.getMaxDamage() - 10);
                        tag.putBoolean("WyrNerfed", true);
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {

        if (event.getPlayer() instanceof ServerPlayer player && hasEffect(player, "stacked_logs")) {
            if (event.getState().is(BlockTags.LOGS)) {

                Optional<Holder.Reference<Enchantment>> silkTouchHolder = player.level().registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT).getHolder(Enchantments.SILK_TOUCH);

                if (silkTouchHolder.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(silkTouchHolder.get(), player.getMainHandItem()) > 0) {
                    return; // Don't apply the effect if the player is using Silk Touch
                }

                // If all checks pass, we handle the drop manually.
                ItemStack logDrop = new ItemStack(event.getState().getBlock().asItem());
                logDrop.setCount(64);

                // Manually spawn the item in the world
                Block.popResource((ServerLevel) event.getLevel(), event.getPos(), logDrop);

                // Set the block to air and cancel the event to prevent default drops
                event.getLevel().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);
                event.setCanceled(true);
            }
        }

        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;

        if (hasEffect(player, "sand_treasure")) {
            if (event.getState().is(Blocks.SAND) || event.getState().is(Blocks.RED_SAND)) {
                if (player.getRandom().nextFloat() <= 0.001) {
                    ServerLevel serverLevel = (ServerLevel) player.level();
                    LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.BURIED_TREASURE);
                    LootParams params = new LootParams.Builder(serverLevel)
                            .withParameter(LootContextParams.ORIGIN, player.position())
                            .withParameter(LootContextParams.THIS_ENTITY, player)
                            .create(LootContextParamSets.CHEST);

                    lootTable.getRandomItems(params).forEach(player::addItem);
                    lootTable.getRandomItems(params).forEach(player::addItem);
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemFished(ItemFishedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (hasEffect(player, "fishing_luck")) {
            int count = fishCatchCount.getOrDefault(player.getUUID(), 0) + 1;
            fishCatchCount.put(player.getUUID(), count);

            if (count >= 5) {
                fishCatchCount.put(player.getUUID(), 0);
                Guardian guardian = EntityType.GUARDIAN.create(player.level());
                if (guardian != null && event.getHookEntity() != null) {
                    guardian.setPos(event.getHookEntity().position());
                    guardian.setTarget(player);
                    player.level().addFreshEntity(guardian);
                }
            }

            if (player.getRandom().nextFloat() <= 0.01) {
                ServerLevel serverLevel = (ServerLevel) player.level();
                LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.SHIPWRECK_TREASURE);
                LootParams params = new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.ORIGIN, player.position())
                        .withParameter(LootContextParams.THIS_ENTITY, player)
                        .create(LootContextParamSets.FISHING);

                event.getDrops().addAll(lootTable.getRandomItems(params));
                event.getDrops().addAll(lootTable.getRandomItems(params));
            }
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockState state = player.level().getBlockState(event.getPos());

        if (hasEffect(player, "triple_ores") && state.getBlock() instanceof EnchantingTableBlock) {
            event.setCanceled(true);
        }

        if (hasEffect(player, "sand_treasure")) {
            if (state.is(Blocks.FURNACE) || state.is(Blocks.BLAST_FURNACE) || state.is(Blocks.SMOKER)) {
                event.setCanceled(true);
            }
        }
        if (hasEffect(player, "phasing")) {
            if (state.getBlock() instanceof DoorBlock) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        if (hasEffect(event.getPlayer(), "triple_ores")) {
            if (event.getRight().getItem() instanceof EnchantedBookItem) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player && hasEffect(player, "daily_diamond")) {
            ItemStack consumedItem = event.getItem();
            WyrSavedData savedData = WyrSavedData.get(player.serverLevel());
            int currentThirst = savedData.getPlayerThirst(player.getUUID());
            int newThirst = currentThirst;

            // Check if the item is MEAT
            if (consumedItem.is(ItemTags.MEAT)) {
                // Lose 2 thirst bars (4 points)
                newThirst = Math.max(0, currentThirst - 4);
            }
            // Check if the item is a MELON
            else if (consumedItem.is(Items.MELON_SLICE) || consumedItem.is(Items.GLISTERING_MELON_SLICE)) {
                // Gain 1 thirst bar (2 points)
                newThirst = Math.min(20, currentThirst + 2);
            }
            else if (consumedItem.is(Items.POTION)) {
                var potionContents = consumedItem.get(DataComponents.POTION_CONTENTS);
                if (potionContents != null && potionContents.potion().isPresent() && potionContents.potion().get().is(Potions.WATER)) {
                    // Gain 2 thirst bars (4 points)
                    newThirst = Math.min(20, currentThirst + 4);
                }
            }

            // If the thirst level changed, update it and sync with the client.
            if (newThirst != currentThirst) {
                savedData.setPlayerThirst(player.getUUID(), newThirst);
                PacketDistributor.sendToPlayer(player, new PacketSyncThirst(newThirst));
            }
        }
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {

    }

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();

        if (hasEffect(player, "stacked_logs")) {
            ItemStack crafted = event.getCrafting();
            if (crafted.getItem() instanceof AxeItem) {
                CompoundTag tag = crafted.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                crafted.setDamageValue(crafted.getMaxDamage() - 10);
                tag.putBoolean("WyrNerfed", true);
                crafted.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }
        if (hasEffect(player, "strength_ii")) {
            if (event.getCrafting().getItem() instanceof ShieldItem) {
                event.getCrafting().setCount(0);
            }
        }
    }

    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (hasEffect(player, "creative_flight") && !player.getAbilities().flying) {
            float distance = event.getDistance();
            if (distance > 3.0F) {
                int damageAmount = (int) Math.ceil(distance - 3.0F);
                player.hurt(player.damageSources().fall(), damageAmount);
            }
            event.setCanceled(true);
            return;
        }
        if (hasEffect(player, "high_jump")) {
            float distance = event.getDistance();
            if (distance > 2.0F) {
                int damageAmount = (int) Math.ceil(distance - 2.0F);
                if (damageAmount > 0) player.hurt(player.damageSources().fall(), (float) damageAmount);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (hasEffect(player, "insta_heal")) event.setAmount(player.getMaxHealth());
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (hasEffect(player, "fire_immunity")) {
                if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
                    event.setCanceled(true);
                } else if (event.getSource().getDirectEntity() instanceof LivingEntity) {
                    event.setAmount(event.getAmount() * 2);
                }
            }
            if (hasEffect(player, "wall_climb") && event.getSource().is(DamageTypeTags.IS_FALL)) {
                event.setAmount(event.getAmount() * 2);
            }
        }
        if (event.getSource().getEntity() instanceof Player player && hasEffect(player, "triple_damage")) {
            if (player.getRandom().nextInt(4) == 0) event.setAmount(event.getAmount() * 3);
        }
    }

    private void applyHasteDurability(Player player, ItemStack item) {
        if (hasEffect(player, "haste_ii") && !player.isCreative() && player instanceof ServerPlayer serverPlayer) {
            if (item.isDamageableItem()) item.hurtAndBreak(1, serverPlayer, EquipmentSlot.MAINHAND);
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (hasEffect(player, "mob_invisibility")) event.setCanceled(true);
        applyHasteDurability(player, player.getMainHandItem());
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        applyHasteDurability(event.getEntity(), event.getItemStack());
    }



    @SubscribeEvent
    public void onSetTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Monster && event.getNewAboutToBeSetTarget() instanceof Player player) {
            if (hasEffect(player, "mob_invisibility")) event.setNewAboutToBeSetTarget(null);
        }
    }

    public static void handleEnderTeleport(Player player) {
        long currentTime = System.currentTimeMillis();
        if (currentTime > teleportCooldowns.getOrDefault(player.getUUID(), 0L)) {
            final double range = 15.0;
            Vec3 startPos = player.getEyePosition();
            Vec3 endPos = startPos.add(player.getLookAngle().scale(range));
            BlockHitResult rayTrace = player.level().clip(new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            Vec3 targetPos = (rayTrace.getType() == HitResult.Type.MISS) ? endPos : rayTrace.getLocation();
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            }
            player.fallDistance = 0.0F;
            player.hurt(player.damageSources().magic(), 4.0F);
            teleportCooldowns.put(player.getUUID(), currentTime + 5000);
        }
    }
}