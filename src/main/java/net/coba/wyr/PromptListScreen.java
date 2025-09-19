package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;

public class PromptListScreen extends Screen {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(WYR.MODID, "textures/gui/prompt_list.png");
    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 256;
    private static final int TEXT_COLOR = 0xFFFFFF;

    // --- Layout Constants ---
    private static final int LIST_TOP_Y_OFFSET = 10; // Moved list up
    private static final int LIST_BOTTOM_Y_OFFSET = 10;
    private static final int LEFT_COLUMN_X_OFFSET = 22; // Adjusted for left alignment
    private static final int RIGHT_COLUMN_X_OFFSET = 137; // Adjusted for left alignment
    private static final int TEXT_WRAP_WIDTH = 95;
    private static final int ITEM_SPACING = 6;
    private static final int SCROLLBAR_X_OFFSET = 242;
    private static final int SCROLLBAR_TOP_Y_OFFSET = 8;
    private static final int SCROLLBAR_BOTTOM_Y_OFFSET = 7;

    private final List<Choice> choices;
    private double scrollAmount = 0.0;
    private boolean isDragging = false;
    private final int contentHeight;

    public PromptListScreen(List<Choice> choices) {
        super(Component.literal("Prompt List"));
        this.choices = choices;

        Font font = Minecraft.getInstance().font;
        int totalHeight = 0;
        for (Choice choice : this.choices) {
            int positiveLines = font.split(Component.literal(choice.positive()), TEXT_WRAP_WIDTH).size();
            int negativeLines = font.split(Component.literal(choice.negative()), TEXT_WRAP_WIDTH).size();

            int positiveHeight = positiveLines * font.lineHeight;
            int negativeHeight = negativeLines * font.lineHeight;
            totalHeight += Math.max(positiveHeight, negativeHeight) + ITEM_SPACING;
        }
        this.contentHeight = totalHeight;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        Font font = Minecraft.getInstance().font;
        int left = (this.width - GUI_WIDTH) / 2;
        int top = (this.height - GUI_HEIGHT) / 2;

        guiGraphics.blit(TEXTURE, left, top, 0, 0, GUI_WIDTH, GUI_HEIGHT);

        // Header text has been removed

        int listTop = top + LIST_TOP_Y_OFFSET;
        int listBottom = top + GUI_HEIGHT - LIST_BOTTOM_Y_OFFSET;
        int listHeight = listBottom - listTop;
        guiGraphics.enableScissor(left, listTop, left + GUI_WIDTH, listBottom);

        int currentY = listTop - (int) (scrollAmount * Math.max(0, contentHeight - listHeight));

        for (Choice choice : this.choices) {
            Component positiveText = Component.literal(choice.positive());
            Component negativeText = Component.literal(choice.negative());

            // --- FIX --- Calculate heights of both text blocks first
            int positiveTextHeight = font.split(positiveText, TEXT_WRAP_WIDTH).size() * font.lineHeight;
            int negativeTextHeight = font.split(negativeText, TEXT_WRAP_WIDTH).size() * font.lineHeight;
            int rowHeight = Math.max(positiveTextHeight, negativeTextHeight);

            // Calculate the vertical offset to middle-align the shorter text block
            int positiveYOffset = (rowHeight - positiveTextHeight) / 2;
            int negativeYOffset = (rowHeight - negativeTextHeight) / 2;

            // Render the text blocks with their calculated offsets
            renderLeftAlignedWordWrap(guiGraphics, font, positiveText, left + LEFT_COLUMN_X_OFFSET, currentY + positiveYOffset);
            renderLeftAlignedWordWrap(guiGraphics, font, negativeText, left + RIGHT_COLUMN_X_OFFSET, currentY + negativeYOffset);

            // Advance Y position by the height of the row
            currentY += rowHeight + ITEM_SPACING;
        }

        guiGraphics.disableScissor();

        if (contentHeight > listHeight) {
            renderScrollbar(guiGraphics, left, top);
        }
    }

    /**
     * --- FIX --- Renamed method and changed logic to render text left-aligned.
     */
    private void renderLeftAlignedWordWrap(GuiGraphics guiGraphics, Font font, Component text, int columnX, int startY) {
        List<FormattedCharSequence> lines = font.split(text, TEXT_WRAP_WIDTH);
        int y = startY;
        for (FormattedCharSequence line : lines) {
            // The X position is now just the column's start, not calculated for centering.
            guiGraphics.drawString(font, line, columnX, y, TEXT_COLOR, false);
            y += font.lineHeight;
        }
    }

    // ... (Scrollbar and mouse handling methods remain the same) ...
    private void renderScrollbar(GuiGraphics guiGraphics, int left, int top) {
        int scrollbarY = top + SCROLLBAR_TOP_Y_OFFSET;
        int scrollbarHeight = GUI_HEIGHT - SCROLLBAR_TOP_Y_OFFSET - SCROLLBAR_BOTTOM_Y_OFFSET;
        int listHeight = GUI_HEIGHT - (LIST_TOP_Y_OFFSET + LIST_BOTTOM_Y_OFFSET);
        int scrollbarX = left + SCROLLBAR_X_OFFSET;

        guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + 6, scrollbarY + scrollbarHeight, 0xFF202020);

        int handleHeight = Mth.clamp((int) ((float) (scrollbarHeight * listHeight) / (float) this.contentHeight), 20, scrollbarHeight);
        int handleY = scrollbarY + (int) (scrollAmount * (scrollbarHeight - handleHeight));

        guiGraphics.fill(scrollbarX, handleY, scrollbarX + 6, handleY + handleHeight, 0xFF808080);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int listHeight = GUI_HEIGHT - (LIST_TOP_Y_OFFSET + LIST_BOTTOM_Y_OFFSET);
        if (contentHeight > listHeight) {
            float scrollStep = 1.0f / Math.max(1, contentHeight - listHeight);
            scrollAmount = Mth.clamp(scrollAmount - (scrollY * scrollStep * 10), 0.0, 1.0);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = (this.width - GUI_WIDTH) / 2;
        int top = (this.height - GUI_HEIGHT) / 2;

        int scrollbarX = left + SCROLLBAR_X_OFFSET;
        int scrollbarY = top + SCROLLBAR_TOP_Y_OFFSET;
        int scrollbarHeight = GUI_HEIGHT - SCROLLBAR_TOP_Y_OFFSET - SCROLLBAR_BOTTOM_Y_OFFSET;
        int listHeight = GUI_HEIGHT - (LIST_TOP_Y_OFFSET + LIST_BOTTOM_Y_OFFSET);

        if (contentHeight > listHeight && mouseX >= scrollbarX && mouseX < scrollbarX + 6 && mouseY >= scrollbarY && mouseY < scrollbarY + scrollbarHeight) {
            this.isDragging = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int scrollbarHeight = GUI_HEIGHT - SCROLLBAR_TOP_Y_OFFSET - SCROLLBAR_BOTTOM_Y_OFFSET;
        int listHeight = GUI_HEIGHT - (LIST_TOP_Y_OFFSET + LIST_BOTTOM_Y_OFFSET);

        if (this.isDragging && contentHeight > listHeight) {
            int handleHeight = Mth.clamp((int) ((float) (scrollbarHeight * listHeight) / (float) this.contentHeight), 20, scrollbarHeight);
            double scrollableHeight = scrollbarHeight - handleHeight;

            if (scrollableHeight > 0) {
                double pixelsPerScroll = 1.0 / scrollableHeight;
                this.scrollAmount = Mth.clamp(this.scrollAmount + (dragY * pixelsPerScroll), 0.0, 1.0);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}