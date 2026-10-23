package net.coba.wyr;

import net.minecraft.world.level.block.Blocks;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.coba.wyr.ai.DefendPlayerGoal;
import net.coba.wyr.ai.FollowPlayerGoal;
import net.coba.wyr.item.ModItems;
import net.coba.wyr.sound.ModSounds;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.brewing.PlayerBrewedPotionEvent;
import net.neoforged.neoforge.event.brewing.PotionBrewEvent;
import net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.world.entity.monster.Enderman;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.tags.FluidTags;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.client.gui.components.Tooltip;
import java.util.ArrayList;
import java.util.Collections;

import java.util.stream.Collectors;
import java.util.*;

public class ChoiceEffects {
    // In-memory maps for effects active on currently online players
    private static final Map<UUID, Set<String>> activeEffects = new HashMap<>();
    private static final Map<UUID, Integer> playerTickCounters = new HashMap<>();
    private static final Map<UUID, Long> teleportCooldowns = new HashMap<>();
    private static final Map<UUID, Long> dailyDiamondCooldown = new HashMap<>();
    private static final Map<UUID, BlockPos> respawnLocations = new HashMap<>();
    private static final Map<UUID, ResourceKey<Level>> respawnDimensions = new HashMap<>();
    private static final Map<UUID, Horse> fastHorses = new HashMap<>();
    private static final Map<UUID, Set<UUID>> tamedMonsters = new HashMap<>();
    private static final Map<UUID, Integer> randomSoundTimers = new HashMap<>();
    private static final Map<UUID, ResourceKey<net.minecraft.world.level.biome.Biome>> spawnBiomes = new HashMap<>();
    private static final Map<UUID, BlockPos> spawnPositions = new HashMap<>();
    private static final Map<UUID, Long> lastBiomeMsgTime = new HashMap<>();
    private static final Map<UUID, Long> effectStartTimes = new HashMap<>();
    // Pending one-shot teleport applied on the first tick after respawn so the
    // player appears directly in the locked biome instead of being dragged
    // back by the per-tick push-back ("magnet") logic.
    private static final Map<UUID, BlockPos> pendingBiomeRespawns = new HashMap<>();
    private static final Set<BlockPos> playerPlacedDebris = new HashSet<>();


    private static final Identifier INSTA_HEAL_HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(WYR.MODID, "insta_heal_health_modifier");
    private static final Identifier EXTRA_HEARTS_HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(WYR.MODID, "extra_hearts_health_modifier");
    private static final Identifier GOLEM_HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(WYR.MODID, "golem_health");
    private static final Identifier GOLEM_STRENGTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(WYR.MODID, "golem_strength");
    private static final Identifier GOLEM_SPEED_MODIFIER_ID = Identifier.fromNamespaceAndPath(WYR.MODID, "golem_speed");
    private static final Identifier WOLF_HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(WYR.MODID, "wolf_health");
    private static final Identifier WOLF_STRENGTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(WYR.MODID, "wolf_strength");
    private static final Identifier WOLF_SPEED_MODIFIER_ID = Identifier.fromNamespaceAndPath(WYR.MODID, "wolf_speed");

    private static final Identifier SCALE_MODIFIER_ID = Identifier.fromNamespaceAndPath(WYR.MODID, "half_scale");
    private static final Identifier SNEAK_SPEED_MODIFIER_ID = Identifier.fromNamespaceAndPath(WYR.MODID, "sneak_speed");

    private static final Identifier TRIPLE_HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(WYR.MODID, "triple_health_modifier");
    private static final Identifier DOUBLE_HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(WYR.MODID, "double_health_modifier");
    private static final Identifier COMBINED_HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(WYR.MODID, "combined_health_modifier");

    private static final Map<UUID, Long> durabilityPenaltyPlayers = new HashMap<>();

    private static final Map<UUID, Entity> lastVehicleMap = new HashMap<>();

    // At the top of ChoiceEffects.java
    private final Set<Entity> processedEntitiesThisTick = new HashSet<>();

    private static final Map<UUID, Boolean> wasSneaking = new HashMap<>();

    private static final Map<UUID, Long> keyScrambleTimers = new HashMap<>();

    private static final Map<UUID, Long> crosshairRandomizeTimers = new HashMap<>();

    private static final Map<UUID, Integer> autoSmeltSoundTimers = new HashMap<>();

    private static final Map<UUID, Integer> fishingCatchCount = new HashMap<>();

    private static final Set<UUID> hasEnteredEnd = new HashSet<>();

    private static final Map<UUID, Integer> talkingVillagerTimers = new HashMap<>();
    private static final List<String> VILLAGER_PHRASES = List.of(
            "Hmmph!",
            "Got any emeralds?",
            "What a deal!",
            "Don't touch my crops.",
            "The bell is about to ring.",
            "Have you seen the news?",
            "Michael D is a noob",
            "You're trash",
            "Get lost kid",
            "Get over here",
            "I'm sexy and I know it",
            "You're dogwater",
            "Skibidi Toilet",
            "That's cap",
            "Yolo",
            "TTYL",
            "Bye Felicia",
            "Why you always lying",
            "Are you sure about that",
            "Hide your kids, hide you wife",
            "I like trains",
            "Get good",
            "Look at this dude",
            "Get a room",
            "Suck it",
            "Let him cook",
            "I like to move it move it",
            "I'm fast as F**K boi",
            "Look at me now",
            "Mom Spaghetti",
            "Get rekted nerd",
            "Missed me",
            "Over and out",
            "Fr e sha  voca do",
            "You shall not pass",
            "May the force be with you",
            "Heres Johnny",
            "Nice work kid",
            "Michael D is such a fine young man",
            "I'm a bad guy",
            "Frankly, my dear, I don't give a d**m",
            "I'm gonna make him an offer he can't refuse",
            "There's no place like home",
            "You talkin' to me",
            "You had me at 'hello",
            "I'll be back",
            "You can't handle the truth!",
            "Louis, I think this is the beginning of a beautiful friendship"
    );

    private static final Map<DyeColor, Item> WOOL_BY_COLOR = java.util.Arrays.stream(DyeColor.values())
            .collect(java.util.stream.Collectors.toMap(java.util.function.Function.identity(), Items.WOOL::pick));

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        Player player = event.getPlayer();
        if (hasEffect(player, "daily_totem_no_armor")) {
            ItemStack droppedStack = event.getEntity().getItem();
            CustomData customData = droppedStack.get(DataComponents.CUSTOM_DATA);
            if (customData != null && customData.copyTag().getBooleanOr("wyr_locked_totem", false)) {
                // Prevent dropping the locked totem
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("You cannot drop the daily totem!"));
            }
        }
    }

    @SubscribeEvent
    public void onEnchantmentLevelSet(EnchantmentLevelSetEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            BlockPos pos = BlockPos.of(event.getEnchantLevel());
            Player player = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5.0, p -> true);

            if (player != null && hasEffect(player, "perfect_enchants_destroy_chance")) {
                if (player.getRandom().nextFloat() < 0.5f) {
                    event.getItem().setCount(0);
                    level.playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                }
            }
            
            // Prevent enchanting books for infinite_xp_no_anvils
            if (player != null && hasEffect(player, "infinite_xp_no_anvils")) {
                event.getItem().setCount(0);
                player.sendSystemMessage(Component.literal("You cannot enchant books with infinite_xp_no_anvils!"));
            }
        }
    }

    private static Set<String> getActiveEffects(Player player) {
        return activeEffects.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
    }

    public static boolean hasEffect(Player player, String effectId) {
        if (player == null) return false;
        if (player.level().isClientSide()) {
            return ClientEffectCache.hasEffect(effectId);
        }
        return getActiveEffects(player).contains(effectId);
    }

    public static long getTeleportCooldown(Player player) {
        return teleportCooldowns.getOrDefault(player.getUUID(), 0L);
    }

    private static ServerPlayer getServerPlayer(Player player) {
        if (player instanceof ServerPlayer sp) {
            return sp;
        }
        if (player.level().isClientSide() || net.coba.wyr.WYR.getServer() == null) return null;
        return net.coba.wyr.WYR.getServer().getPlayerList().getPlayer(player.getUUID());
    }

    public static void randomizeCrosshair(ServerPlayer player) {
        long newSeed = new Random().nextLong();
        WyrSavedData.get((ServerLevel) player.level()).setPlayerCrosshairSeed(player.getUUID(), newSeed);
        PacketDistributor.sendToPlayer(player, new PacketSyncCrosshairSeed(newSeed));
        WYR.LOGGER.info("Randomizing crosshair for {} with new seed.", player.getName().getString());
    }

    public static void applyInitialEffect(Player player, String effectId) {
        var serverPlayer = getServerPlayer(player);
        if (serverPlayer == null) return;
        var enchantRegistry = player.level().holderLookup(Registries.ENCHANTMENT);

        if ("wool_stack_random_crosshair".equals(effectId)) {
            randomizeCrosshair(serverPlayer);
            crosshairRandomizeTimers.put(player.getUUID(), player.level().getGameTime() + (60 * 20)); // 1 minute timer
        }

        if ("texture_scramble".equals(effectId)) {
            serverPlayer.getInventory().add(new ItemStack(ModItems.UNLIMITED_SODA.get()));
            if (player instanceof ServerPlayer sp) PacketDistributor.sendToPlayer(sp, new PacketSyncTextureScrambleStatus(true));
        }

        if ("random_food_no_jump".equals(effectId)) {
            // Give one random food item immediately when the effect is chosen
            List<Item> foods = BuiltInRegistries.ITEM.stream()
                    .filter(item -> item.components().has(DataComponents.FOOD))
                    .toList();
            if (!foods.isEmpty()) {
                ItemStack foodStack = new ItemStack(foods.get(serverPlayer.getRandom().nextInt(foods.size())));
                // Capture the name BEFORE add() - add() empties the stack, which
                // would make getHoverName() return "Air".
                Component foodName = foodStack.getHoverName();
                serverPlayer.getInventory().add(foodStack);
                serverPlayer.sendSystemMessage(Component.literal("You received random food: ").append(foodName));
            }
        }

        if ("backpack_no_shulkers".equals(effectId)) {
            serverPlayer.getInventory().add(new ItemStack(ModItems.BACKPACK.get()));
        }

        if ("strength_ii".equals(effectId)) {
            serverPlayer.level().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("minecraft", "shield")))
                    .ifPresent(recipeHolder -> serverPlayer.getRecipeBook().remove(recipeHolder.id()));
        }

        if ("fast_swim_muted".equals(effectId)) {
            if (player instanceof ServerPlayer sp) PacketDistributor.sendToPlayer(sp, new PacketSyncMuteStatus(true));
        }

        if ("sneak_bonemeal_fov_change".equals(effectId)) {
            if (player instanceof ServerPlayer sp) PacketDistributor.sendToPlayer(sp, new PacketSyncFovChangeStatus(true));
        }
        if ("no_fall_damage_low_render_distance".equals(effectId)) {
            if (player instanceof ServerPlayer sp) PacketDistributor.sendToPlayer(sp, new PacketSyncRenderDistanceStatus(true));
        }

        if ("no_hunger_random_shader".equals(effectId)) {
            if (player instanceof ServerPlayer sp) PacketDistributor.sendToPlayer(sp, new net.coba.wyr.PacketSyncShaderStatus(true));
        }

        if ("hero_of_village_random_sounds".equals(effectId)) {
            if (player instanceof ServerPlayer sp) PacketDistributor.sendToPlayer(sp, new PacketSyncRandomSoundsStatus(true));
        }

        if ("choose_spawn_biome_lock".equals(effectId)) {
            player.level().getBiome(player.blockPosition()).unwrapKey().ifPresent(key -> spawnBiomes.put(player.getUUID(), key));
            effectStartTimes.put(player.getUUID(), player.level().getGameTime());
            // Send packet to the client to open the GUI
        }

        if ("op_bow_only".equals(effectId)) {
            ItemStack opBow = new ItemStack(Items.BOW);
            opBow.enchant(enchantRegistry.getOrThrow(Enchantments.INFINITY), 1);
            opBow.enchant(enchantRegistry.getOrThrow(Enchantments.MENDING), 1);
            opBow.enchant(enchantRegistry.getOrThrow(Enchantments.POWER), 10);
            opBow.enchant(enchantRegistry.getOrThrow(Enchantments.LOOTING), 10);
            opBow.enchant(enchantRegistry.getOrThrow(Enchantments.KNOCKBACK), 10);
            opBow.set(DataComponents.CUSTOM_NAME, Component.literal("The Only Weapon"));
            serverPlayer.getInventory().add(opBow);
            serverPlayer.getInventory().add(new ItemStack(Items.ARROW));
        }
        if ("fast_horse_dark_fear".equals(effectId)) {
            Horse horse = EntityTypes.HORSE.create(serverPlayer.level(), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
            if (horse != null) {
                horse.setPos(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
                horse.setTamed(true);
                horse.setOwner(serverPlayer);
                horse.getInventory().setItem(0, new ItemStack(Items.SADDLE));
                // Max speed horse (0.3375 is max horse speed)
                Objects.requireNonNull(horse.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.3375);
                // Set max health and jump strength
                Objects.requireNonNull(horse.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(30.0);
                Objects.requireNonNull(horse.getAttribute(Attributes.JUMP_STRENGTH)).setBaseValue(1.0);
                horse.setHealth(30.0f);
                serverPlayer.level().addFreshEntity(horse);
                fastHorses.put(serverPlayer.getUUID(), horse);
            }
        }
        if ("unbreakable_elytra_no_boost".equals(effectId)) {
            ItemStack elytra = new ItemStack(Items.ELYTRA);
            elytra.set(DataComponents.UNBREAKABLE, net.minecraft.util.Unit.INSTANCE);
            serverPlayer.getInventory().add(elytra);
        }
        if ("stack_64_world_height".equals(effectId)) {
            // Teleport to the top of the world (max build height)
            int worldHeight = serverPlayer.level().dimensionType().minY() + serverPlayer.level().dimensionType().height();
            serverPlayer.teleportTo((ServerLevel) serverPlayer.level(), serverPlayer.getX(), worldHeight, serverPlayer.getZ(), java.util.Set.of(), serverPlayer.getYRot(), serverPlayer.getXRot(), false);

            // Set all stackable items to a full stack of 64
            for (int i = 0; i < serverPlayer.getInventory().getContainerSize(); i++) {
                ItemStack stack = serverPlayer.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.isStackable()) {
                    stack.setCount(stack.getMaxStackSize());
                }
            }
            // NOTE: no early return here - the effect must still be registered
            // via reapplyPersistentEffect() below so hasEffect() returns true.
        }
        if ("choose_spawn_biome_lock".equals(effectId)) {
            player.level().getBiome(player.blockPosition()).unwrapKey().ifPresent(key -> spawnBiomes.put(player.getUUID(), key));
        }
                reapplyPersistentEffect(player, effectId);
    }

    private static void scrambleKeys(ServerPlayer player) {
        Random random = new Random();
        int scrambleState = random.nextInt(4) + 1;

        WYR.LOGGER.info("[WYR DEBUG] SERVER [from scrambleKeys]: Scrambling keys for {} to state {}", player.getName().getString(), scrambleState);

        WyrSavedData savedData = WyrSavedData.get((ServerLevel) player.level());
        savedData.setPlayerScrambleState(player.getUUID(), scrambleState);

        PacketDistributor.sendToPlayer(player, new PacketSyncScrambleState(scrambleState));
    }

    public static void syncAllClientEffects(ServerPlayer player) {
        if (player == null) return;

        int scrambleState = WyrSavedData.get((ServerLevel) player.level()).getPlayerScrambleState(player.getUUID());
        if (!hasEffect(player, "speed_v_random_keys")) {
            scrambleState = 0;
        }
        PacketDistributor.sendToPlayer(player, new PacketSyncScrambleState(scrambleState));

        // Check the server's current in-memory state for all client-side effects
        boolean hasSeeMobHp = ChoiceEffects.hasEffect(player, "see_mob_hp_double_health");
        boolean hasStackedLogs = ChoiceEffects.hasEffect(player, "stacked_logs");
        boolean hasRandomSounds = ChoiceEffects.hasEffect(player, "hero_of_village_random_sounds");
        boolean hasRandomShader = ChoiceEffects.hasEffect(player, "no_hunger_random_shader");
        boolean isMuted = ChoiceEffects.hasEffect(player, "fast_swim_muted");
        boolean hasFovChange = ChoiceEffects.hasEffect(player, "sneak_bonemeal_fov_change");
        boolean hasLowRenderDistance = ChoiceEffects.hasEffect(player, "no_fall_damage_low_render_distance");
        boolean hasWaterWalk = ChoiceEffects.hasEffect(player, "water_walk");
        boolean hasNoJump = ChoiceEffects.hasEffect(player, "no_fall_damage_no_jump") || ChoiceEffects.hasEffect(player, "random_food_no_jump");
        boolean hasStrengthII = ChoiceEffects.hasEffect(player, "strength_ii");
        boolean hasHalfHunger = ChoiceEffects.hasEffect(player, "cheap_trades_half_hunger");
        boolean hasTextureScramble = ChoiceEffects.hasEffect(player, "texture_scramble");
        boolean hasHighlightOres = hasEffect(player, "Highlight_ores_no_ladders");
        boolean hasRandomCrosshair = hasEffect(player, "wool_stack_random_crosshair");
        boolean hasItemScramble = hasEffect(player, "item_texture_scramble_melon_only");

        // Send a single, correct sync packet for each client-side effect
        Set<String> active = getActiveEffects(player);
        PacketDistributor.sendToPlayer(player, new PacketSyncAllEffects(new java.util.ArrayList<>(active)));
        PacketDistributor.sendToPlayer(player, new PacketSyncSeeMobHpStatus(hasSeeMobHp));
        PacketDistributor.sendToPlayer(player, new PacketSyncStackedLogsStatus(hasStackedLogs));
        PacketDistributor.sendToPlayer(player, new PacketSyncRandomSoundsStatus(hasRandomSounds));
        PacketDistributor.sendToPlayer(player, new PacketSyncShaderStatus(hasRandomShader));
        PacketDistributor.sendToPlayer(player, new PacketSyncMuteStatus(hasEffect(player, "fast_swim_muted")));
        PacketDistributor.sendToPlayer(player, new PacketSyncFovChangeStatus(hasFovChange));
        PacketDistributor.sendToPlayer(player, new PacketSyncRenderDistanceStatus(hasLowRenderDistance));
        PacketDistributor.sendToPlayer(player, new PacketSyncWaterWalkStatus(hasWaterWalk));
        PacketDistributor.sendToPlayer(player, new PacketSyncScrambleState(scrambleState));
        PacketDistributor.sendToPlayer(player, new PacketSyncNoJumpStatus(hasNoJump));
        PacketDistributor.sendToPlayer(player, new PacketSyncStrengthIIStatus(hasStrengthII));
        PacketDistributor.sendToPlayer(player, new PacketSyncHalfHungerStatus(hasHalfHunger));
        PacketDistributor.sendToPlayer(player, new PacketSyncTextureScrambleStatus(hasTextureScramble));
        PacketDistributor.sendToPlayer(player, new PacketSyncHighlightOresStatus(hasHighlightOres));
        PacketDistributor.sendToPlayer(player, new PacketSyncRandomCrosshairStatus(hasRandomCrosshair));
        PacketDistributor.sendToPlayer(player, new PacketSyncItemScrambleStatus(hasItemScramble));

        if (hasRandomCrosshair) {
            long seed = WyrSavedData.get((ServerLevel) player.level()).getPlayerCrosshairSeed(player.getUUID());
            PacketDistributor.sendToPlayer(player, new PacketSyncCrosshairSeed(seed));
        }
    }

    private static void applyHealthModifierToNearbyMobs(ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(128.0); // Increase radius to 128 blocks for better coverage
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> !(entity instanceof Player));

        for (LivingEntity entity : nearbyEntities) {
            AttributeInstance health = entity.getAttribute(Attributes.MAX_HEALTH);
            if (health != null && !health.hasModifier(DOUBLE_HEALTH_MODIFIER_ID)) {
                health.addPermanentModifier(new AttributeModifier(DOUBLE_HEALTH_MODIFIER_ID, 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                entity.setHealth(entity.getMaxHealth()); // Heal mob to its new max health
            }
            // Ensure the mob's health is synced to clients for HP bar visibility
            entity.setHealth(entity.getHealth());
        }
    }


    public static void reapplyPersistentEffect(Player player, String effectId) {
        var serverPlayer = getServerPlayer(player);
        if (serverPlayer == null) return;

        Set<String> playerEffects = getActiveEffects(player);
        playerEffects.add(effectId);

        if ("strength_ii".equals(effectId)) {
            serverPlayer.level().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("minecraft", "shield")))
                    .ifPresent(recipeHolder -> serverPlayer.getRecipeBook().remove(recipeHolder.id()));
        }

        if ("fast_climb_short".equals(effectId)) {
            AttributeInstance scale = serverPlayer.getAttribute(Attributes.SCALE);
            if (scale != null) {
                scale.removeModifier(SCALE_MODIFIER_ID);
                scale.addPermanentModifier(new AttributeModifier(SCALE_MODIFIER_ID, -0.5, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        AttributeInstance maxHealth = serverPlayer.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            if ("insta_heal".equals(effectId)) {
                maxHealth.removeModifier(INSTA_HEAL_HEALTH_MODIFIER_ID);
                maxHealth.addPermanentModifier(new AttributeModifier(INSTA_HEAL_HEALTH_MODIFIER_ID, -14.0, AttributeModifier.Operation.ADD_VALUE));
            } else if ("extra_hearts".equals(effectId)) {
                maxHealth.removeModifier(EXTRA_HEARTS_HEALTH_MODIFIER_ID);
                maxHealth.addPermanentModifier(new AttributeModifier(EXTRA_HEARTS_HEALTH_MODIFIER_ID, 20.0, AttributeModifier.Operation.ADD_VALUE));
            }
        }
        if ("creative_flight".equals(effectId)) {
            serverPlayer.getAbilities().mayfly = true;
            serverPlayer.onUpdateAbilities();
        }
        if ("keep_inventory_berries_only".equals(effectId)) {
            player.level().getServer().getGameRules().set(GameRules.KEEP_INVENTORY, true, player.level().getServer());
        }
    }

    public static void applyEffect(ServerPlayer player, String effectId, boolean isInitialApply) {
        var serverPlayer = getServerPlayer(player);
        Set<String> playerEffects = getActiveEffects(player);
        boolean wasAlreadyActive = playerEffects.contains(effectId);

        playerEffects.add(effectId);

        if ("strength_ii".equals(effectId)) {
            serverPlayer.level().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("minecraft", "shield")))
                    .ifPresent(recipeHolder -> serverPlayer.getRecipeBook().remove(recipeHolder.id()));
        }

        if ("fast_climb_short".equals(effectId)) {
            AttributeInstance scale = serverPlayer.getAttribute(Attributes.SCALE);
            if (scale != null) {
                scale.removeModifier(SCALE_MODIFIER_ID);
                scale.addPermanentModifier(new AttributeModifier(SCALE_MODIFIER_ID, -0.5, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        AttributeInstance maxHealth = serverPlayer.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            if ("insta_heal".equals(effectId)) {
                maxHealth.removeModifier(INSTA_HEAL_HEALTH_MODIFIER_ID);
                maxHealth.addPermanentModifier(new AttributeModifier(INSTA_HEAL_HEALTH_MODIFIER_ID, -14.0, AttributeModifier.Operation.ADD_VALUE));
            } else if ("extra_hearts".equals(effectId)) {
                maxHealth.removeModifier(EXTRA_HEARTS_HEALTH_MODIFIER_ID);
                maxHealth.addPermanentModifier(new AttributeModifier(EXTRA_HEARTS_HEALTH_MODIFIER_ID, 20.0, AttributeModifier.Operation.ADD_VALUE));
            }
        }
        if ("creative_flight".equals(effectId)) {
            serverPlayer.getAbilities().mayfly = true;
            serverPlayer.onUpdateAbilities();
        }
        if ("keep_inventory_berries_only".equals(effectId)) {
            player.level().getServer().getGameRules().set(GameRules.KEEP_INVENTORY, true, player.level().getServer());
        }

        if (isInitialApply && !wasAlreadyActive) {
            var enchantRegistry = player.level().holderLookup(Registries.ENCHANTMENT);
            if ("op_bow_only".equals(effectId)) {
                ItemStack opBow = new ItemStack(Items.BOW);
                opBow.enchant(enchantRegistry.getOrThrow(Enchantments.INFINITY), 1);
                opBow.enchant(enchantRegistry.getOrThrow(Enchantments.MENDING), 1);
                opBow.enchant(enchantRegistry.getOrThrow(Enchantments.POWER), 10);
                opBow.enchant(enchantRegistry.getOrThrow(Enchantments.LOOTING), 10);
                opBow.enchant(enchantRegistry.getOrThrow(Enchantments.KNOCKBACK), 10);
                opBow.set(DataComponents.CUSTOM_NAME, Component.literal("The Only Weapon"));
                player.getInventory().add(opBow);
                player.getInventory().add(new ItemStack(Items.ARROW));
            }
            if ("backpack_no_shulkers".equals(effectId)) {
                player.getInventory().add(new ItemStack(ModItems.BACKPACK.get()));
            }
            if ("daily_totem_no_armor".equals(effectId)) {
                ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
                CustomData.update(DataComponents.CUSTOM_DATA, totem, tag -> tag.putBoolean("wyr_locked_totem", true));
                player.setItemSlot(EquipmentSlot.OFFHAND, totem);
            }
            if ("stack_64_world_height".equals(effectId)) {
                // Teleport to the top of the world (max build height)
                int worldHeight = serverPlayer.level().dimensionType().minY() + serverPlayer.level().dimensionType().height();
                serverPlayer.teleportTo((ServerLevel) serverPlayer.level(), serverPlayer.getX(), worldHeight, serverPlayer.getZ(), java.util.Set.of(), serverPlayer.getYRot(), serverPlayer.getXRot(), false);
                // Set all stackable items to a full stack of 64
                for (int i = 0; i < serverPlayer.getInventory().getContainerSize(); i++) {
                    ItemStack stack = serverPlayer.getInventory().getItem(i);
                    if (!stack.isEmpty() && stack.isStackable()) {
                        stack.setCount(stack.getMaxStackSize());
                    }
                }
            }

            switch (effectId) {
                case "fire_sword_self_damage" -> {
                    ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
                    sword.enchant(enchantRegistry.getOrThrow(Enchantments.FIRE_ASPECT), 5);
                    serverPlayer.getInventory().add(sword);
                }
                case "lava_fishing_rod" -> {
                    ItemStack rod = new ItemStack(Items.FISHING_ROD);
                    rod.enchant(enchantRegistry.getOrThrow(Enchantments.LUCK_OF_THE_SEA), 5);
                    rod.enchant(enchantRegistry.getOrThrow(Enchantments.LURE), 5);
                    serverPlayer.getInventory().add(rod);
                }
                case "unbreakable_anvil_no_xp" -> {
                    ItemStack anvil = new ItemStack(Items.ANVIL);
                    anvil.set(DataComponents.UNBREAKABLE, net.minecraft.util.Unit.INSTANCE);
                    serverPlayer.getInventory().add(anvil);
                }
                case "golem_suit_slow" -> {
                    try {
                        serverPlayer.getInventory().add(new ItemStack(ModItems.GOLEM_HELMET.get()));
                        serverPlayer.getInventory().add(new ItemStack(ModItems.GOLEM_CHESTPLATE.get()));
                        serverPlayer.getInventory().add(new ItemStack(ModItems.GOLEM_LEGGINGS.get()));
                        serverPlayer.getInventory().add(new ItemStack(ModItems.GOLEM_BOOTS.get()));
                        serverPlayer.sendSystemMessage(Component.literal("You received a full golem armor set!"));
                    } catch (Exception e) {
                        serverPlayer.sendSystemMessage(Component.literal("Error giving golem armor: " + e.getMessage()));
                        WYR.LOGGER.error("Error giving golem armor to player", e);
                    }
                }
                case "personal_teleporter_scramble" -> serverPlayer.getInventory().add(new ItemStack(ModItems.PERSONAL_TELEPORTER.get()));
                case "texture_scramble" -> serverPlayer.getInventory().add(new ItemStack(ModItems.UNLIMITED_SODA.get()));
                case "unlimited_ice_cream_brain_freeze" -> serverPlayer.getInventory().add(new ItemStack(ModItems.UNLIMITED_ICE_CREAM.get()));
                case "infinite_xp_no_anvils" -> serverPlayer.setExperienceLevels(1000000000);
            }
        }

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }

        // Sync all client-side effect flags so effects like
        // see_mob_hp_double_health actually reach the client. Previously the
        // sync packet was only sent from syncAllClientEffects(), which is not
        // called here, so the client's hasSeeMobHp flag was never set and the
        // HP bar never rendered.
        if (serverPlayer != null) {
            syncAllClientEffects(serverPlayer);
        }
    }

    public static void removeEffect(Player player, String effectId) {
        getActiveEffects(player).remove(effectId);
        ServerPlayer serverPlayer = getServerPlayer(player);
        if (serverPlayer == null) return;
        syncAllClientEffects(serverPlayer);

        if ("strength_ii".equals(effectId)) {
            serverPlayer.level().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("minecraft", "shield")))
                    .ifPresent(recipeHolder -> serverPlayer.getRecipeBook().add(recipeHolder.id()));
        }

        switch (effectId) {
            case "cheap_trades_half_hunger":
                // Restore full hunger when the effect is removed/cleared
                serverPlayer.getFoodData().setFoodLevel(20);
                PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncHalfHungerStatus(false));
                break;
            case "texture_scramble":
                PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncTextureScrambleStatus(false));
                break;
            case "sneak_bonemeal_fov_change":
                PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncFovChangeStatus(false));
                break;
            case "no_fall_damage_low_render_distance":
                PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncRenderDistanceStatus(false));
                break;
            case "fast_swim_muted":
                PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncMuteStatus(false));
                serverPlayer.removeEffect(MobEffects.DOLPHINS_GRACE);
                break;
            case "infinite_xp_no_anvils":
                serverPlayer.setExperienceLevels(0);
                serverPlayer.setExperiencePoints(0);
                break;
            case "no_hunger_random_shader":
                PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncShaderStatus(false));
                break;
            case "stacked_logs":
                PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncStackedLogsStatus(false));
                break;
            case "keep_inventory_berries_only":
                if (net.coba.wyr.WYR.getServer() != null) {
                    boolean anotherPlayerHasEffect = false;
                    for (ServerPlayer p : net.coba.wyr.WYR.getServer().getPlayerList().getPlayers()) {
                        if (!p.getUUID().equals(player.getUUID()) && hasEffect(p, "keep_inventory_berries_only")) {
                            anotherPlayerHasEffect = true;
                            break;
                        }
                    }
                    if (!anotherPlayerHasEffect) {
                        player.level().getServer().getGameRules().set(GameRules.KEEP_INVENTORY, false, player.level().getServer());
                    }
                }
                break;
            case "hero_of_village_random_sounds":
                PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncRandomSoundsStatus(false));
                serverPlayer.removeEffect(MobEffects.HERO_OF_THE_VILLAGE);
                break;
            case "resistance_ii":
                serverPlayer.removeEffect(MobEffects.RESISTANCE);
                break;
            case "water_walk":
                serverPlayer.removeEffect(MobEffects.SLOWNESS);
                break;
            case "night_vision":
                serverPlayer.removeEffect(MobEffects.NIGHT_VISION);
                break;
            case "daily_totem_no_armor":
                // Remove the locked totem from offhand when choice is cleared
                ItemStack offhandItem = player.getItemBySlot(EquipmentSlot.OFFHAND);
                CustomData customData = offhandItem.get(DataComponents.CUSTOM_DATA);
                boolean hasLockedTotem = customData != null && customData.copyTag().getBooleanOr("wyr_locked_totem", false);
                if (offhandItem.is(Items.TOTEM_OF_UNDYING) && hasLockedTotem) {
                    player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                    player.sendSystemMessage(Component.literal("Daily totem removed as choice is cleared."));
                }
                break;
            case "fast_climb_short":
                AttributeInstance scaleAttr = serverPlayer.getAttribute(Attributes.SCALE);
                if (scaleAttr != null) scaleAttr.removeModifier(SCALE_MODIFIER_ID);
                break;
            case "fast_day_slow_night":
            case "fast_night_slow_day":
                serverPlayer.removeEffect(MobEffects.SPEED);
                serverPlayer.removeEffect(MobEffects.SLOWNESS);
                break;
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
            case "golem_suit_slow":
                AttributeInstance health = serverPlayer.getAttribute(Attributes.MAX_HEALTH);
                if (health != null) health.removeModifier(GOLEM_HEALTH_MODIFIER_ID);
                AttributeInstance strength = serverPlayer.getAttribute(Attributes.ATTACK_DAMAGE);
                if (strength != null) strength.removeModifier(GOLEM_STRENGTH_MODIFIER_ID);
                AttributeInstance speed = serverPlayer.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speed != null) speed.removeModifier(GOLEM_SPEED_MODIFIER_ID);
                break;
            case "one_hit_dragon_no_end":
                hasEnteredEnd.remove(serverPlayer.getUUID());
                break;
            case "fishing_luck":
                fishingCatchCount.remove(serverPlayer.getUUID());
                break;
            case "random_good_bad_potions":
                // Reset effect reapplication
                break;
        }
    }

    public static void clearActiveEffects(Player player) {
        UUID playerUUID = player.getUUID();
        wasSneaking.remove(playerUUID);
        activeEffects.remove(playerUUID);
        playerTickCounters.remove(playerUUID);
        teleportCooldowns.remove(playerUUID);
        dailyDiamondCooldown.remove(playerUUID);
        respawnLocations.remove(playerUUID);
        respawnDimensions.remove(playerUUID);
        fastHorses.remove(playerUUID);
        tamedMonsters.remove(playerUUID);
        randomSoundTimers.remove(playerUUID);
        spawnBiomes.remove(playerUUID);

        ServerPlayer serverPlayer = getServerPlayer(player);
        if (serverPlayer != null) {
            serverPlayer.refreshDimensions();
            AttributeInstance maxHealth = serverPlayer.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.removeModifier(INSTA_HEAL_HEALTH_MODIFIER_ID);
                maxHealth.removeModifier(EXTRA_HEARTS_HEALTH_MODIFIER_ID);
                maxHealth.removeModifier(GOLEM_HEALTH_MODIFIER_ID);
            }
            AttributeInstance strength = serverPlayer.getAttribute(Attributes.ATTACK_DAMAGE);
            if (strength != null) strength.removeModifier(GOLEM_STRENGTH_MODIFIER_ID);
            AttributeInstance speed = serverPlayer.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) speed.removeModifier(GOLEM_SPEED_MODIFIER_ID);

            if (serverPlayer.getHealth() > serverPlayer.getMaxHealth()) {
                serverPlayer.setHealth(serverPlayer.getMaxHealth());
            }
            if (!serverPlayer.isCreative()) {
                serverPlayer.getAbilities().mayfly = false;
                serverPlayer.getAbilities().flying = false;
                serverPlayer.onUpdateAbilities();
            }
            AttributeInstance scale = serverPlayer.getAttribute(Attributes.SCALE);
            if (scale != null) {
                scale.removeModifier(SCALE_MODIFIER_ID);
            }
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        WyrCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity(); // Get the player once

        // Existing logic for another effect
        if (hasEffect(player, "hidden_from_mobs_slow_ores") && isOre(event.getState())) {
            event.setNewSpeed(event.getOriginalSpeed() / 2.0F);
        }

        // --- THIS IS THE NEW LOGIC TO ADD ---
        if (hasEffect(player, "triple_drops_slow_break")) {
            ItemStack heldItem = player.getMainHandItem();

            // The isNaturalBlock() check is now added to this condition
            if (!heldItem.isEmpty()
                    && heldItem.isCorrectToolForDrops(event.getState())
                    && isNaturalBlock(event.getState())) {

                // Make blocks take 3x longer to break (divide speed by 3)
                event.setNewSpeed(event.getOriginalSpeed() / 3.0F);
            }
        }
    }

    public static void clearAllEffects(Player player) {
        ServerPlayer serverPlayer = getServerPlayer(player); // Get the serverPlayer instance
        if (serverPlayer == null) return;

        Set<String> effects = new HashSet<>(getActiveEffects(player));
        for (String effectId : effects) {
            removeEffect(player, effectId);
        }

        PlayerChoiceTracker.clearChoices(serverPlayer);
        clearActiveEffects(player);
        syncAllClientEffects(serverPlayer);
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        clearActiveEffects(event.getEntity());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        activeEffects.clear();
        playerTickCounters.clear();
        teleportCooldowns.clear();
        dailyDiamondCooldown.clear();
        respawnLocations.clear();
        respawnDimensions.clear();
        fastHorses.clear();
        tamedMonsters.clear();
        randomSoundTimers.clear();
        spawnBiomes.clear();
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer() == null) return;
        long currentTime = event.getServer().overworld().getGameTime();
        WyrSavedData savedData = WyrSavedData.get(event.getServer().overworld());

        int globalInterval = savedData.getGlobalPromptInterval();

        talkingVillagerTimers.entrySet().removeIf(entry -> {
            if (entry.getValue() <= 0) {
                Entity entity = event.getServer().overworld().getEntity(entry.getKey());
                if (entity instanceof Villager villager) {
                    villager.setCustomName(null);
                    villager.setCustomNameVisible(false);
                }
                return true;
            }
            return false;
        });
        talkingVillagerTimers.replaceAll((uuid, timer) -> timer - 1);

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            int interval = (globalInterval > 0) ? globalInterval : savedData.getPlayerPromptInterval(player.getUUID());
            long nextPromptTime = savedData.getNextPromptTime(player.getUUID());

            if (interval > 0 && nextPromptTime == Long.MAX_VALUE) {
                savedData.setNextPromptTime(player.getUUID(), currentTime + interval);
                nextPromptTime = currentTime + interval;
            }

            if (interval > 0 && PlayerChoiceTracker.getPromptCount(player) < 40 && !PlayerChoiceTracker.isPromptPending(player)) {
                if (currentTime >= nextPromptTime) {
                    try {
                        // STEP 1: Test choice selection without packet sending
                        System.out.println("INTERVAL TIMER TRIGGERED for player " + player.getName().getString() + 
                                         " with interval " + interval + " ticks");
                        
                        List<Choice> newChoices = getSimpleRandomChoices(player);
                        if (newChoices != null && !newChoices.isEmpty()) {
                            System.out.println("Successfully selected " + newChoices.size() + " choices:");
                            for (Choice choice : newChoices) {
                                System.out.println("  - " + choice.getId() + ": " + choice.positive() + " / " + choice.negative());
                            }
                            
                            // CLIENT-SIDE POPUP SYSTEM: Set flag and let client handle popup display
                            try {
                                PlayerChoiceTracker.setPromptPending(player, true);
                                
                                // Validate choices before setting flag
                                boolean allChoicesValid = true;
                                for (Choice choice : newChoices) {
                                    if (choice == null || choice.getId() == null || choice.getId().isEmpty()) {
                                        allChoicesValid = false;
                                        break;
                                    }
                                }
                                
                                if (allChoicesValid) {
                                    System.out.println("Setting popup flag for player " + player.getName().getString() + " with " + newChoices.size() + " choices:");
                                    for (Choice choice : newChoices) {
                                        System.out.println("  - " + choice.getId() + ": " + choice.positive() + " / " + choice.negative());
                                    }
                                    
                                    // Store choices for client-side retrieval
                                    savedData.setPendingChoices(player.getUUID(), newChoices);
                                    
                                    // Trigger the client-side WyrScreen popup using a simple trigger packet
                                    try {
                                        System.out.println("Triggering client-side WyrScreen popup for player " + player.getName().getString());
                                        for (Choice choice : newChoices) {
                                            System.out.println("  - " + choice.getId() + ": " + choice.positive() + " / " + choice.negative());
                                        }
                                        
                                        // Store choices for client-side retrieval
                                        savedData.setPendingChoices(player.getUUID(), newChoices);
                                        
                                        // Send the trigger packet to the client
                                        PacketDistributor.sendToPlayer(player, new PacketTriggerClientPopup());
                                        
                                        System.out.println("Successfully sent trigger packet to player " + player.getName().getString());
                                    } catch (Exception popupError) {
                                        System.err.println("Error sending trigger packet: " + popupError.getMessage());
                                        popupError.printStackTrace();
                                        
                                        // Fallback to command message if trigger fails
                                        try {
                                            Component fallbackMessage = Component.literal("=== WOULD YOU RATHER ===\n")
                                                .append(Component.literal("1. " + newChoices.get(0).positive() + " / " + newChoices.get(0).negative() + "\n"))
                                                .append(Component.literal("2. " + newChoices.get(1).positive() + " / " + newChoices.get(1).negative() + "\n"))
                                                .append(Component.literal("Use /wyr choice <1 or 2> to select"))
                                                .withStyle(ChatFormatting.YELLOW);
                                            
                                            player.sendSystemMessage(fallbackMessage);
                                            System.out.println("Fallback: Sent choices via chat to player " + player.getName().getString());
                                        } catch (Exception fallbackError) {
                                            System.err.println("Even fallback chat message failed for player " + player.getName().getString() + ": " + fallbackError.getMessage());
                                        }
                                    }
                                } else {
                                    System.err.println("Invalid choices detected, not setting popup flag for player " + player.getName().getString());
                                    PlayerChoiceTracker.setPromptPending(player, false);
                                }
                                
                            } catch (Exception popupError) {
                                System.err.println("Error setting popup flag for player " + player.getName().getString() + ": " + popupError.getMessage());
                                popupError.printStackTrace();
                                
                                // Fallback to chat message if popup fails
                                try {
                                    Component fallbackMessage = Component.literal("=== WOULD YOU RATHER ===\n")
                                        .append(Component.literal("1. " + newChoices.get(0).positive() + " / " + newChoices.get(0).negative() + "\n"))
                                        .append(Component.literal("2. " + newChoices.get(1).positive() + " / " + newChoices.get(1).negative() + "\n"))
                                        .append(Component.literal("Use /wyr choice <1 or 2> to select"))
                                        .withStyle(ChatFormatting.YELLOW);
                                    
                                    player.sendSystemMessage(fallbackMessage);
                                    System.out.println("Fallback: Sent choices via chat to player " + player.getName().getString());
                                } catch (Exception fallbackError) {
                                    System.err.println("Even fallback chat message failed for player " + player.getName().getString() + ": " + fallbackError.getMessage());
                                }
                            }
                        } else {
                            System.out.println("No choices available for player " + player.getName().getString());
                        }
                        
                        // Reset the timer for next interval
                        savedData.setNextPromptTime(player.getUUID(), currentTime + interval);
                        
                    } catch (Exception e) {
                        System.err.println("Error in minimal interval timer for player " + player.getName().getString() + ": " + e.getMessage());
                        savedData.setNextPromptTime(player.getUUID(), currentTime + interval);
                    }
                }
            }
        }
    }

    private static List<Choice> getSimpleRandomChoices(ServerPlayer player) {
        try {
            // Get all available choices from the registry
            List<Choice> allChoices = new ArrayList<>(ChoiceRegistry.getAllChoices());
            
            // Remove any null or invalid choices
            allChoices.removeIf(choice -> choice == null || choice.getId() == null || choice.getId().isEmpty());
            
            // Get player's active choices to avoid conflicts
            Set<String> activeChoiceIds = PlayerChoiceTracker.getChoices(player)
                    .stream()
                    .map(Choice::getId)
                    .collect(Collectors.toSet());
            
            // Remove active choices and their conflicts
            allChoices.removeIf(choice -> {
                String choiceId = choice.getId();
                return ChoiceRegistry.conflictsWith(choiceId, activeChoiceIds);
            });
            
            // If we have at least 2 choices, pick 2 random ones
            if (allChoices.size() >= 2) {
                Collections.shuffle(allChoices);
                List<Choice> selectedChoices = new ArrayList<>();
                selectedChoices.add(allChoices.get(0));
                selectedChoices.add(allChoices.get(1));
                return selectedChoices;
            }
            
            return Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error in getSimpleRandomChoices: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @SubscribeEvent
    public void onPlayerInteractWithVillager(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof Villager villager && event.getEntity() instanceof Player player) {

            if (player.level().isClientSide()) {
                return;
            }

            if (hasEffect(player, "cheap_trades_half_hunger") || hasEffect(player, "cheap_trades_no_ore_drops")) {
                // This new logic applies a temporary, player-specific discount
                for (MerchantOffer offer : villager.getOffers()) {
                    // Calculate the discount needed to make the price 1
                    int discount = -offer.getCostA().getCount() + 1;
                    offer.addToSpecialPriceDiff(discount);
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        
        // Block mace repair in crafting table
        if (hasEffect(player, "mace_max_damage_weak")) {
            ItemStack crafted = event.getCrafting();
            if (crafted.is(Items.MACE)) {
                // Clear the crafting result to prevent the repair
                event.getCrafting().setCount(0);
                player.sendSystemMessage(Component.literal("You cannot repair maces with this effect!"));
            }
        }
        ItemStack craftedItem = event.getCrafting();
        // Check if the crafted item is our special unbreakable tool
        if (event.getCrafting().get(DataComponents.UNBREAKABLE) != null &&
                event.getCrafting().getOrDefault(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false)) {

            // If it is, check if the player has the effect
            if (hasEffect(event.getEntity(), "unbreakable_tools_netherite")) {
                // Clear all items from the crafting grid
                event.getInventory().clearContent();
            }
        }

        CustomData customData = craftedItem.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.copyTag().getBooleanOr("wyr_delete_item_marker", false)) {
            if (hasEffect(player, "enchant_level_x_delete_item")) {
                CustomData.update(DataComponents.CUSTOM_DATA, craftedItem, tag -> tag.remove("wyr_delete_item_marker"));

                List<ItemStack> inventoryItems = player.getInventory().getNonEquipmentItems().stream()
                        .filter(s -> !s.isEmpty() && !s.equals(craftedItem))
                        .collect(Collectors.toList());

                if (!inventoryItems.isEmpty()) {
                    // --- FIX: This now shrinks the stack by 1 instead of deleting the whole stack ---
                    ItemStack itemToShrink = inventoryItems.get(player.getRandom().nextInt(inventoryItems.size()));
                    itemToShrink.shrink(1);
                } else {
                    // Damage player if no items to delete
                    player.hurt(player.damageSources().magic(), 4.0F);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (hasEffect(player, "water_walk")) {
                // Prevent lava/fire damage while the player is on a lava surface
                // (the block at or just below their feet is lava). If the player is
                // submerged deeper in lava or walks into fire on normal ground,
                // they should still take damage.
                boolean feetLava = player.level().getFluidState(player.blockPosition()).is(FluidTags.LAVA);
                boolean belowLava = player.level().getFluidState(player.blockPosition().below()).is(FluidTags.LAVA);
                boolean onLavaSurface = belowLava || (feetLava && player.getY() % 1.0 > 0.9);
                boolean isLava = event.getSource() == player.damageSources().lava();
                boolean isFire = event.getSource().is(DamageTypeTags.IS_FIRE);
                if ((isLava || isFire) && onLavaSurface) {
                    event.setCanceled(true);
                }
            }
            if (hasEffect(player, "nether_water_fast_lava") && event.getSource() == player.damageSources().lava()) {
                // Double the lava damage
                event.setAmount(event.getAmount() * 2.0F);
            }
        }

        // see_mob_hp_double_health: the player deals half damage to mobs
        // (equivalent to mobs having 2x health against this player).
        if (event.getSource().getEntity() instanceof Player attacker
                && hasEffect(attacker, "see_mob_hp_double_health")
                && event.getEntity() instanceof LivingEntity
                && !(event.getEntity() instanceof Player)) {
            event.setAmount(event.getAmount() * 0.5F);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (hasEffect(player, "eat_anything_hunger_chance") && player.isUsingItem()) {
            ItemStack stack = player.getUseItem();
            if (stack.get(DataComponents.FOOD) == null) {
                int useTicks = player.getTicksUsingItem();
                if (useTicks % 4 == 0) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.GENERIC_EAT, SoundSource.PLAYERS,
                            0.5F + 0.5F * (float)player.getRandom().nextInt(2),
                            (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F + 1.0F);
                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(new net.minecraft.core.particles.ItemParticleOption(ParticleTypes.ITEM, stack.getItem()),
                                player.getX(), player.getY() + player.getEyeHeight() - 0.1, player.getZ(),
                                5, 0.1, 0.1, 0.1, 0.05);
                    }
                }
            }
        }

        Long penaltyUntil = durabilityPenaltyPlayers.get(player.getUUID());
        if (penaltyUntil != null) {
            if (player.level().getGameTime() < penaltyUntil) {
                for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                    if (stack.isDamageableItem()) {
                        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                        if (!customData.copyTag().getBooleanOr("wyr_penalized", false)) {
                            int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();
                            int damageToAdd = remainingDurability / 2;
                            stack.setDamageValue(stack.getDamageValue() + damageToAdd);

                            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean("wyr_penalized", true));
                        }
                    }
                }
            } else {
                durabilityPenaltyPlayers.remove(player.getUUID());
            }
        }

        if (hasEffect(player, "no_hunger_no_regen")) {
            if (player.getFoodData().needsFood()) {
                player.getFoodData().setFoodLevel(20);
                player.getFoodData().setSaturation(5.0F);
            }
        }

        if (hasEffect(player, "auto_repair_no_enchant") && player.tickCount % 20 == 0) {
            // Repair all items in hotbar and main inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isDamageableItem() && stack.getDamageValue() > 0) {
                    stack.setDamageValue(Math.max(0, stack.getDamageValue() - 5));
                }
            }
        }

        if (hasEffect(player, "item_magnet_no_stack")) {
            AABB area = player.getBoundingBox().inflate(10.0);
            List<ItemEntity> items = player.level().getEntitiesOfClass(ItemEntity.class, area);
            for (ItemEntity itemEntity : items) {
                itemEntity.snapTo(player.getX(), player.getY(), player.getZ(), 0.0F, 0.0F);
            }
            List<ExperienceOrb> orbs = player.level().getEntitiesOfClass(ExperienceOrb.class, area);
            for (ExperienceOrb orb : orbs) {
                orb.snapTo(player.getX(), player.getY(), player.getZ(), 0.0F, 0.0F);
            }

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getCount() > 1) {
                    int excess = stack.getCount() - 1;
                    player.drop(new ItemStack(stack.getItem(), excess), false, null);
                    stack.setCount(1);
                }
            }
        }

        if (hasEffect(player, "Highlight_ores_no_ladders") && player.onClimbable()) {
            Vec3 motion = player.getDeltaMovement();
            if (motion.y > 0) {
                player.setDeltaMovement(motion.x, -0.2D, motion.z);
            }
        }

        if (hasEffect(player, "cheap_trades_half_hunger")) {
            var foodData = player.getFoodData();

            if (foodData.getFoodLevel() > 10) {
                foodData.setFoodLevel(10);
            }
            // Cap at 10 every tick to ensure it stays at half hunger
            if (foodData.getFoodLevel() > 10) {
                foodData.setFoodLevel(10);
            }

            if (player.level().getGameRules().get(GameRules.NATURAL_HEALTH_REGENERATION) &&
                    player.getHealth() < player.getMaxHealth() &&
                    foodData.getFoodLevel() >= 10 &&
                    player.tickCount % 80 == 0)
            {
                player.heal(1.0F);
            }
        }

        if (hasEffect(player, "strength_ii")) {
            ItemStack carried = player.containerMenu.getCarried();
            if (carried.is(Items.SHIELD)) {
                player.containerMenu.setCarried(ItemStack.EMPTY);
                player.drop(carried, true, null);
            }

            for (int i = 0; i < player.getInventory().getNonEquipmentItems().size(); i++) {
                ItemStack stackInSlot = player.getInventory().getNonEquipmentItems().get(i);
                if (stackInSlot.is(Items.SHIELD)) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                    player.drop(stackInSlot, true, null);
                }
            }
        }

        if (hasEffect(player, "mob_loot_inventory_slots")) {
            for (int i = 9; i < 36; i++) {
                ItemStack stackInSlot = player.getInventory().getItem(i);
                if (!stackInSlot.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                    player.drop(stackInSlot, true, null);
                }
            }
        }

        if (hasEffect(player, "fast_climb_short") && player.onClimbable()) {
            // Enhanced climbing speed for ladders and other climbable blocks
            Vec3 motion = player.getDeltaMovement();
            
            if (player.isCrouching()) {
                // Faster descent when crouching (sliding down)
                player.setDeltaMovement(motion.x, Math.min(motion.y * 1.5, -0.3), motion.z);
            } else if (player.isSprinting() || player.isJumping()) {
                // Much faster upward climbing when sprinting or jumping
                player.setDeltaMovement(motion.x, Math.min(motion.y * 2.5, 0.4), motion.z);
            } else if (motion.y > 0) {
                // Normal fast climbing upward
                player.setDeltaMovement(motion.x, Math.min(motion.y * 2.0, 0.3), motion.z);
            } else {
                // Faster downward movement
                player.setDeltaMovement(motion.x, Math.max(motion.y * 1.5, -0.2), motion.z);
            }
        }

        if (hasEffect(player, "lava_fishing_rod")) {
            if (player.fishing != null) {
                if (player.fishing.isInWater()) {
                    player.fishing.discard();
                }
            }
        }

        if (hasEffect(player, "infinite_sprint_no_elytra")) {
            ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chestItem.is(Items.ELYTRA)) {
                player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);

                if (!player.getInventory().add(chestItem)) {
                    player.drop(chestItem, false, null);
                }
            }
        }

        boolean isSneakingNow = player.isCrouching();
        boolean wasSneakingBefore = wasSneaking.getOrDefault(player.getUUID(), false);

        if (hasEffect(player, "sneak_bonemeal_fov_change") && isSneakingNow && !wasSneakingBefore) {
            BlockPos center = player.blockPosition();
            ServerLevel level = (ServerLevel) player.level();

            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, -1, -3), center.offset(3, 1, 3))) {
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof BonemealableBlock bonemealable) {
                    if (bonemealable.isValidBonemealTarget(level, pos, state, null)) {
                        bonemealable.performBonemeal(level, player.getRandom(), pos, state, null);
                        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                15, 0.5, 0.5, 0.5, 0.0);
                    }
                }
            }
        }
        wasSneaking.put(player.getUUID(), isSneakingNow);

        if (hasEffect(player, "random_food_no_jump")) {
            // Disable jump by removing any jump effects and preventing jumping
            if (player.hasEffect(MobEffects.JUMP_BOOST)) {
                player.removeEffect(MobEffects.JUMP_BOOST);
            }
            // Prevent jumping by setting jumping to false
            player.setJumping(false);
            // Also reduce jump power to prevent jumping
            player.setJumping(false);
        }

        if (hasEffect(player, "enchant_level_x_delete_item")) {
            List<ItemStack> allItems = new ArrayList<>(player.getInventory().getNonEquipmentItems());
            allItems.add(player.containerMenu.getCarried());

            for (ItemStack stack : allItems) {
                if (!stack.isEmpty()) {
                    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                    if (customData != null && customData.copyTag().getBooleanOr("wyr_delete_item_marker", false)) {
                        CompoundTag tag = customData.copyTag();
                        tag.remove("wyr_delete_item_marker");
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                        List<ItemStack> inventoryItems = player.getInventory().getNonEquipmentItems().stream()
                                .filter(s -> !s.isEmpty() && !s.equals(stack))
                                .collect(Collectors.toList());

                        if (!inventoryItems.isEmpty()) {
                            inventoryItems.get(player.getRandom().nextInt(inventoryItems.size())).shrink(1);
                        } else {
                            player.hurt(player.damageSources().magic(), 4.0F);
                        }
                        break;
                    }
                }
            }
        }

        if (hasEffect(player, "fast_horse_dark_fear") && player.level().getOverworldClockTime() % 24000 >= 13000 && player.level().getOverworldClockTime() % 24000 < 23000) {
            Horse horse = fastHorses.get(player.getUUID());
            if (horse != null) {
                // Prevent riding at night
                if (player.isPassenger() && player.getVehicle() == horse) {
                    player.stopRiding();
                    player.sendSystemMessage(Component.literal("Your horse is afraid of the dark!"));
                }
                // Move toward light source and stay there
                if (!horse.isLeashed()) {
                    BlockPos lightPos = findNearestLightSource(horse);
                    if (lightPos != null) {
                        horse.getNavigation().moveTo(lightPos.getX(), lightPos.getY(), lightPos.getZ(), 1.0);
                    }
                }
            }
        }


        if (hasEffect(player, "choose_spawn_biome_lock")) {
            // Apply a pending respawn teleport on the first tick after respawn
            // so the player appears directly in the locked biome rather than
            // spawning at their bed/world spawn and being dragged back.
            BlockPos pendingRespawn = pendingBiomeRespawns.remove(player.getUUID());
            if (pendingRespawn != null) {
                ServerLevel respawnLevel = (ServerLevel) player.level();
                BlockPos safeRespawn = findSafeTeleportPosition(respawnLevel, pendingRespawn);
                player.teleportTo(respawnLevel, safeRespawn.getX() + 0.5, safeRespawn.getY(), safeRespawn.getZ() + 0.5,
                        java.util.Set.of(), player.getYRot(), player.getXRot(), false);
            }
            ResourceKey<net.minecraft.world.level.biome.Biome> spawnBiome = spawnBiomes.get(player.getUUID());
            if (spawnBiome == null && player instanceof ServerPlayer sp) {
                WyrSavedData data = WyrSavedData.get((ServerLevel) sp.level());
                String biomeStr = data.getPlayerSpawnBiome(player.getUUID());
                if (!biomeStr.isEmpty()) {
                    ResourceKey<net.minecraft.world.level.biome.Biome> loadedBiome = ResourceKey.create(net.minecraft.core.registries.Registries.BIOME, Identifier.parse(biomeStr));
                    spawnBiomes.put(player.getUUID(), loadedBiome);
                    spawnBiome = loadedBiome;
                }
            }
            if (spawnBiome != null) {
                // Check if 10 days have passed since effect was applied
                long effectStartTime = effectStartTimes.getOrDefault(player.getUUID(), player.level().getGameTime());
                long timeSinceEffect = player.level().getGameTime() - effectStartTime;
                if (timeSinceEffect < (10 * 24000)) { // 10 Minecraft days
                    ResourceKey<net.minecraft.world.level.biome.Biome> finalSpawnBiome = spawnBiome;
                    player.level().getBiome(player.blockPosition()).unwrapKey().ifPresent(currentBiome -> {
                        if (currentBiome != finalSpawnBiome) {
                            BlockPos spawn = spawnPositions.get(player.getUUID());
                            if (spawn == null && player instanceof ServerPlayer sp) {
                                WyrSavedData data = WyrSavedData.get((ServerLevel) sp.level());
                                spawn = data.getPlayerSpawnPosition(player.getUUID());
                                if (spawn != null) {
                                    spawnPositions.put(player.getUUID(), spawn);
                                }
                            }
                            if (spawn == null && player instanceof ServerPlayer serverPlayer && serverPlayer.getRespawnConfig() != null) {
                                spawn = serverPlayer.getRespawnConfig().respawnData().pos();
                            }
                            if (spawn == null) {
                                spawn = player.blockPosition();
                            }
                            if (spawn != null) {
                                // Calculate push-back target (4 blocks back towards spawn)
                                net.minecraft.world.phys.Vec3 pPos = player.position();
                                net.minecraft.world.phys.Vec3 sPos = new net.minecraft.world.phys.Vec3(spawn.getX() + 0.5, pPos.y, spawn.getZ() + 0.5);
                                double dx = pPos.x - sPos.x;
                                double dz = pPos.z - sPos.z;
                            double dist = Math.sqrt(dx * dx + dz * dz);
                                BlockPos targetPos;
                                if (dist > 4.0) {
                                    double ratio = (dist - 4.0) / dist;
                                    double tx = sPos.x + dx * ratio;
                                    double tz = sPos.z + dz * ratio;
                                    targetPos = new BlockPos((int) Math.floor(tx), (int) player.getY(), (int) Math.floor(tz));
                                } else {
                                    targetPos = spawn;
                                }
                            BlockPos safePos = findSafeTeleportPosition((ServerLevel) player.level(), targetPos);
                                player.teleportTo((ServerLevel) player.level(), safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, java.util.Set.of(), player.getYRot(), player.getXRot(), false);
                                
                                // Send chat message with remaining days (once every 40 ticks / 2s)
                                long gameTime = player.level().getGameTime();
                                long ticksLeft = (10 * 24000) - timeSinceEffect;
                                double daysLeft = (double) ticksLeft / 24000.0;
                                long lastMsg = lastBiomeMsgTime.getOrDefault(player.getUUID(), 0L);
                                if (gameTime - lastMsg >= 40) {
                                    String msg = String.format("You cannot leave biome until %.1f Minecraft days are done", daysLeft);
                                    player.sendSystemMessage(Component.literal(msg));
                                    lastBiomeMsgTime.put(player.getUUID(), gameTime);
                                }
                            }
                        }
                    });
                }
            }
        }

        if (hasEffect(player, "unbreakable_tools_netherite") && player.containerMenu instanceof CraftingMenu craftingMenu) {
            ItemStack centerItem = craftingMenu.getSlot(5).getItem();

            boolean patternMatches = !centerItem.isEmpty() && centerItem.isDamageableItem();
            if (patternMatches) {
                // Check for 16 netherite blocks (2 blocks per slot for 8 surrounding slots)
                int totalNetheriteBlocks = 0;
                for (int i = 1; i <= 9; i++) {
                    if (i == 5) continue;
                    ItemStack slotItem = craftingMenu.getSlot(i).getItem();
                    if (slotItem.is(Items.NETHERITE_BLOCK)) {
                        totalNetheriteBlocks += slotItem.getCount();
                    }
                }
                if (totalNetheriteBlocks != 16) {
                    patternMatches = false;
                }
            }

            if (patternMatches) {
                ItemStack unbreakableTool = centerItem.copy();
                unbreakableTool.set(DataComponents.UNBREAKABLE, net.minecraft.util.Unit.INSTANCE);
                unbreakableTool.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

                craftingMenu.getSlot(0).set(unbreakableTool);
            } else {
                ItemStack resultItem = craftingMenu.getSlot(0).getItem();
                if (!resultItem.isEmpty() && resultItem.get(DataComponents.UNBREAKABLE) != null) {
                    craftingMenu.getSlot(0).set(ItemStack.EMPTY);
                }
            }
        }


        boolean hasSoundEffect = hasEffect(player, "emeralds_from_mobs_siren") || hasEffect(player, "instant_tame_creeper_hiss");
        if (hasSoundEffect) {
            int timer = randomSoundTimers.getOrDefault(player.getUUID(), -1);
            if (timer == -1) {
                timer = 30 * 20 + (20*30);
                randomSoundTimers.put(player.getUUID(), timer);
            }

            timer--;
            if (timer <= 0) {
                DeferredHolder<SoundEvent, SoundEvent> soundToPlay = null;
                float volume = 1.0F, pitch = 1.0F;
                if (hasEffect(player, "emeralds_from_mobs_siren")) {
                    soundToPlay = ModSounds.EMERALDS_SIREN;
                    volume = 10.0F;
                } else if (hasEffect(player, "instant_tame_creeper_hiss")) {
                    soundToPlay = ModSounds.creeper_hiss;
                    volume = 0.8F;
                }
                if (soundToPlay != null) PacketDistributor.sendToPlayer(player, new PacketPlayCustomSound(soundToPlay.getId(), volume, pitch));

                timer = player.getRandom().nextInt(20 * 60) + (20*30); // Random interval between 30s and 1.5m
            }
            randomSoundTimers.put(player.getUUID(), timer);
        }else {
            randomSoundTimers.remove(player.getUUID());
        }

        if (hasEffect(player, "auto_smelt_random_sound")) {
            int autoSmeltTimer = autoSmeltSoundTimers.getOrDefault(player.getUUID(), 0);
            autoSmeltTimer--;

            if (autoSmeltTimer <= 0) {
                // Now we just call the one event, and sounds.json does the random part
                PacketDistributor.sendToPlayer(player, new PacketPlayCustomSound(ModSounds.auto_random_sound.getId(), 1.5F, 1.0F));

                // Random interval between 30s and 1.5m
                autoSmeltTimer = player.getRandom().nextInt(20 * 60) + (20 * 30);
            }
            autoSmeltSoundTimers.put(player.getUUID(), autoSmeltTimer);
        } else {
            // If the player doesn't have the effect, remove them from the timer map.
            autoSmeltSoundTimers.remove(player.getUUID());
        }

        Set<String> playerEffects = getActiveEffects(player);
        if (playerEffects.isEmpty()) return;

        // Permanent Potion Effects
        if (hasEffect(player, "speed_iv")) player.addEffect(new MobEffectInstance(MobEffects.SPEED, 2, 3, true, false));
        if (hasEffect(player, "haste_ii")) player.addEffect(new MobEffectInstance(MobEffects.HASTE, 2, 1, true, false));
        if (hasEffect(player, "high_jump")) player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 2, 4, true, false));
        if (hasEffect(player, "water_breathing")) player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 2, 0, true, false));
        if (hasEffect(player, "night_vision")) player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false));
        if (hasEffect(player, "dolphin_grace")) player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 2, 0, true, false));
        if (hasEffect(player, "fast_swim_muted")) {
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 1000000, 1, true, false, true));
        }
        if (hasEffect(player, "resistance_ii")) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1000000, 1, true, false, true));
            // Disable sprinting
            player.setSprinting(false);
        }
        if (hasEffect(player, "strength_ii")) player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 2, 1, true, false));
        if (hasEffect(player, "water_walk")) player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 1000000, 1, true, false, true));
        if (hasEffect(player, "hero_of_village_random_sounds")) player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 1000000, 0, true, false, true));
        
        // random_good_bad_potions is now handled by the effect event handler

        // Tick-based logic
        int ticks = playerTickCounters.getOrDefault(player.getUUID(), 0);
        if (hasEffect(player, "give_decoration_blind")) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 1000000, 1, true, false, true));
        }

        if (ticks % 20 == 0) { // Every second
            if (hasEffect(player, "passive_xp_no_mending")) player.giveExperiencePoints(7);
            
            // Cap food level at 10 and saturation at half for cheap_trades_half_hunger.
            // Capping saturation low ensures hunger drains at the normal rate
            // (high saturation would otherwise delay hunger loss after damage).
            if (hasEffect(player, "cheap_trades_half_hunger")) {
                if (player.getFoodData().getFoodLevel() > 10) {
                    player.getFoodData().setFoodLevel(10);
                }
                if (player.getFoodData().getSaturationLevel() > 5.0F) {
                    player.getFoodData().setSaturation(5.0F);
                }
            }
            
            // Check if player is riding horse at night and dismount
            if (hasEffect(player, "fast_horse_dark_fear")) {
                long gameTime = player.level().getOverworldClockTime() % 24000;
                boolean isNight = gameTime >= 13000 && gameTime < 23000;
                if (isNight && player.getVehicle() instanceof AbstractHorse) {
                    player.stopRiding();
                    player.sendSystemMessage(Component.literal("Your horse is afraid of the dark!"));
                }
            }
        }
        // Sync the random-food countdown to the client once per second so the
        // HUD can show how many seconds remain until the next food item.
        if (ticks % 20 == 0 && hasEffect(player, "random_food_no_jump") && player instanceof ServerPlayer sp) {
            int remainingTicks = 600 - (ticks % 600);
            PacketDistributor.sendToPlayer(sp, new PacketSyncFoodTimer(remainingTicks / 20));
        }
        if (ticks % 600 == 0) { // Every 30 seconds
            if (hasEffect(player, "wind_charge_respawn_mob")) player.getInventory().add(new ItemStack(Items.WIND_CHARGE));
            if (hasEffect(player, "random_food_no_jump")) {
                List<Item> foods = BuiltInRegistries.ITEM.stream().filter(item -> item.components().has(DataComponents.FOOD)).toList();
                if (!foods.isEmpty()) {
                    ItemStack foodStack = new ItemStack(foods.get(player.getRandom().nextInt(foods.size())));
                    // Capture the name BEFORE add() - add() empties the stack.
                    Component foodName = foodStack.getHoverName();
                    player.getInventory().add(foodStack);
                    player.sendSystemMessage(Component.literal("You received random food: ").append(foodName));
                }
            }
        }
        playerTickCounters.put(player.getUUID(), ticks + 1);

        // Negative Consequences & Other Logic
        if (hasEffect(player, "speed_iv")) player.getFoodData().addExhaustion(0.015f * 3);
        if (hasEffect(player, "water_breathing") && !player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) { ItemStack headItem = player.getItemBySlot(EquipmentSlot.HEAD); player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY); player.drop(headItem, true, null); }
        if (hasEffect(player, "extra_hearts") && !player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) { ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST); player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY); player.drop(chestItem, true, null); }
        if (hasEffect(player, "triple_damage") && !player.getItemBySlot(EquipmentSlot.FEET).isEmpty()) { ItemStack feetItem = player.getItemBySlot(EquipmentSlot.FEET); player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY); player.drop(feetItem, true, null); }
        if (hasEffect(player, "resistance_ii")) {
            player.setSprinting(false);
            // Remove speed potion effects
            if (player.hasEffect(MobEffects.SPEED)) {
                player.removeEffect(MobEffects.SPEED);
            }
            // Also prevent sprinting by disabling flight ability
            player.getAbilities().mayfly = false;
            // Continuously prevent sprinting
            if (player.isSprinting()) {
                player.setSprinting(false);
            }
        }
        if (hasEffect(player, "dolphin_grace") && !player.isInWater() && !player.isCreative()) player.hurt(player.damageSources().drown(), 1.0f);
        if (hasEffect(player, "fast_horse_dark_fear") && player.level().getOverworldClockTime() % 24000 >= 13000 && player.level().getOverworldClockTime() % 24000 < 23000) {
            Horse horse = fastHorses.get(player.getUUID());
            if (horse != null && !horse.isLeashed()) {
                horse.getNavigation().stop();
                horse.setDeltaMovement(0, horse.getDeltaMovement().y, 0);
            }
        }
        if (hasEffect(player, "no_hunger_random_shader") || hasEffect(player, "infinite_sprint_no_elytra")) {
            if (player.getFoodData().needsFood()) player.getFoodData().setFoodLevel(20);
        }
        if (hasEffect(player, "creative_flight") && player.getAbilities().flying) {
            if (ticks % 200 == 0) {
                List<ItemStack> inventory = new ArrayList<>(player.getInventory().getNonEquipmentItems());
                inventory.removeIf(ItemStack::isEmpty);
                if (!inventory.isEmpty()) {
                    inventory.get(player.getRandom().nextInt(inventory.size())).shrink(1);
                }
            }
        }
        if (hasEffect(player, "daily_diamond")) {
            long currentTime = player.level().getGameTime();
            long lastTime = dailyDiamondCooldown.getOrDefault(player.getUUID(), 0L);
            if (currentTime > lastTime && player.level().getOverworldClockTime() % 24000 < 100) {
                player.getInventory().add(new ItemStack(Items.DIAMOND_BLOCK));
                dailyDiamondCooldown.put(player.getUUID(), currentTime + 23000);
            }
            WyrSavedData savedData = WyrSavedData.get((ServerLevel) player.level());
            int currentThirst = savedData.getPlayerThirst(player.getUUID());
            float currentExhaustion = savedData.getPlayerThirstExhaustion(player.getUUID());
            if (currentExhaustion > 4.0F) {
                currentExhaustion -= 4.0F;
                if (currentThirst > 0) savedData.setPlayerThirst(player.getUUID(), currentThirst - 1);
            }
            if (currentThirst <= 0 && player.tickCount % 80 == 0) player.hurt(player.damageSources().starve(), 1.0F);
            if (player.tickCount % 20 == 0) PacketDistributor.sendToPlayer(player, new PacketSyncThirst(currentThirst));
            savedData.setPlayerThirstExhaustion(player.getUUID(), currentExhaustion);
        }
        if (hasEffect(player, "no_durability_loss_adventure_mode")) {
            // Prevent durability loss by setting items to unbreakable, but preserve current damage
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isDamageableItem() && !stack.has(DataComponents.UNBREAKABLE)) {
                    // Store current damage before making unbreakable
                    int currentDamage = stack.getDamageValue();
                    stack.set(DataComponents.UNBREAKABLE, net.minecraft.util.Unit.INSTANCE);
                    stack.setDamageValue(currentDamage); // Restore the damage value
                }
            }
        }
        if (hasEffect(player, "daily_totem_no_armor")) {
            // Daily totem logic - only give if no totem exists anywhere in inventory
            if ((player.level().getOverworldClockTime() % 24000 >= 23000 || player.level().getOverworldClockTime() % 24000 <= 13000) && player.level().getOverworldClockTime() % 24000 < 100 && ticks % 24000 > 100) {
                boolean hasTotemAnywhere = false;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.is(Items.TOTEM_OF_UNDYING)) {
                        hasTotemAnywhere = true;
                        break;
                    }
                }
                if (!hasTotemAnywhere) {
                    ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
                    CustomData.update(DataComponents.CUSTOM_DATA, totem, tag -> tag.putBoolean("wyr_locked_totem", true));
                    player.setItemSlot(EquipmentSlot.OFFHAND, totem);
                }
            }

            // Enforce the lock - keep totem in offhand and prevent any other totems
            ItemStack offhandItem = player.getOffhandItem();
            CustomData customData = offhandItem.get(DataComponents.CUSTOM_DATA);
            boolean hasLockedTotem = customData != null && customData.copyTag().getBooleanOr("wyr_locked_totem", false);
            if (!offhandItem.is(Items.TOTEM_OF_UNDYING) || !hasLockedTotem) {
                // Remove any totems from inventory and place locked totem in offhand
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.is(Items.TOTEM_OF_UNDYING)) {
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                    }
                }
                ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
                CustomData.update(DataComponents.CUSTOM_DATA, totem, tag -> tag.putBoolean("wyr_locked_totem", true));
                player.setItemInHand(InteractionHand.OFF_HAND, totem);
            }

            // Block armor wearing but allow picking it up
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                    ItemStack armorItem = player.getItemBySlot(slot);
                    if (!armorItem.isEmpty()) {
                        // Remove armor from equipment slot and move to inventory
                        player.setItemSlot(slot, ItemStack.EMPTY);
                        player.getInventory().add(armorItem);
                        player.sendSystemMessage(Component.literal("You cannot wear armor with this choice!"));
                    }
                }
            }
        }
    }

    public static void setSpawnBiomeLock(Player player, ResourceKey<net.minecraft.world.level.biome.Biome> biomeKey, BlockPos pos) {
        spawnBiomes.put(player.getUUID(), biomeKey);
        spawnPositions.put(player.getUUID(), pos);
    }

    private static BlockPos findNearestLightSource(Entity entity) {
        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;
        BlockPos center = entity.blockPosition();
        int searchRadius = 32;

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (entity.level().getBrightness(LightLayer.BLOCK, pos) > 7) {
                        double dist = Math.sqrt(x*x + y*y + z*z);
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            nearest = pos;
                        }
                    }
                }
            }
        }
        return nearest;
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        Set<String> playerEffects = getActiveEffects(player);
        if (playerEffects.isEmpty()) return;
        for (String effectId : playerEffects) {
            switch (effectId) {
                case "golem_suit_slow": handleGolemSet(player); break;
                case "midas_touch_no_hp_bar":
                    BlockPos below = player.blockPosition().below();
                    BlockState statebelowblock = player.level().getBlockState(below); // Get the block state once

                    // Add a check to ensure the block is not air
                    if (player.onGround() && !statebelowblock.is(Blocks.AIR) && !statebelowblock.is(Blocks.GOLD_BLOCK)) {
                        player.level().setBlock(below, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
                    }
                    break;
                case "double_debris_vampire":
                    if ((player.level().getOverworldClockTime() % 24000 >= 23000 || player.level().getOverworldClockTime() % 24000 <= 13000) && player.level().canSeeSky(player.blockPosition()) && !player.isCreative()) {
                        player.igniteForSeconds(2);
                    }
                    break;
                case "fast_day_slow_night":
                    long timeOfDay = player.level().getOverworldClockTime() % 24000;
                    // Day time: 0-13000 (dawn to dusk), Night time: 13000-24000 (dusk to dawn)
                    if (timeOfDay >= 0 && timeOfDay <= 13000) {
                        // Speed IV (amplifier 3) during day
                        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 3, true, false));
                    } else {
                        // Slowness II (amplifier 1) during night
                        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 1, true, false));
                    }
                    break;
                case "fast_night_slow_day":
                    long timeOfDay2 = player.level().getOverworldClockTime() % 24000;
                    // Day time: 0-13000 (dawn to dusk), Night time: 13000-24000 (dusk to dawn)
                    if (timeOfDay2 >= 0 && timeOfDay2 <= 13000) {
                        // Slowness II (amplifier 1) during day
                        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 1, true, false));
                    } else {
                        // Speed IV (amplifier 3) during night
                        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 3, true, false));
                    }
                    break;
                case "speed_v_random_keys": player.addEffect(new MobEffectInstance(MobEffects.SPEED, 2, 4, true, false)); break;
                case "hero_of_village_random_sounds": player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 2, 0, true, false)); break;
                case "slow_fall_side_place_only": player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 2, 0, true, false)); break;
                case "sneak_invisible_no_move":
                    if (player.isCrouching()) {
                        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 2, 0, true, false));
                        player.setDeltaMovement(Vec3.ZERO);
                        // Prevent movement by setting movement input to zero
                        player.xxa = 0;
                        player.zza = 0;
                    }
                    break;
                case "fast_swim_muted":
                    // Apply permanent Dolphin's Grace II effect
                    player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999, 1, true, false));
                    // Ensure mute status is sent to client
                    if (player instanceof ServerPlayer sp) {
                        PacketDistributor.sendToPlayer(sp, new PacketSyncMuteStatus(true));
                    }
                    break;
                case "fast_horse_dark_fear":
                    if (!(player.level().getOverworldClockTime() % 24000 < 12000) && player.getVehicle() instanceof AbstractHorse) {
                        player.stopRiding();
                        player.sendSystemMessage(Component.literal("Your horse is afraid of the dark and threw you off!"));
                    }
                    break;
                case "infinite_sprint_no_elytra": player.setSprinting(true); break;
            }
        }

        if (hasEffect(player, "water_walk")) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 2, 1, true, false));

            BlockPos feetPos = player.blockPosition();
            net.minecraft.world.level.material.FluidState feetFluid = player.level().getFluidState(feetPos);
            net.minecraft.world.level.material.FluidState belowFluid = player.level().getFluidState(feetPos.below());
            boolean feetInFluid = feetFluid.is(FluidTags.WATER) || feetFluid.is(FluidTags.LAVA);
            boolean belowIsFluid = belowFluid.is(FluidTags.WATER) || belowFluid.is(FluidTags.LAVA);

            if (feetInFluid || belowIsFluid) {
                // The surface is the top of the fluid block the player is over.
                double surfaceY = (feetInFluid ? feetPos.getY() : feetPos.getY() - 1) + 1.0D;
                // Keep the player on the surface (don't sink), but preserve
                // horizontal movement so shift + direction keys still move them.
                if (player.getY() < surfaceY - 0.01D) {
                    player.setPos(player.getX(), surfaceY, player.getZ());
                }
                player.setOnGround(true);
                player.fallDistance = 0F;
                if (player.getDeltaMovement().y < 0) {
                    player.setDeltaMovement(player.getDeltaMovement().x, 0.0D, player.getDeltaMovement().z);
                }
                // Prevent fire damage when walking on lava
                if (feetFluid.is(FluidTags.LAVA) || belowFluid.is(FluidTags.LAVA)) {
                    player.clearFire();
                }
            }
        }

        if (hasEffect(player, "stacked_logs")) {
            for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                if (stack.isCorrectToolForDrops(Blocks.OAK_LOG.defaultBlockState())) {
                    CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    if (!tag.getBooleanOr("WyrNerfed", false)) {
                        stack.set(DataComponents.MAX_DAMAGE, 10);
                        stack.setDamageValue(0);
                        tag.putBoolean("WyrNerfed", true);
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    }
                }
            }
        }
    }

    private static boolean isNaturalBlock(BlockState state) {
        return isOre(state) ||
                state.is(BlockTags.LOGS) ||
                state.is(BlockTags.DIRT) ||
                state.is(BlockTags.SAND) ||
                state.is(BlockTags.BASE_STONE_OVERWORLD) ||
                state.is(BlockTags.BASE_STONE_NETHER);
    }

    
    @SubscribeEvent
    public void onBlockBreak(net.neoforged.neoforge.event.level.block.BreakBlockEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;
        ServerPlayer serverPlayer = getServerPlayer(player);
        if (serverPlayer == null) return;

        BlockState state = event.getState();
        BlockPos pos = event.getPos();
        ServerLevel level = (ServerLevel) event.getLevel();
        ItemStack tool = player.getMainHandItem();

        if (hasEffect(player, "sleep_heal_random_drops")) {
            List<Item> allItems = BuiltInRegistries.ITEM.stream().toList();
            if (!allItems.isEmpty()) {
                Item randomItem = allItems.get(player.getRandom().nextInt(allItems.size()));

                if (!player.isCreative()) {
                    if (tool.isDamageableItem()) {
                        tool.hurtAndBreak(1, serverPlayer, EquipmentSlot.MAINHAND);
                    }
                }

                event.setCanceled(true);

                player.level().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);
                Block.popResource((ServerLevel) player.level(), event.getPos(), new ItemStack(randomItem));
            }
            return;
        }

        if (hasEffect(player, "unbreakable_tools_cobblestone_only")) {
            event.setCanceled(true);
            player.level().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);
            Block.popResource((ServerLevel)player.level(), event.getPos(), new ItemStack(Items.COBBLESTONE));
            return;
        }
        if (hasEffect(player, "triple_drops_slow_break")) {
            var enchantRegistry = player.level().holderLookup(Registries.ENCHANTMENT);

            int silkLevel = EnchantmentHelper.getItemEnchantmentLevel(enchantRegistry.getOrThrow(Enchantments.SILK_TOUCH), tool);

            if (silkLevel == 0 && isNaturalBlock(event.getState()) && !player.isCreative()) {
                List<ItemStack> drops = Block.getDrops(event.getState(), ((ServerLevel) serverPlayer.level()), event.getPos(), null, player, tool);
                event.setCanceled(true);
                player.level().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);

                for (ItemStack stack : drops) {
                    stack.setCount(stack.getCount() * 3);
                    Block.popResource(((ServerLevel) serverPlayer.level()), event.getPos(), stack);
                }
                
                // Apply durability loss for triple_drops_slow_break
                if (tool.isDamageableItem()) {
                    tool.hurtAndBreak(1, serverPlayer, EquipmentSlot.MAINHAND);
                }
                return;
            }
        }
        int multiplier = 1;
        if (hasEffect(player, "luck_ores_one_hp") && player.getHealth() <= 2.0F) {
            multiplier = 5;
        } else if (hasEffect(player, "triple_ores")) {
            multiplier = 3;
        }

        if (multiplier > 1 && !player.isCreative()) {
            event.setCanceled(true);
            level.destroyBlock(pos, false, player);

            List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, tool);
            for (ItemStack drop : drops) {
                drop.setCount(drop.getCount() * multiplier);
                Block.popResource(level, pos, drop);
            }
            tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            return;
        }
        if (hasEffect(player, "stacked_logs")) {
            if (event.getState().is(BlockTags.LOGS)) {
                var silkTouchHolder = player.level().holderLookup(Registries.ENCHANTMENT).get(Enchantments.SILK_TOUCH);
                if (silkTouchHolder.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(silkTouchHolder.get(), player.getMainHandItem()) > 0) return;

                // In creative mode, break normally
                if (player.isCreative()) {
                    return;
                }

                // Check if the block was placed by a player
                CompoundTag blockEntityTag = null;
                BlockEntity be = event.getLevel().getBlockEntity(pos);
                if (be != null) {
                    blockEntityTag = be.saveWithoutMetadata(event.getLevel().registryAccess());
                }

                boolean isPlayerPlaced = blockEntityTag != null && blockEntityTag.getBooleanOr("PlayerPlaced", false);

                ItemStack logDrop = new ItemStack(event.getState().getBlock().asItem());
                
                // If player placed, drop 1. If natural, drop 64.
                logDrop.setCount(isPlayerPlaced ? 1 : 64);
                Block.popResource((ServerLevel) event.getLevel(), event.getPos(), logDrop);
                event.getLevel().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);

                if (tool.isDamageableItem()) {
                    tool.hurtAndBreak(1, serverPlayer, EquipmentSlot.MAINHAND);
                }

                event.setCanceled(true);
            }
        }
        if (event.getState().is(Blocks.ANCIENT_DEBRIS)) {
            boolean isPlaced = playerPlacedDebris.remove(event.getPos());
            if (hasEffect(player, "double_debris_vampire") && !isPlaced) {
                var silkTouchHolder = player.level().holderLookup(Registries.ENCHANTMENT).get(Enchantments.SILK_TOUCH);
                if (silkTouchHolder.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(silkTouchHolder.get(), player.getMainHandItem()) > 0) return;

                if (!player.isCreative()) {
                    List<ItemStack> drops = Block.getDrops(event.getState(), ((ServerLevel) serverPlayer.level()), event.getPos(), null, player, tool);
                    event.setCanceled(true);
                    event.getLevel().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);

                    for (ItemStack stack : drops) {
                        stack.setCount(stack.getCount() * 2);
                        Block.popResource(((ServerLevel) serverPlayer.level()), event.getPos(), stack);
                    }

                    if (tool.isDamageableItem()) {
                        tool.hurtAndBreak(1, serverPlayer, EquipmentSlot.MAINHAND);
                    }
                }
                return;
            }
        }
        if (hasEffect(player, "auto_smelt_random_sound") && isOre(event.getState())) {
            event.setCanceled(true);
            player.level().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);

            if (!player.isCreative()) {
                List<ItemStack> drops = Block.getDrops(event.getState(), ((ServerLevel) serverPlayer.level()), event.getPos(), null, player, player.getMainHandItem());

                for (ItemStack dropStack : drops) {
                    Optional<RecipeHolder<SmeltingRecipe>> recipe = serverPlayer.level().getServer().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(dropStack), player.level());

                    if (recipe.isPresent()) {
                        ItemStack smeltedResult = recipe.get().value().assemble(new SingleRecipeInput(dropStack)).copy();
                        smeltedResult.setCount(dropStack.getCount());
                        Block.popResource(((ServerLevel) serverPlayer.level()), event.getPos(), smeltedResult);
                    } else {
                        Block.popResource(((ServerLevel) serverPlayer.level()), event.getPos(), dropStack);
                    }
                }

                if (tool.isDamageableItem()) {
                    tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                }

                player.giveExperiencePoints(1);
            }
            return; // Stop processing
        }
        if (hasEffect(player, "cheap_trades_no_ore_drops") && isOre(event.getState())) {
            event.setCanceled(true);
            player.level().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);
        }
        
        // Apply durability loss for haste_ii when actually breaking blocks
        if (hasEffect(player, "haste_ii") && !player.isCreative() && tool.isDamageableItem()) {
            tool.hurtAndBreak(1, serverPlayer, EquipmentSlot.MAINHAND);
        }
    }

    private static boolean isOre(BlockState state) {
        return state.is(BlockTags.IRON_ORES) || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.COPPER_ORES) ||
                state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE) ||
                state.is(Blocks.LAPIS_ORE) || state.is(Blocks.DEEPSLATE_LAPIS_ORE) ||
                state.is(Blocks.REDSTONE_ORE) || state.is(Blocks.DEEPSLATE_REDSTONE_ORE) ||
                state.is(Blocks.EMERALD_ORE) || state.is(Blocks.DEEPSLATE_EMERALD_ORE) ||
                state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE) ||
                state.is(Blocks.NETHER_GOLD_ORE) || state.is(Blocks.NETHER_QUARTZ_ORE) ||
                state.is(Blocks.ANCIENT_DEBRIS);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getPlacedBlock().is(Blocks.ANCIENT_DEBRIS)) {
            playerPlacedDebris.add(event.getPos());
        }

        // fast_smelt_damage is per-player: record who placed each furnace so
        // only the owner's furnace smelts in 1s (and only they take damage).
        if (event.getLevel().getBlockEntity(event.getPos()) instanceof net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity furnace) {
            ((net.coba.wyr.util.FurnaceOwnerAccess) furnace).wyr$setOwner(player.getUUID());
        }

        int placementY = event.getPos().getY();

        // Check if player has the effect and set appropriate limit
        boolean hasBuildLimitEffect = hasEffect(player, "build_limit_638_top_bottom_only");

        if (hasBuildLimitEffect) {
            // Players with this effect can build up to Y=638
            if (placementY > 638) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("You can only build up to Y=638 with this choice."));
            }
        } else {
            // Players WITHOUT this effect are capped at the normal build limit (319).
            // NOTE: reaching Y>319 requires the world to be generated with a raised
            // build height (e.g. a datapack/dimension_type with height >= 640).
            if (placementY > 319) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("You can only build up to Y=319."));
            }
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockState state = player.level().getBlockState(event.getPos());

        if (hasEffect(player, "slow_fall_side_place_only")) {
            if (event.getItemStack().getItem() instanceof BlockItem) {
                Direction face = event.getFace();

                if (face == Direction.UP || face == Direction.DOWN) {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        if (hasEffect(event.getEntity(), "auto_repair_no_enchant")) {
            if (event.getLevel().getBlockState(event.getPos()).getBlock() instanceof EnchantingTableBlock) {
                // Block all enchanting table usage
                event.setCanceled(true);
            }
        }

        if (hasEffect(player, "mace_max_damage_weak")) {
            if (state.getBlock() instanceof EnchantingTableBlock) {
                // Block enchanting table usage
                event.setCanceled(true);
                return;
            }
        }

        if (hasEffect(player, "infinite_xp_no_anvils")) {
            // Block only enchanting tables, allow anvils
            if (state.getBlock() instanceof EnchantingTableBlock) {
                event.setCanceled(true);
                return;
            }
        }

        if (hasEffect(player, "bonemeal_ores_cobblestone") && event.getItemStack().is(Items.BONE_MEAL)) {
            BlockState clickedState = player.level().getBlockState(event.getPos());
            if (isOre(clickedState)) {
                if (!player.level().isClientSide()) {
                    event.setCanceled(true);

                    if (!player.isCreative()) {
                        event.getItemStack().shrink(1);
                    }

                    Block oreBlock = clickedState.getBlock();
                    List<BlockPos> vein = findVein(player.level(), event.getPos(), oreBlock);

                    // 50% chance to replace entire vein with cobblestone
                    if (player.getRandom().nextFloat() < 0.5) {
                        vein.forEach(veinPos -> player.level().setBlock(veinPos, Blocks.COBBLESTONE.defaultBlockState(), 3));
                    } else {
                        // Success path: keep original vein, spawn 2x original vein size more ores around it
                        int originalVeinSize = vein.size();
                        int blocksToPlace = originalVeinSize * 2;
                        
                        Set<BlockPos> potentialSpots = new HashSet<>();
                        for (BlockPos veinPos : vein) {
                            for (Direction dir : Direction.values()) {
                                BlockPos adjacent = veinPos.relative(dir);
                                if (player.level().isEmptyBlock(adjacent) && !vein.contains(adjacent)) {
                                    potentialSpots.add(adjacent);
                                }
                            }
                        }
                        
                        // If not enough air spots, replace adjacent non-bedrock blocks
                        if (potentialSpots.size() < blocksToPlace) {
                            for (BlockPos veinPos : vein) {
                                for (Direction dir : Direction.values()) {
                                    BlockPos adjacent = veinPos.relative(dir);
                                    if (!vein.contains(adjacent) && !potentialSpots.contains(adjacent)) {
                                        BlockState adjacentState = player.level().getBlockState(adjacent);
                                        if (!adjacentState.is(Blocks.BEDROCK) && adjacentState.getDestroySpeed(player.level(), adjacent) >= 0) {
                                            potentialSpots.add(adjacent);
                                        }
                                    }
                                }
                            }
                        }
                        
                        List<BlockPos> spotsToPlace = new ArrayList<>(potentialSpots);
                        Collections.shuffle(spotsToPlace);
                        int actualBlocksToPlace = Math.min(blocksToPlace, spotsToPlace.size());
                        for (int i = 0; i < actualBlocksToPlace; i++) {
                            player.level().setBlock(spotsToPlace.get(i), oreBlock.defaultBlockState(), 3);
                        }
                    }
                }
                return; // Stop processing this event
            }
        }

        if (hasEffect(player, "insta_grow_bread_only") && event.getItemStack().is(Items.BONE_MEAL)) {
            BlockState clickedState = player.level().getBlockState(event.getPos());

            // Work on any bonemeal-able block (all crops, saplings, stems, etc.)
            if (clickedState.getBlock() instanceof BonemealableBlock bonemealable && !player.level().isClientSide()) {
                // Cancel the default bonemeal action
                event.setCanceled(true);

                // Manually consume one bonemeal from the player's hand
                if (!player.isCreative()) {
                    event.getItemStack().shrink(1);
                }

                ServerLevel serverLevel = (ServerLevel) player.level();
                BlockPos pos = event.getPos();

                // Repeatedly apply bonemeal until the block is fully grown (first try)
                for (int i = 0; i < 20; i++) {
                    BlockState current = serverLevel.getBlockState(pos);
                    if (!(current.getBlock() instanceof BonemealableBlock b)
                            || !b.isValidBonemealTarget(serverLevel, pos, current, null)) {
                        break;
                    }
                    b.performBonemeal(serverLevel, serverLevel.getRandom(), pos, current, null);
                }

                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        15, 0.5, 0.5, 0.5, 0.0);
            }
        }

        if (hasEffect(player, "villager_talk_no_fence_gates") && state.is(BlockTags.FENCE_GATES)) {
            event.setCanceled(true);
            return;
        }

        if (hasEffect(player, "build_limit_638_top_bottom_only")) {
            // Only allow placement on top and bottom faces
            if (event.getItemStack().getItem() instanceof BlockItem) {
                Direction face = event.getFace();
                if (face != Direction.UP && face != Direction.DOWN) {
                    event.setCanceled(true);
                    if (!event.getLevel().isClientSide()) {
                        player.sendSystemMessage(Component.literal("You can only place blocks on top and bottom faces with this choice."));
                    }
                    return;
                }
            }
        }

        if (hasEffect(player, "triple_ores") && state.getBlock() instanceof EnchantingTableBlock) event.setCanceled(true);
        if (hasEffect(player, "sand_treasure") && (state.is(Blocks.FURNACE) || state.is(Blocks.BLAST_FURNACE) || state.is(Blocks.SMOKER))) event.setCanceled(true);
        if (hasEffect(player, "phasing") && state.getBlock() instanceof DoorBlock) event.setCanceled(true);
        if (hasEffect(player, "tame_hostiles_no_crafting") && state.is(Blocks.CRAFTING_TABLE)) event.setCanceled(true);
        if (hasEffect(player, "creeper_cake_no_fence_gates") && state.is(BlockTags.FENCE_GATES)) event.setCanceled(true);
        if (hasEffect(player, "no_phantoms_no_beds") && state.is(BlockTags.BEDS)) event.setCanceled(true);
        if (hasEffect(player, "backpack_no_shulkers") && state.getBlock() instanceof ShulkerBoxBlock) event.setCanceled(true);
        if (hasEffect(player, "nether_water_fast_lava") && player.level().dimension() == Level.NETHER && event.getItemStack().is(Items.WATER_BUCKET)) {
            // Allow water placement in Nether - return early to prevent other checks from canceling
            return;
        }
    }

    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        Player player = event.getPlayer();
        if (hasEffect(player, "enchant_level_x_delete_item")) {
            ItemStack left = event.getLeft();
            ItemStack right = event.getRight();

            // Allow combining enchanted books/items to go past max level
            if (!left.isEmpty() && !right.isEmpty()) {
                // Check if we're combining enchanted books or enchanting an item with a book
                if (right.is(Items.ENCHANTED_BOOK) || left.is(Items.ENCHANTED_BOOK) || left.isEnchantable()) {
                    // Get enchantments from both items
                    ItemEnchantments leftEnchants = EnchantmentHelper.getEnchantmentsForCrafting(left);
                    ItemEnchantments rightEnchants = EnchantmentHelper.getEnchantmentsForCrafting(right);
                    
                    if (!rightEnchants.isEmpty()) {
                        // Create output item by copying left item
                        ItemStack output = left.copy();
                        if (left.is(Items.ENCHANTED_BOOK) && !right.is(Items.ENCHANTED_BOOK)) {
                            // If left is book and right is not, we shouldn't do anything (not standard)
                            return;
                        }
                        
                        // If left is an item and right is a book (standard enchanting)
                        // Or if both are books (book merging)
                        // Or if both are items of the same type (item merging)
                        if (left.is(Items.ENCHANTED_BOOK) || right.is(Items.ENCHANTED_BOOK) || left.getItem() == right.getItem()) {
                            ItemEnchantments.Mutable combined = new ItemEnchantments.Mutable(leftEnchants);
                            int highestNewLevel = 1;
                            boolean upgraded = false;
                            
                            for (var enchantEntry : rightEnchants.entrySet()) {
                                var enchant = enchantEntry.getKey();
                                int rightLevel = enchantEntry.getValue();
                                int currentLevel = combined.getLevel(enchant);
                                int newLevel;
                                if (currentLevel == rightLevel) {
                                    newLevel = currentLevel + 1;
                                    upgraded = true;
                                } else {
                                    newLevel = Math.max(currentLevel, rightLevel);
                                    if (newLevel > currentLevel) {
                                        upgraded = true;
                                    }
                                }
                                
                                // Allow levels to go past maximum up to 10
                                newLevel = Math.min(10, newLevel);
                                combined.set(enchant, newLevel);
                                if (newLevel > highestNewLevel) {
                                    highestNewLevel = newLevel;
                                }
                            }
                            
                            if (upgraded) {
                                // Apply combined enchantments to output
                                EnchantmentHelper.setEnchantments(output, combined.toImmutable());
                                
                                // Set the output and mark for potential item deletion
                                event.setOutput(output);
                                CustomData.update(DataComponents.CUSTOM_DATA, output, tag -> tag.putBoolean("wyr_delete_item_marker", true));
                                
                                // Set material cost and level cost
                                event.setMaterialCost(1);
                                event.setXpCost(highestNewLevel * 3);
                            }
                        }
                    }
                }
            }
        }

        if (hasEffect(event.getPlayer(), "auto_repair_no_enchant")) {
            // Block all enchanted books in anvil
            if (event.getRight().is(Items.ENCHANTED_BOOK)) {
                event.setCanceled(true);
            }
        }

        if (hasEffect(player, "triple_ores")) {
            if (event.getRight().is(Items.ENCHANTED_BOOK)) event.setCanceled(true);
        }
        if (hasEffect(player, "infinite_xp_no_anvils")) {
            // Block enchanted books in anvil
            if (event.getRight().is(Items.ENCHANTED_BOOK)) {
                event.setCanceled(true);
            }
            // Block mace repair in anvil
            if (event.getLeft().is(Items.MACE) || event.getRight().is(Items.MACE)) {
                event.setCanceled(true);
            }
            // Block breeze rod repair in anvil
            if (event.getRight().is(Items.BREEZE_ROD)) {
                event.setCanceled(true);
            }
        }
        if (hasEffect(player, "passive_xp_no_mending")) {
            var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(event.getRight());
            if (enchantments.keySet().stream().anyMatch(h -> h.is(Enchantments.MENDING))) event.setCanceled(true);
        }
        if (hasEffect(player, "mace_max_damage_weak")) {
            // Block mace repair in crafting table (both slots)
            ItemStack left = event.getLeft();
            ItemStack right = event.getRight();
            if (!left.isEmpty() && !right.isEmpty()) {
                // Check if either slot has a mace
                if (left.is(Items.MACE) || right.is(Items.MACE)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
        if (hasEffect(player, "stacked_logs") && event.getLeft().isCorrectToolForDrops(Blocks.OAK_LOG.defaultBlockState())) {
            // Block all ingot repairs for axes
            ItemStack right = event.getRight();
            if (!right.isEmpty() && (right.isCorrectToolForDrops(Blocks.OAK_LOG.defaultBlockState()) || 
                right.is(Items.IRON_INGOT) || right.is(Items.GOLD_INGOT) || 
                right.is(Items.COPPER_INGOT) || right.is(Items.NETHERITE_INGOT))) {
                event.setCanceled(true);
            }
        }
        if (hasEffect(player, "unbreakable_tools_netherite")) {
            ItemStack left = event.getLeft();
            ItemStack right = event.getRight();

            // Check if combining 16 netherite blocks with a tool
            if (!left.isEmpty() && !right.isEmpty()) {
                boolean isNetheriteBlocks = (left.is(Items.NETHERITE_BLOCK) && left.getCount() >= 16) ||
                                            (right.is(Items.NETHERITE_BLOCK) && right.getCount() >= 16);
                
                boolean isDurableItem = (left.isDamageableItem() && !left.isEmpty()) ||
                                   (right.isDamageableItem() && !right.isEmpty());

                if (isNetheriteBlocks && isDurableItem) {
                    ItemStack durableItem = left.isDamageableItem() ? left : right;
                    
                    ItemStack unbreakableItem = durableItem.copy();
                    unbreakableItem.setCount(1);
                    unbreakableItem.set(DataComponents.UNBREAKABLE, net.minecraft.util.Unit.INSTANCE);
                    event.setOutput(unbreakableItem);
                    event.setXpCost(1);
                    event.setMaterialCost(16);
                }
            }
        }
    }

    
    @SubscribeEvent
    public void onPlayerFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack item = event.getItem();
        FoodProperties food = item.get(DataComponents.FOOD);

        if (hasEffect(player, "eat_anything_hunger_chance") && food == null) {
            player.getFoodData().eat(1, 1.0F);

            if (player.getRandom().nextFloat() < 0.1) {
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0));
            }

            // Play eating sound effect
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, 1.0F);

            if (!player.isCreative()) {
                ItemStack result = event.getItem().copy();
                result.shrink(1);
                event.setResultStack(result);
            }
            return;
        }

        if (hasEffect(player, "double_food_no_hunger_bar") && food != null) {
            player.getFoodData().eat(food.nutrition(), food.saturation());
        }

                        
        if (hasEffect(player, "daily_diamond")) {
            WyrSavedData savedData = WyrSavedData.get((ServerLevel) player.level());
            int currentThirst = savedData.getPlayerThirst(player.getUUID());
            int newThirst = currentThirst;
            if (item.is(ItemTags.MEAT)) newThirst = Math.max(0, currentThirst - 4);
            else if (item.is(Items.MELON_SLICE)) newThirst = Math.min(20, currentThirst + 2);
            else if (item.is(Items.POTION)) {
                var potionContents = item.get(DataComponents.POTION_CONTENTS);
                if (potionContents != null && potionContents.potion().isPresent() && potionContents.potion().get().is(Potions.WATER)) newThirst = Math.min(20, currentThirst + 4);
            }
            if (newThirst != currentThirst) {
                savedData.setPlayerThirst(player.getUUID(), newThirst);
                PacketDistributor.sendToPlayer(player, new PacketSyncThirst(newThirst));
            }
        }
        if (hasEffect(player, "eat_anything_hunger_chance")) {
            if (food == null && player.getRandom().nextFloat() < 0.1) {
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0));
            }
        }
    }

    
    @SubscribeEvent
    public void onPlayerUseItemStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack itemStack = event.getItem();

            if (hasEffect(player, "item_texture_scramble_melon_only")) {
                if (itemStack.get(DataComponents.FOOD) != null && !itemStack.is(Items.MELON_SLICE)) {
                    event.setCanceled(true);
                }
            }
            if (hasEffect(player, "keep_inventory_berries_only")) {
                if (itemStack.get(DataComponents.FOOD) != null && !itemStack.is(Items.SWEET_BERRIES)) {
                    event.setCanceled(true);
                    return;
                }
            }
            if (hasEffect(player, "insta_grow_bread_only") && itemStack.get(DataComponents.FOOD) != null && !itemStack.is(Items.BREAD)) {
                event.setCanceled(true);
            }
            if (hasEffect(player, "stack_512_melon_only") && itemStack.get(DataComponents.FOOD) != null && !itemStack.is(Items.MELON_SLICE)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (hasEffect(player, "unbreakable_elytra_no_boost") && player.isFallFlying() && event.getItemStack().is(Items.FIREWORK_ROCKET)) event.setCanceled(true);
        if (hasEffect(player, "eat_anything_hunger_chance") && event.getItemStack().get(DataComponents.FOOD) == null && player.canEat(false)) player.startUsingItem(event.getHand());

        if (hasEffect(player, "cheap_trades_half_hunger")) {
            if (event.getItemStack().get(DataComponents.FOOD) != null && player.getFoodData().getFoodLevel() >= 10) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (hasEffect(player, "creative_flight") && !player.getAbilities().flying) {
            float distance = (float) event.getDistance();
            if (distance > 3.0F) player.hurt(player.damageSources().fall(), (int) Math.ceil(distance - 3.0F));
            event.setCanceled(true);
            return;
        }
        if (hasEffect(player, "high_jump")) {
            if (event.getDistance() > 2.0f) player.hurt(player.damageSources().fall(), (float)(event.getDistance() - 2.0f));
            event.setCanceled(true);
        }
        if (hasEffect(player, "mace_max_damage_weak")) {
            ItemStack mace = player.getMainHandItem();
            if (mace.is(Items.MACE)) {
                // Keep the mace at its default durability (the "weak" downside is
                // that it cannot be enchanted or repaired).
                // If fall distance is 3 or more blocks, deal massive damage to kill instantly
                if (event.getDistance() >= 3.0f) {
                    event.setDamageMultiplier(1000.0f);
                }
            }
        }
        if (hasEffect(player, "no_fall_damage_low_render_distance") || hasEffect(player, "no_fall_damage_no_jump")) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (event.getEntity() instanceof Player player && hasEffect(player, "sleep_heal_random_drops")) {
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(5.0F);
        }
    }

    @SubscribeEvent
    public void onHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player && hasEffect(player, "insta_heal")) event.setAmount(player.getMaxHealth());
    }

    @SubscribeEvent
    public void onLivingHurt(LivingIncomingDamageEvent event) {

        if (event.getSource().getEntity() instanceof Player player) {
            // If that player has the effect, double their damage
            if (hasEffect(player, "half_health_mobs_no_drops")) {
                event.setAmount(event.getAmount() * 2);
            }
        }
        if(event.getEntity() instanceof Player player && hasEffect(player, "respawn_on_death_half_durability")) {
            respawnLocations.put(player.getUUID(), player.blockPosition());
            respawnDimensions.put(player.getUUID(), player.level().dimension());
        }
        if (event.getEntity().getType() == EntityTypes.ENDER_DRAGON && event.getSource().getEntity() instanceof Player player && hasEffect(player, "one_hit_dragon_no_end")) {
            event.setAmount(Float.MAX_VALUE);
        }
        if (event.getEntity() instanceof Player player) {
            if (hasEffect(player, "fire_immunity")) {
                if (event.getSource().is(DamageTypeTags.IS_FIRE)) event.setCanceled(true);
                else if (event.getSource().getDirectEntity() instanceof LivingEntity) event.setAmount(event.getAmount() * 2);
            }
            if (hasEffect(player, "wall_climb") && event.getSource().is(DamageTypeTags.IS_FALL)) event.setAmount(event.getAmount() * 2);
        }
        if (event.getSource().getEntity() instanceof Player player && hasEffect(player, "triple_damage") && player.getRandom().nextInt(4) == 0) {
            event.setAmount(event.getAmount() * 3);
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Monster monster && event.getSource().getEntity() instanceof Player player) {
            if (hasEffect(player, "wind_charge_respawn_mob") && !monster.entityTags().contains("wyr_respawned")) {
                Monster newMob = (Monster) monster.getType().create(player.level(), EntitySpawnReason.REINFORCEMENT);
                if (newMob != null) {
                    newMob.setPos(monster.position());
                    newMob.addTag("wyr_respawned");
                    player.level().addFreshEntity(newMob);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {

        if (event.getEntity() instanceof ServerPlayer player && hasEffect(player, "respawn_on_death_half_durability")) {
            durabilityPenaltyPlayers.put(player.getUUID(), player.level().getGameTime() + 300);
            // Disable keep inventory game rule
            // Note: GameRules API changed - simplified approach
        }
        if (event.getEntity() instanceof ServerPlayer player && hasEffect(player, "respawn_on_death_half_durability")) {
            BlockPos respawnPos = respawnLocations.get(player.getUUID());
            ResourceKey<Level> respawnDim = respawnDimensions.get(player.getUUID());
            if (respawnPos != null && respawnDim != null) {
                ServerLevel targetLevel = player.level().getServer().getLevel(respawnDim);
                if (targetLevel != null) player.teleportTo(targetLevel, respawnPos.getX() + 0.5, respawnPos.getY(), respawnPos.getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot(), false);
                respawnLocations.remove(player.getUUID());
                respawnDimensions.remove(player.getUUID());
            }
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (hasEffect(player, "cheap_trades_half_hunger")) {
            player.getFoodData().setFoodLevel(10);
        }

        // choose_spawn_biome_lock: queue a respawn teleport to the locked
        // biome position. The teleport is deferred to the first tick after
        // respawn (handled in onPlayerTick) because this event fires before
        // vanilla finishes positioning the respawned player, so a direct
        // teleport here would be overridden and the per-tick push-back would
        // visibly drag them back like a magnet. After 10 in-game days the
        // effect expires and the player respawns normally.
        if (hasEffect(player, "choose_spawn_biome_lock")) {
            long effectStartTime = effectStartTimes.getOrDefault(player.getUUID(), player.level().getGameTime());
            long timeSinceEffect = player.level().getGameTime() - effectStartTime;
            if (timeSinceEffect < (10 * 24000)) {
                BlockPos locked = spawnPositions.get(player.getUUID());
                if (locked == null) {
                    WyrSavedData data = WyrSavedData.get((ServerLevel) player.level());
                    locked = data.getPlayerSpawnPosition(player.getUUID());
                    if (locked != null) {
                        spawnPositions.put(player.getUUID(), locked);
                    }
                }
                if (locked != null) {
                    pendingBiomeRespawns.put(player.getUUID(), locked);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        // This event fires when a player respawns
        if (!event.isWasDeath()) return;

        // Get the original player object before they died
        ServerPlayer originalPlayer = (ServerPlayer) event.getOriginal();
        ServerPlayer newPlayer = (ServerPlayer) event.getEntity();
        List<String> choiceIds = PlayerChoiceTracker.getChoices(originalPlayer)
                .stream()
                .map(Choice::getId)
                .toList();

        AttributeInstance maxHealth = newPlayer.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            if (choiceIds.contains("insta_heal")) {
                maxHealth.addPermanentModifier(new AttributeModifier(INSTA_HEAL_HEALTH_MODIFIER_ID, -14.0, AttributeModifier.Operation.ADD_VALUE));
            }
            if (choiceIds.contains("extra_hearts")) {
                maxHealth.addPermanentModifier(new AttributeModifier(EXTRA_HEARTS_HEALTH_MODIFIER_ID, 20.0, AttributeModifier.Operation.ADD_VALUE));
            }
        }
        
        // Handle XP retention for infinite_xp_no_anvils
        if (choiceIds.contains("infinite_xp_no_anvils")) {
            newPlayer.setExperienceLevels(1000000000);
        }
        
        // After applying modifiers, set the player's health to their new max value.
        newPlayer.setHealth(newPlayer.getMaxHealth());
    }

    @SubscribeEvent
    public void onXpPickup(PlayerXpEvent.PickupXp event) {
        if (hasEffect(event.getEntity(), "unbreakable_anvil_no_xp")) {
            event.setCanceled(true); // Cancel the XP orb pickup
        }
    }

    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        String command = event.getParseResults().getReader().getString();
        
        // Block /xp add command for unbreakable_anvil_no_xp
        if (command.startsWith("/xp add")) {
            for (Player player : event.getParseResults().getContext().getSource().getLevel().players()) {
                if (hasEffect(player, "unbreakable_anvil_no_xp")) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
        
        // Handle wyrxp command for infinite_xp_no_anvils
        if (command.startsWith("/wyrxp")) {
            for (Player player : event.getParseResults().getContext().getSource().getLevel().players()) {
                if (hasEffect(player, "infinite_xp_no_anvils")) {
                    // Parse the amount from command: /wyrxp <amount>
                    String[] parts = command.split(" ");
                    if (parts.length >= 2) {
                        try {
                            int amount = Integer.parseInt(parts[1]);
                            // Set maximum XP limit
                            int currentXP = player.experienceLevel;
                            int maxXP = 1000000000; // 1 billion XP max
                            int newXP = Math.min(currentXP + amount, maxXP);
                            player.giveExperiencePoints(newXP - currentXP);
                        } catch (NumberFormatException e) {
                            // Invalid number, ignore
                        }
                    }
                    event.setCanceled(true);
                    return;
                } else {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public void onExplosionStart(ExplosionEvent.Start event) {
        // The method is now getDirectSourceEntity()
        if (event.getExplosion().getDirectSourceEntity() instanceof Creeper creeper) {
            Level level = event.getLevel();

            // The method is getEntitiesOfClass() with no hyphen
            boolean playerWithEffectIsNearby = !level.getEntitiesOfClass(
                    Player.class,
                    creeper.getBoundingBox().inflate(16),
                    player -> hasEffect(player, "creeper_cake_no_fence_gates")
            ).isEmpty();

            if (playerWithEffectIsNearby) {
                event.setCanceled(true);

                if (!level.isClientSide()) {
                    level.playSound(null, creeper.getX(), creeper.getY(), creeper.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F) * 0.7F);
                    ((ServerLevel)level).sendParticles(ParticleTypes.EXPLOSION_EMITTER, creeper.getX(), creeper.getY(), creeper.getZ(), 1, 0, 0, 0, 0);

                    for (int i = 0; i < 5; i++) {
                        ItemEntity cake = new ItemEntity(level, creeper.getX(), creeper.getY(), creeper.getZ(), new ItemStack(Items.CAKE));
                        cake.setDeltaMovement(RandomSource.create().nextGaussian() * 0.2, 0.3, RandomSource.create().nextGaussian() * 0.2);
                        level.addFreshEntity(cake);
                    }

                    creeper.discard();
                }
            }
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        // Only cancel attacks on hostile mobs, allow attacks on passive mobs
        if (hasEffect(player, "mob_invisibility")) {
            Entity target = event.getTarget();
            if (target instanceof LivingEntity livingTarget && livingTarget instanceof Mob) {
                // Check if the target is a hostile mob
                if (livingTarget instanceof Monster || livingTarget instanceof Slime || livingTarget instanceof Ghast || 
                    livingTarget instanceof Phantom || livingTarget instanceof WitherBoss || livingTarget instanceof EnderDragon) {
                    event.setCanceled(true);
                }
            }
        }
        // Remove applyHasteDurability from entity attacks - only apply to block breaking
        if (hasEffect(player, "op_bow_only") && !(player.getMainHandItem().getItem() instanceof BowItem)) event.setCanceled(true);
        
        // Mace max damage - make it one hit kill and apply durability loss
        if (hasEffect(player, "mace_max_damage_weak") && player.getMainHandItem().is(Items.MACE)) {
            Entity target = event.getTarget();
            if (target instanceof LivingEntity livingTarget) {
                // Deal massive damage to kill instantly
                livingTarget.hurt(livingTarget.damageSources().playerAttack(player), Float.MAX_VALUE);
                // Apply durability loss
                ItemStack mace = player.getMainHandItem();
                if (!player.isCreative()) {
                    mace.hurtAndBreak(1, player, player.getUsedItemHand());
                }
                event.setCanceled(true); // Cancel the original attack since we already dealt damage
            }
        }
        
        if (hasEffect(player, "fire_sword_self_damage")) {
            var enchantRegistry = player.level().holderLookup(Registries.ENCHANTMENT);
            int fireLevel = EnchantmentHelper.getItemEnchantmentLevel(enchantRegistry.getOrThrow(Enchantments.FIRE_ASPECT), player.getMainHandItem());
            if (fireLevel > 0) player.hurt(player.damageSources().onFire(), 2.0F);
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        // Don't apply durability loss here - it should only apply when actually breaking blocks
    }

    @SubscribeEvent
    public void onSetTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Monster monster) {
            LivingEntity newTarget = event.getNewAboutToBeSetTarget();
            if (newTarget != null) {
                for (Map.Entry<UUID, Set<UUID>> entry : tamedMonsters.entrySet()) {
                    UUID ownerUUID = entry.getKey();
                    if (entry.getValue().contains(monster.getUUID())) {
                        if (newTarget.getUUID().equals(ownerUUID)) {
                            event.setNewAboutToBeSetTarget(null);
                            return;
                        }
                    }
                }
            }
        }

        if (event.getNewAboutToBeSetTarget() instanceof Player player) {
            if (hasEffect(player, "mob_invisibility") || hasEffect(player, "hidden_from_mobs_slow_ores")) {
                event.setNewAboutToBeSetTarget(null);
            }
        }
    }

    public static UUID getTamedMobOwner(UUID mobUUID) {
        for (Map.Entry<UUID, Set<UUID>> entry : tamedMonsters.entrySet()) {
            if (entry.getValue().contains(mobUUID)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static boolean isMobTamed(UUID mobUUID) {
        return tamedMonsters.values().stream().anyMatch(set -> set.contains(mobUUID));
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // TEMPORARILY DISABLED FOR HANG DEBUGGING
        return;
        // if (event.getLevel().isClientSide() || !(event.getEntity() instanceof LivingEntity entity) || entity instanceof Player) {
        //     return;
        // }
        //
        // AABB area = entity.getBoundingBox().inflate(64.0);
        // List<Player> nearbyPlayers = event.getLevel().getEntitiesOfClass(Player.class, area);
        //
        // if (nearbyPlayers.isEmpty()) {
        //     return;
        // }
        //
        // AttributeInstance health = entity.getAttribute(Attributes.MAX_HEALTH);
        // if (health == null) {
        //     return;
        // }
        //
        // for (Player player : nearbyPlayers) {
        //     if (hasEffect(player, "see_mob_hp_double_health")) {
        //         // Only affect hostile mobs (Monster type or specific hostile entities)
        //         if (entity instanceof Monster || entity instanceof Slime || entity instanceof Ghast || 
        //             entity instanceof Phantom || entity instanceof WitherBoss || entity instanceof EnderDragon) {
        //             if (!health.hasModifier(DOUBLE_HEALTH_MODIFIER_ID)) {
        //                 health.addPermanentModifier(new AttributeModifier(DOUBLE_HEALTH_MODIFIER_ID, 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        //             }
        //         }
        //     }
        //
        //     if (hasEffect(player, "triple_loot_triple_health")) {
        //         if (!health.hasModifier(TRIPLE_HEALTH_MODIFIER_ID)) {
        //             health.addPermanentModifier(new AttributeModifier(TRIPLE_HEALTH_MODIFIER_ID, 2.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        //         }
        //     }
        //
        //     if (hasEffect(player, "weather_control_creeper")) {
        //         // Only affect creepers
        //         if (entity instanceof Creeper creeper) {
        //             // Prevent creeper from exploding on spawn
        //             creeper.setSwellDir(-1);
        //             // Make creeper powered state false to ensure it's a normal creeper
        //             // Note: setPowered method not available in this version
        //         }
        //     }
        // }
        //
        // if (health.getModifier(DOUBLE_HEALTH_MODIFIER_ID) != null || health.getModifier(TRIPLE_HEALTH_MODIFIER_ID) != null) {
        //     entity.setHealth(entity.getMaxHealth());
        // }
    }

    @SubscribeEvent
    public void onAnimalTame(AnimalTameEvent event) {

        if (event.getTamer() instanceof Player player && event.getAnimal() instanceof Wolf wolf && hasEffect(player, "strong_wolves_one_hp")) {
            AttributeInstance health = wolf.getAttribute(Attributes.MAX_HEALTH);

            if (health != null) {
                health.addPermanentModifier(new AttributeModifier(WOLF_HEALTH_MODIFIER_ID, -health.getBaseValue() + 1, AttributeModifier.Operation.ADD_VALUE));
            }
            // Set the wolf's current health to 1 HP
            wolf.setHealth(1.0f);

            // Keep the strength and speed boosts
            AttributeInstance damage = wolf.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damage != null) {
                damage.addPermanentModifier(new AttributeModifier(WOLF_STRENGTH_MODIFIER_ID, 4.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
            AttributeInstance speed = wolf.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.addPermanentModifier(new AttributeModifier(WOLF_SPEED_MODIFIER_ID, 2.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (hasEffect(player, "mob_loot_inventory_slots")) event.getDrops().forEach(itemEntity -> itemEntity.getItem().setCount(itemEntity.getItem().getCount() * 5));
            if (hasEffect(player, "triple_loot_triple_health") && event.getEntity() instanceof Monster) event.getDrops().forEach(itemEntity -> itemEntity.getItem().setCount(itemEntity.getItem().getCount() * 3));
            if (hasEffect(player, "half_health_mobs_no_drops")) event.getDrops().clear();
            if (hasEffect(player, "emeralds_from_mobs_siren") && event.getEntity() instanceof Monster) event.getDrops().add(new ItemEntity(player.level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(Items.EMERALD, 2)));

            if (hasEffect(player, "triple_loot_triple_health")) {
                for (ItemEntity itemEntity : event.getDrops()) {
                    ItemStack stack = itemEntity.getItem();
                    if (!stack.is(Items.SADDLE)) {
                        stack.setCount(stack.getCount() * 3);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack stack = event.getItemStack();
        Entity target = event.getTarget();

        if (hasEffect(player, "instant_tame_creeper_hiss")) {
            if (target instanceof TamableAnimal animal && !animal.isTame()) {
                if (animal instanceof Wolf && stack.is(Items.BONE)) {
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                    animal.setOwner(player);
                    animal.tame(player);
                    animal.level().broadcastEntityEvent(animal, (byte) 7);
                    event.setCanceled(true);
                    return;
                }

                if (animal.isFood(stack)) {
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                    animal.setOwner(player);
                    animal.tame(player);
                    animal.level().broadcastEntityEvent(animal, (byte) 7);
                    event.setCanceled(true);
                    return;
                }
            }
            if (target instanceof AbstractHorse horse && !horse.isTamed()) {
                horse.setTamed(true);
                horse.setOwner(player);
                horse.level().broadcastEntityEvent(horse, (byte) 7);
                event.setCanceled(true);
                return;
            }
        }

        if (hasEffect(player, "wool_stack_random_crosshair") && target instanceof Sheep sheep && stack.getItem() instanceof ShearsItem) {

            if (sheep.isShearable(player, stack, player.level(), sheep.blockPosition())) {
                event.setCanceled(true);

                sheep.level().playSound(null, sheep, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
                sheep.gameEvent(GameEvent.SHEAR, player);
                sheep.setSheared(true);

                Item woolItem = WOOL_BY_COLOR.getOrDefault(sheep.getColor(), Items.WOOL.pick(DyeColor.WHITE));
                if (sheep.level() instanceof ServerLevel serverLevel) {
                    serverLevel.addFreshEntity(new ItemEntity(sheep.level(), sheep.getX(), sheep.getY(), sheep.getZ(), new ItemStack(woolItem, 64)));
                }

                if (!player.isCreative()) {
                    EquipmentSlot slot = event.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                    stack.hurtAndBreak(1, player, slot);
                }
                return;
            }
        }

        if (hasEffect(player, "tame_hostiles_no_crafting") && stack.is(Items.NETHERITE_BLOCK) && target instanceof Mob mob) {
            WYR.LOGGER.info(">>> Taming conditions MET! Attempting to tame mob...");
            if (mob instanceof EnderDragon || mob instanceof WitherBoss) {
                return;
            }

            if (mob instanceof Monster monster) {
                Set<UUID> playerTamed = tamedMonsters.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
                if (!playerTamed.contains(monster.getUUID())) {
                    playerTamed.add(monster.getUUID());
                    monster.setTarget(null);

                    // Clear all old AI goals
                    monster.goalSelector.removeAllGoals(goal -> true);
                    monster.targetSelector.removeAllGoals(goal -> true);

                    // Add new "pet-like" AI
                    monster.goalSelector.addGoal(1, new FloatGoal(monster));
                    // ADDED: Goal to follow the owner
                    monster.goalSelector.addGoal(4, new FollowPlayerGoal(monster, 1.0D, 10.0F, 2.0F));
                    monster.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(monster, 1.0D));
                    monster.goalSelector.addGoal(6, new LookAtPlayerGoal(monster, Player.class, 8.0F));
                    monster.goalSelector.addGoal(6, new RandomLookAroundGoal(monster));

                    // Add new targeting AI
                    monster.targetSelector.addGoal(1, new HurtByTargetGoal(monster));
                    // REPLACED: Use our new goal to proactively defend the player
                    monster.targetSelector.addGoal(2, new DefendPlayerGoal(monster));

                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                    monster.level().broadcastEntityEvent(monster, (byte) 7);
                    event.setCanceled(true);
                }
            }
        }
    }

    private List<BlockPos> findVein(Level level, BlockPos startPos, Block startBlock) {
        List<BlockPos> vein = new ArrayList<>();
        Queue<BlockPos> toCheck = new LinkedList<>();
        Set<BlockPos> checked = new HashSet<>();
        int maxVeinSize = 100; // Safety limit to prevent lag

        toCheck.add(startPos);
        checked.add(startPos);

        while (!toCheck.isEmpty() && vein.size() < maxVeinSize) {
            BlockPos currentPos = toCheck.poll();
            if (level.getBlockState(currentPos).is(startBlock)) {
                vein.add(currentPos);
                for (BlockPos neighbor : BlockPos.betweenClosed(currentPos.offset(-1, -1, -1), currentPos.offset(1, 1, 1))) {
                    if (!checked.contains(neighbor)) {
                        toCheck.add(neighbor.immutable());
                        checked.add(neighbor.immutable());
                    }
                }
            }
        }
        return vein;
    }

    @SubscribeEvent
    public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof Player player && hasEffect(player, "one_hit_dragon_no_end")) {
            if (event.getTo() == Level.END) {
                // Check if player has already entered end with this choice
                if (!hasEnteredEnd.contains(player.getUUID())) {
                    hasEnteredEnd.add(player.getUUID());
                } else {
                    // Teleport back to overworld if already entered once
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    BlockPos respawnPos = serverPlayer.getRespawnConfig() != null ? serverPlayer.getRespawnConfig().respawnData().pos() : null;
                    ServerLevel overworld = serverPlayer.level().getServer().overworld();
                    if (respawnPos != null) {
                        serverPlayer.teleportTo(overworld, respawnPos.getX(), respawnPos.getY(), respawnPos.getZ(), Set.of(), serverPlayer.getYRot(), serverPlayer.getXRot(), false);
                    } else {
                        BlockPos spawn = overworld.getRespawnData().pos();
                        serverPlayer.teleportTo(overworld, spawn.getX(), spawn.getY(), spawn.getZ(), Set.of(), 0.0F, 0.0F, false);
                    }
                    player.sendSystemMessage(Component.literal("You can only enter the End once with this choice!"));
                }
            }
        }
    }

    @SubscribeEvent
    public void onPotionBrew(PotionBrewEvent.Post event) {
        // Simplified approach - just cancel the event if player has effect
        // Note: Full implementation would require access to the brewing stand entity
        // For now, this is a placeholder implementation
    }

    private void applyHasteDurability(Player player, ItemStack item) {
        if (hasEffect(player, "haste_ii") && !player.isCreative() && player instanceof ServerPlayer serverPlayer) {
            if (item.isDamageableItem()) item.hurtAndBreak(1, serverPlayer, EquipmentSlot.MAINHAND);
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
            if (player instanceof ServerPlayer sp) sp.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            player.fallDistance = 0.0F;
            player.hurt(player.damageSources().magic(), 4.0F);
            teleportCooldowns.put(player.getUUID(), currentTime + 5000);
        }
    }

    private void handleGolemSet(Player player) {
        boolean hasFullSet = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GOLEM_HELMET.get()) &&
                player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.GOLEM_CHESTPLATE.get()) &&
                player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.GOLEM_LEGGINGS.get()) &&
                player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.GOLEM_BOOTS.get());

        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance strength = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (health == null || strength == null || speed == null) return;

        if (hasFullSet) {
            // Iron golems have 100 hearts (200 HP), so add 180 HP to player's base 20 HP
            if (!health.hasModifier(GOLEM_HEALTH_MODIFIER_ID)) health.addPermanentModifier(new AttributeModifier(GOLEM_HEALTH_MODIFIER_ID, 180.0, AttributeModifier.Operation.ADD_VALUE));
            // Iron golems deal significant damage (16.5 damage on Easy, up to 32.5 on Hard)
            if (!strength.hasModifier(GOLEM_STRENGTH_MODIFIER_ID)) strength.addPermanentModifier(new AttributeModifier(GOLEM_STRENGTH_MODIFIER_ID, 25.0, AttributeModifier.Operation.ADD_VALUE));
            // Iron golems move very slowly (approximately 0.15 blocks/tick vs player's 0.1 base)
            if (!speed.hasModifier(GOLEM_SPEED_MODIFIER_ID)) speed.addPermanentModifier(new AttributeModifier(GOLEM_SPEED_MODIFIER_ID, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else {
            if (health.hasModifier(GOLEM_HEALTH_MODIFIER_ID)) {
                health.removeModifier(GOLEM_HEALTH_MODIFIER_ID);
                if(player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
            }
            if (strength.hasModifier(GOLEM_STRENGTH_MODIFIER_ID)) strength.removeModifier(GOLEM_STRENGTH_MODIFIER_ID);
            if (speed.hasModifier(GOLEM_SPEED_MODIFIER_ID)) speed.removeModifier(GOLEM_SPEED_MODIFIER_ID);
        }
    }

    @SubscribeEvent
    public void onPlayerFishItem(net.neoforged.neoforge.event.entity.player.ItemFishedEvent event) {
        Player player = event.getEntity();
        if (hasEffect(player, "fishing_luck")) {
            UUID playerUUID = player.getUUID();
            int catchCount = fishingCatchCount.getOrDefault(playerUUID, 0) + 1;
            fishingCatchCount.put(playerUUID, catchCount);
            
            // Summon guardian every 5th catch
            if (catchCount % 5 == 0) {
                Guardian guardian = EntityTypes.GUARDIAN.create(player.level(), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                if (guardian != null) {
                    guardian.setPos(player.getX() + player.getRandom().nextGaussian() * 3.0, 
                                   player.getY() + 2.0, 
                                   player.getZ() + player.getRandom().nextGaussian() * 3.0);
                    guardian.setTarget(player);
                    player.level().addFreshEntity(guardian);
                    player.sendSystemMessage(Component.literal("A Guardian has been summoned by your fishing luck!"));
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityMount(net.neoforged.neoforge.event.entity.EntityMountEvent event) {
        if (event.getEntityMounting() instanceof Player player) {
            if (hasEffect(player, "fast_horse_dark_fear")) {
                long gameTime = player.level().getOverworldClockTime() % 24000;
                boolean isNight = gameTime >= 13000 && gameTime < 23000;
                
                if (event.isMounting() && event.getEntityBeingMounted() instanceof AbstractHorse) {
                    // Prevent mounting at night
                    if (isNight) {
                        event.setCanceled(true);
                        player.sendSystemMessage(Component.literal("Your horse is afraid of the dark and refuses to be ridden at night!"));
                    }
                    // Allow mounting during day
                }
            }
        }
    }

    @SubscribeEvent
    public void onEffectAdded(MobEffectEvent.Added event) {
        if (event.getEntity() instanceof Player player && hasEffect(player, "random_good_bad_potions")) {
            Holder<MobEffect> addedEffect = event.getEffectInstance().getEffect();
            
            // Check if the added effect is a "good" effect
            if (isGoodEffect(addedEffect)) {
                // Add a random bad effect
                List<Holder<MobEffect>> badEffects = List.of(
                    MobEffects.WEAKNESS,
                    MobEffects.SLOWNESS,
                    MobEffects.HUNGER,
                    MobEffects.POISON,
                    MobEffects.WITHER,
                    MobEffects.BLINDNESS,
                    MobEffects.NAUSEA,
                    MobEffects.MINING_FATIGUE,
                    MobEffects.UNLUCK
                );
                
                Holder<MobEffect> randomBadEffect = badEffects.get(player.getRandom().nextInt(badEffects.size()));
                int duration = 200 + player.getRandom().nextInt(200); // 10-20 seconds
                int amplifier = player.getRandom().nextInt(2); // 0 or 1
                
                player.addEffect(new MobEffectInstance(randomBadEffect, duration, amplifier, true, false));
                player.sendSystemMessage(Component.literal("You feel a strange negative effect..."));
            }
        }
    }

    private boolean isGoodEffect(Holder<MobEffect> effect) {
        return effect.is(MobEffects.STRENGTH) ||
               effect.is(MobEffects.SPEED) ||
               effect.is(MobEffects.JUMP_BOOST) ||
               effect.is(MobEffects.REGENERATION) ||
               effect.is(MobEffects.INSTANT_HEALTH) ||
               effect.is(MobEffects.RESISTANCE) ||
               effect.is(MobEffects.FIRE_RESISTANCE) ||
               effect.is(MobEffects.WATER_BREATHING) ||
               effect.is(MobEffects.NIGHT_VISION) ||
               effect.is(MobEffects.ABSORPTION) ||
               effect.is(MobEffects.HASTE) ||
               effect.is(MobEffects.LUCK) ||
               effect.is(MobEffects.DOLPHINS_GRACE) ||
               effect.is(MobEffects.CONDUIT_POWER) ||
               effect.is(MobEffects.HERO_OF_THE_VILLAGE) ||
               effect.is(MobEffects.HEALTH_BOOST) ||
               effect.is(MobEffects.SATURATION);
    }

    private static BlockPos findSafeTeleportPosition(ServerLevel level, BlockPos pos) {
        // Force-load the chunk first: if it isn't loaded, the heightmap returns
        // the world floor and the player ends up teleported deep underground.
        level.getChunkAt(pos);
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        return new BlockPos(pos.getX(), y + 1, pos.getZ());
    }
}
