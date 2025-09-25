package net.coba.wyr.mixin.client;

import net.coba.wyr.ChoiceEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    /**
     * This Mixin targets the renderFood method in Minecraft's Gui class.
     * We use @Inject to run our own code right at the start of that method.
     * If the player has our effect, we cancel the rest of the method, preventing the hunger bar from ever rendering.
     */
    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void onRenderFood(GuiGraphics guiGraphics, Player player, int top, int right, CallbackInfo ci) {
        // We get the player from the client-side Minecraft instance, as this is a client-only effect.
        Player clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer != null && ChoiceEffects.hasEffect(clientPlayer, "midas_touch_no_hp_bar")) {
            // Cancel the event, which stops the original renderFood method from running.
            ci.cancel();
        }
    }
}