package net.coba.wyr.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.coba.wyr.ChoiceEffects;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class CustomBlockLootModifier extends LootModifier {

    // Codec for serialization/deserialization
    public static final MapCodec<CustomBlockLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst).apply(inst, CustomBlockLootModifier::new));

    public CustomBlockLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // Safely get the player and block state from the loot context
        if (!(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player player) ||
                !(context.getParamOrNull(LootContextParams.BLOCK_STATE) instanceof BlockState state)) {
            return generatedLoot;
        }

        System.out.println("Loot Modifier Check -> Player: " + player.getName().getString() + " | Has triple_ores: " + ChoiceEffects.hasEffect(player, "triple_ores"));

        // Create a new list to hold the modified drops
        ObjectArrayList<ItemStack> newDrops = new ObjectArrayList<>();

        // Iterate through the original drops and apply your logic
        for (ItemStack stack : generatedLoot) {
            ItemStack newStack = stack.copy();

            if (ChoiceEffects.hasEffect(player, "triple_ores") &&
                    (state.is(BlockTags.IRON_ORES) || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.DIAMOND_ORES) ||
                            state.is(BlockTags.EMERALD_ORES) || state.is(BlockTags.LAPIS_ORES) || state.is(BlockTags.COPPER_ORES) ||
                            state.is(BlockTags.COAL_ORES) || state.is(BlockTags.REDSTONE_ORES))) {
                newStack.setCount(stack.getCount() * 3);
            }

            if (ChoiceEffects.hasEffect(player, "stacked_logs") && state.is(BlockTags.LOGS)) {
                newStack.setCount(64);
            }
            newDrops.add(newStack);
        }

        // Return the final list of drops
        return newDrops;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}