package net.coba.wyr.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.coba.wyr.ChoiceEffects;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class CustomBlockLootModifier extends LootModifier {

    public static final MapCodec<CustomBlockLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst).apply(inst, CustomBlockLootModifier::new));

    public CustomBlockLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player player) ||
                !(context.getParamOrNull(LootContextParams.BLOCK_STATE) instanceof BlockState state)) {
            return generatedLoot;
        }

        ObjectArrayList<ItemStack> newDrops = new ObjectArrayList<>();

        for (ItemStack stack : generatedLoot) {
            ItemStack newStack = stack.copy();

            if (ChoiceEffects.hasEffect(player, "triple_ores") && isOre(state)) {
                newStack.setCount(stack.getCount() * 3);
            }

            if (ChoiceEffects.hasEffect(player, "luck_ores_one_hp") && player.getHealth() <= 1.0F && isOre(state)) {
                newStack.setCount(stack.getCount() * 5);
            }

            if (ChoiceEffects.hasEffect(player, "stacked_logs") && state.is(BlockTags.LOGS)) {
                newStack.setCount(64);
            }

            newDrops.add(newStack);
        }

        return newDrops;
    }

    // Helper method to check if a block is a type of ore
    private static boolean isOre(BlockState state) {
        return state.is(BlockTags.IRON_ORES) || state.is(BlockTags.GOLD_ORES) ||
                state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES) ||
                state.is(BlockTags.LAPIS_ORES) || state.is(BlockTags.COPPER_ORES) ||
                state.is(BlockTags.COAL_ORES) || state.is(BlockTags.REDSTONE_ORES) ||
                state.is(Blocks.NETHER_GOLD_ORE) || state.is(Blocks.NETHER_QUARTZ_ORE) ||
                state.is(Blocks.ANCIENT_DEBRIS); // Added Ancient Debris as an ore
    }


    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}