package net.coba.wyr;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.MutableComponent;


public class ClientEffectsHandler {

    @SubscribeEvent
    public void onComputeFov(ComputeFovModifierEvent event) {
        if (ClientEffectCache.hasFovChange()) {
            Player player = event.getPlayer();
            int selectedSlot = player.getInventory().getSelectedSlot();

            float maxFovDecrease = 40.0F;
            float fovDecrease = (selectedSlot / 8.0F) * maxFovDecrease;

            float currentFov = Minecraft.getInstance().options.fov().get().floatValue();
            if (currentFov == 0) return;

            float newFov = currentFov - fovDecrease;
            float newModifier = (newFov / currentFov);

            event.setNewFovModifier(event.getNewFovModifier() * newModifier);
        }
    }
}