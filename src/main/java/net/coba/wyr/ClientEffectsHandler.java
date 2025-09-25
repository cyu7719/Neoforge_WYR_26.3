package net.coba.wyr;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class ClientEffectsHandler {

    private final Random random = new Random();
    private boolean isCurrentlyReplacingSound = false;
    private int shaderTimer = 0;
    private boolean shaderWasActive = false;
    private static final List<ResourceLocation> SHADERS = List.of(
            new ResourceLocation("shaders/post/notch.json"),
            new ResourceLocation("shaders/post/fxaa.json"),
            new ResourceLocation("shaders/post/art.json"),
            new ResourceLocation("shaders/post/bits.json"),
            new ResourceLocation("shaders/post/bumpy.json"),
            new ResourceLocation("shaders/post/creeper.json"),
            new ResourceLocation("shaders/post/deconverge.json"),
            new ResourceLocation("shaders/post/flip.json"),
            new ResourceLocation("shaders/post/green.json"),
            new ResourceLocation("shaders/post/invert.json"),
            new ResourceLocation("shaders/post/pencil.json"),
            new ResourceLocation("shaders/post/phosphor.json"),
            new ResourceLocation("shaders/post/sobel.json"),
            new ResourceLocation("shaders/post/spider.json"),
            new ResourceLocation("shaders/post/wobble.json")
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

        if (shaderIsActive) {
            shaderTimer++;
            if (shaderTimer >= 200) { // 10 seconds (20 ticks/sec * 10)
                shaderTimer = 0;
                // Apply a new random shader
                ResourceLocation randomShader = SHADERS.get(random.nextInt(SHADERS.size()));
                mc.gameRenderer.loadEffect(randomShader);
            }
        }

        // If the effect was just turned off, disable the shader
        if (shaderWasActive && !shaderIsActive) {
            mc.gameRenderer.shutdownEffect();
        }

        this.shaderWasActive = shaderIsActive;
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        if (ClientEffectCache.hasRandomSounds() && !this.isCurrentlyReplacingSound) {
            this.isCurrentlyReplacingSound = true;
            Minecraft mc = Minecraft.getInstance();

            if (mc.player != null) {
                List<Holder.Reference<SoundEvent>> allSounds = BuiltInRegistries.SOUND_EVENT.holders().toList();
                if (!allSounds.isEmpty()) {
                    SoundEvent randomSound = allSounds.get(random.nextInt(allSounds.size())).value();
                    var originalSound = event.getSound();

                    // --- FIX: Use the full constructor to create the new sound instance ---
                    var newSound = new SimpleSoundInstance(
                            randomSound.getLocation(),
                            originalSound.getSource(),
                            originalSound.getVolume(),
                            originalSound.getPitch(),
                            mc.player.getRandom(), // Use the player's random source
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
            this.isCurrentlyReplacingSound = false;
        }
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Post<LivingEntity, ?> event) {
        LivingEntity entity = event.getEntity();
        Minecraft mc = Minecraft.getInstance();

        // Don't render for players or if you can't see the entity
        if (mc.player == null || !mc.player.hasLineOfSight(entity) || entity instanceof Player) {
            return;
        }

        // Check the client-side cache to see if the effect is active
        if (ClientEffectCache.hasSeeMobHp()) {
            //  Get rendering components directly from the event ---
            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource bufferSource = event.getMultiBufferSource();
            Font font = mc.font;

            float health = entity.getHealth();
            float maxHealth = entity.getMaxHealth();
            Component hpComponent = Component.literal(String.format("%.1f / %.1f ❤", health, maxHealth));

            poseStack.pushPose();
            // Position the text above the entity's head
            poseStack.translate(0, entity.getBbHeight() + 0.5f, 0);
            poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            poseStack.scale(-0.025f, -0.025f, 0.025f);

            // --- FIX: Use the Font renderer to draw text in the world ---
            font.drawInBatch(
                    hpComponent,
                    -font.width(hpComponent) / 2.0F, // Center the text
                    0,
                    0xFFFFFF, // White text color
                    false, // No shadow
                    poseStack.last().pose(),
                    bufferSource,
                    Font.DisplayMode.SEE_THROUGH, // Render text through walls slightly
                    0x90000000, // Semi-transparent black background
                    event.getPackedLight() // Use the entity's lighting
            );

            poseStack.popPose();
        }
    }
}