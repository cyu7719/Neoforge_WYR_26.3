package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ChoiceButton extends AbstractButton {

    @FunctionalInterface
    public interface OnPress {
        void onPress(ChoiceButton button);
    }

    private final Component positiveText;
    private final Component negativeText;
    private final OnPress onPress;

    // --- COLOR DEFINITIONS ---
    private static final int TEXT_BOX_GRAY = 0xFF404040;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int GO_TEXT_COLOR = 0xFFFFFF55; // Bright Yellow

    public ChoiceButton(int x, int y, int width, int height,
                        Component positiveText, Component negativeText,
                        OnPress onPress) {
        super(x, y, width, height, Component.empty());
        this.positiveText = positiveText;
        this.negativeText = negativeText;
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;

        // The button is composed of two gray boxes for text, with a gap in the middle.
        int gap = 8;
        int boxHeight = (this.getHeight() - gap) / 2;
        int topBoxY = this.getY();
        int bottomBoxY = this.getY() + boxHeight + gap;

        // Draw top gray box for positive text
        guiGraphics.fill(this.getX(), topBoxY, this.getX() + this.getWidth(), topBoxY + boxHeight, TEXT_BOX_GRAY);

        // Draw bottom gray box for negative text
        guiGraphics.fill(this.getX(), bottomBoxY, this.getX() + this.getWidth(), bottomBoxY + boxHeight, TEXT_BOX_GRAY);

        // Draw "GO!" text in the middle gap, which is purely decorative
        Component goText = Component.literal("but");
        int goTextWidth = font.width(goText);
        int goTextY = this.getY() + boxHeight + (gap / 2) - (font.lineHeight / 2);
        guiGraphics.drawString(font, goText, this.getX() + (this.getWidth() - goTextWidth) / 2, goTextY, GO_TEXT_COLOR, true);

        // --- Text Rendering ---
        int textMarginX = 5;
        int textMarginY = 5;
        int textWidth = this.getWidth() - (textMarginX * 2);

        // Draw the positive choice text (top box) with word wrap
        guiGraphics.drawWordWrap(font, this.positiveText, this.getX() + textMarginX, topBoxY + textMarginY, textWidth, TEXT_COLOR);

        // Draw the negative choice text (bottom box) with word wrap
        guiGraphics.drawWordWrap(font, this.negativeText, this.getX() + textMarginX, bottomBoxY + textMarginY, textWidth, TEXT_COLOR);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        // A click anywhere in the widget triggers the single onPress event
        this.onPress();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
