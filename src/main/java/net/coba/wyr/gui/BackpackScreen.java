package net.coba.wyr.gui;

import net.coba.wyr.WYR;
import net.coba.wyr.inventory.BackpackMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class BackpackScreen extends AbstractContainerScreen<BackpackMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(WYR.MODID, "textures/gui/backpack.png");

    public BackpackScreen(BackpackMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, 176, 222);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.extractBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        
        // Draw backpack background
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        super.extractRenderState(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }
}