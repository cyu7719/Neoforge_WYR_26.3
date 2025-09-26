// ChoiceEffects.java
package net.coba.wyr;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.coba.wyr.item.ModItems;
import net.coba.wyr.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.brewing.PlayerBrewedPotionEvent;
import net.neoforged.neoforge.event.brewing.PotionBrewEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.minecraft.world.entity.monster.EnderMan;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.minecraft.world.entity.EntityDimensions;

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


    private static final ResourceLocation INSTA_HEAL_HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(WYR.MODID, "insta_heal_health_modifier");
    private static final ResourceLocation EXTRA_HEARTS_HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(WYR.MODID, "extra_hearts_health_modifier");
    private static final ResourceLocation GOLEM_HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(WYR.MODID, "golem_health");
    private static final ResourceLocation GOLEM_STRENGTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(WYR.MODID, "golem_strength");
    private static final ResourceLocation GOLEM_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(WYR.MODID, "golem_speed");
    private static final ResourceLocation WOLF_HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(WYR.MODID, "wolf_health");
    private static final ResourceLocation WOLF_STRENGTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(WYR.MODID, "wolf_strength");
    private static final ResourceLocation WOLF_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(WYR.MODID, "wolf_speed");

    private static final Map<UUID, Long> keyScrambleTimers = new HashMap<>();


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
            "are you sure about that",
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
            "ou talkin' to me",
            "You had me at 'hello",
            "I'll be back",
            "You can't handle the truth!",
            "Louis, I think this is the beginning of a beautiful friendship"
    );

    private static final Map<DyeColor, Item> WOOL_BY_COLOR = Map.ofEntries(
            Map.entry(DyeColor.WHITE, Items.WHITE_WOOL),
            Map.entry(DyeColor.ORANGE, Items.ORANGE_WOOL),
            Map.entry(DyeColor.MAGENTA, Items.MAGENTA_WOOL),
            Map.entry(DyeColor.LIGHT_BLUE, Items.LIGHT_BLUE_WOOL),
            Map.entry(DyeColor.YELLOW, Items.YELLOW_WOOL),
            Map.entry(DyeColor.LIME, Items.LIME_WOOL),
            Map.entry(DyeColor.PINK, Items.PINK_WOOL),
            Map.entry(DyeColor.GRAY, Items.GRAY_WOOL),
            Map.entry(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_WOOL),
            Map.entry(DyeColor.CYAN, Items.CYAN_WOOL),
            Map.entry(DyeColor.PURPLE, Items.PURPLE_WOOL),
            Map.entry(DyeColor.BLUE, Items.BLUE_WOOL),
            Map.entry(DyeColor.BROWN, Items.BROWN_WOOL),
            Map.entry(DyeColor.GREEN, Items.GREEN_WOOL),
            Map.entry(DyeColor.RED, Items.RED_WOOL),
            Map.entry(DyeColor.BLACK, Items.BLACK_WOOL)
    );

    @SubscribeEvent
    public void onEntitySize(EntityEvent.Size event) {
        if (event.getEntity() instanceof Player player && hasEffect(player, "fast_climb_short")) {
            EntityDimensions originalDimensions = player.getDimensions(player.getPose());
            EntityDimensions newDimensions = originalDimensions.scale(1.0F, 0.5F);
            // The correct method is setNewSize()
            event.setNewSize(newDimensions);
        }
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        Player player = event.getPlayer();
        if (hasEffect(player, "daily_totem_no_armor")) {
            ItemStack droppedStack = event.getEntity().getItem();
            CustomData customData = droppedStack.get(DataComponents.CUSTOM_DATA);
            if (customData != null && customData.copyTag().getBoolean("wyr_locked_totem")) {
                event.setCanceled(true); // Prevent dropping the locked totem
            }
        }
    }


    private static Set<String> getActiveEffects(Player player) {
        return activeEffects.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
    }

    public static boolean hasEffect(Player player, String effectId) {
        if (player == null) return false;
        return getActiveEffects(player).contains(effectId);
    }

    public static long getTeleportCooldown(Player player) {
        return teleportCooldowns.getOrDefault(player.getUUID(), 0L);
    }

    private static ServerPlayer getServerPlayer(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }
        if (player.level().isClientSide() || player.getServer() == null) return null;
        return player.getServer().getPlayerList().getPlayer(player.getUUID());
    }

    public static void applyInitialEffect(Player player, String effectId) {
        var serverPlayer = getServerPlayer(player);
        if (serverPlayer == null) return;
        var enchantRegistry = player.level().holderLookup(Registries.ENCHANTMENT);

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

        if ("speed_v_random_keys".equals(effectId)) {
            if (player instanceof ServerPlayer) {
                // Set the timer for the next scramble
                keyScrambleTimers.put(player.getUUID(), player.level().getGameTime() + (10 * 60 * 20)); // 10 minutes
                // Apply the first scramble immediately
                scrambleKeys(serverPlayer);
            }
        }

        if ("choose_spawn_biome_lock".equals(effectId)) {
            player.level().getBiome(player.blockPosition()).unwrapKey().ifPresent(key -> spawnBiomes.put(player.getUUID(), key));
            // Send packet to the client to open the GUI
            PacketDistributor.sendToPlayer(serverPlayer, new PacketOpenBiomeGui());
        }

        // --- ONE-TIME ACTIONS ---
        if ("op_bow_only".equals(effectId)) {
            ItemStack opBow = new ItemStack(Items.BOW);
            opBow.enchant(enchantRegistry.getOrThrow(Enchantments.INFINITY), 1);
            opBow.enchant(enchantRegistry.getOrThrow(Enchantments.POWER), 5);
            opBow.enchant(enchantRegistry.getOrThrow(Enchantments.PUNCH), 5);
            opBow.enchant(enchantRegistry.getOrThrow(Enchantments.LOOTING), 5);
            opBow.set(DataComponents.CUSTOM_NAME, Component.literal("The Only Weapon"));
            serverPlayer.getInventory().add(opBow);
            serverPlayer.getInventory().add(new ItemStack(Items.ARROW));
        }
        if ("fast_horse_dark_fear".equals(effectId)) {
            Horse horse = EntityType.HORSE.create(serverPlayer.level());
            if (horse != null) {
                horse.setPos(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
                horse.setTamed(true);
                horse.setOwnerUUID(serverPlayer.getUUID());
                horse.getInventory().setItem(0, new ItemStack(Items.SADDLE));
                Objects.requireNonNull(horse.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.1 + (0.3375 * 0.7));
                serverPlayer.level().addFreshEntity(horse);
                fastHorses.put(serverPlayer.getUUID(), horse);
            }
        }
        if ("unbreakable_elytra_no_boost".equals(effectId)) {
            ItemStack elytra = new ItemStack(Items.ELYTRA);
            elytra.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
            serverPlayer.getInventory().add(elytra);
        }
        if ("unbreakable_anvil_no_xp".equals(effectId)) {
            serverPlayer.getInventory().add(new ItemStack(Items.ANVIL));
        }
        if ("stack_64_world_height".equals(effectId)) {
            serverPlayer.teleportTo(serverPlayer.getX(), serverPlayer.level().getMaxBuildHeight() + 1, serverPlayer.getZ());
        }
        if ("choose_spawn_biome_lock".equals(effectId)) {
            player.level().getBiome(player.blockPosition()).unwrapKey().ifPresent(key -> spawnBiomes.put(player.getUUID(), key));
        }
        switch (effectId) {
            case "fire_sword_self_damage" -> {
                ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
                sword.enchant(enchantRegistry.getOrThrow(Enchantments.FIRE_ASPECT), 10);
                serverPlayer.getInventory().add(sword);
            }
            case "lava_fishing_rod" -> {
                ItemStack rod = new ItemStack(Items.FISHING_ROD);
                rod.enchant(enchantRegistry.getOrThrow(Enchantments.LUCK_OF_THE_SEA), 5);
                serverPlayer.getInventory().add(rod);
            }
            case "golem_suit_slow" -> {
                serverPlayer.getInventory().add(new ItemStack(ModItems.GOLEM_HELMET.get()));
                serverPlayer.getInventory().add(new ItemStack(ModItems.GOLEM_CHESTPLATE.get()));
                serverPlayer.getInventory().add(new ItemStack(ModItems.GOLEM_LEGGINGS.get()));
                serverPlayer.getInventory().add(new ItemStack(ModItems.GOLEM_BOOTS.get()));
            }
            case "personal_teleporter_scramble" -> serverPlayer.getInventory().add(new ItemStack(ModItems.PERSONAL_TELEPORTER.get()));
            case "unlimited_ice_cream_brain_freeze" -> serverPlayer.getInventory().add(new ItemStack(ModItems.UNLIMITED_ICE_CREAM.get()));
            case "infinite_xp_no_anvils" -> serverPlayer.setExperienceLevels(1000000000);
        }
        reapplyPersistentEffect(player, effectId);
    }

    private static void scrambleKeys(ServerPlayer player) {
        Random random = new Random();
        int scrambleState = random.nextInt(4) + 1; // Random state from 1 to 4
        // Here you would save the state to WyrSavedData
        // For now, we'll just send it
        PacketDistributor.sendToPlayer(player, new PacketSyncScrambleState(scrambleState));
    }


    public static void reapplyPersistentEffect(Player player, String effectId) {
        var serverPlayer = getServerPlayer(player);
        if (serverPlayer == null) return;

        Set<String> playerEffects = getActiveEffects(player);
        if (playerEffects.contains(effectId)) return;
        playerEffects.add(effectId);

        if ("fast_climb_short".equals(effectId)) {
            player.refreshDimensions();
        }

        if ("speed_v_random_keys".equals(effectId)) {
            WyrSavedData savedData = WyrSavedData.get(((ServerPlayer)player).serverLevel());
            int scrambleState = savedData.getPlayerScrambleState(player.getUUID()); // You will need to add this method to WyrSavedData
            if (player instanceof ServerPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncScrambleState(scrambleState));
            }
        }

        if ("stacked_logs".equals(effectId)) {
            PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncStackedLogsStatus(true));
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
            player.level().getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true, player.getServer());
        }
    }

    public static void removeEffect(Player player, String effectId) {
        getActiveEffects(player).remove(effectId);
        ServerPlayer serverPlayer = getServerPlayer(player);
        if (serverPlayer == null) return;

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
            case "golem_suit_slow":
                AttributeInstance health = serverPlayer.getAttribute(Attributes.MAX_HEALTH);
                if (health != null) health.removeModifier(GOLEM_HEALTH_MODIFIER_ID);
                AttributeInstance strength = serverPlayer.getAttribute(Attributes.ATTACK_DAMAGE);
                if (strength != null) strength.removeModifier(GOLEM_STRENGTH_MODIFIER_ID);
                AttributeInstance speed = serverPlayer.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speed != null) speed.removeModifier(GOLEM_SPEED_MODIFIER_ID);
                break;
        }
    }

    public static void clearActiveEffects(Player player) {
        UUID playerUUID = player.getUUID();
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
        }
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        // Check if the player has the effect and is breaking an ore
        if (hasEffect(event.getEntity(), "hidden_from_mobs_slow_ores") && isOre(event.getState())) {
            // Set the new mining speed to half of the original
            event.setNewSpeed(event.getOriginalSpeed() / 2.0F);
        }
    }

    public static void clearAllEffects(Player player) {
        ServerPlayer serverPlayer = getServerPlayer(player); // Get the ServerPlayer instance
        if (serverPlayer == null) return;

        if (getActiveEffects(player).contains("fast_swim_muted")) {
            if (serverPlayer != null) PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncMuteStatus(false));
        }

        if (getActiveEffects(player).contains("no_hunger_random_shader")) {
            if (serverPlayer != null) PacketDistributor.sendToPlayer(serverPlayer, new net.coba.wyr.PacketSyncShaderStatus(false));
        }

        if (getActiveEffects(player).contains("hero_of_village_random_sounds")) {
            PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncRandomSoundsStatus(false));
        }

        if (hasEffect(player, "keep_inventory_berries_only") && player.getServer() != null) {
            boolean anotherPlayerHasEffect = false;
            for (ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
                if (!p.getUUID().equals(player.getUUID()) && hasEffect(p, "keep_inventory_berries_only")) {
                    anotherPlayerHasEffect = true;
                    break;
                }
            }
            if (!anotherPlayerHasEffect) {
                player.level().getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(false, player.getServer());
            }
        }
        if (serverPlayer != null) {
            if(getActiveEffects(player).contains("stacked_logs")) {
                PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncStackedLogsStatus(false));
            }
            PlayerChoiceTracker.clearChoices(serverPlayer);
        }
        clearActiveEffects(player);
    }

    // --- EVENT HANDLERS ---

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

        // --- Villager Talk Logic ---
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
            int interval = savedData.getPlayerPromptInterval(player.getUUID());
            if (interval > 0 && PlayerChoiceTracker.getPromptCount(player) < 20 && !PlayerChoiceTracker.isPromptPending(player)) {
                long nextPromptTime = savedData.getNextPromptTime(player.getUUID());
                if (currentTime >= nextPromptTime) {
                    List<Choice> newChoices = ChoiceRegistry.getFilteredRandomChoicesForPlayer(player);
                    if (!newChoices.isEmpty()) {
                        PlayerChoiceTracker.setPromptPending(player, true);
                        PacketDistributor.sendToPlayer(player, new PacketSyncNewChoices(newChoices));
                    }
                    savedData.setNextPromptTime(player.getUUID(), currentTime + interval);
                    player.serverLevel().getDataStorage().save();
                }
            }

            if (player.tickCount % 40 == 0) {
                boolean hasEffect = hasEffect(player, "strong_wolves_one_hp");
                // Find all tamed wolves owned by this player
                List<Wolf> wolves = player.level().getEntitiesOfClass(Wolf.class, player.getBoundingBox().inflate(128),
                        wolf -> wolf.isTame() && player.getUUID().equals(wolf.getOwnerUUID()));

                for (Wolf wolf : wolves) {
                    boolean isModified = wolf.getPersistentData().getBoolean("wyr_wolf_modified");
                    AttributeInstance health = wolf.getAttribute(Attributes.MAX_HEALTH);
                    AttributeInstance damage = wolf.getAttribute(Attributes.ATTACK_DAMAGE);
                    AttributeInstance speed = wolf.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (health == null || damage == null || speed == null) continue;

                    if (hasEffect && !isModified) {
                        // Apply the 1HP effect
                        health.addPermanentModifier(new AttributeModifier(WOLF_HEALTH_MODIFIER_ID, -health.getBaseValue() + 1, AttributeModifier.Operation.ADD_VALUE));
                        damage.addPermanentModifier(new AttributeModifier(WOLF_STRENGTH_MODIFIER_ID, 4.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        speed.addPermanentModifier(new AttributeModifier(WOLF_SPEED_MODIFIER_ID, 2.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                        wolf.setHealth(1.0f);
                        wolf.getPersistentData().putBoolean("wyr_wolf_modified", true);
                    } else if (!hasEffect && isModified) {
                        // Remove the effect and restore health
                        health.removeModifier(WOLF_HEALTH_MODIFIER_ID);
                        damage.removeModifier(WOLF_STRENGTH_MODIFIER_ID);
                        speed.removeModifier(WOLF_SPEED_MODIFIER_ID);
                        wolf.setHealth(wolf.getMaxHealth());
                        wolf.getPersistentData().remove("wyr_wolf_modified");
                    }
                }
            }

            if (hasEffect(player, "nether_water_fast_lava")) {
                if (player.tickCount % 10 == 0) { // Check every half-second to avoid lag
                    ServerLevel level = player.serverLevel();
                    BlockPos.betweenClosedStream(player.blockPosition().offset(-8, -8, -8), player.blockPosition().offset(8, 8, 8))
                            .forEach(pos -> {
                                BlockState state = level.getBlockState(pos);
                                if (state.is(Blocks.LAVA)) {
                                    // Force the lava to update (flow) very quickly
                                    level.scheduleTick(pos, state.getFluidState().getType(), 1);
                                }
                            });
                }
            }

            if (hasEffect(player, "speed_v_random_keys")) {
                long nextScrambleTime = keyScrambleTimers.getOrDefault(player.getUUID(), 0L);
                if (currentTime >= nextScrambleTime) {
                    scrambleKeys(player);
                    keyScrambleTimers.put(player.getUUID(), currentTime + (10 * 60 * 20));
                }
            }

            // --- Villager Talk Trigger ---
            if (hasEffect(player, "villager_talk_no_fence_gates") && player.tickCount % 20 == 0) {
                if (player.getRandom().nextFloat() < 0.20) { // 20% chance each second
                    List<Villager> nearbyVillagers = player.level().getEntitiesOfClass(Villager.class, player.getBoundingBox().inflate(32), v -> !talkingVillagerTimers.containsKey(v.getUUID()));
                    if (!nearbyVillagers.isEmpty()) {
                        Villager villager = nearbyVillagers.get(player.getRandom().nextInt(nearbyVillagers.size()));
                        String phrase = VILLAGER_PHRASES.get(player.getRandom().nextInt(VILLAGER_PHRASES.size()));
                        villager.setCustomName(Component.literal(phrase));
                        villager.setCustomNameVisible(true);
                        talkingVillagerTimers.put(villager.getUUID(), 100);
                    }
                }
            }

            if (hasEffect(player, "fast_smelt_damage")) {
                if (player.tickCount % 5 == 0) {
                    BlockPos.betweenClosedStream(player.blockPosition().offset(-8, -8, -8), player.blockPosition().offset(8, 8, 8))
                            .forEach(pos -> {
                                BlockEntity be = player.level().getBlockEntity(pos);
                                if (be instanceof AbstractFurnaceBlockEntity furnace) {
                                    if (furnace.getPersistentData().getInt("litTime") > 0) {
                                        for (int i = 0; i < 19; i++) {
                                            AbstractFurnaceBlockEntity.serverTick(player.level(), pos, player.level().getBlockState(pos), furnace);
                                        }
                                    }
                                }
                            });
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteractWithVillager(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof Villager villager && event.getEntity() instanceof Player player) {

            if (player.level().isClientSide) {
                return;
            }

            if (hasEffect(player, "cheap_trades_sink_boats") || hasEffect(player, "cheap_trades_no_ore_drops")) {
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
        // Check if the crafted item is our special unbreakable tool
        if (event.getCrafting().get(DataComponents.UNBREAKABLE) != null &&
                event.getCrafting().getOrDefault(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false)) {

            // If it is, check if the player has the effect
            if (hasEffect(event.getEntity(), "unbreakable_tools_netherite")) {
                // Clear all items from the crafting grid
                event.getInventory().clearContent();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (hasEffect(player, "lava_fishing_rod")) {
            // Check if the player has a fishing bobber cast
            if (player.fishing != null) {
                // If the bobber is in water, immediately retract it
                if (player.fishing.isInWater()) {
                    player.fishing.discard(); // Removes the bobber entity
                }
            }
        }

        if (hasEffect(player, "infinite_sprint_no_elytra")) {
            ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chestItem.is(Items.ELYTRA)) {
                // --- THIS IS THE FIXED LINE ---
                // The chestplate is index 2 in the armor list
                player.getInventory().armor.set(2, ItemStack.EMPTY);

                // Try to add it back to the main inventory, or drop it if full
                if (!player.getInventory().add(chestItem)) {
                    player.drop(chestItem, false);
                }
            }
        }

        if (hasEffect(player, "sneak_bonemeal_fov_change") && player.isCrouching() && player.tickCount % 20 == 0) { // Once per second while sneaking
            BlockPos center = player.blockPosition().below();
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, 0, -3), center.offset(3, 0, 3))) {
                BlockState state = player.level().getBlockState(pos);
                if (state.getBlock() instanceof BonemealableBlock bonemealable) {
                    if (bonemealable.isValidBonemealTarget(player.level(), pos, state)) {
                        bonemealable.performBonemeal((ServerLevel) player.level(), player.getRandom(), pos, state);
                    }
                }
            }
        }

        if (hasEffect(player, "random_food_no_jump")) {
            // Apply a jump debuff with a high enough amplifier to make jumping impossible.
            // The last 'false' argument hides the effect icon from the GUI.
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 2, 128, true, false, false));
        }

        if (hasEffect(player, "no_durability_loss_adventure_mode")) {
            // Create a list of all items the player is wearing or holding
            List<ItemStack> equipment = new ArrayList<>();
            equipment.add(player.getMainHandItem());
            equipment.add(player.getOffhandItem());
            player.getInventory().armor.forEach(equipment::add);

            for (ItemStack stack : equipment) {
                // If the item can be damaged and has taken damage, repair it by 1 point
                if (stack.isDamageableItem() && stack.getDamageValue() > 0) {
                    stack.setDamageValue(stack.getDamageValue() - 1);
                }
            }
        }

        if (hasEffect(player, "enchant_level_x_delete_item")) {
            // Check both inventory and cursor for the marked item
            List<ItemStack> allItems = new ArrayList<>(player.getInventory().items);
            allItems.add(player.containerMenu.getCarried());

            for (ItemStack stack : allItems) {
                if (!stack.isEmpty()) {
                    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                    // Check if the component exists and has our marker
                    if (customData != null && customData.copyTag().getBoolean("wyr_delete_item_marker")) {
                        CompoundTag tag = customData.copyTag();
                        tag.remove("wyr_delete_item_marker"); // Remove the marker
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag)); // Set the data back

                        List<ItemStack> inventoryItems = player.getInventory().items.stream()
                                .filter(s -> !s.isEmpty() && !s.equals(stack))
                                .collect(Collectors.toList());

                        if (!inventoryItems.isEmpty()) {
                            inventoryItems.get(player.getRandom().nextInt(inventoryItems.size())).setCount(0);
                        }
                        break;
                    }
                }
            }
        }

        // --- NEW LOGIC FOR HORSE ---
        if (hasEffect(player, "fast_horse_dark_fear") && player.level().isNight()) {
            Horse horse = fastHorses.get(player.getUUID());
            if (horse != null) {
                // Stop horse from wandering
                if (!horse.isLeashed()) {
                    horse.getNavigation().stop();
                    horse.setDeltaMovement(0, horse.getDeltaMovement().y, 0);
                }
                // Dismount player if they are riding it
                if (player.isPassenger() && player.getVehicle() == horse) {
                    player.stopRiding();
                }
            }
        }


        if (hasEffect(player, "choose_spawn_biome_lock")) {
            ResourceKey<net.minecraft.world.level.biome.Biome> spawnBiome = spawnBiomes.get(player.getUUID());
            if (spawnBiome != null && player.level().getDayTime() < (10 * 24000)) { // 10 Minecraft days
                player.level().getBiome(player.blockPosition()).unwrapKey().ifPresent(currentBiome -> {
                    if (currentBiome != spawnBiome) {
                        BlockPos spawn = player.getRespawnPosition();
                        if (spawn != null) {
                            player.teleportTo(spawn.getX(), spawn.getY(), spawn.getZ());
                            player.sendSystemMessage(Component.literal("You cannot leave your spawn biome yet!"));
                        }
                    }
                });
            }
        }

        if (hasEffect(player, "unbreakable_tools_netherite") && player.containerMenu instanceof CraftingMenu craftingMenu) {
            ItemStack centerItem = craftingMenu.getSlot(5).getItem();

            boolean patternMatches = !centerItem.isEmpty() && centerItem.isDamageableItem();
            if (patternMatches) {
                for (int i = 1; i <= 9; i++) {
                    if (i == 5) continue;
                    ItemStack slotItem = craftingMenu.getSlot(i).getItem();
                    if (!slotItem.is(Items.NETHERITE_INGOT) || slotItem.getCount() != 4) {
                        patternMatches = false;
                        break;
                    }
                }
            }

            if (patternMatches) {
                ItemStack unbreakableTool = centerItem.copy();
                // Makes the item unbreakable but hides the "Unbreakable" tooltip
                unbreakableTool.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
                // Adds the enchantment glint without adding any actual enchantments
                unbreakableTool.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

                craftingMenu.getSlot(0).set(unbreakableTool);
            } else {
                ItemStack resultItem = craftingMenu.getSlot(0).getItem();
                if (!resultItem.isEmpty() && resultItem.get(DataComponents.UNBREAKABLE) != null) {
                    craftingMenu.getSlot(0).set(ItemStack.EMPTY);
                }
            }
        }


        boolean hasSoundEffect = hasEffect(player, "emeralds_from_mobs_siren") || hasEffect(player, "instant_tame_creeper_hiss") || hasEffect(player, "auto_smelt_random_sound");
        if (hasSoundEffect) {
            int timer = randomSoundTimers.getOrDefault(player.getUUID(), -1);
            if (timer == -1) {
                timer = 20 * 30;
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
                } else if (hasEffect(player, "auto_smelt_random_sound")) {
                    soundToPlay = ModSounds.auto_smelt_knock;
                    volume = 1.5F;
                }
                if (soundToPlay != null) PacketDistributor.sendToPlayer(player, new PacketPlayCustomSound(soundToPlay.getId(), volume, pitch));
                timer = player.getRandom().nextInt(20 * 60) + (20*30); // Random interval between 30s and 1.5m
            }
            randomSoundTimers.put(player.getUUID(), timer);
        }

        Set<String> playerEffects = getActiveEffects(player);
        if (playerEffects.isEmpty()) return;

        // Permanent Potion Effects
        if (hasEffect(player, "speed_iv")) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2, 3, true, false));
        if (hasEffect(player, "haste_ii")) player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 2, 1, true, false));
        if (hasEffect(player, "high_jump")) player.addEffect(new MobEffectInstance(MobEffects.JUMP, 2, 4, true, false));
        if (hasEffect(player, "water_breathing")) player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 2, 0, true, false));
        if (hasEffect(player, "night_vision")) player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false));
        if (hasEffect(player, "dolphin_grace")) player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 2, 0, true, false));
        if (hasEffect(player, "resistance_ii")) player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2, 1, true, false));
        if (hasEffect(player, "strength_ii")) player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2, 1, true, false));

        // Tick-based logic
        int ticks = playerTickCounters.getOrDefault(player.getUUID(), 0);

        if (hasEffect(player, "mob_loot_inventory_slots")) {
            for (int i = 9; i < 36; i++) { // Only check main inventory, not hotbar
                ItemStack stackInSlot = player.getInventory().getItem(i);
                if (!stackInSlot.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                    player.drop(stackInSlot, true, false);
                }
            }
        }
        if (hasEffect(player, "give_decoration_blind") && player.level().isDay() && player.level().canSeeSky(player.blockPosition())) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, true, false));
        }
        if (hasEffect(player, "choose_spawn_biome_lock")) {
            ResourceKey<net.minecraft.world.level.biome.Biome> spawnBiome = spawnBiomes.get(player.getUUID());
            if (spawnBiome != null && player.level().getDayTime() < (10 * 24000)) {
                player.level().getBiome(player.blockPosition()).unwrapKey().ifPresent(currentBiome -> {
                    if (currentBiome != spawnBiome) {
                        BlockPos spawn = player.getRespawnPosition();
                        if (spawn != null) {
                            player.teleportTo(spawn.getX(), spawn.getY(), spawn.getZ());
                            player.sendSystemMessage(Component.literal("You cannot leave your spawn biome yet!"));
                        }
                    }
                });
            }
        }
        if (hasEffect(player, "stack_64_world_height")) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.isStackable() && stack.getCount() < Math.min(64, stack.getMaxStackSize())) {
                    stack.setCount(Math.min(64, stack.getMaxStackSize()));
                }
            }
        }
        if (hasEffect(player, "fast_climb_short") && player.onClimbable()) {
            Vec3 motion = player.getDeltaMovement();
            if (motion.y < 0.2) {
                player.setDeltaMovement(motion.x, 0.2, motion.z);
            }
        }

        if (ticks % 200 == 0) { // Every 10 seconds
            if (hasEffect(player, "passive_xp_no_mending")) player.giveExperiencePoints(7);
        }
        if (ticks % 600 == 0) { // Every 30 seconds
            if (hasEffect(player, "wind_charge_respawn_mob")) player.getInventory().add(new ItemStack(Items.WIND_CHARGE));
        }
        if (ticks % 1200 == 0) { // Every 1 minute
            if (hasEffect(player, "random_food_no_jump")) {
                List<Item> foods = BuiltInRegistries.ITEM.stream().filter(item -> item.components().has(DataComponents.FOOD)).toList();
                if (!foods.isEmpty()) player.getInventory().add(new ItemStack(foods.get(player.getRandom().nextInt(foods.size()))));
            }
        }
        playerTickCounters.put(player.getUUID(), ticks + 1);

        // Negative Consequences & Other Logic
        if (hasEffect(player, "speed_iv")) player.getFoodData().addExhaustion(0.015f * 3);
        if (hasEffect(player, "water_breathing") && !player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) player.drop(player.getInventory().armor.set(3, ItemStack.EMPTY), true);
        if (hasEffect(player, "extra_hearts") && !player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) player.drop(player.getInventory().armor.set(2, ItemStack.EMPTY), true);
        if (hasEffect(player, "triple_damage") && !player.getItemBySlot(EquipmentSlot.FEET).isEmpty()) player.drop(player.getInventory().armor.set(0, ItemStack.EMPTY), true);
        if (hasEffect(player, "resistance_ii") && player.isSprinting()) player.setSprinting(false);
        if (hasEffect(player, "dolphin_grace") && !player.isInWater() && !player.isCreative()) player.hurt(player.damageSources().drown(), 1.0f);
        if (hasEffect(player, "fast_horse_dark_fear") && player.level().isNight()) {
            Horse horse = fastHorses.get(player.getUUID());
            if (horse != null && !horse.isLeashed()) {
                horse.getNavigation().stop();
                horse.setDeltaMovement(0, horse.getDeltaMovement().y, 0);
            }
        }
        if (hasEffect(player, "cheap_trades_sink_boats") && player.getVehicle() instanceof Boat boat) {
            // Apply a small downward force to make the boat sink
            boat.setDeltaMovement(boat.getDeltaMovement().add(0, -0.02, 0));

            // Add some bubble particles to show it's sinking
            if (player.tickCount % 5 == 0) { // Spawn particles every quarter-second
                ((ServerLevel)player.level()).sendParticles(ParticleTypes.BUBBLE, boat.getX(), boat.getY() + 0.5, boat.getZ(), 5, 0.3, 0.3, 0.3, 0.0);
            }
        }
        if (hasEffect(player, "no_hunger_random_shader") || hasEffect(player, "infinite_sprint_no_elytra")) {
            if (player.getFoodData().needsFood()) player.getFoodData().setFoodLevel(20);
        }
        if (hasEffect(player, "creative_flight") && player.getAbilities().flying) {
            if (ticks % 200 == 0) {
                List<ItemStack> inventory = new ArrayList<>(player.getInventory().items);
                inventory.removeIf(ItemStack::isEmpty);
                if (!inventory.isEmpty()) inventory.get(player.getRandom().nextInt(inventory.size())).setCount(0);
            }
        }
        if (hasEffect(player, "daily_diamond")) {
            long currentTime = player.level().getGameTime();
            long lastTime = dailyDiamondCooldown.getOrDefault(player.getUUID(), 0L);
            if (currentTime > lastTime && player.level().getDayTime() % 24000 < 100) {
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
            if (player.level().isNight() && player.gameMode.getGameModeForPlayer() != GameType.ADVENTURE) player.setGameMode(GameType.ADVENTURE);
            else if (player.level().isDay() && player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) player.setGameMode(GameType.SURVIVAL);
        }
        if (hasEffect(player, "daily_totem_no_armor") && player.level().isDay() && player.level().getDayTime() % 24000 < 100 && ticks % 24000 > 100) {
            if (player.getOffhandItem().isEmpty()) player.getInventory().offhand.set(0, new ItemStack(Items.TOTEM_OF_UNDYING));
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        Set<String> playerEffects = getActiveEffects(player);
        if (playerEffects.isEmpty()) return;
        for (String effectId : playerEffects) {
            switch (effectId) {
                case "golem_suit_slow": handleGolemSet(player); break;
                case "water_walk":
                    if (!player.isCrouching()) {
                        BlockPos playerPos = player.blockPosition();
                        BlockState state = player.level().getBlockState(playerPos.below());
                        if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
                            Vec3 motion = player.getDeltaMovement();
                            player.setDeltaMovement(motion.x, 0.0, motion.z);
                            player.setOnGround(true);
                        }
                    }
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 1, true, false));
                    break;
                case "wall_climb":
                    if(player.horizontalCollision && !player.getAbilities().flying) {
                        Vec3 motion = player.getDeltaMovement();
                        player.setDeltaMovement(motion.x, 0.2, motion.z);
                    }
                    break;
                case "midas_touch_no_hp_bar":
                    BlockPos below = player.blockPosition().below();
                    if (player.onGround() && !player.level().getBlockState(below).is(Blocks.GOLD_BLOCK)) {
                        player.level().setBlock(below, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
                    }
                    break;
                case "double_debris_vampire":
                    if (player.level().isDay() && player.level().canSeeSky(player.blockPosition()) && !player.isCreative()) {
                        player.igniteForSeconds(2);
                    }
                    break;
                case "fast_day_slow_night":
                    if (player.level().isDay()) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2, 3, true, false));
                    else player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 255, true, false));
                    break;
                case "fast_night_slow_day":
                    if (player.level().isNight()) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2, 3, true, false));
                    else player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 255, true, false));
                    break;
                case "speed_v_random_keys": player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2, 4, true, false)); break;
                case "hero_of_village_random_sounds": player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 2, 0, true, false)); break;
                case "slow_fall_side_place_only": player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 2, 0, true, false)); break;
                case "sneak_invisible_no_move":
                    if (player.isCrouching()) {
                        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 2, 0, true, false));
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 255, true, false));
                    }
                    break;
                case "fast_swim_muted":
                    if (player.isInWater()) player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 2, 1, true, false));
                    break;
                case "no_fall_damage_sneak_speed": player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 0, true, false)); break;
                case "infinite_sprint_no_elytra": player.setSprinting(true); break;
            }
        }
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
        Player player = event.getPlayer();
        if (player.level().isClientSide) return;
        ServerPlayer serverPlayer = getServerPlayer(player);
        if (serverPlayer == null) return;

        if (hasEffect(player, "sleep_heal_random_drops")) {
            List<Item> allItems = BuiltInRegistries.ITEM.stream().toList();
            if (!allItems.isEmpty()) {
                Item randomItem = allItems.get(player.getRandom().nextInt(allItems.size()));
                event.setCanceled(true);
                player.level().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);
                Block.popResource((ServerLevel)player.level(), event.getPos(), new ItemStack(randomItem));
            }
            return;
        }
        if (hasEffect(player, "unbreakable_tools_cobblestone_only")) {
            event.setCanceled(true);
            player.level().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);
            Block.popResource((ServerLevel)player.level(), event.getPos(), new ItemStack(Items.COBBLESTONE));
            return;
        }
        if(hasEffect(player, "triple_drops_slow_break")) {
            List<ItemStack> drops = Block.getDrops(event.getState(), serverPlayer.serverLevel(), event.getPos(), null, player, player.getMainHandItem());
            event.setCanceled(true);
            player.level().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);
            for(ItemStack stack : drops) {
                stack.setCount(stack.getCount() * 3);
                Block.popResource(serverPlayer.serverLevel(), event.getPos(), stack);
            }
        }
        if (hasEffect(player, "stacked_logs")) {
            if (event.getState().is(BlockTags.LOGS)) {
                var silkTouchHolder = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(Enchantments.SILK_TOUCH);
                if (silkTouchHolder.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(silkTouchHolder.get(), player.getMainHandItem()) > 0) return;
                ItemStack logDrop = new ItemStack(event.getState().getBlock().asItem());
                logDrop.setCount(64);
                Block.popResource((ServerLevel) event.getLevel(), event.getPos(), logDrop);
                event.getLevel().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);
                event.setCanceled(true);
            }
        }
        if (hasEffect(player, "auto_smelt_random_sound") && isOre(event.getState())) {
            // Cancel the original event so we can control the outcome
            event.setCanceled(true);
            // Manually break the block
            player.level().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);

            // Only process drops and durability if the player is NOT in creative mode
            if (!player.isCreative()) {
                List<ItemStack> drops = Block.getDrops(event.getState(), serverPlayer.serverLevel(), event.getPos(), null, player, player.getMainHandItem());

                for (ItemStack dropStack : drops) {
                    Optional<RecipeHolder<SmeltingRecipe>> recipe = serverPlayer.getServer().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(dropStack), player.level());

                    if (recipe.isPresent()) {
                        ItemStack smeltedResult = recipe.get().value().getResultItem(player.level().registryAccess()).copy();
                        smeltedResult.setCount(dropStack.getCount());
                        Block.popResource(serverPlayer.serverLevel(), event.getPos(), smeltedResult);
                    } else {
                        Block.popResource(serverPlayer.serverLevel(), event.getPos(), dropStack);
                    }
                }

                // Manually apply durability damage to the tool
                ItemStack tool = player.getMainHandItem();
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
    }

    private static boolean isOre(BlockState state) {
        return state.is(BlockTags.IRON_ORES) || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES) || state.is(BlockTags.LAPIS_ORES) || state.is(BlockTags.COPPER_ORES) || state.is(BlockTags.COAL_ORES) || state.is(BlockTags.REDSTONE_ORES) || state.is(Blocks.NETHER_GOLD_ORE) || state.is(Blocks.NETHER_QUARTZ_ORE);
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockState state = player.level().getBlockState(event.getPos());

        if (hasEffect(player, "villager_talk_no_fence_gates") && state.is(BlockTags.FENCE_GATES)) {
            event.setCanceled(true);
            return;
        }

        if (hasEffect(player, "build_limit_638_top_bottom_only")) {
            // Check if the player is holding a block to prevent canceling other right-click actions
            if (event.getItemStack().getItem() instanceof BlockItem) {
                Direction face = event.getFace();
                if (face != Direction.UP && face != Direction.DOWN) {
                    event.setCanceled(true);
                    return; // Stop processing to prevent placing the block
                }
            }
        }

        if (hasEffect(player, "triple_ores") && state.getBlock() instanceof EnchantingTableBlock) event.setCanceled(true);
        if (hasEffect(player, "sand_treasure") && (state.is(Blocks.FURNACE) || state.is(Blocks.BLAST_FURNACE) || state.is(Blocks.SMOKER))) event.setCanceled(true);
        if (hasEffect(player, "phasing") && state.getBlock() instanceof DoorBlock) event.setCanceled(true);
        if (hasEffect(player, "tame_hostiles_no_crafting") && state.is(Blocks.CRAFTING_TABLE)) event.setCanceled(true);
        if (hasEffect(player, "creeper_cake_no_fence_gates") && state.is(BlockTags.FENCE_GATES)) event.setCanceled(true);
        if (hasEffect(player, "no_phantoms_no_beds") && state.is(BlockTags.BEDS)) event.setCanceled(true);
        if (hasEffect(player, "extra_slots_no_shulkers") && state.getBlock() instanceof ShulkerBoxBlock) event.setCanceled(true);
        if (hasEffect(player, "infinite_xp_no_anvils") && (state.is(BlockTags.ANVIL) || state.is(Blocks.ENCHANTING_TABLE))) event.setCanceled(true);
        if (hasEffect(player, "nether_water_fast_lava") && player.level().dimension() == Level.NETHER && event.getItemStack().is(Items.WATER_BUCKET)) {
            if(!player.isCreative()) {
                // Correctly replace the water bucket with an empty one
                player.setItemInHand(event.getHand(), new ItemStack(Items.BUCKET));
            }
            player.level().setBlock(event.getPos().relative(event.getFace()), Blocks.WATER.defaultBlockState(), 3);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        Player player = event.getPlayer();
        if (hasEffect(player, "enchant_level_x_delete_item")) {
            ItemStack left = event.getLeft();
            ItemStack right = event.getRight();
            var leftEnchants = EnchantmentHelper.getEnchantmentsForCrafting(left);
            var rightEnchants = EnchantmentHelper.getEnchantmentsForCrafting(right);

            for (Map.Entry<Holder<Enchantment>, Integer> entry : rightEnchants.entrySet()) {
                Holder<Enchantment> ench = entry.getKey();
                int rightLevel = entry.getValue();
                int leftLevel = leftEnchants.getLevel(ench);

                // FIX: This condition now correctly caps the level at max + 1
                if (leftLevel == rightLevel && leftLevel == ench.value().getMaxLevel()) {
                    ItemStack output = left.copy();
                    EnchantmentHelper.updateEnchantments(output, (builder) -> builder.set(ench, leftLevel + 1));

                    CustomData customData = output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    CompoundTag tag = customData.copyTag();

                    // Add a special tag to the item so we know when to delete another item
                    tag.putBoolean("wyr_delete_item_marker", true);

                    event.setOutput(output);
                    event.setCost(10); // Set a cost for the operation
                    return;
                }
            }
        }
        // ... (rest of the anvil logic remains)
        if (hasEffect(player, "triple_ores")) {
            if (event.getRight().getItem() instanceof EnchantedBookItem) event.setCanceled(true);
        }
        if (hasEffect(player, "mace_max_damage_weak") && event.getLeft().is(Items.MACE)) event.setCanceled(true);
        if (hasEffect(player, "passive_xp_no_mending")) {
            var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(event.getRight());
            if (enchantments.keySet().stream().anyMatch(h -> h.is(Enchantments.MENDING))) event.setCanceled(true);
        }
        if (hasEffect(player, "stacked_logs") && event.getLeft().getItem() instanceof AxeItem) event.setCanceled(true);
    }

    // Add this new event handler method
    @SubscribeEvent
    public void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (hasEffect(event.getEntity(), "fast_smelt_damage")) {
            // Deal half a heart of damage
            event.getEntity().hurt(event.getEntity().damageSources().magic(), 1.0F);
        }
    }

    @SubscribeEvent
    public void onPlayerFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack item = event.getItem();
        FoodProperties food = item.get(DataComponents.FOOD);

        if (hasEffect(player, "eat_anything_hunger_chance") && food == null) {
            // Restore 1 hunger and 1 saturation
            player.getFoodData().eat(1, 1.0F);

            // 10% chance to get Hunger effect
            if (player.getRandom().nextFloat() < 0.1) {
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0));
            }

            // Manually consume one item
            if (!player.isCreative()) {
                ItemStack result = event.getItem().copy();
                result.shrink(1);
                event.setResultStack(result);
            }
            return; // Stop processing to prevent other effects from triggering on this non-food item
        }

        if (hasEffect(player, "double_food_no_hunger_bar") && food != null) {
            player.getFoodData().eat(food.nutrition(), food.saturation());
        }
        if (hasEffect(player, "random_good_bad_potions")) {
            List<Holder<MobEffect>> goodEffects = BuiltInRegistries.MOB_EFFECT.holders().filter(h -> h.value().isBeneficial()).collect(Collectors.toList());
            List<Holder<MobEffect>> badEffects = BuiltInRegistries.MOB_EFFECT.holders().filter(h -> !h.value().isBeneficial()).collect(Collectors.toList());

            if(!goodEffects.isEmpty() && !badEffects.isEmpty()) {
                Holder<MobEffect> goodEffect = goodEffects.get(player.getRandom().nextInt(goodEffects.size()));
                Holder<MobEffect> badEffect = badEffects.get(player.getRandom().nextInt(badEffects.size()));

                // --- NEW LOGIC: Choose a random amplifier (level) from 0-3, which corresponds to level I-IV ---
                int goodAmplifier = player.getRandom().nextInt(4);
                int badAmplifier = player.getRandom().nextInt(4);

                player.addEffect(new MobEffectInstance(goodEffect, 600, goodAmplifier)); // 30 seconds
                player.addEffect(new MobEffectInstance(badEffect, 600, badAmplifier));
            }
        }

        if (hasEffect(player, "daily_diamond")) {
            WyrSavedData savedData = WyrSavedData.get(player.serverLevel());
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
            if (hasEffect(player, "keep_inventory_berries_only")) {
                // Check if the item is food, but is NOT sweet berries
                if (itemStack.get(DataComponents.FOOD) != null && !itemStack.is(Items.SWEET_BERRIES)) {
                    event.setCanceled(true); // Cancel the eating action
                    return; // Stop processing
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
    }

    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (hasEffect(player, "creative_flight") && !player.getAbilities().flying) {
            float distance = event.getDistance();
            if (distance > 3.0F) player.hurt(player.damageSources().fall(), (int) Math.ceil(distance - 3.0F));
            event.setCanceled(true);
            return;
        }
        if (hasEffect(player, "high_jump")) {
            if (event.getDistance() > 2.0f) player.hurt(player.damageSources().fall(), (event.getDistance() - 2.0f));
            event.setCanceled(true);
        }
        if (hasEffect(player, "mace_max_damage_weak")) {
            if (player.getMainHandItem().is(Items.MACE) && event.getDistance() >= 3.0F) event.setDamageMultiplier(100f);
            else if (event.getDistance() > 3.0F) event.setDamageMultiplier(0.5f);
        }
        if (hasEffect(player, "no_fall_damage_low_render_distance") || hasEffect(player, "no_fall_damage_sneak_speed")) event.setCanceled(true);
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
        if (event.getEntity().getType() == EntityType.ENDER_DRAGON && event.getSource().getEntity() instanceof Player player && hasEffect(player, "one_hit_dragon_no_end")) {
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
            if (hasEffect(player, "wind_charge_respawn_mob") && !monster.getTags().contains("wyr_respawned")) {
                Monster newMob = (Monster) monster.getType().create(player.level());
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
            BlockPos respawnPos = respawnLocations.get(player.getUUID());
            ResourceKey<Level> respawnDim = respawnDimensions.get(player.getUUID());
            if (respawnPos != null && respawnDim != null) {
                ServerLevel targetLevel = player.getServer().getLevel(respawnDim);
                if (targetLevel != null) player.teleportTo(targetLevel, respawnPos.getX() + 0.5, respawnPos.getY(), respawnPos.getZ() + 0.5, player.getYRot(), player.getXRot());
                respawnLocations.remove(player.getUUID());
                respawnDimensions.remove(player.getUUID());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        // This event fires when a player respawns
        if (!event.isWasDeath()) return;

        // Get the original player object before they died
        ServerPlayer originalPlayer = (ServerPlayer) event.getOriginal();

        // --- CORRECTED LOGIC ---
        // We check the player's saved data instead of active effects, which is more reliable after death.
        boolean hasHalfDurabilityEffect = PlayerChoiceTracker.getChoices(originalPlayer).stream()
                .anyMatch(c -> c.getId().equals("respawn_on_death_half_durability"));

        if (hasHalfDurabilityEffect) {
            for (int i = 0; i < originalPlayer.getInventory().getContainerSize(); i++) {
                ItemStack stack = originalPlayer.getInventory().getItem(i);
                if (stack.isDamageableItem()) {
                    // This calculation correctly halves the REMAINING durability
                    // and handles the edge case of 1 durability left.
                    int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();
                    int damageToAdd = remainingDurability / 2;
                    stack.setDamageValue(stack.getDamageValue() + damageToAdd);
                }
                // Copy the modified item to the new player
                event.getEntity().getInventory().setItem(i, stack);
            }
        }
    }

    @SubscribeEvent
    public void onXpPickup(PlayerXpEvent.PickupXp event) {
        if (hasEffect(event.getEntity(), "unbreakable_anvil_no_xp")) {
            event.setCanceled(true); // Cancel the XP orb pickup
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

                if (!level.isClientSide) {
                    level.playSound(null, creeper.getX(), creeper.getY(), creeper.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);
                    ((ServerLevel)level).sendParticles(ParticleTypes.EXPLOSION_EMITTER, creeper.getX(), creeper.getY(), creeper.getZ(), 1, 0, 0, 0, 0);

                    for (int i = 0; i < 20; i++) {
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
        if (hasEffect(player, "mob_invisibility")) event.setCanceled(true);
        applyHasteDurability(player, player.getMainHandItem());
        if (hasEffect(player, "op_bow_only") && !(player.getMainHandItem().getItem() instanceof BowItem)) event.setCanceled(true);
        if (hasEffect(player, "fire_sword_self_damage")) {
            var enchantRegistry = player.level().holderLookup(Registries.ENCHANTMENT);
            int fireLevel = EnchantmentHelper.getItemEnchantmentLevel(enchantRegistry.getOrThrow(Enchantments.FIRE_ASPECT), player.getMainHandItem());
            if (fireLevel > 0) player.igniteForSeconds(fireLevel * 8);
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        applyHasteDurability(event.getEntity(), event.getItemStack());
    }

    @SubscribeEvent
    public void onSetTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Monster monster) {
            for (Map.Entry<UUID, Set<UUID>> entry : tamedMonsters.entrySet()) {
                UUID ownerUUID = entry.getKey();
                if (entry.getValue().contains(monster.getUUID())) {
                    Player owner = monster.level().getPlayerByUUID(ownerUUID);
                    if (owner != null && monster.getTarget() != null && monster.getTarget().equals(owner)) monster.setTarget(null);
                    else if (owner != null && monster.getTarget() == null && owner.getLastHurtByMob() != null) monster.setTarget(owner.getLastHurtByMob());
                }
            }
            if (event.getNewAboutToBeSetTarget() instanceof Player player) {
                if (hasEffect(player, "mob_invisibility") || hasEffect(player, "hidden_from_mobs_slow_ores") || (hasEffect(player, "night_vision") && !(event.getEntity() instanceof EnderMan))) {
                    event.setNewAboutToBeSetTarget(null);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity entity && !(entity instanceof Player)) {
            entity.level().players().forEach(player -> {
                AttributeInstance health = entity.getAttribute(Attributes.MAX_HEALTH);
                if (health == null) return;

                // Use a more specific modifier ID to prevent conflicts between effects
                ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(WYR.MODID, "health_mod_" + player.getUUID() + "_" + entity.getType().getDescriptionId());
                health.removeModifier(modifierId);

                // This logic now applies 2x health to ALL mobs for this effect
                if (hasEffect(player, "see_mob_hp_double_health")) {
                    health.addPermanentModifier(new AttributeModifier(modifierId, 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }

                // --- THIS IS THE CORRECTED LOGIC ---
                // The check for "instanceof Animal" has been removed, so this now applies 3x health to ALL mobs.
                if (hasEffect(player, "triple_loot_triple_health")) {
                    // An Operation of ADD_MULTIPLIED_TOTAL with a value of 2.0 results in 3x total health (100% base + 200% bonus).
                    health.addPermanentModifier(new AttributeModifier(modifierId, 2.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }

                // Update the entity's health to its new max health
                entity.setHealth(entity.getMaxHealth());
            });
        }
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
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        // In this event, getEntity() is the player.
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack stack = event.getItemStack();
        Entity target = event.getTarget();

        if (hasEffect(player, "instant_tame_creeper_hiss") && target instanceof TamableAnimal animal) {
            // Check if the animal can be tamed with the item and isn't already tamed
            if (animal.isFood(stack) && !animal.isTame()) {
                if (!player.isCreative()) {
                    stack.shrink(1); // Consume the taming item
                }
                animal.tame(player); // Force the tame to succeed
                animal.level().broadcastEntityEvent(animal, (byte)7); // Show heart particles
                event.setCanceled(true); // Cancel the original interaction
                return; // Stop processing since we handled the tame
            }
        }

        // --- Corrected Logic for "wool_stack_random_crosshair" ---
        if (hasEffect(player, "wool_stack_random_crosshair") && target instanceof Sheep sheep && stack.getItem() instanceof ShearsItem) {

            // --- THIS IS THE CORRECTED LINE ---
            // The isShearable() method requires the player, item, level, and position.
            if (sheep.isShearable(player, stack, player.level(), sheep.blockPosition())) {
                // We are handling this interaction manually, so cancel the default behavior
                event.setCanceled(true);

                sheep.level().playSound(null, sheep, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
                sheep.gameEvent(GameEvent.SHEAR, player);
                sheep.setSheared(true); // Manually set the sheep to sheared state

                // Use our new map to get the correct wool color
                Item woolItem = WOOL_BY_COLOR.getOrDefault(sheep.getColor(), Items.WHITE_WOOL);
                sheep.spawnAtLocation(new ItemStack(woolItem, 64));

                // Correctly damage the shears
                if (!player.isCreative()) {
                    EquipmentSlot slot = event.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                    stack.hurtAndBreak(1, player, slot);
                }
                return; // Stop processing since we've handled the shearing
            }
        }

        // --- Logic for "tame_hostiles_no_crafting" ---
        if (hasEffect(player, "tame_hostiles_no_crafting") && stack.is(Items.NETHERITE_BLOCK) && target instanceof Monster monster) {
            Set<UUID> playerTamed = tamedMonsters.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
            if (!playerTamed.contains(monster.getUUID())) {
                playerTamed.add(monster.getUUID());
                monster.setTarget(null);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                player.level().playSound(null, monster.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                event.setCanceled(true);
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
    public void onBonemeal(BonemealEvent event) {
        Player player = event.getPlayer();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();

        if (hasEffect(player, "bonemeal_ores_cobblestone") && isOre(level.getBlockState(pos))) {
            if (level.isClientSide) return;

            event.setCanceled(true);
            if (!player.isCreative()) {
                event.getStack().shrink(1);
            }

            Block oreBlock = level.getBlockState(pos).getBlock();
            List<BlockPos> vein = findVein(level, pos, oreBlock);

            if (player.getRandom().nextFloat() < 0.5) { // 50% chance of failure
                // Failure: Replace the entire vein with cobblestone
                vein.forEach(veinPos -> level.setBlock(veinPos, Blocks.COBBLESTONE.defaultBlockState(), 3));
            } else {
                // Success: Find potential spots to add new ore
                Set<BlockPos> potentialSpots = new HashSet<>();
                for (BlockPos veinPos : vein) {
                    for (Direction dir : Direction.values()) {
                        BlockPos adjacent = veinPos.relative(dir);
                        if (level.isEmptyBlock(adjacent) || level.getBlockState(adjacent).is(BlockTags.BASE_STONE_OVERWORLD)) {
                            potentialSpots.add(adjacent);
                        }
                    }
                }

                List<BlockPos> spotsToPlace = new ArrayList<>(potentialSpots);
                Collections.shuffle(spotsToPlace);

                // Place up to 4 new ore blocks
                for (int i = 0; i < 4 && i < spotsToPlace.size(); i++) {
                    level.setBlock(spotsToPlace.get(i), oreBlock.defaultBlockState(), 3);
                }
            }
            return;
        }

        if (hasEffect(player, "insta_grow_bread_only") && !level.isClientSide) {
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();

            // Logic for Saplings
            if (block instanceof SaplingBlock sapling) {
                event.setCanceled(true); // Stop the default bonemeal action
                if (!player.isCreative()) {
                    event.getStack().shrink(1); // Consume the bonemeal
                }
                // This is the correct, public method to trigger tree growth.
                // It uses the vanilla logic to generate a full tree.
                sapling.performBonemeal((ServerLevel) level, player.getRandom(), pos, state);
                return;
            }

            // Logic for Crops
            if (block instanceof CropBlock crop) {
                event.setCanceled(true);
                // The variable was incorrect. It should be 'crop'.
                while (crop.isValidBonemealTarget(level, pos, level.getBlockState(pos))) {
                    crop.performBonemeal((ServerLevel) level, player.getRandom(), pos, level.getBlockState(pos));
                }
                if (!player.isCreative()) {
                    event.getStack().shrink(1);
                }
                return;
            }
        }
    }

    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        if (event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player) {
            String commandName = event.getParseResults().getContext().getRootNode().getName();

            if (hasEffect(player, "give_decoration_blind") && commandName.equals("give")) {
                // Cancel the original command to bypass the permission check
                event.setCanceled(true);

                try {
                    // Manually parse the arguments the player provided
                    CommandContext<CommandSourceStack> context = event.getParseResults().getContext().build(event.getParseResults().getReader().getString());
                    ItemInput itemInput = context.getArgument("item", ItemInput.class);
                    int count = 1;
                    try {
                        count = context.getArgument("count", Integer.class);
                    } catch (IllegalArgumentException e) {
                        // Count is optional, defaults to 1
                    }

                    ItemStack stackToGive = itemInput.createItemStack(count, false);

                    // Check if the item is in our custom tag
                    if (stackToGive.is(ModTags.Items.DECORATION_ITEMS)) {
                        // Success: Give the item and send a success message
                        player.getInventory().add(stackToGive.copy());
                        player.sendSystemMessage(Component.literal("Gave " + stackToGive.getCount() + " [" + stackToGive.getHoverName().getString() + "] to " + player.getName().getString()));
                    } else {
                        // Failure: Item is not a decoration, send an error message
                        player.sendSystemMessage(Component.literal("You can only /give yourself decoration items."));
                    }
                } catch (Exception e) {
                    // The command was likely malformed, send a vanilla-like error
                    player.sendSystemMessage(Component.literal("Invalid command syntax."));
                }
            }

            if (hasEffect(player, "weather_control_creeper") && commandName.equals("weather")) {
                Creeper creeper = EntityType.CREEPER.create(player.level());
                if (creeper != null) {
                    creeper.setPos(player.position());
                    creeper.ignite();
                    player.level().addFreshEntity(creeper);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof Player player && hasEffect(player, "one_hit_dragon_no_end")) {
            if (event.getTo() == Level.END) {
                // This will teleport the player back to their spawn point in the overworld.
                ServerPlayer serverPlayer = (ServerPlayer) player;
                BlockPos respawnPos = serverPlayer.getRespawnPosition();
                ServerLevel overworld = serverPlayer.server.overworld();
                if (respawnPos != null) {
                    serverPlayer.teleportTo(overworld, respawnPos.getX(), respawnPos.getY(), respawnPos.getZ(), serverPlayer.getRespawnAngle(), 0);
                } else {
                    BlockPos spawn = overworld.getSharedSpawnPos();
                    serverPlayer.teleportTo(overworld, spawn.getX(), spawn.getY(), spawn.getZ(), 0, 0);
                }
            }
        }
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
            if (player instanceof ServerPlayer serverPlayer) serverPlayer.teleportTo(targetPos.x, targetPos.y, targetPos.z);
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
            if (!health.hasModifier(GOLEM_HEALTH_MODIFIER_ID)) health.addPermanentModifier(new AttributeModifier(GOLEM_HEALTH_MODIFIER_ID, 80.0, AttributeModifier.Operation.ADD_VALUE));
            if (!strength.hasModifier(GOLEM_STRENGTH_MODIFIER_ID)) strength.addPermanentModifier(new AttributeModifier(GOLEM_STRENGTH_MODIFIER_ID, 14.0, AttributeModifier.Operation.ADD_VALUE));
            if (!speed.hasModifier(GOLEM_SPEED_MODIFIER_ID)) speed.addPermanentModifier(new AttributeModifier(GOLEM_SPEED_MODIFIER_ID, -0.7, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else {
            if (health.hasModifier(GOLEM_HEALTH_MODIFIER_ID)) {
                health.removeModifier(GOLEM_HEALTH_MODIFIER_ID);
                if(player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
            }
            if (strength.hasModifier(GOLEM_STRENGTH_MODIFIER_ID)) strength.removeModifier(GOLEM_STRENGTH_MODIFIER_ID);
            if (speed.hasModifier(GOLEM_SPEED_MODIFIER_ID)) speed.removeModifier(GOLEM_SPEED_MODIFIER_ID);
        }
    }
}