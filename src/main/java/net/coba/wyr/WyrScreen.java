package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

public class WyrScreen extends Screen {

    // --- Texture Definitions ---
    private static final Identifier TEXTURE_DEFAULT = Identifier.fromNamespaceAndPath(WYR.MODID, "textures/gui/choices.png");
    private static final Identifier TEXTURE_LEFT_HIGHLIGHT = Identifier.fromNamespaceAndPath(WYR.MODID, "textures/gui/left_highlighted.png");
    private static final Identifier TEXTURE_LEFT_SELECTED = Identifier.fromNamespaceAndPath(WYR.MODID, "textures/gui/left_selected.png");
    private static final Identifier TEXTURE_RIGHT_HIGHLIGHT = Identifier.fromNamespaceAndPath(WYR.MODID, "textures/gui/right_highlighted.png");
    private static final Identifier TEXTURE_RIGHT_SELECTED = Identifier.fromNamespaceAndPath(WYR.MODID, "textures/gui/right_selected.png");

    // --- GUI LAYOUT CONSTANTS ---
    // These values MUST match the exact pixel dimensions of your main GUI background image.
    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 172;

    // --- CLICKABLE PANEL CONSTANTS ---
    // These define the invisible areas that detect mouse clicks.
    private static final int PANEL_WIDTH = 127;
    private static final int PANEL_HEIGHT = 128;

    private static final int OVERLAY_WIDTH = 256;
    private static final int OVERLAY_HEIGHT = 254;
    private static final int LEFT_PANEL_X_OFFSET = 8;
    private static final int RIGHT_PANEL_X_OFFSET = 128;
    private static final int PANEL_Y_OFFSET = 13;

    private static final int TEXT_WRAP_WIDTH = 98;
    private static final int TEXT_MARGIN_X = 8;
    private static final int POSITIVE_TEXT_Y_OFFSET = 14;
    private static final int NEGATIVE_TEXT_Y_OFFSET = 90;


    private final List<Choice> choices;
    private int selectedSide = -1;

    private ClickablePanel leftPanel;
    private ClickablePanel rightPanel;
    private Button confirmButton;
    public static boolean isWyrScreenOpen = false;


    public WyrScreen(List<Choice> choices) {
        super(Component.literal("Would You Rather"));
        this.choices = choices;
        isWyrScreenOpen = true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        // Return false to prevent the screen from closing when the ESC key is pressed.
        return false;
    }

    @Override
    protected void init() {
        super.init();

        // Calculate the top-left corner of the entire GUI
        int left = (this.width - GUI_WIDTH) / 2;
        int top = (this.height - GUI_HEIGHT) / 2;

        // Position the clickable panels using the new offset constants
        int leftPanelX = left + LEFT_PANEL_X_OFFSET;
        int panelY = top + PANEL_Y_OFFSET;
        int rightPanelX = left + RIGHT_PANEL_X_OFFSET;

        this.leftPanel = this.addRenderableWidget(new ClickablePanel(leftPanelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, () -> selectChoice(0)));
        this.rightPanel = this.addRenderableWidget(new ClickablePanel(rightPanelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, () -> selectChoice(1)));

        this.confirmButton = this.addRenderableWidget(Button.builder(Component.literal("Confirm"), btn -> {
            if (selectedSide != -1 && Minecraft.getInstance().player != null) {
                Choice selectedChoice = choices.get(selectedSide);
                ClientPacketDistributor.sendToServer(new PacketApplyEffect(selectedChoice.getId()));
                PromptManager.resetAndResumeTimer();
                if (!selectedChoice.getId().equals("choose_spawn_biome_lock")) {
                    this.onClose();
                }
            }
        }).bounds(this.width / 2 - 50, top + GUI_HEIGHT + 8, 100, 20).build());
        this.confirmButton.active = false;
    }

    private void selectChoice(int side) {
        this.selectedSide = side;
        this.confirmButton.active = true;
    }

    @Override
    public void onClose() {
        isWyrScreenOpen = false;
        super.onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Calculate the top-left corner of the GUI for positioning all elements
        int left = (this.width - GUI_WIDTH) / 2;
        int top = (this.height - GUI_HEIGHT) / 2;


        // Render the vanilla widgets (e.g., the Confirm button) on top of everything else.
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        // Always draw the default background texture first.
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_DEFAULT, left, top, 0.0F, 0.0F, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);


        // Calculate the absolute screen coordinates for the overlays using the offsets.
        int leftOverlayX = left + LEFT_PANEL_X_OFFSET;
        int rightOverlayX = left + RIGHT_PANEL_X_OFFSET;
        int overlayY = top + PANEL_Y_OFFSET;

        // Draw the highlight or selection texture for the left panel if needed.
        if (selectedSide == 0) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_LEFT_SELECTED, leftOverlayX, overlayY, 0.0F, 0.0F, OVERLAY_WIDTH, OVERLAY_HEIGHT, OVERLAY_WIDTH, OVERLAY_HEIGHT);
        } else if (leftPanel.isHoveredOrFocused()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_LEFT_HIGHLIGHT, leftOverlayX, overlayY, 0.0F, 0.0F, OVERLAY_WIDTH, OVERLAY_HEIGHT, OVERLAY_WIDTH, OVERLAY_HEIGHT);
        }

        // Draw the highlight or selection texture for the right panel if needed.
        if (selectedSide == 1) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_RIGHT_SELECTED, rightOverlayX, overlayY, 0.0F, 0.0F, OVERLAY_WIDTH, OVERLAY_HEIGHT, OVERLAY_WIDTH, OVERLAY_HEIGHT);
        } else if (rightPanel.isHoveredOrFocused()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_RIGHT_HIGHLIGHT, rightOverlayX, overlayY, 0.0F, 0.0F, OVERLAY_WIDTH, OVERLAY_HEIGHT, OVERLAY_WIDTH, OVERLAY_HEIGHT);
        }


        // Draw the choice text on top of the background and any overlays.
        Choice leftChoice = choices.get(0);
        Choice rightChoice = choices.get(1);
        renderChoiceText(guiGraphics, leftChoice, leftPanel.getX(), leftPanel.getY());
        renderChoiceText(guiGraphics, rightChoice, rightPanel.getX(), rightPanel.getY());

    }

    private void renderChoiceText(GuiGraphicsExtractor guiGraphics, Choice choice, int panelX, int panelY) {
        Font font = Minecraft.getInstance().font;
        int textColor = 0xFFFFFFFF;

        // Calculate absolute positions using the panel's origin and the new offsets
        int textX = panelX + TEXT_MARGIN_X;
        int positiveTextY = panelY + POSITIVE_TEXT_Y_OFFSET;
        int negativeTextY = panelY + NEGATIVE_TEXT_Y_OFFSET;

        // Render the positive text (top box)
        guiGraphics.textWithWordWrap(font, Component.literal(choice.positive()), textX, positiveTextY, TEXT_WRAP_WIDTH, textColor);

        // Render the negative text (bottom box)
        guiGraphics.textWithWordWrap(font, Component.literal(choice.negative()), textX, negativeTextY, TEXT_WRAP_WIDTH, textColor);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
