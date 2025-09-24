package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

@OnlyIn(Dist.CLIENT)
public class ClientEffectsHandler {

    @SubscribeEvent
    public void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // --- EFFECT: midas_touch_no_hp_bar ---
        // A full implementation requires more advanced rendering hooks (Mixins) to
        // selectively cancel the health bar. This is a placeholder for that logic.
        if (ChoiceEffects.hasEffect(mc.player, "midas_touch_no_hp_bar")) {
            // Logic to hide health bar would go here.
        }
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Post<LivingEntity, ?> event) {
        LivingEntity entity = event.getEntity();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.hasLineOfSight(entity)) return;

        // --- EFFECT: see_mob_hp_double_health ---
        if (ChoiceEffects.hasEffect(mc.player, "see_mob_hp_double_health") && entity instanceof Monster) {
            /*GuiGraphics graphics = event.getGuiGraphics();
            float health = entity.getHealth();
            float maxHealth = entity.getMaxHealth();
            String hpText = String.format("%.1f / %.1f ❤", health, maxHealth);

            int textWidth = mc.font.width(hpText);
            float scale = 0.025f;

            graphics.pose().pushPose();
            graphics.pose().translate(0, entity.getBbHeight() + 0.7f, 0);
            graphics.pose().mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            graphics.pose().scale(-scale, -scale, scale);

            // Draw a semi-transparent background for readability
            graphics.fill(-textWidth / 2 - 2, -2, textWidth / 2 + 2, mc.font.lineHeight, 0x80000000);
            graphics.drawString(mc.font, hpText, -textWidth / 2, 0, 0xFFFFFFFF, false);
            graphics.pose().popPose();*/
        }
    }

    // --- EFFECT: golem_suit_slow / sneak_invisible_no_move (FIXED ZOOM) ---
    @SubscribeEvent
    public void onComputeFov(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        if (ChoiceEffects.hasEffect(player, "golem_suit_slow") || ChoiceEffects.hasEffect(player, "sneak_invisible_no_move")) {
            // This event allows us to counteract the FOV change from slowness effects
            // by returning the base FOV, preventing the zoom.
            event.setNewFovModifier(1.0F);
        }
    }
}