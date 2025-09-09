package net.coba.wyr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A dedicated class to hold all possible choices.
 * This makes it easy to add, remove, or change choices without touching the GUI code.
 */
public class ChoiceRegistry {

    private static final List<Choice> CHOICES = new ArrayList<>();
    private static final Random RANDOM = new Random();

    static {
        // All 20 choices with unique IDs.
        CHOICES.add(new Choice("creative_flight", "You can fly like in Creative Mode and take no fall damage", "but a random item from your inventory, armor slot, or offhand gets deleted every 10s you are in flight."));
        CHOICES.add(new Choice("speed_iv", "You have permanent Speed IV", "but your hunger depletes three times as fast."));
        CHOICES.add(new Choice("insta_heal", "You regenerate health instantly anytime you heal", "but your maximum health is only three hearts."));
        CHOICES.add(new Choice("fire_immunity", "You are immune to fire and lava", "but you take double damage from all melee attacks."));
        CHOICES.add(new Choice("water_breathing", "You can breathe underwater indefinitely", "but you can't wear a helmet."));
        CHOICES.add(new Choice("haste_ii", "You have permanent Haste II", "but your tools lose durability twice as fast."));
        CHOICES.add(new Choice("high_jump", "You can jump 5 blocks high", "but you take fall damage from any jump higher than 2 blocks."));
        CHOICES.add(new Choice("mob_invisibility", "You are completely invisible to all mobs", "but you cannot deal any damage to them."));
        CHOICES.add(new Choice("no_hunger", "You no longer need to eat", "but you can no longer regenerate health naturally (only potions and beacons)."));
        CHOICES.add(new Choice("strength_ii", "You have a permanent Strength II effect", "but you cannot use a shield."));
        CHOICES.add(new Choice("night_vision", "You can see perfectly in the dark (permanent Night Vision)", "but Endermen are always hostile towards you."));
        CHOICES.add(new Choice("ender_teleport", "You can teleport like an Enderman (15 block range, 5-second cooldown)", "but you take two hearts of damage each time you do."));
        CHOICES.add(new Choice("extra_hearts", "You have a second row of hearts", "but you cannot wear chestplates."));
        CHOICES.add(new Choice("water_walk", "You can walk on water and lava (like Frost Walker)", "but you are permanently under the Slowness I effect."));
        CHOICES.add(new Choice("dolphin_grace", "You have a permanent Dolphin's Grace effect", "but you can't leave the water without taking damage."));
        CHOICES.add(new Choice("wall_climb", "You can climb walls like a spider", "but you take double damage from falls."));
        CHOICES.add(new Choice("potion_immunity", "You are immune to all negative potion effects", "but you cannot benefit from any positive potion effects."));
        CHOICES.add(new Choice("phasing", "You can phase through 1-block thick walls", "but you cannot open doors."));
        CHOICES.add(new Choice("triple_damage", "Your attacks have a 25% chance of dealing triple damage", "but you cannot wear boots."));
        CHOICES.add(new Choice("resistance_ii", "You have permanent Resistance II", "but you cannot sprint."));
    }

    /**
     * Fetches a specified number of random, unique choices from the list.
     * @param count The number of choices to get.
     * @return A list of random choices.
     */
    public static List<Choice> getRandomChoices(int count) {
        if (count > CHOICES.size()) {
            throw new IllegalArgumentException("Cannot get more random choices than exist in the registry.");
        }
        List<Choice> shuffled = new ArrayList<>(CHOICES);
        Collections.shuffle(shuffled, RANDOM);
        return shuffled.subList(0, count);
    }

    public static Choice getChoiceById(String id) {
        for (Choice choice : CHOICES) {
            if (choice.getId().equals(id)) {
                return choice;
            }
        }
        return null;
    }
}