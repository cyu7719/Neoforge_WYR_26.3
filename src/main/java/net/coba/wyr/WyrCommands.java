package net.coba.wyr;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WyrCommands {

    // A list of all possible effect IDs for command suggestions
    private static final List<String> EFFECT_IDS = ChoiceRegistry.getAllChoices().stream()
            .map(Choice::getId)
            .collect(Collectors.toList());

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Main command node: /wyr
        dispatcher.register(Commands.literal("wyr")
                .requires(source -> source.hasPermission(2)) // Requires permission level 2 (op)

                // Subcommand: /wyr apply <effectId>
                .then(Commands.literal("apply")
                        .then(Commands.argument("effectId", StringArgumentType.string())
                                .suggests(WyrCommands::getEffectSuggestions) // Autocomplete suggestions
                                .executes(WyrCommands::applyEffect)))

                // Subcommand: /wyr clear
                .then(Commands.literal("clear")
                        .executes(WyrCommands::clearEffects))

                // --- NEW SUBCOMMAND ---
                // Subcommand: /wyr remove <effectId>
                .then(Commands.literal("remove")
                        .then(Commands.argument("effectId", StringArgumentType.string())
                                .suggests(WyrCommands::getActiveEffectSuggestions) // Autocomplete for active effects
                                .executes(WyrCommands::removeEffect)))
        );
    }

    // This method provides the autocomplete suggestions for the 'apply' command
    private static CompletableFuture<Suggestions> getEffectSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(EFFECT_IDS, builder);
    }

    // --- NEW METHOD ---
    // This provides autocomplete suggestions for the 'remove' command, showing only active effects.
    private static CompletableFuture<Suggestions> getActiveEffectSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            return Suggestions.empty();
        }
        List<String> activeIds = PlayerChoiceTracker.getChoices(player).stream()
                .map(Choice::getId)
                .toList();
        return SharedSuggestionProvider.suggest(activeIds, builder);
    }


    // The logic that runs for '/wyr apply'
    private static int applyEffect(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        String effectId = StringArgumentType.getString(context, "effectId");
        if (!EFFECT_IDS.contains(effectId)) {
            context.getSource().sendFailure(Component.literal("Unknown effect ID: " + effectId));
            return 0;
        }

        Set<String> activeChoiceIds = PlayerChoiceTracker.getChoices(player)
                .stream()
                .map(Choice::getId)
                .collect(Collectors.toSet());

        // Use the new helper method to check for conflicts.
        if (ChoiceRegistry.conflictsWith(effectId, activeChoiceIds)) {
            context.getSource().sendFailure(Component.literal("Cannot apply this effect as it conflicts with one you already have."));
            return 0; // Stop execution if there's a conflict.
        }

        // Find the full Choice object from the ID.
        Choice choice = ChoiceRegistry.getChoiceById(effectId);
        if (choice == null) {
            context.getSource().sendFailure(Component.literal("Internal error: Could not find choice for ID: " + effectId));
            return 0;
        }

        // Apply the game effect
        ChoiceEffects.applyInitialEffect(player, effectId);
        // AND add the choice to the tracker so it appears in the GUI list.
        PlayerChoiceTracker.addChoice(player, choice);
        // --- END OF FIX ---

        context.getSource().sendSuccess(() -> Component.literal("Applied effect: " + effectId), true);
        return 1;
    }

    // --- NEW METHOD ---
    // The logic that runs for '/wyr remove'
    private static int removeEffect(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        String effectIdToRemove = StringArgumentType.getString(context, "effectId");
        List<String> activeIds = PlayerChoiceTracker.getChoices(player).stream()
                .map(Choice::getId)
                .toList();

        if (!activeIds.contains(effectIdToRemove)) {
            context.getSource().sendFailure(Component.literal("You do not have the effect: " + effectIdToRemove));
            return 0;
        }

        // Remove the game effect
        ChoiceEffects.removeEffect(player, effectIdToRemove);
        // Remove the choice from persistent data
        PlayerChoiceTracker.removeChoice(player, effectIdToRemove);

        context.getSource().sendSuccess(() -> Component.literal("Removed effect: " + effectIdToRemove), true);
        return 1;
    }


    // The logic that runs for '/wyr clear'
    private static int clearEffects(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        ChoiceEffects.clearAllEffects(player);
        context.getSource().sendSuccess(() -> Component.literal("All 'Would You Rather' effects cleared."), true);
        return 1;
    }
}