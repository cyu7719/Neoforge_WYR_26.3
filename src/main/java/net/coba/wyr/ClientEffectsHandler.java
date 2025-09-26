package net.coba.wyr;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry; // NEW IMPORT
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries; // NEW IMPORT
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

import java.util.ArrayList; // NEW IMPORT
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class ClientEffectsHandler {

    private final Random random = new Random();
    private boolean isCurrentlyReplacingSound = false;
    private int shaderTimer = 0;
    private boolean shaderWasActive = false;
    private static final List<ResourceLocation> SHADERS = List.of(
            ResourceLocation.fromNamespaceAndPath("minecraft", "notch"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "fxaa"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "art"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "bits"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "bumpy"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "creeper"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "deconverge"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "flip"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "green"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "invert"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "pencil"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "phosphor"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "sobel"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "spider"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "wobble")
    );

    @SubscribeEvent
    public void onMovementInputUpdate(MovementInputUpdateEvent event) {
        int state = ClientEffectCache.getScrambleState();
        if (state == 0) return; // 0 means normal controls

        float forward = event.getInput().forwardImpulse;
        float strafe = event.getInput().leftImpulse;

        switch (state) {
            case 1: // Forward/Back swapped
                event.getInput().forwardImpulse = -forward;
                break;
            case 2: // Left/Right swapped
                event.getInput().leftImpulse = -strafe;
                break;
            case 3: // Forward is Left, Left is Back, etc. (Rotated 90 degrees)
                event.getInput().forwardImpulse = strafe;
                event.getInput().leftImpulse = -forward;
                break;
            case 4: // Forward is Right, Right is Back, etc. (Rotated -90 degrees)
                event.getInput().forwardImpulse = -strafe;
                event.getInput().leftImpulse = forward;
                break;
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        boolean shaderIsActive = ClientEffectCache.hasRandomShader();

        if (ClientEffectCache.hasLowRenderDistance()) {
            // Forcefully set render distance to 5 if it's anything else
            if (mc.options.renderDistance().get() != 5) {
                mc.options.renderDistance().set(5);
            }
        }

        if (shaderIsActive) {
            shaderTimer++;
            if (shaderTimer >= 200) { // 10 seconds (20 ticks/sec * 10)
                shaderTimer = 0;
                ResourceLocation randomShader = SHADERS.get(random.nextInt(SHADERS.size()));
                mc.gameRenderer.loadEffect(randomShader);
            }
        }

        if (shaderWasActive && !shaderIsActive) {
            mc.gameRenderer.shutdownEffect();
        }

        this.shaderWasActive = shaderIsActive;
    }

    @SubscribeEvent
    public void onComputeFov(ComputeFovModifierEvent event) {
        if (ClientEffectCache.hasFovChange()) {
            Player player = event.getPlayer();
            int selectedSlot = player.getInventory().selected; // 0-8
            // Base FOV is 1.0F. We add a modifier based on the slot.
            // Slot 0 = +0.0 (normal), Slot 8 = +0.4 (very zoomed out)
            float fovModifier = 1.0F + (selectedSlot * 0.05F);
            event.setNewFovModifier(fovModifier);
        }
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        if (ClientEffectCache.hasRandomSounds() && !this.isCurrentlyReplacingSound) {
            this.isCurrentlyReplacingSound = true;
            Minecraft mc = Minecraft.getInstance();

            if (ClientEffectCache.isMuted()) {
                event.setSound(null);
                return;
            }

            if (mc.player != null && mc.level != null) {
                var originalSound = event.getSound();

                if (originalSound != null && originalSound.getSound() != null) {
                    // --- FIX: Get the complete sound list from the dynamic registry ---
                    Registry<SoundEvent> soundRegistry = mc.level.registryAccess().registryOrThrow(Registries.SOUND_EVENT);
                    List<SoundEvent> allSounds = new ArrayList<>();
                    soundRegistry.iterator().forEachRemaining(allSounds::add);
                    // --- END FIX ---

                    if (!allSounds.isEmpty()) {
                        SoundEvent randomSound = allSounds.get(random.nextInt(allSounds.size()));

                        var newSound = new SimpleSoundInstance(
                                randomSound.getLocation(),
                                originalSound.getSource(),
                                originalSound.getVolume(),
                                originalSound.getPitch(),
                                mc.player.getRandom(),
                                originalSound.isLooping(),
                                originalSound.getDelay(),
                                originalSound.getAttenuation(),
                                originalSound.getX(),
                                originalSound.getY(),
                                originalSound.getZ(),
                                originalSound.isRelative()
                        );
                        event.setSound(newSound);
                    }
                }
            }
            this.isCurrentlyReplacingSound = false;
        }
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Post<LivingEntity, ?> event) {
        LivingEntity entity = event.getEntity();
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || !mc.player.hasLineOfSight(entity) || entity instanceof Player) {
            return;
        }

        if (ClientEffectCache.hasSeeMobHp()) {
            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource bufferSource = event.getMultiBufferSource();
            Font font = mc.font;

            float health = entity.getHealth();
            float maxHealth = entity.getMaxHealth();
            Component hpComponent = Component.literal(String.format("%.1f / %.1f ❤", health, maxHealth));

            poseStack.pushPose();
            poseStack.translate(0, entity.getBbHeight() + 0.5f, 0);
            poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            poseStack.scale(-0.025f, -0.025f, 0.025f);

            font.drawInBatch(
                    hpComponent,
                    -font.width(hpComponent) / 2.0F,
                    0,
                    0xFFFFFF,
                    false,
                    poseStack.last().pose(),
                    bufferSource,
                    Font.DisplayMode.SEE_THROUGH,
                    0x90000000,
                    event.getPackedLight()
            );

            poseStack.popPose();
        }
    }
}