package net.coba.wyr;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class ClientRenderEvents {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // TODO: Fix API changes for RenderLevelStageEvent, Camera, RenderType
        // Temporarily disabled due to API changes in NeoForge 26.1.2
        return;
    }

    private static boolean isOre(BlockState state) {
        return state.is(BlockTags.IRON_ORES) || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.COPPER_ORES) ||
                state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE) ||
                state.is(Blocks.LAPIS_ORE) || state.is(Blocks.DEEPSLATE_LAPIS_ORE) ||
                state.is(Blocks.REDSTONE_ORE) || state.is(Blocks.DEEPSLATE_REDSTONE_ORE) ||
                state.is(Blocks.EMERALD_ORE) || state.is(Blocks.DEEPSLATE_EMERALD_ORE) ||
                state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE) ||
                state.is(Blocks.NETHER_QUARTZ_ORE) || state.is(Blocks.ANCIENT_DEBRIS);
    }

    private static float[] getOreColor(Block block) {
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) return new float[]{0.2f, 0.2f, 0.2f};
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) return new float[]{0.8f, 0.5f, 0.2f};
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return new float[]{0.8f, 0.7f, 0.6f};
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE) return new float[]{1.0f, 1.0f, 0.0f};
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) return new float[]{0.1f, 0.2f, 0.8f};
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return new float[]{1.0f, 0.0f, 0.0f};
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return new float[]{0.1f, 1.0f, 0.3f};
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return new float[]{0.2f, 0.9f, 1.0f};
        if (block == Blocks.NETHER_QUARTZ_ORE) return new float[]{0.9f, 0.9f, 0.9f};
        if (block == Blocks.ANCIENT_DEBRIS) return new float[]{0.4f, 0.3f, 0.3f};
        return new float[]{1.0f, 1.0f, 1.0f};
    }
}