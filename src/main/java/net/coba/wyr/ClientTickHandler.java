package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class ClientTickHandler {

    private int shaderTickCounter = 0;
    private final Random random = new Random();
    static String currentShaderName = null;

    // SSS-style shaders that have compatible post effects in 1.21.7.
    static final List<String> SECRET_SHADERS = List.of(
            "bits", "creeper", "desaturate", "green",
            "spider", "invert", "blur", "sobel", "outline",
            "deconverge", "blobs", "blobs2", "wobble", "ntsc", "art"
    );

    static final Map<String, Identifier> SHADER_IDS = Map.ofEntries(
            Map.entry("bits", Identifier.fromNamespaceAndPath(WYR.MODID, "bits")),
            Map.entry("creeper", Identifier.tryParse("minecraft:creeper")),
            Map.entry("desaturate", Identifier.fromNamespaceAndPath(WYR.MODID, "desaturate")),
            Map.entry("green", Identifier.fromNamespaceAndPath(WYR.MODID, "green")),
            Map.entry("spider", Identifier.tryParse("minecraft:spider")),
            Map.entry("invert", Identifier.tryParse("minecraft:invert")),
            Map.entry("blur", Identifier.tryParse("minecraft:blur")),
            Map.entry("sobel", Identifier.fromNamespaceAndPath(WYR.MODID, "sobel")),
            Map.entry("outline", Identifier.fromNamespaceAndPath(WYR.MODID, "outline")),
            Map.entry("deconverge", Identifier.fromNamespaceAndPath(WYR.MODID, "deconverge")),
            Map.entry("blobs", Identifier.fromNamespaceAndPath(WYR.MODID, "blobs")),
            Map.entry("blobs2", Identifier.fromNamespaceAndPath(WYR.MODID, "blobs2")),
            Map.entry("wobble", Identifier.fromNamespaceAndPath(WYR.MODID, "wobble")),
            Map.entry("ntsc", Identifier.fromNamespaceAndPath(WYR.MODID, "ntsc")),
            Map.entry("art", Identifier.fromNamespaceAndPath(WYR.MODID, "art"))
    );

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        var gameRenderer = Minecraft.getInstance().gameRenderer;

        if (ShaderTestScreen.manualClear) {
            ShaderTestScreen.manualClear = false;
            ShaderTestScreen.manualMode = false;
            gameRenderer.getRequestedPostEffects().clear();
            currentShaderName = null;
            return;
        }
        if (ShaderTestScreen.manualShader != null) {
            String shaderName = ShaderTestScreen.manualShader;
            ShaderTestScreen.manualShader = null;
            ShaderTestScreen.manualMode = true;
            currentShaderName = shaderName;
            Identifier shaderId = SHADER_IDS.get(shaderName);
            if (shaderId != null) {
                gameRenderer.getRequestedPostEffects().clear(); gameRenderer.getRequestedPostEffects().add(shaderId);
                WYR.LOGGER.info("[shader_test] applied {}", shaderName);
            }
            return;
        }
        if (ShaderTestScreen.manualMode) {
            return;
        }

        if (ClientEffectCache.hasRandomShader()) {
            ClientEffectCache.setShaderTimerSeconds(Math.max(0, (200 - shaderTickCounter) / 20));
            shaderTickCounter++;
            if (shaderTickCounter >= 200) {
                shaderTickCounter = 0;

                String shaderName;
                do {
                    shaderName = SECRET_SHADERS.get(random.nextInt(SECRET_SHADERS.size()));
                } while (shaderName.equals(currentShaderName));
                currentShaderName = shaderName;
                WYR.LOGGER.info("[no_hunger_random_shader] switching to: {}", shaderName);

                Identifier shaderId = SHADER_IDS.get(shaderName);
                if (shaderId != null) {
                    gameRenderer.getRequestedPostEffects().clear(); gameRenderer.getRequestedPostEffects().add(shaderId);
                }
            }
        } else {
            ClientEffectCache.setShaderTimerSeconds(-1);
            if (!gameRenderer.getAppliedPostEffects().isEmpty()) {
                gameRenderer.getRequestedPostEffects().clear();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!ClientEffectCache.REGISTRIES_READY) {
            WYR.LOGGER.info("Client has logged in. Registries are now ready.");
            ClientEffectCache.REGISTRIES_READY = true;
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientEffectCache.REGISTRIES_READY = false;
        var gameRenderer = Minecraft.getInstance().gameRenderer;
        ClientEffectCache.setHasRandomSounds(false);
        ClientEffectCache.setHasItemTextureScramble(false);
        ClientEffectCache.setHasTextureScramble(false);
        ClientEffectCache.setShaderTimerSeconds(-1);

        if (gameRenderer != null && !gameRenderer.getAppliedPostEffects().isEmpty()) {
            gameRenderer.getRequestedPostEffects().clear();
        }
        shaderTickCounter = 0;
    }
}