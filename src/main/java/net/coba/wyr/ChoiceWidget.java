package net.coba.wyr;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public class ChoiceWidget extends AbstractWidget {

    private boolean selected = false;
    private final ResourceLocation defaultTexture;
    private final ResourceLocation hoverTexture;
    private final ResourceLocation selectedTexture;
    private final Runnable onPress;
    private final boolean isPositive; // To determine text color

    public ChoiceWidget(int x, int y, int width, int height, Component message,
                        ResourceLocation defaultTex, ResourceLocation hoverTex, ResourceLocation selectedTex,
                        Runnable onPress, boolean isPositive) {
        super(x, y, width, height, message);
        this.defaultTexture = defaultTex;
        this.hoverTexture = hoverTex;
        this.selectedTexture = selectedTex;
        this.onPress = onPress;
        this.isPositive = isPositive;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.onPress.run();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation currentTexture = defaultTexture;
        if (selected) {
            currentTexture = selectedTexture;
        } else if (isHoveredOrFocused()) {
            currentTexture = hoverTexture;
        }

        // Draw the base texture for the choice box
        RenderSystem.enableBlend();
        guiGraphics.blit(currentTexture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
        RenderSystem.disableBlend();

        // Draw the text inside
        if (this.getMessage() != null) {
            int textColor = isPositive ? 0x55FF55 : 0xFF5555; // Green for positive, Red for negative
            int textMargin = 15; // Margin from the edge of the widget
            int availableWidth = this.getWidth() - (textMargin * 2);
            List<net.minecraft.util.FormattedCharSequence> lines = Minecraft.getInstance().font.split(this.getMessage(), availableWidth);
            int totalTextHeight = lines.size() * Minecraft.getInstance().font.lineHeight;
            int startY = this.getY() + (this.getHeight() - totalTextHeight) / 2;

            for (net.minecraft.util.FormattedCharSequence line : lines) {
                int textX = this.getX() + (this.getWidth() - Minecraft.getInstance().font.width(line)) / 2;
                // Draw with a shadow for better readability
                guiGraphics.drawString(Minecraft.getInstance().font, line, textX, startY, textColor, true);
                startY += Minecraft.getInstance().font.lineHeight;
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}