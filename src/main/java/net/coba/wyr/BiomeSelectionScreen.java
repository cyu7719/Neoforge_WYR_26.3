package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.network.PacketDistributor;

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
            this.allBiomes = this.minecraft.level.registryAccess().registryOrThrow(Registries.BIOME).holders()
                    .map(holder -> holder.unwrapKey().orElseThrow())
                    .sorted(Comparator.comparing(ResourceKey::location))
                    .collect(Collectors.toList());
        }

        // The height is increased to fill the space where the button was
        this.biomeList = new BiomeList(this.minecraft, this.width, this.height - 38, 30, 20);
        this.addRenderableWidget(this.biomeList);

        // The "Cancel" button has been removed.
    }

    // --- NEW METHODS to lock the screen ---
    @Override
    public void onClose() {
        // Do nothing when onClose is called, preventing the screen from closing.
    }

    @Override
    public boolean shouldCloseOnEsc() {
        // Return false to disable closing the screen with the Escape key.
        return false;
    }
    // --- END OF NEW METHODS ---


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.biomeList.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 13, 0xFFFFFF);
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
                String biomeName = biomeKey.location().getNamespace() + ":" + biomeKey.location().getPath();
                this.selectButton = Button.builder(Component.literal(biomeName), (button) -> {
                    PacketDistributor.sendToServer(new PacketChooseBiome(biomeKey.location()));
                    // This will now call our new, empty onClose() method
                    BiomeSelectionScreen.this.onClose();
                }).build();
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovering, float partialTick) {
                this.selectButton.setX(left + (width / 2) - 100);
                this.selectButton.setY(top);
                this.selectButton.setWidth(200);
                this.selectButton.render(guiGraphics, mouseX, mouseY, partialTick);
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