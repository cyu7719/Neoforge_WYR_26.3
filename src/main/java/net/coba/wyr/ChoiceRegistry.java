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
        // List of the 23 choice pairs
        CHOICES.add(new Choice("creative_flight", "You can fly like in Creative Mode", "A random item from your inventory gets deleted every 10s you are in flight."));
        CHOICES.add(new Choice("speed_iv", "You have permanent Speed IV", "Your hunger depletes three times as fast."));
        CHOICES.add(new Choice("insta_heal", "You regenerate health instantly anytime you heal", "Your maximum health is only three hearts."));
        CHOICES.add(new Choice("fire_immunity", "You are immune to fire and lava", "You take double damage from all melee attacks."));
        CHOICES.add(new Choice("water_breathing", "You can breathe underwater indefinitely", "You can't wear a helmet."));
        CHOICES.add(new Choice("haste_ii", "You have permanent Haste II", "Your tools lose durability twice as fast."));
        CHOICES.add(new Choice("high_jump", "You can jump 5 blocks high", "You take fall damage from any jump higher than 2 blocks."));
        CHOICES.add(new Choice("mob_invisibility", "You are completely invisible to all mobs", "You cannot deal any damage to them."));
        CHOICES.add(new Choice("strength_ii", "You have a permanent Strength II effect", "You cannot use a shield."));
        CHOICES.add(new Choice("night_vision", "You can see perfectly in the dark", "Endermen are always hostile towards you."));
        CHOICES.add(new Choice("ender_teleport", "You can teleport like an Enderman (15 block range, 5s cooldown)", "You take two hearts of damage each time you do."));
        CHOICES.add(new Choice("extra_hearts", "You have a second row of hearts", "You cannot wear chestplates."));
        CHOICES.add(new Choice("water_walk", "You can walk on water and lava", "You are permanently under the Slowness I effect."));
        CHOICES.add(new Choice("dolphin_grace", "You have a permanent Dolphin's Grace effect", "You can't leave the water without taking damage."));
        CHOICES.add(new Choice("wall_climb", "You can climb walls like a spider", "You take double damage from falls."));
        CHOICES.add(new Choice("phasing", "You can phase through 1 block thick walls", "You cannot open doors."));
        CHOICES.add(new Choice("triple_damage", "Your attacks have a 25% chance of dealing triple damage", "You cannot wear boots."));
        CHOICES.add(new Choice("resistance_ii", "You have permanent Resistance II", "You cannot sprint."));
        CHOICES.add(new Choice("fishing_luck", "When fishing, there's a 1% chance to get treasure", "Every 5th catch spawns a hostile Guardian."));
        CHOICES.add(new Choice("daily_diamond", "You get one diamond block every Minecraft day", "You get a thirst meter."));
        CHOICES.add(new Choice("triple_ores", "Every ore you mine drops 3x the resources", "You cannot enchant anything."));
        CHOICES.add(new Choice("sand_treasure", "All sand you break has a 0.1% chance to drop a treasure item", "You can no longer smelt items in a furnace."));
        CHOICES.add(new Choice("stacked_logs", "Every log you break drops a full stack of logs", "Every axe you find or craft has a durability of 10."));

        // Conflicting choice pair that does not show up when either one is selected
        CONFLICTING_CHOICES.put("insta_heal", "extra_hearts");
        CONFLICTING_CHOICES.put("extra_hearts", "insta_heal");
        CONFLICTING_CHOICES.put("water_walk", "water_breathing");
        CONFLICTING_CHOICES.put("water_breathing", "water_walk");
        CONFLICTING_CHOICES.put("dolphin_grace", "insta_heal");
        CONFLICTING_CHOICES.put("insta_heal", "dolphin_grace");
        CONFLICTING_CHOICES.put("stacked_logs", "haste_ii");
        CONFLICTING_CHOICES.put("haste_ii", "stacked_logs");
        CONFLICTING_CHOICES.put("speed_iv", "resistance_ii");
        CONFLICTING_CHOICES.put("resistance_ii", "speed_iv");
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