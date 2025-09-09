package net.coba.wyr;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.List;

public class WyrScreen extends Screen {

    private final List<Choice> choices;

    private ChoiceButton leftChoiceButton;
    private ChoiceButton rightChoiceButton;
    private Button confirmButton;

    // A value of -1 means nothing is selected. 0 for left, 1 for right.
    private int selectedSide = -1;

    public static boolean isWyrScreenOpen = false;

    // --- COLOR DEFINITIONS ---
    private static final int BORDER_GRAY = 0xFF888888;
    private static final int BACKGROUND_BLACK = 0xFF000000;
    private static final int TITLE_COLOR = 0xFFFFFFFF;
    private static final int OR_COLOR = 0xFFFFFFFF;

    // Left Panel Colors
    private static final int LEFT_BORDER_DARK = 0xFF0000AA;    // Dark Blue
    private static final int LEFT_BORDER_HOVER = 0xFF5555FF;   // Lighter Blue
    private static final int LEFT_BORDER_SELECTED = 0xFF00FFFF; // Bright Light Blue

    // Right Panel Colors
    private static final int RIGHT_BORDER_DARK = 0xFFAA0000;     // Dark Red
    private static final int RIGHT_BORDER_HOVER = 0xFFFF5555;    // Lighter Red
    private static final int RIGHT_BORDER_SELECTED = 0xFFFF0000; // Bright Red

    public WyrScreen(List<Choice> choices) {
        super(Component.literal("Would You Rather"));
        this.choices = choices;
        isWyrScreenOpen = true;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Layout calculations
        int panelWidth = 140;
        int panelHeight = 120;
        int spacing = 30;
        int totalContentWidth = (panelWidth * 2) + spacing;

        int leftPanelX = centerX - totalContentWidth / 2;
        int rightPanelX = leftPanelX + panelWidth + spacing;
        int panelY = centerY - panelHeight / 2;

        // Ensure we have choices to display
        Choice leftRawChoice = choices.size() > 0 ? choices.get(0) : new Choice("missing_left", "Left choice missing", "Check ChoiceRegistry.java");
        Choice rightRawChoice = choices.size() > 1 ? choices.get(1) : new Choice("missing_right", "Right choice missing", "Check ChoiceRegistry.java");

        // Create the choice buttons
        leftChoiceButton = new ChoiceButton(leftPanelX, panelY, panelWidth, panelHeight,
                Component.literal(leftRawChoice.positive()),
                Component.literal(leftRawChoice.negative()),
                button -> selectChoice(0)); // Click on left panel selects side 0
        this.addRenderableWidget(leftChoiceButton);

        rightChoiceButton = new ChoiceButton(rightPanelX, panelY, panelWidth, panelHeight,
                Component.literal(rightRawChoice.positive()),
                Component.literal(rightRawChoice.negative()),
                button -> selectChoice(1)); // Click on right panel selects side 1
        this.addRenderableWidget(rightChoiceButton);

        // Main frame dimensions, calculated to fit the content
        int frameWidth = totalContentWidth + 24;
        int frameHeight = panelHeight + 40;
        int frameX = centerX - frameWidth / 2;
        int frameY = centerY - frameHeight / 2;

        // Position the confirm button below the main frame
        int confirmButtonY = frameY + frameHeight + 8;
        confirmButton = Button.builder(Component.literal("Confirm"), btn -> {
            if (selectedSide != -1 && Minecraft.getInstance().player != null) {
                Choice selectedChoice = choices.get(selectedSide);
                // Apply the selected effect
                ChoiceEffects.applyEffect(Minecraft.getInstance().player, selectedChoice.getId());
                PromptManager.resetAndResumeTimer(); // Resume the timer
                this.onClose();
            }
        }).bounds(centerX - 50, confirmButtonY, 100, 20).build();
        confirmButton.active = false; // Disabled until a choice is made
        this.addRenderableWidget(confirmButton);
    }

    private void selectChoice(int side) {
        this.selectedSide = side;
        this.confirmButton.active = true; // Enable confirm button upon selection
    }

    @Override
    public void onClose() {
        isWyrScreenOpen = false;
        super.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draw the semi-transparent background overlay
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        Font font = Minecraft.getInstance().font;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // --- Main Frame Drawing ---
        int frameWidth = (leftChoiceButton.getWidth() * 2) + 30 + 24;
        int frameHeight = leftChoiceButton.getHeight() + 40;
        int frameX = centerX - frameWidth / 2;
        int frameY = centerY - frameHeight / 2;
        int borderThickness = 4;

        // 1. Outer grey border
        guiGraphics.fill(frameX, frameY, frameX + frameWidth, frameY + frameHeight, BORDER_GRAY);

        // 2. Inner black background
        guiGraphics.fill(frameX + borderThickness, frameY + borderThickness,
                frameX + frameWidth - borderThickness, frameY + frameHeight - borderThickness, BACKGROUND_BLACK);

        // 3. Title text
        Component title = Component.literal("Would You Rather");
        int titleWidth = font.width(title);
        // Custom title position to be inside the top border
        guiGraphics.fill(centerX - titleWidth / 2 - 4, frameY - 2, centerX + titleWidth / 2 + 4, frameY + font.lineHeight + 2, BORDER_GRAY);
        guiGraphics.drawString(font, title, centerX - titleWidth / 2, frameY, TITLE_COLOR, false);

        // 4. "OR" text between panels
        int orTextY = leftChoiceButton.getY() + leftChoiceButton.getHeight() / 2 - font.lineHeight / 2;
        guiGraphics.drawString(font, "OR", centerX - font.width("OR") / 2, orTextY, OR_COLOR, true);

        // --- Choice Panel Border Drawing ---
        int panelBorderThickness = 2;

        // Determine left border color based on state (selected, hovered, or default)
        int leftBorderColor = LEFT_BORDER_DARK;
        if (selectedSide == 0) {
            leftBorderColor = LEFT_BORDER_SELECTED;
        } else if (leftChoiceButton.isHoveredOrFocused()) {
            leftBorderColor = LEFT_BORDER_HOVER;
        }
        guiGraphics.fill(leftChoiceButton.getX() - panelBorderThickness, leftChoiceButton.getY() - panelBorderThickness,
                leftChoiceButton.getX() + leftChoiceButton.getWidth() + panelBorderThickness, leftChoiceButton.getY() + leftChoiceButton.getHeight() + panelBorderThickness,
                leftBorderColor);

        // Determine right border color based on state
        int rightBorderColor = RIGHT_BORDER_DARK;
        if (selectedSide == 1) {
            rightBorderColor = RIGHT_BORDER_SELECTED;
        } else if (rightChoiceButton.isHoveredOrFocused()) {
            rightBorderColor = RIGHT_BORDER_HOVER;
        }
        guiGraphics.fill(rightChoiceButton.getX() - panelBorderThickness, rightChoiceButton.getY() - panelBorderThickness,
                rightChoiceButton.getX() + rightChoiceButton.getWidth() + panelBorderThickness, rightChoiceButton.getY() + rightChoiceButton.getHeight() + panelBorderThickness,
                rightBorderColor);

        // Render all widgets (buttons) on top of our custom-drawn GUI
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}