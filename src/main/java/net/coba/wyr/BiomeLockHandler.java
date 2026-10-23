package net.coba.wyr;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Handles the biome lock countdown display and restrictions.
 */
public class BiomeLockHandler {
    
    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        
        if (player == null) return;
        
        // Check if player has the choose_spawn_biome_lock effect
        if (!ChoiceEffects.hasEffect(player, "choose_spawn_biome_lock")) return;
        
        // Get remaining time in Minecraft days
        long gameTime = player.level().getGameTime();
        long totalLockTime = 10 * 24000; // 10 Minecraft days
        long remainingTime = totalLockTime - gameTime;
        
        if (remainingTime > 0) {
            renderCountdownTitle(remainingTime);
        }
    }
    
    private void renderCountdownTitle(long remainingTime) {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        
        // Calculate remaining days and hours
        long remainingDays = remainingTime / 24000;
        long remainingHours = (remainingTime % 24000) / 1000;
        
        // Simplified rendering - just log the countdown for debugging
        // Full rendering implementation would require more complex GUI handling
        System.out.println("Biome Lock: " + remainingDays + "d " + remainingHours + "h remaining");
    }
}
