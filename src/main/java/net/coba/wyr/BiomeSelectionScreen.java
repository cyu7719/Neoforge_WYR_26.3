package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BiomeSelectionScreen extends Screen {

    private List<ResourceKey<Biome>> allBiomes;
    private BiomeList biomeList;

    public BiomeSelectionScreen() {
        super(Component.literal("Choose Your Starting Biome"));
    }

    @Override
    protected void init() {
        if (this.minecraft != null && this.minecraft.level != null) {
            this.allBiomes = this.minecraft.level.registryAccess().lookupOrThrow(Registries.BIOME)
                    .listElements()
                    .filter(holder -> holder.is(BiomeTags.IS_OVERWORLD))
                    .map(holder -> holder.key())
                    .sorted(Comparator.comparing(key -> key.identifier()))
                    .collect(Collectors.toList());
        }

        this.biomeList = new BiomeList(this.minecraft, this.width, this.height - 38, 30, 20);
        this.addRenderableWidget(this.biomeList);
    }

    @Override
    public void onClose() {
        // This remains empty to prevent closing with ESC
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        this.biomeList.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    class BiomeList extends net.minecraft.client.gui.components.ContainerObjectSelectionList<BiomeList.Entry> {
        public BiomeList(Minecraft mc, int width, int height, int y, int itemHeight) {
            super(mc, width, height, y, itemHeight);
            if (BiomeSelectionScreen.this.allBiomes != null) {
                BiomeSelectionScreen.this.allBiomes.forEach(key -> this.addEntry(new Entry(key)));
            }
        }

        public class Entry extends net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry<Entry> {
            private final Button selectButton;

            public Entry(ResourceKey<Biome> biomeKey) {
                String biomeName = biomeKey.identifier().getNamespace() + ":" + biomeKey.identifier().getPath();
                this.selectButton = Button.builder(Component.literal(biomeName), (button) -> {
                    ClientPacketDistributor.sendToServer(new PacketChooseBiome(biomeKey.identifier()));

                    // --- THIS IS THE FIX ---
                    // Instead of calling the blocked onClose(), we directly close the screen.
                    Minecraft.getInstance().gui.setScreen(null);
                    // --- END OF FIX ---

                }).build();
            }

            @Override
            public void extractContent(final GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
                this.selectButton.setX(this.getX() + (this.getWidth() / 2) - 100);
                this.selectButton.setY(this.getY());
                this.selectButton.setWidth(200);
                this.selectButton.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
            }

            @Override
            public List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
                return List.of(this.selectButton);
            }

            @Override
            public List<? extends net.minecraft.client.gui.narration.NarratableEntry> narratables() {
                return List.of(this.selectButton);
            }
        }
    }
}
