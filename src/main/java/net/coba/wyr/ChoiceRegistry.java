package net.coba.wyr;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;


import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

public class ChoiceRegistry {

    private static final List<Choice> CHOICES = new ArrayList<>();
    private static final Random RANDOM = new Random();
    private static final Map<String, String> CONFLICTING_CHOICES = new HashMap<>();
    private static List<String> ALL_POSSIBLE_PAIR_KEYS_CACHE = null;

    static {
        CHOICES.add(new Choice("creative_flight", "You can fly like in Creative Mode", "A random item from your inventory gets deleted every 10s you are in flight"));
        CHOICES.add(new Choice("speed_iv", "You have permanent Speed IV", "Your hunger depletes 3x times as fast"));
        CHOICES.add(new Choice("insta_heal", "You regenerate health instantly anytime you heal", "Your maximum health is only three hearts"));
        CHOICES.add(new Choice("fire_immunity", "You are immune to fire and lava", "You take double damage from all melee attacks"));
        CHOICES.add(new Choice("water_breathing", "You can breathe underwater indefinitely", "You can't wear a helmet"));
        CHOICES.add(new Choice("haste_ii", "You have permanent Haste II", "Your tools lose durability twice as fast"));
        CHOICES.add(new Choice("high_jump", "You can jump 5 blocks high", "You take fall damage from any jump higher than 2 blocks"));
        CHOICES.add(new Choice("mob_invisibility", "You are completely invisible to allhostile mobs", "You cannot deal any damage to them"));
        CHOICES.add(new Choice("strength_ii", "You have a permanent Strength II effect", "You cannot use a shield."));
        CHOICES.add(new Choice("night_vision", "You can see perfectly in the dark", "Endermen are always hostile towards you"));
        CHOICES.add(new Choice("ender_teleport", "You can teleport like an Enderman (15 block range, 5s cooldown)", "You take two hearts of damage each time you do"));
        CHOICES.add(new Choice("extra_hearts", "You have a second row of hearts", "You cannot wear chestplates"));
        CHOICES.add(new Choice("water_walk", "You can walk on water and lava", "You are permanently under the Slowness II effect"));
        CHOICES.add(new Choice("dolphin_grace", "You have a permanent Dolphin's Grace effect", "You can't leave the water without taking damage"));
        CHOICES.add(new Choice("wall_climb", "You can climb walls like a spider", "You take double damage from falls"));
        CHOICES.add(new Choice("phasing", "You can phase through 1 block thick walls", "You cannot open doors"));
        CHOICES.add(new Choice("triple_damage", "Your attacks have a 25% chance of dealing triple damage", "You cannot wear boots"));
        CHOICES.add(new Choice("resistance_ii", "You have permanent Resistance II", "You cannot sprint"));
        CHOICES.add(new Choice("fishing_luck", "When fishing, there's a 1% chance to get treasure", "Every 5th catch spawns a hostile Guardian"));
        CHOICES.add(new Choice("daily_diamond", "You get one diamond block every Minecraft day", "You get a thirst meter"));
        CHOICES.add(new Choice("triple_ores", "Every ore you mine drops 3x the resources", "You cannot enchant anything"));
        CHOICES.add(new Choice("sand_treasure", "All sand you break has a 0.1% chance to drop a treasure items", "You can no longer smelt anything"));
        CHOICES.add(new Choice("stacked_logs", "Every log you break drops a full stack of logs", "Every axe you find or craft has a durability of 10"));
        CHOICES.add(new Choice("mob_loot_inventory_slots", "You get 5x the drops from every mob", "Your inventory only has 9 slots"));
        CHOICES.add(new Choice("unbreakable_tools_netherite", "You can combine 16 netherite blocks with a tool in an anvil to make it unbreakable", "It costs 16 netherite blocks"));
        CHOICES.add(new Choice("nether_water_fast_lava", "You can place water in the Nether", "You take double damage from lava"));
        CHOICES.add(new Choice("weather_control_creeper", "You can control the weather with the weather command", "Every time you change it, It spawns a creeper that instantly explodes"));
        CHOICES.add(new Choice("insta_grow_bread_only", "You can grow trees instantly with bone meal", "You can only eat bread"));
        CHOICES.add(new Choice("strong_wolves_one_hp", "All your tamed wolves are 4x stronger and 2x faster", "They have only 1hp"));
        CHOICES.add(new Choice("op_bow_only", "You spawn a bow the has permanent Infinity, mending, power 10, looting 10, and knockback 10", "You can only use a bows to attack"));
        CHOICES.add(new Choice("mace_max_damage_weak", "Your mace deals maximum smash damage", "It has the durability of a 5 and cannot be enchanted"));
        CHOICES.add(new Choice("one_hit_dragon_no_end", "You can kill the Ender Dragon in one hit", "You cannot enter the end dimension ever again"));
        CHOICES.add(new Choice("fire_sword_self_damage", "You spawn a diamond sword that has Fire Aspect X", "You apply fire damage to yourself"));
        CHOICES.add(new Choice("lava_fishing_rod", "You get a Luck of the Sea V and lure V fishing rod", "You can only fish in lava"));
        CHOICES.add(new Choice("choose_spawn_biome_lock", "You can choose which biome you spawn in", "You cannot leave that biome for 10 Minecraft days"));
        CHOICES.add(new Choice("bonemeal_ores_cobblestone", "You can use bonemeal on ore veins to make them 3x larger", "It has a 50% chance to turn the entire vein into cobblestone"));
        CHOICES.add(new Choice("golem_suit_slow", "You get a golem suit that gives you the health and strength of an Iron Golem", "You move the same speed as an Iron golem"));
        CHOICES.add(new Choice("see_mob_hp_double_health", "You can see the hp of every hositle mob", "They have 2x health"));
        CHOICES.add(new Choice("enchant_level_x_delete_item", "You can put enchantments up to level X", "A random item in your inventory will be deleted every time you enchant above max lvl"));
        CHOICES.add(new Choice("passive_xp_no_mending", "You get 7xp second", "You can't apply Mending to anything"));
        CHOICES.add(new Choice("fast_horse_dark_fear", "You spawn a tamed horse that is as fast as a speed IV potion", "It is afraid of the dark"));
        CHOICES.add(new Choice("unbreakable_elytra_no_boost", "You get an elytra that is unbreakable", "You can't use fireworks to boost"));
        CHOICES.add(new Choice("fast_smelt_damage", "You can smelt any item in 1s with a furnace, smoker, or blast furnace", "You will lose half hp every time the items are done smelting"));
        CHOICES.add(new Choice("daily_totem_no_armor", "You get a Totem of Undying in your off-hand every morning", "You cannot wear any armor"));
        CHOICES.add(new Choice("villager_talk_no_fence_gates", "You can understand what Villagers are saying", "You cannot use fence gates"));
        CHOICES.add(new Choice("give_decoration_blind", "You can use /wyrgive to get any decoration blocks or decoration items", "You get blinded by sunlight"));
        CHOICES.add(new Choice("personal_teleporter_scramble", "You get a pocket teleporter to return to a set point", "It scrambles your inventory each time"));
        CHOICES.add(new Choice("luck_ores_one_hp", "All ores drop 5x", "It only works when you are at 1 hp or less"));
        CHOICES.add(new Choice("eat_anything_hunger_chance", "You can eat any item as food", "Non-food items have a 10% chance of giving you the hunger I effect for 30s"));
        CHOICES.add(new Choice("wind_charge_respawn_mob", "You get a Wind Charge every 30s seconds", "Every hostile mob you kill will respawn instantly"));
        CHOICES.add(new Choice("sneak_invisible_no_move", "You can turn invisible while sneaking", "You can't move"));
        CHOICES.add(new Choice("speed_v_random_keys", "You get permanent speed V", "Your movement keys are changed every 10min"));
        CHOICES.add(new Choice("hero_of_village_random_sounds", "You get permanent hero of the village", "Every sound is randomized"));
        CHOICES.add(new Choice("backpack_no_shulkers", "You get a backpack with 54 slots", "You can no longer use shulker boxes"));
        CHOICES.add(new Choice("build_limit_638_top_bottom_only", "You can build to height limit of 638", "You can only place on the top and bottom of blocks"));
        CHOICES.add(new Choice("keep_inventory_berries_only", "You keep your inventory on death", "You can only eat sweet berries."));
        CHOICES.add(new Choice("midas_touch_no_hp_bar", "You get midas touch", "You can no longer see your hp"));
        CHOICES.add(new Choice("cheap_trades_half_hunger", "Get offered cheap trades from villager", "Your hunger bar is cut in half"));
        CHOICES.add(new Choice("no_hunger_random_shader", "You no longer need to eat", "You apply a random shader every 10s"));
        CHOICES.add(new Choice("auto_smelt_random_sound", "Every ore gets smelted", "a random sound plays every so often"));
        CHOICES.add(new Choice("random_food_no_jump", "You get random food every 30s", "You cannot jump"));
        CHOICES.add(new Choice("sneak_bonemeal_fov_change", "You can bonemeal when you sneak", "Your fov is determined by your selected hot bar slot"));
        CHOICES.add(new Choice("no_fall_damage_low_render_distance", "You no longer take fall damage", "Your render distance is changed to 5 chunks"));
        CHOICES.add(new Choice("stack_64_world_height", "Every item in your inventory get set to stack of 64", "Teleport to world height"));
        CHOICES.add(new Choice("instant_tame_creeper_hiss", "You tame any animal on the first try", "You hear the creeper hissing sound randomly forever"));
        CHOICES.add(new Choice("fast_swim_muted", "You get dolphines grace II", "Your games sound is muted"));
        CHOICES.add(new Choice("wool_stack_random_crosshair", "You get a full stack of wool every time you shear a sheep", "Your crosshair is randomly set every 1min"));
        CHOICES.add(new Choice("infinite_sprint_no_elytra", "Be able to run infinitely without running out of hunger", "Never be able to fly"));
        CHOICES.add(new Choice("unlimited_ice_cream_brain_freeze", "Have unlimited ice cream", "It gives you brain freeze every time you eat it"));
        CHOICES.add(new Choice("fast_climb_short", "You can climb 2x faster", "You become short"));
        CHOICES.add(new Choice("unbreakable_tools_cobblestone_only", "Have unbreakable tools", "Only ever get cobblestone"));
        CHOICES.add(new Choice("hidden_from_mobs_slow_ores", "Be completely hidden from hostile mobs", "Ores take twice as long to mine"));
        CHOICES.add(new Choice("half_health_mobs_no_drops", "Have hostile mobs with half health", "They drop nothing"));
        CHOICES.add(new Choice("triple_loot_triple_health", "Have mobs drop 3x loot", "They have 3x health"));
        CHOICES.add(new Choice("triple_drops_slow_break", "Every block you mine drops 3x", "It takes 3x as long to break"));
        CHOICES.add(new Choice("no_fall_damage_no_jump", "Take no fall damage", "You cannot jump"));
        CHOICES.add(new Choice("sleep_heal_random_drops", "Sleeping in a bed heals you to full health and hunger", "Every block you break is randomized"));
        CHOICES.add(new Choice("respawn_on_death_half_durability", "You respawn where you died", "All your items loose half durability"));
        CHOICES.add(new Choice("infinite_xp_no_anvils", "You get infinite xp", "You can't use anvils or enchanting table"));
        CHOICES.add(new Choice("unbreakable_anvil_no_xp", "You get an anvil that never breaks when you use it", "You cannot gain any xp"));
        CHOICES.add(new Choice("creeper_cake_no_fence_gates", "Creepers explode into a flurry of cakes", "You cannot use fence gates"));
        CHOICES.add(new Choice("cheap_trades_no_ore_drops", "Get offered very cheap trades from villager", "Any ore you mine drops nothing"));
        CHOICES.add(new Choice("random_good_bad_potions", "Every time you get a random good potion effect", "You also get a random bad effect"));
        CHOICES.add(new Choice("no_phantoms_no_beds", "Phantoms never spawn", "You can't use beds to sleep or set your spawn point"));
        CHOICES.add(new Choice("slow_fall_side_place_only", "Have permanent slow fall effect", "You can only place on sides of blocks"));
        CHOICES.add(new Choice("emeralds_from_mobs_siren", "Hostile mobs always drop 2 emeralds", "???"));
        CHOICES.add(new Choice("double_debris_vampire", "Ancient debris always drops 2", "You are a vampire"));
        CHOICES.add(new Choice("fast_day_slow_night", "You move 4x as fast during the day", "You move very slowly during the night"));
        CHOICES.add(new Choice("fast_night_slow_day", "You move 4x as fast during the night", "You move slowly during the day"));
        CHOICES.add(new Choice("double_food_no_hunger_bar", "Every food you eat gives you 2x hunger and saturation", "You cannot see your hunger bar"));
        CHOICES.add(new Choice("texture_scramble", "You get unlimited soda", "All block textures in the world are randomized"));
        CHOICES.add(new Choice("auto_repair_no_enchant", "Held tools and weapons automatically repair themselves over time", "You can no longer enchant any items"));
        CHOICES.add(new Choice("item_texture_scramble_melon_only", "ll item textures are randomized", "You can only eat melons"));
        //CHOICES.add(new Choice("Highlight_ores_no_ladders", "You can see ores through walls", "You cannot climb ladders"));
        //CHOICES.add(new Choice("tame_hostiles_no_crafting", "You can tame any hostile mob with a block of netherite that will follow you and fight for you", "You cannot use crafting table"));
        //CHOICES.add(new Choice("no_durability_loss_adventure_mode", "No longer lose durability on your items", "You are set to adventure mod every time it is night"));
        //CHOICES.add(new Choice("item_magnet_no_stack", "Dropped items and XP orbs are pulled towards you from a 10-block radius", "You can only stack items to 1"));
        //CHOICES.add(new Choice("perfect_enchants_destroy_chance", "Enchanting tables always offer three perfect, max-level enchantments", "There's a 50% chance the item is destroyed in the process"));
        //CHOICES.add(new Choice("no_hunger_no_regen", "Your hunger bar never decreases", "Your health never regenerates naturally"));
        //CHOICES.add(new Choice("lava_swim_water_damage", "Every lava works like water", "Every water works like lava"));

        // --- Audio Conflicts (Muted vs Sound-Based Choices) ---
        // 'fast_swim_muted' mutes the game, making these choices useless
        CONFLICTING_CHOICES.put("fast_swim_muted", "instant_tame_creeper_hiss");
        CONFLICTING_CHOICES.put("instant_tame_creeper_hiss", "fast_swim_muted");

        CONFLICTING_CHOICES.put("fast_swim_muted", "hero_of_village_random_sounds");
        CONFLICTING_CHOICES.put("hero_of_village_random_sounds", "fast_swim_muted");

        CONFLICTING_CHOICES.put("fast_swim_muted", "auto_smelt_random_sound");
        CONFLICTING_CHOICES.put("auto_smelt_random_sound", "fast_swim_muted");

        // --- Creative Flight vs Movement Redundancies ---`
        // If you can fly, these movement buffs/debuffs are irrelevant
        CONFLICTING_CHOICES.put("creative_flight", "wall_climb");
        CONFLICTING_CHOICES.put("wall_climb", "creative_flight");

        CONFLICTING_CHOICES.put("creative_flight", "high_jump");
        CONFLICTING_CHOICES.put("high_jump", "creative_flight");

        CONFLICTING_CHOICES.put("creative_flight", "infinite_sprint_no_elytra");
        CONFLICTING_CHOICES.put("infinite_sprint_no_elytra", "creative_flight");

        // --- Fall Damage Exploits ---
        // These choices negate the specific "Take extra fall damage" penalty of others
        CONFLICTING_CHOICES.put("no_fall_damage_low_render_distance", "wall_climb");
        CONFLICTING_CHOICES.put("wall_climb", "no_fall_damage_low_render_distance");
        CONFLICTING_CHOICES.put("no_fall_damage_no_jump", "wall_climb");
        CONFLICTING_CHOICES.put("wall_climb", "no_fall_damage_no_jump");

        CONFLICTING_CHOICES.put("no_fall_damage_low_render_distance", "high_jump");
        CONFLICTING_CHOICES.put("high_jump", "no_fall_damage_low_render_distance");
        CONFLICTING_CHOICES.put("no_fall_damage_no_jump", "high_jump");
        CONFLICTING_CHOICES.put("high_jump", "no_fall_damage_no_jump");

        // --- Hunger/Food Logic ---
        // If you don't need to eat, these penalties/items are irrelevant
        CONFLICTING_CHOICES.put("no_hunger_random_shader", "cheap_trades_half_hunger");
        CONFLICTING_CHOICES.put("cheap_trades_half_hunger", "no_hunger_random_shader");

        CONFLICTING_CHOICES.put("no_hunger_random_shader", "unlimited_ice_cream_brain_freeze");
        CONFLICTING_CHOICES.put("unlimited_ice_cream_brain_freeze", "no_hunger_random_shader");

        // --- Golem Suit (Armor Slot Locking) ---
        // Golem suit forces armor on, conflicting with "No [Slot] Armor" penalties
        CONFLICTING_CHOICES.put("golem_suit_slow", "water_breathing");
        CONFLICTING_CHOICES.put("water_breathing", "golem_suit_slow");

        CONFLICTING_CHOICES.put("golem_suit_slow", "extra_hearts");
        CONFLICTING_CHOICES.put("extra_hearts", "golem_suit_slow");

        CONFLICTING_CHOICES.put("golem_suit_slow", "triple_damage");
        CONFLICTING_CHOICES.put("triple_damage", "golem_suit_slow");

        CONFLICTING_CHOICES.put("golem_suit_slow", "daily_totem_no_armor");
        CONFLICTING_CHOICES.put("daily_totem_no_armor", "golem_suit_slow");

        // --- Impossible Actions ---
        // 'no_phantoms_no_beds' disables beds, making 'sleep_heal' impossible
        CONFLICTING_CHOICES.put("no_phantoms_no_beds", "sleep_heal_random_drops");
        CONFLICTING_CHOICES.put("sleep_heal_random_drops", "no_phantoms_no_beds");

        // --- Biome Lock Logic ---
        // Teleporter might conflict with being locked in a specific biome
        CONFLICTING_CHOICES.put("choose_spawn_biome_lock", "personal_teleporter_scramble");
        CONFLICTING_CHOICES.put("personal_teleporter_scramble", "choose_spawn_biome_lock");

        // --- Building Restrictions ---
        // If you have both, you can't use doors OR fence gates (very annoying for bases)
        CONFLICTING_CHOICES.put("phasing", "villager_talk_no_fence_gates");
        CONFLICTING_CHOICES.put("villager_talk_no_fence_gates", "phasing");


        //Trading
        CONFLICTING_CHOICES.put("cheap_trades_sink_boat", "cheap_trades_no_ore_drops");
        CONFLICTING_CHOICES.put("cheap_trades_no_ore_drops", "cheap_trades_sink_boat");
        CONFLICTING_CHOICES.put("hero_of_village_random_sounds", "cheap_trades_sink_boat");
        CONFLICTING_CHOICES.put("cheap_trades_sink_boat", "hero_of_village_random_sounds");
        CONFLICTING_CHOICES.put("hero_of_village_random_sounds", "cheap_trades_no_ore_drops");
        CONFLICTING_CHOICES.put("cheap_trades_no_ore_drops", "hero_of_village_random_sounds");

        //Smelting
        CONFLICTING_CHOICES.put("fast_smelt_damage", "sand_treasure");
        CONFLICTING_CHOICES.put("sand_treasure", "fast_smelt_damage");
        CONFLICTING_CHOICES.put("nether_water_fast_lava", "sand_treasure");
        CONFLICTING_CHOICES.put("sand_treasure", "nether_water_fast_lava");

        //Resource & Mining Conflicts
        CONFLICTING_CHOICES.put("luck_ores_one_hp", "unbreakable_tools_cobblestone_only");
        CONFLICTING_CHOICES.put("unbreakable_tools_cobblestone_only", "luck_ores_one_hp");
        CONFLICTING_CHOICES.put("luck_ores_one_hp", "cheap_trades_no_ore_drops");
        CONFLICTING_CHOICES.put("cheap_trades_no_ore_drops", "luck_ores_one_hp");
        CONFLICTING_CHOICES.put("triple_ores", "unbreakable_tools_cobblestone_only");
        CONFLICTING_CHOICES.put("unbreakable_tools_cobblestone_only", "triple_ores");
        CONFLICTING_CHOICES.put("triple_ores", "cheap_trades_no_ore_drops");
        CONFLICTING_CHOICES.put("cheap_trades_no_ore_drops", "triple_ores");
        CONFLICTING_CHOICES.put("bonemeal_ores_cobblestone", "unbreakable_tools_cobblestone_only");
        CONFLICTING_CHOICES.put("unbreakable_tools_cobblestone_only", "bonemeal_ores_cobblestone");
        CONFLICTING_CHOICES.put("bonemeal_ores_cobblestone", "cheap_trades_no_ore_drops");
        CONFLICTING_CHOICES.put("cheap_trades_no_ore_drops", "bonemeal_ores_cobblestone");
        CONFLICTING_CHOICES.put("double_debris_vampire", "unbreakable_tools_cobblestone_only");
        CONFLICTING_CHOICES.put("unbreakable_tools_cobblestone_only", "double_debris_vampire");
        CONFLICTING_CHOICES.put("double_debris_vampire", "cheap_trades_no_ore_drops");
        CONFLICTING_CHOICES.put("cheap_trades_no_ore_drops", "double_debris_vampire");
        CONFLICTING_CHOICES.put("sleep_heal_random_drops", "unbreakable_tools_cobblestone_only");
        CONFLICTING_CHOICES.put("unbreakable_tools_cobblestone_only", "sleep_heal_random_drops");
        CONFLICTING_CHOICES.put("sand_treasure", "unbreakable_tools_cobblestone_only");
        CONFLICTING_CHOICES.put("unbreakable_tools_cobblestone_only", "sand_treasure");
        CONFLICTING_CHOICES.put("sand_treasure", "sleep_heal_random_drops");
        CONFLICTING_CHOICES.put("sleep_heal_random_drops", "sand_treasure");
        CONFLICTING_CHOICES.put("sand_treasure", "auto_smelt");
        CONFLICTING_CHOICES.put("auto_smelt", "sand_treasure");
        CONFLICTING_CHOICES.put("tame_hostiles_no_crafting", "unbreakable_tools_netherite");
        CONFLICTING_CHOICES.put("unbreakable_tools_netherite", "tame_hostiles_no_crafting");

        //Combat & Mob Conflicts
        CONFLICTING_CHOICES.put("mob_invisibility", "mob_loot_inventory_slots");
        CONFLICTING_CHOICES.put("mob_loot_inventory_slots", "mob_invisibility");
        CONFLICTING_CHOICES.put("mob_invisibility", "triple_loot_triple_health");
        CONFLICTING_CHOICES.put("triple_loot_triple_health", "mob_invisibility");
        CONFLICTING_CHOICES.put("mob_invisibility", "half_health_mobs_no_drops");
        CONFLICTING_CHOICES.put("half_health_mobs_no_drops", "mob_invisibility");
        CONFLICTING_CHOICES.put("mob_invisibility", "emeralds_from_mobs_siren");
        CONFLICTING_CHOICES.put("emeralds_from_mobs_siren", "mob_invisibility");
        CONFLICTING_CHOICES.put("mob_invisibility", "wind_charge_respawn_mob");
        CONFLICTING_CHOICES.put("wind_charge_respawn_mob", "mob_invisibility");
        CONFLICTING_CHOICES.put("strength_ii", "op_bow_only");
        CONFLICTING_CHOICES.put("op_bow_only", "strength_ii");
        CONFLICTING_CHOICES.put("triple_loot_triple_health", "see_mob_hp_double_health");
        CONFLICTING_CHOICES.put("see_mob_hp_double_health", "triple_loot_triple_health");
        CONFLICTING_CHOICES.put("see_mob_hp_double_health", "half_health_mobs_no_drops");
        CONFLICTING_CHOICES.put("half_health_mobs_no_drops", "see_mob_hp_double_health");

        //Movement & Player Stat Conflicts
        CONFLICTING_CHOICES.put("resistance_ii", "infinite_sprint_no_elytra");
        CONFLICTING_CHOICES.put("infinite_sprint_no_elytra", "resistance_ii");
        CONFLICTING_CHOICES.put("no_fall_damage_sneak_speed", "speed_iv");
        CONFLICTING_CHOICES.put("speed_iv", "no_fall_damage_sneak_speed");
        CONFLICTING_CHOICES.put("no_fall_damage_sneak_speed", "speed_v_random_keys");
        CONFLICTING_CHOICES.put("speed_v_random_keys", "no_fall_damage_sneak_speed");
        CONFLICTING_CHOICES.put("no_fall_damage_sneak_speed", "fast_day_slow_night");
        CONFLICTING_CHOICES.put("fast_day_slow_night", "no_fall_damage_sneak_speed");
        CONFLICTING_CHOICES.put("no_fall_damage_sneak_speed", "fast_night_slow_day");
        CONFLICTING_CHOICES.put("fast_night_slow_day", "no_fall_damage_sneak_speed");
        CONFLICTING_CHOICES.put("water_walk", "speed_iv");
        CONFLICTING_CHOICES.put("speed_iv", "water_walk");
        CONFLICTING_CHOICES.put("water_walk", "speed_v_random_keys");
        CONFLICTING_CHOICES.put("speed_v_random_keys", "water_walk");
        CONFLICTING_CHOICES.put("water_walk", "fast_day_slow_night");
        CONFLICTING_CHOICES.put("fast_day_slow_night", "water_walk");
        CONFLICTING_CHOICES.put("water_walk", "fast_night_slow_day");
        CONFLICTING_CHOICES.put("fast_night_slow_day", "water_walk");
        CONFLICTING_CHOICES.put("speed_v_random_keys", "fast_day_slow_night");
        CONFLICTING_CHOICES.put("fast_day_slow_night", "speed_v_random_keys");
        CONFLICTING_CHOICES.put("speed_v_random_keys", "fast_night_slow_day");
        CONFLICTING_CHOICES.put("fast_night_slow_day", "speed_v_random_keys");

        //Armor & Equipment Conflicts
        CONFLICTING_CHOICES.put("daily_totem_no_armor", "water_breathing");
        CONFLICTING_CHOICES.put("water_breathing", "daily_totem_no_armor");
        CONFLICTING_CHOICES.put("extra_hearts", "daily_totem_no_armor");
        CONFLICTING_CHOICES.put("daily_totem_no_armor", "extra_hearts");
        CONFLICTING_CHOICES.put("insta_heal", "extra_hearts");
        CONFLICTING_CHOICES.put("extra_hearts", "insta_heal");
        CONFLICTING_CHOICES.put("daily_totem_no_armor", "triple_damage");
        CONFLICTING_CHOICES.put("triple_damage", "daily_totem_no_armor");
        CONFLICTING_CHOICES.put("unbreakable_elytra_no_boost", "daily_totem_no_armor");
        CONFLICTING_CHOICES.put("daily_totem_no_armor", "unbreakable_elytra_no_boost");
        CONFLICTING_CHOICES.put("unbreakable_elytra_no_boost", "water_breathing");
        CONFLICTING_CHOICES.put("water_breathing", "unbreakable_elytra_no_boost");
        CONFLICTING_CHOICES.put("unbreakable_elytra_no_boost", "extra_hearts");
        CONFLICTING_CHOICES.put("extra_hearts", "unbreakable_elytra_no_boost");
        CONFLICTING_CHOICES.put("unbreakable_elytra_no_boost", "triple_damage");
        CONFLICTING_CHOICES.put("triple_damage", "unbreakable_elytra_no_boost");

        //Hunger Conflicts
        CONFLICTING_CHOICES.put("insta_grow_bread_only", "item_texture_scramble_melon_only");
        CONFLICTING_CHOICES.put("item_texture_scramble_melon_only", "insta_grow_bread_only");
        CONFLICTING_CHOICES.put("insta_grow_bread_only", "keep_inventory_berries_only");
        CONFLICTING_CHOICES.put("keep_inventory_berries_only", "insta_grow_bread_only");
        CONFLICTING_CHOICES.put("item_texture_scramble_melon_only", "keep_inventory_berries_only");
        CONFLICTING_CHOICES.put("keep_inventory_berries_only", "item_texture_scramble_melon_only");
        CONFLICTING_CHOICES.put("no_hunger_random_shader", "speed_iv");
        CONFLICTING_CHOICES.put("speed_iv", "no_hunger_random_shader");
        CONFLICTING_CHOICES.put("no_hunger_random_shader", "keep_inventory_berries_only");
        CONFLICTING_CHOICES.put("keep_inventory_berries_only", "no_hunger_random_shader");
        CONFLICTING_CHOICES.put("no_hunger_random_shader", "insta_grow_bread_only");
        CONFLICTING_CHOICES.put("insta_grow_bread_only", "no_hunger_random_shader");
        CONFLICTING_CHOICES.put("no_hunger_random_shader", "item_texture_scramble_melon_only");
        CONFLICTING_CHOICES.put("item_texture_scramble_melon_only", "no_hunger_random_shader");
        CONFLICTING_CHOICES.put("no_hunger_random_shader", "random_food_no_jump");
        CONFLICTING_CHOICES.put("random_food_no_jump", "no_hunger_random_shader");
        CONFLICTING_CHOICES.put("no_hunger_random_shader", "double_food_no_hunger_bar");
        CONFLICTING_CHOICES.put("double_food_no_hunger_bar", "no_hunger_random_shader");
        CONFLICTING_CHOICES.put("no_hunger_random_shader", "eat_anything_hunger_chance");
        CONFLICTING_CHOICES.put("eat_anything_hunger_chance", "no_hunger_random_shader");
        CONFLICTING_CHOICES.put("item_texture_scramble_melon_only", "random_food_no_jump");
        CONFLICTING_CHOICES.put("random_food_no_jump", "item_texture_scramble_melon_only");
        CONFLICTING_CHOICES.put("insta_grow_bread_only", "random_food_no_jump");
        CONFLICTING_CHOICES.put("random_food_no_jump", "insta_grow_bread_only");
        CONFLICTING_CHOICES.put("keep_inventory_berries_only", "random_food_no_jump");
        CONFLICTING_CHOICES.put("random_food_no_jump", "keep_inventory_berries_only");

        //XP & Enchanting Conflicts
        CONFLICTING_CHOICES.put("unbreakable_anvil_no_xp", "infinite_xp_no_anvils");
        CONFLICTING_CHOICES.put("infinite_xp_no_anvils", "unbreakable_anvil_no_xp");
        CONFLICTING_CHOICES.put("unbreakable_anvil_no_xp", "passive_xp_no_mending");
        CONFLICTING_CHOICES.put("passive_xp_no_mending", "unbreakable_anvil_no_xp");

        //Random
        CONFLICTING_CHOICES.put("creeper_cake_no_fence_gates", "villager_talk_no_fence_gates");
        CONFLICTING_CHOICES.put("villager_talk_no_fence_gates", "creeper_cake_no_fence_gates");
    }

    private static List<String> getAllPossiblePairKeys() {
        if (ALL_POSSIBLE_PAIR_KEYS_CACHE == null) {
            List<String> keys = new ArrayList<>();
            for (int i = 0; i < CHOICES.size(); i++) {
                for (int j = i + 1; j < CHOICES.size(); j++) {
                    keys.add(getPairKey(CHOICES.get(i), CHOICES.get(j)));
                }
            }
            ALL_POSSIBLE_PAIR_KEYS_CACHE = Collections.unmodifiableList(keys);
        }
        return ALL_POSSIBLE_PAIR_KEYS_CACHE;
    }

    private static String getPairKey(Choice c1, Choice c2) {
        return getPairKey(c1.getId(), c2.getId());
    }

    private static String getPairKey(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? id1 + ";" + id2 : id2 + ";" + id1;
    }

    public static List<Choice> getFilteredRandomChoicesForPlayer(ServerPlayer player) {
        WyrSavedData savedData = WyrSavedData.get((ServerLevel) player.level());
        Set<String> shownChoicePairs = savedData.getShownChoicePairs();

        Set<String> activeChoiceIds = PlayerChoiceTracker.getChoices(player)
                .stream()
                .map(Choice::getId)
                .collect(Collectors.toSet());

        Set<String> forbiddenIds = new HashSet<>();
        for (String activeId : activeChoiceIds) {
            forbiddenIds.add(activeId);
            if (CONFLICTING_CHOICES.containsKey(activeId)) {
                forbiddenIds.add(CONFLICTING_CHOICES.get(activeId));
            }
        }

        List<String> availableKeys = new ArrayList<>(getAllPossiblePairKeys());
        availableKeys.removeAll(shownChoicePairs);

        availableKeys.removeIf(key -> {
            String[] ids = key.split(";");
            return forbiddenIds.contains(ids[0]) || forbiddenIds.contains(ids[1]);
        });

        if (availableKeys.isEmpty()) {
            return Collections.emptyList();
        }

        String randomKey = availableKeys.get(RANDOM.nextInt(availableKeys.size()));
        shownChoicePairs.add(randomKey);
        savedData.setDirty(); // Mark data to be saved to disk

        Choice[] choices = getChoicesFromKey(randomKey);
        return Arrays.stream(choices).filter(Objects::nonNull).toList();
    }

    public static boolean conflictsWith(String choiceId, Set<String> activeChoiceIds) {
        if (activeChoiceIds.contains(choiceId)) {
            return true;
        }
        if (!CONFLICTING_CHOICES.containsKey(choiceId)) {
            return false;
        }
        String conflictingId = CONFLICTING_CHOICES.get(choiceId);
        return activeChoiceIds.contains(conflictingId);
    }

    public static Choice getChoiceById(String id) {
        return CHOICES.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    public static Choice[] getChoicesFromKey(String key) {
        String[] ids = key.split(";");
        if (ids.length != 2) return new Choice[]{null, null};
        Choice choice1 = getChoiceById(ids[0]);
        Choice choice2 = getChoiceById(ids[1]);
        if (choice1 == null || choice2 == null) {
            // If either choice is null, return empty array to prevent crashes
            return new Choice[0];
        }
        return new Choice[]{choice1, choice2};
    }

    public static List<Choice> getAllChoices() {
        return Collections.unmodifiableList(CHOICES);
    }
}