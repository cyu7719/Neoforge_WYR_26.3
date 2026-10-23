package net.coba.wyr;

import net.coba.wyr.WYR;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ShaderTestScreen extends Screen {
    private static final int COLUMNS = 4;
    private static final int BUTTON_W = 90;
    private static final int BUTTON_H = 20;
    private static final int SPACING_X = 100;
    private static final int SPACING_Y = 25;
    private static final int START_X = 20;
    private static final int START_Y = 40;

    static String manualShader = null;
    static boolean manualClear = false;
    static boolean manualMode = false;

    private final Map<String, Button> shaderButtons = new HashMap<>();

    public ShaderTestScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        int i = 0;
        for (String name : ClientTickHandler.SECRET_SHADERS) {
            Identifier id = ClientTickHandler.SHADER_IDS.get(name);
            if (id == null) {
                continue;
            }
            int col = i % COLUMNS;
            int row = i / COLUMNS;
            int x = START_X + col * SPACING_X;
            int y = START_Y + row * SPACING_Y;
            Button b = addRenderableWidget(Button.builder(Component.literal(name), (button) -> request(name))
                    .bounds(x, y, BUTTON_W, BUTTON_H)
                    .build());
            b.setOverrideRenderHighlightedSprite(() -> false);
            shaderButtons.put(name, b);
            i++;
        }

        int clearCol = i % COLUMNS;
        int clearRow = i / COLUMNS;
        int clearX = START_X + clearCol * SPACING_X;
        int clearY = START_Y + clearRow * SPACING_Y;
        addRenderableWidget(Button.builder(Component.literal("clear"), (button) -> {
                    WYR.LOGGER.info("[shader_test] clear clicked");
                    manualClear = true;
                    manualMode = false;
                    manualShader = null;
                    Minecraft.getInstance().gui.setScreen(null);
                })
                .bounds(clearX, clearY, BUTTON_W, BUTTON_H)
                .build());
        i++;

        int closeCol = i % COLUMNS;
        int closeRow = i / COLUMNS;
        int closeX = START_X + closeCol * SPACING_X;
        int closeY = START_Y + closeRow * SPACING_Y;
        addRenderableWidget(Button.builder(Component.literal("close"), (button) -> Minecraft.getInstance().gui.setScreen(null))
                .bounds(closeX, closeY, BUTTON_W, BUTTON_H)
                .build());
    }

    private void request(String name) {
        WYR.LOGGER.info("[shader_test] request clicked: {}", name);
        manualShader = name;
        manualMode = true;
        manualClear = false;
        Minecraft.getInstance().gui.setScreen(null);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.text(this.font, this.title, START_X, START_Y - 20, 0xFFFFFFFF, true);

        String active = ClientTickHandler.currentShaderName;
        if (active != null) {
            Button b = shaderButtons.get(active);
            if (b != null) {
                int bx = b.getX();
                int by = b.getY();
                int bw = b.getWidth();
                int bh = b.getHeight();
                int border = 2;
                int white = 0xFFFFFFFF;
                guiGraphics.fill(bx - border, by - border, bx + bw + border, by, white);
                guiGraphics.fill(bx - border, by + bh, bx + bw + border, by + bh + border, white);
                guiGraphics.fill(bx - border, by, bx, by + bh, white);
                guiGraphics.fill(bx + bw, by, bx + bw + border, by + bh, white);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
