package net.coba.wyr;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WyrCommands {

    private static final Map<String, Integer> INTERVAL_MAP = Map.of(
            "off", 0, "30s", 600, "1min", 1200, "3min", 3600, "5min", 6000, "10min", 12000
    );

    private static final List<String> EFFECT_IDS = ChoiceRegistry.getAllChoices().stream()
            .map(Choice::getId)
            .collect(Collectors.toList());

    // ++ METHOD SIGNATURE IS NOW CORRECT FOR THE NEW EVENT ++
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {

        // Main command node: /wyr
        dispatcher.register(Commands.literal("wyr")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("apply")
                        .then(Commands.argument("effectId", StringArgumentType.string())
                                .suggests(WyrCommands::getEffectSuggestions)
                                .executes(WyrCommands::applyEffect)))
                .then(Commands.literal("clear")
                        .executes(WyrCommands::clearEffects))
                .then(Commands.literal("remove")
                        .then(Commands.argument("effectId", StringArgumentType.string())
                                .suggests(WyrCommands::getActiveEffectSuggestions)
                                .executes(WyrCommands::removeEffect)))
                .then(Commands.literal("setinterval")
                        .then(Commands.argument("interval", StringArgumentType.string())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(INTERVAL_MAP.keySet(), builder))
                                .executes(WyrCommands::setServerInterval)))
        );

        // Register the new /wyrgive command
        dispatcher.register(Commands.literal("wyrgive")
                .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                        .suggests(WyrCommands::getDecorationItemSuggestions)
                        .executes(WyrCommands::executeWyrGive)
                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                .executes(WyrCommands::executeWyrGive)))
        );

                dispatcher.register(Commands.literal("wyrweather")
                        .then(Commands.literal("clear").executes(WyrCommands::executeWyrWeather))
                        .then(Commands.literal("rain").executes(WyrCommands::executeWyrWeather))
                        .then(Commands.literal("thunder").executes(WyrCommands::executeWyrWeather))
        );
    }

    private static int executeWyrWeather(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!ChoiceEffects.hasEffect(player, "weather_control_creeper")) {
            context.getSource().sendFailure(Component.literal("You do not have the required effect to use this command."));
            return 0;
        }
        Creeper creeper = EntityTypes.CREEPER.create(player.level(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (creeper != null) {
            creeper.setPos(player.position());
            creeper.ignite();
            player.level().addFreshEntity(creeper);
        }

        String weatherType = context.getNodes().get(1).getNode().getName();
        CommandSourceStack elevatedSource = player.createCommandSourceStack().withPermission(PermissionSet.ALL_PERMISSIONS);

        player.level().getServer().getCommands().performPrefixedCommand(elevatedSource, "weather " + weatherType);
        return 1;
    }

    private static CompletableFuture<Suggestions> getDecorationItemSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Iterable<Holder<Item>> itemHolders = net.minecraft.core.registries.BuiltInRegistries.ITEM.getTagOrEmpty(ModTags.Items.DECORATION_ITEMS);

        // Get all items in the tag
        List<String> suggestions = new ArrayList<>();
        for (Holder<Item> holder : itemHolders) {
            holder.unwrapKey().ifPresent(key -> {
                String itemId = key.registry().toString();
                // Remove minecraft: prefix for cleaner display
                if (itemId.startsWith("minecraft:")) {
                    suggestions.add(itemId.substring(10));
                } else {
                    suggestions.add(itemId);
                }
            });
        }
        
        return SharedSuggestionProvider.suggest(suggestions, builder);
    }

    private static int executeWyrGive(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        if (!ChoiceEffects.hasEffect(player, "give_decoration_blind")) {
            context.getSource().sendFailure(Component.literal("You do not have the required effect to use this command."));
            return 0;
        }

        ItemInput itemInput = ItemArgument.getItem(context, "item");

        int count = 1;
        try {
            count = IntegerArgumentType.getInteger(context, "count");
        } catch (IllegalArgumentException e) {
        }

        ItemStack stackToGive = itemInput.createItemStack(count);
        Item item = stackToGive.getItem();
        
        // Check if item is in decoration_items tag using holder
        boolean isDecorationItem = item.builtInRegistryHolder().is(ModTags.Items.DECORATION_ITEMS);

        if (isDecorationItem) {
            boolean added = player.getInventory().add(stackToGive.copy());
            if (added) {
                int finalCount = count;
                context.getSource().sendSuccess(() -> Component.literal("Gave " + finalCount + " [" + stackToGive.getHoverName().getString() + "]"), true);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("Your inventory is full."));
                return 0;
            }
        } else {
            context.getSource().sendFailure(Component.literal("You can only give yourself decoration items with this command."));
            return 0;
        }
    }

    private static int setServerInterval(CommandContext<CommandSourceStack> context) {
        String intervalKey = StringArgumentType.getString(context, "interval");
        if (!INTERVAL_MAP.containsKey(intervalKey)) {
            context.getSource().sendFailure(Component.literal("Invalid interval. Use one of: " + String.join(", ", INTERVAL_MAP.keySet())));
            return 0;
        }
        int intervalTicks = INTERVAL_MAP.get(intervalKey);
        ServerLevel level = context.getSource().getLevel();
        WyrSavedData.get(level).setGlobalPromptInterval(intervalTicks);
        String feedback = intervalTicks == 0 ? "Disabled" : intervalKey;
        context.getSource().sendSuccess(() -> Component.literal("Server-wide prompt interval set to: " + feedback), true);
        return 1;
    }

    private static CompletableFuture<Suggestions> getEffectSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(EFFECT_IDS, builder);
    }

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

    private static int applyEffect(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String effectId = StringArgumentType.getString(context, "effectId");
        if (!EFFECT_IDS.contains(effectId)) {
            context.getSource().sendFailure(Component.literal("Unknown effect ID: " + effectId));
            return 0;
        }
        Set<String> activeChoiceIds = PlayerChoiceTracker.getChoices(player).stream()
                .map(Choice::getId)
                .collect(Collectors.toSet());
        if (ChoiceRegistry.conflictsWith(effectId, activeChoiceIds)) {
            context.getSource().sendFailure(Component.literal("Cannot apply this effect as it conflicts with one you already have."));
            return 0;
        }
        Choice choice = ChoiceRegistry.getChoiceById(effectId);
        if (choice == null) {
            context.getSource().sendFailure(Component.literal("Internal error: Could not find choice for ID: " + effectId));
            return 0;
        }

        ChoiceEffects.applyEffect(player, effectId, true);
        PlayerChoiceTracker.addChoice(player, choice);

        ChoiceEffects.syncAllClientEffects(player);

        context.getSource().sendSuccess(() -> Component.literal("Applied effect: " + effectId), true);

        if (effectId.equals("choose_spawn_biome_lock")) {
            PacketDistributor.sendToPlayer(player, new PacketOpenBiomeGui());
        }

        return 1;
    }

    private static int removeEffect(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String effectIdToRemove = StringArgumentType.getString(context, "effectId");
        List<String> activeIds = PlayerChoiceTracker.getChoices(player).stream()
                .map(Choice::getId)
                .toList();
        if (!activeIds.contains(effectIdToRemove)) {
            context.getSource().sendFailure(Component.literal("You do not have the effect: " + effectIdToRemove));
            return 0;
        }
        ChoiceEffects.removeEffect(player, effectIdToRemove);
        PlayerChoiceTracker.removeChoice(player, effectIdToRemove);
        context.getSource().sendSuccess(() -> Component.literal("Removed effect: " + effectIdToRemove), true);
        return 1;
    }

    private static int clearEffects(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ChoiceEffects.clearAllEffects(player);
        context.getSource().sendSuccess(() -> Component.literal("All 'Would You Rather' effects cleared."), true);
        return 1;
    }

    
    
    private static void dummyTest(CommandSourceStack source) {
        // PermissionSet is no longer available in this form
    }
}
