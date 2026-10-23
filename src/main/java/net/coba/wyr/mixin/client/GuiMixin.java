package net.coba.wyr.mixin.client;

import net.coba.wyr.ChoiceEffects;
import net.coba.wyr.ClientEffectCache;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(Hud.class)
public class GuiMixin {

    /**
     * This Mixin targets the extractHealthLevel method in Minecraft's Gui class.
     * We use @Inject to run our own code right at the start of that method.
     * If the player has our effect, we cancel the rest of the method, preventing the health bar from ever rendering.
     */
    @Inject(method = "extractHealthLevel", at = @At("HEAD"), cancellable = true)
    private void extractHealthLevel(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        Player clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer != null && ChoiceEffects.hasEffect(clientPlayer, "midas_touch_no_hp_bar")) {
            ci.cancel();
        }
    }

    @Inject(method = "extractFoodLevel", at = @At("HEAD"), cancellable = true)
    private void extractFoodLevel(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        Player clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer != null && ChoiceEffects.hasEffect(clientPlayer, "double_food_no_hunger_bar")) {
            ci.cancel();
        }
    }

    @ModifyConstant(method = "extractFoodLevel", constant = @Constant(intValue = 10))
    private int wyr_modifyHungerBarLength(int original) {
        if (ClientEffectCache.hasHalfHunger()) {
            return 5;
        }

        return original;
    }

    @Shadow
    @Final
    private static Identifier CROSSHAIR_SPRITE;

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void wyr_randomizeCrosshairPosition(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ClientEffectCache.hasRandomCrosshair()) {
            ci.cancel();

            Minecraft mc = Minecraft.getInstance();
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            long seed = ClientEffectCache.getCrosshairSeed();
            Random random = new Random(seed);

            int crosshairSize = 15;
            int margin = 30;

            int x = margin + random.nextInt(screenWidth - (margin * 2) - crosshairSize);
            int y = margin + random.nextInt(screenHeight - (margin * 2) - crosshairSize);

            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, CROSSHAIR_SPRITE, x, y, crosshairSize, crosshairSize);
        }
    }
}