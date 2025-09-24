package net.coba.wyr;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;


import java.util.*;
import java.util.stream.Collectors;

public class ChoiceRegistry {

    private static final List<Choice> CHOICES = new ArrayList<>();
    private static final Random RANDOM = new Random();
    private static final Map<String, String> CONFLICTING_CHOICES = new HashMap<>();
    private static List<String> ALL_POSSIBLE_PAIR_KEYS_CACHE = null;

    static {
        // --- Original Choices ---
        CHOICES.add(new Choice("creative_flight", "You can fly like in Creative Mode", "A random item from your inventory gets deleted every 10s you are in flight"));
        CHOICES.add(new Choice("speed_iv", "You have permanent Speed IV", "Your hunger depletes three times as fast"));
        CHOICES.add(new Choice("insta_heal", "You regenerate health instantly anytime you heal", "Your maximum health is only three hearts"));
        CHOICES.add(new Choice("fire_immunity", "You are immune to fire and lava", "You take double damage from all melee attacks"));
        CHOICES.add(new Choice("water_breathing", "You can breathe underwater indefinitely", "You can't wear a helmet"));
        CHOICES.add(new Choice("haste_ii", "You have permanent Haste II", "Your tools lose durability twice as fast"));
        CHOICES.add(new Choice("high_jump", "You can jump 5 blocks high", "You take fall damage from any jump higher than 2 blocks"));
        CHOICES.add(new Choice("mob_invisibility", "You are completely invisible to all mobs", "You cannot deal any damage to them"));
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
        CHOICES.add(new Choice("sand_treasure", "All sand you break has a 0.1% chance to drop a treasure item", "You can no longer smelt items in a furnace"));
        CHOICES.add(new Choice("stacked_logs", "Every log you break drops a full stack of logs", "Every axe you find or craft has a durability of 10"));

        CHOICES.add(new Choice("mob_loot_inventory_slots", "You get 5x the drops from every mob", "Your inventory only has 9 slots"));
        CHOICES.add(new Choice("unbreakable_tools_netherite", "You can craft an unbreakable version of any tool", "It costs 32 netherite blocks"));
        CHOICES.add(new Choice("nether_water_fast_lava", "You can place water in the Nether", "All the lava in the nether and overwold flows as fast as water"));
        CHOICES.add(new Choice("weather_control_creeper", "You can control the weather with the weather command", "Every time you change it, It spawns a creeper that instantly explodes"));
        CHOICES.add(new Choice("insta_grow_bread_only", "You can grow trees instantly with bone meal", "You can only eat bread"));
        CHOICES.add(new Choice("strong_wolves_one_hp", "All your tamed wolves are 4x stronger and 2x faster", "They have only 1hp"));
        CHOICES.add(new Choice("op_bow_only", "You spawn a bow the has permanent Infinity, power 5, looting 5, and knockback 5", "You can only use a bows to attack"));
        CHOICES.add(new Choice("tame_hostiles_no_crafting", "You can tame any hostile mob with a block of netherite that will follow you and fight for you", "You cannot use crafting table"));
        CHOICES.add(new Choice("mace_max_damage_weak", "Your mace deals maximum smash damage from a 3-block fall", "It has the durability of a wood axe and cannot be enchanted"));
        CHOICES.add(new Choice("one_hit_dragon_no_end", "You can kill the Ender Dragon in one hit", "You cannot enter the end dimension ever again"));
        CHOICES.add(new Choice("fire_sword_self_damage", "You spawn a diamond sword that has Fire Aspect X", "You apply fire damage to yourself and it does 2x more damage"));
        CHOICES.add(new Choice("lava_fishing_rod", "You get a Luck of the Sea V fishing rod", "You can only fish in lava"));
        CHOICES.add(new Choice("choose_spawn_biome_lock", "You can choose which biome you spawn in", "You cannot leave that biome for 10 Minecraft days"));
        CHOICES.add(new Choice("bonemeal_ores_cobblestone", "You can use bonemeal on ore veins to make them 2x larger", "It has a 50% chance to turn the entire vein into cobblestone"));
        CHOICES.add(new Choice("golem_suit_slow", "You get a golem suit that gives you the health and strength of an Iron Golem", "You move at the speed of a sneaking player"));
        CHOICES.add(new Choice("see_mob_hp_double_health", "You can see the hp of every mob", "They have 2x health"));
        CHOICES.add(new Choice("enchant_level_x_delete_item", "You can put enchantments up to level X", "A random item in your inventory will be deleted every time you enchant above max lvl"));
        CHOICES.add(new Choice("stack_512_melon_only", "You can stack anything to 512", "You can only eat melons"));
        CHOICES.add(new Choice("passive_xp_no_mending", "You get 7xp 10s", "You can't apply Mending to anything"));
        CHOICES.add(new Choice("fast_horse_dark_fear", "You spawn a tamed horse that is as fast as a speed IV potion", "It is afraid of the dark"));
        CHOICES.add(new Choice("unbreakable_elytra_no_boost", "You get an elytra that is unbreakable", "You can't use fireworks to boost"));
        CHOICES.add(new Choice("fast_smelt_damage", "You can smelt any item in 1s with a furnace, smoker, or blast furnace", "You will lose half hp every time the items are done smelting"));
        CHOICES.add(new Choice("daily_totem_no_armor", "You get a Totem of Undying in your off-hand every morning", "You cannot wear any armor"));
        CHOICES.add(new Choice("villager_talk_no_fence_gates", "You can understand what Villagers are saying every time they make a noise", "You cannot use fence gates"));
        CHOICES.add(new Choice("give_decoration_blind", "You can use /give to get any decoration blocks or decoration items", "You get blinded by sunlight"));
        CHOICES.add(new Choice("personal_teleporter_scramble", "You get a personal teleporter to return to a set point", "It scrambles your inventory each time"));
        CHOICES.add(new Choice("luck_ores_one_hp", "You get permanent luck effect that 5x all ore drops", "It only works when you are at 1 hp or less"));
        CHOICES.add(new Choice("eat_anything_hunger_chance", "You can eat any item as food", "Non-food items have a 10% chance of giving you the hunger I effect for 30s"));
        CHOICES.add(new Choice("wind_charge_respawn_mob", "You get a Wind Charge every 30s seconds", "Every hostile mob you kill will respawn instantly"));
        CHOICES.add(new Choice("sneak_invisible_no_move", "You can turn invisible while sneaking", "You can't move"));
        CHOICES.add(new Choice("speed_v_random_keys", "You get permanent speed V", "Your movement keys are changed every 10min"));
        CHOICES.add(new Choice("hero_of_village_random_sounds", "You get permanent hero of the village", "Every sound is random"));
        CHOICES.add(new Choice("extra_slots_no_shulkers", "Your inventory has 9 extra slots", "You can no longer use shulker boxes"));
        CHOICES.add(new Choice("build_limit_638_top_bottom_only", "You can build to height limit of 638", "You can only place on the top and bottom of blocks"));
        CHOICES.add(new Choice("keep_inventory_berries_only", "You keep your inventory on death", "You can only eat sweet berries."));
        CHOICES.add(new Choice("midas_touch_no_hp_bar", "You get midas touch", "You can no longer see your hp"));
        CHOICES.add(new Choice("cheap_trades_sink_boats", "Get offered cheap trades from villager", "You sink boats"));
        CHOICES.add(new Choice("no_durability_loss_adventure_mode", "No longer lose durability on your items", "You are set to adventure mod every time it is night"));
        CHOICES.add(new Choice("no_hunger_random_shader", "You no longer need to eat", "You apply a random shader every 10s"));
        CHOICES.add(new Choice("auto_smelt_random_sound", "Every ore gets smelted", "a random sound plays every 30s"));
        CHOICES.add(new Choice("random_food_no_jump", "You get random food every 1min", "You cannot jump"));
        CHOICES.add(new Choice("sneak_bonemeal_fov_change", "You can bonemeal when you sneak", "Your fov is determined by your selected hot bar slot"));
        CHOICES.add(new Choice("no_fall_damage_low_render_distance", "You no longer take fall damage", "Your render distance is changed to 5 chunks"));
        CHOICES.add(new Choice("stack_64_world_height", "Items in you inventory get set to stack of 64", "Telepot to world height"));
        CHOICES.add(new Choice("instant_tame_creeper_hiss", "You tame any animal on the first try", "You hear the creeper hissing sound randomly forever"));
        CHOICES.add(new Choice("fast_swim_muted", "You can swim 2x as fast", "Your game is muted"));
        CHOICES.add(new Choice("wool_stack_random_crosshair", "You get a full stack of wool every time you shear a sheep", "Your crosshair is randomly set every 1min"));
        CHOICES.add(new Choice("infinite_sprint_no_elytra", "Be able to run infinitely without running out of hunger", "Never be able to fly"));
        CHOICES.add(new Choice("unlimited_ice_cream_brain_freeze", "Have unlimited ice cream", "It give you brain freeze every time you eat it"));
        CHOICES.add(new Choice("fast_climb_short", "You can climb 2x faster", "You become short"));
        CHOICES.add(new Choice("unbreakable_tools_cobblestone_only", "Have unbreakable tools", "Only ever get cobblestone"));
        CHOICES.add(new Choice("hidden_from_mobs_slow_ores", "Be completely hidden from hostile mobs", "Ores take twice as long to mine"));
        CHOICES.add(new Choice("half_health_mobs_no_drops", "Have hostile mobs with half health", "They drop nothing"));
        CHOICES.add(new Choice("triple_loot_triple_health", "Have mobs or hostile mobs drop 3x loot", "They have 3x health"));
        CHOICES.add(new Choice("triple_drops_slow_break", "Every block you mine drops 3x", "It takes 2x as long to break"));
        CHOICES.add(new Choice("no_fall_damage_sneak_speed", "Take no fall damage", "Move at sneaking speed forever"));
        CHOICES.add(new Choice("sleep_heal_random_drops", "Sleeping in a bed heals you to full health and hunger", "Every block you break is random"));
        CHOICES.add(new Choice("respawn_on_death_half_durability", "You respawn where you died", "All you items loose half durability"));
        CHOICES.add(new Choice("infinite_xp_no_anvils", "You get infinite xp", "You can’t use anvils or enchanting table"));
        CHOICES.add(new Choice("unbreakable_anvil_no_xp", "You get an anvil that never breaks when you use it", "You cannot gain any xp"));
        CHOICES.add(new Choice("creeper_cake_no_fence_gates", "Creepers explode into a flurry of cakes", "You cannot use fence gates"));
        CHOICES.add(new Choice("cheap_trades_no_ore_drops", "Get offered very cheap trades from villager", "Any ore you mine drops nothing"));
        CHOICES.add(new Choice("random_good_bad_potions", "Every time you eat get a random good potion effect", "You also get a random bad effect"));
        CHOICES.add(new Choice("no_phantoms_no_beds", "Phantoms never spawn", "You can’t use beds to sleep or set your spawn point"));
        CHOICES.add(new Choice("slow_fall_side_place_only", "Have permanent slow fall effect", "You can only place on sides of blocks"));
        CHOICES.add(new Choice("emeralds_from_mobs_siren", "Hostile mobs always drop 2 emeralds", "???"));
        CHOICES.add(new Choice("double_debris_vampire", "Ancient debris always drops 2", "You are a vampire"));
        CHOICES.add(new Choice("fast_day_slow_night", "You move 4x as fast during the day", "You move very slowly during the night"));
        CHOICES.add(new Choice("fast_night_slow_day", "You move 4x as fast during the night", "You move slowly during the day"));
        CHOICES.add(new Choice("double_food_no_hunger_bar", "Every food you eat gives you 2x hunger and saturation", "You cannot see your hunger bar"));


        // Resource & Mining Conflicts
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

        // Combat & Mob Conflicts
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

        // Movement & Player Stat Conflicts
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

        // Armor & Equipment Conflicts
        CONFLICTING_CHOICES.put("daily_totem_no_armor", "water_breathing");
        CONFLICTING_CHOICES.put("water_breathing", "daily_totem_no_armor");
        CONFLICTING_CHOICES.put("daily_totem_no_armor", "extra_hearts");
        CONFLICTING_CHOICES.put("extra_hearts", "daily_totem_no_armor");
        CONFLICTING_CHOICES.put("daily_totem_no_armor", "triple_damage");
        CONFLICTING_CHOICES.put("triple_damage", "daily_totem_no_armor");

        // Hunger Conflicts
        CONFLICTING_CHOICES.put("insta_grow_bread_only", "stack_512_melon_only");
        CONFLICTING_CHOICES.put("stack_512_melon_only", "insta_grow_bread_only");
        CONFLICTING_CHOICES.put("insta_grow_bread_only", "keep_inventory_berries_only");
        CONFLICTING_CHOICES.put("keep_inventory_berries_only", "insta_grow_bread_only");
        CONFLICTING_CHOICES.put("stack_512_melon_only", "keep_inventory_berries_only");
        CONFLICTING_CHOICES.put("keep_inventory_berries_only", "stack_512_melon_only");
        CONFLICTING_CHOICES.put("no_hunger_random_shader", "speed_iv");
        CONFLICTING_CHOICES.put("speed_iv", "no_hunger_random_shader");
        CONFLICTING_CHOICES.put("no_hunger_random_shader", "keep_inventory_berries_only");
        CONFLICTING_CHOICES.put("keep_inventory_berries_only", "no_hunger_random_shader");
        CONFLICTING_CHOICES.put("no_hunger_random_shader", "insta_grow_bread_only");
        CONFLICTING_CHOICES.put("insta_grow_bread_only", "no_hunger_random_shader");
        CONFLICTING_CHOICES.put("no_hunger_random_shader", "stack_512_melon_only");
        CONFLICTING_CHOICES.put("stack_512_melon_only", "no_hunger_random_shader");
        CONFLICTING_CHOICES.put("no_hunger_random_shader", "random_food_no_jump");
        CONFLICTING_CHOICES.put("random_food_no_jump", "no_hunger_random_shader");
        CONFLICTING_CHOICES.put("no_hunger_random_shader", "double_food_no_hunger_bar");
        CONFLICTING_CHOICES.put("double_food_no_hunger_bar", "no_hunger_random_shader");
        CONFLICTING_CHOICES.put("no_hunger_random_shader", "eat_anything_hunger_chance");
        CONFLICTING_CHOICES.put("eat_anything_hunger_chance", "no_hunger_random_shader");

        // XP & Enchanting Conflicts
        CONFLICTING_CHOICES.put("unbreakable_anvil_no_xp", "infinite_xp_no_anvils");
        CONFLICTING_CHOICES.put("infinite_xp_no_anvils", "unbreakable_anvil_no_xp");
        CONFLICTING_CHOICES.put("unbreakable_anvil_no_xp", "passive_xp_no_mending");
        CONFLICTING_CHOICES.put("passive_xp_no_mending", "unbreakable_anvil_no_xp");
        CONFLICTING_CHOICES.put("triple_ores", "infinite_xp_no_anvils");
        CONFLICTING_CHOICES.put("infinite_xp_no_anvils", "triple_ores");
        CONFLICTING_CHOICES.put("triple_ores", "enchant_level_x_delete_item");
        CONFLICTING_CHOICES.put("enchant_level_x_delete_item", "triple_ores");
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
        WyrSavedData savedData = WyrSavedData.get(player.serverLevel());
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

        return Arrays.asList(getChoicesFromKey(randomKey));
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
        return new Choice[]{getChoiceById(ids[0]), getChoiceById(ids[1])};
    }

    public static List<Choice> getAllChoices() {
        return Collections.unmodifiableList(CHOICES);
    }
}