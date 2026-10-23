package net.coba.wyr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.*;

// A simple client-side class to store the state of server-side effects.
public class ClientEffectCache {
    private static boolean hasStackedLogs = false;
    private static boolean hasSeeMobHp = false;
    private static int scrambleState = 0;
    private static boolean hasRandomSounds = false;
    private static boolean hasRandomShader = false;
    private static boolean isMuted = false;
    private static final Map<SoundEvent, SoundEvent> SOUND_MAP = new HashMap<>();
    private static List<SoundEvent> allSoundEvents = null;
    private static final Random RANDOM = new Random();

    // --- FOV CHANGE FIELDS ---
    private static boolean hasFovChange = false;
    private static int originalFov = -1;

    // --- RENDER DISTANCE FIELDS ---
    private static boolean hasLowRenderDistance = false;
    private static int originalRenderDistance = -1;
    private static boolean hasWaterWalk = false;
    private static List<Block> allGameBlocks = null;
    private static boolean hasNoJump = false;
    private static int foodTimerSeconds = -1;
    private static int shaderTimerSeconds = -1;
    private static boolean hasTextureScramble = false;
    private static final Map<BlockState, BlockState> BLOCK_STATE_MAP = new java.util.concurrent.ConcurrentHashMap<>();
    private static boolean hasStrengthII = false;
    private static boolean hasNoHungerBar = false;
    private static boolean hasHalfHunger = false;
    private static boolean isOreHighlightActive = false;
    private static boolean hasHighlightOres = false;

    private static boolean hasRandomCrosshair = false;
    private static long crosshairSeed = 0L;

    private static boolean hasItemTextureScramble = false;
    private static final Map<Item, Item> ITEM_MAP = new java.util.concurrent.ConcurrentHashMap<>();
    private static List<Item> allGameItems = null;

    public static boolean REGISTRIES_READY = false;

    @SubscribeEvent
    public void onTitleScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen) {
            if (!ClientEffectCache.REGISTRIES_READY) {
                WYR.LOGGER.info("Registries are now ready.");
                ClientEffectCache.REGISTRIES_READY = true;
            }
        }
    }

    private static boolean wyr$isExcludedSound(SoundEvent sound) {
        return BuiltInRegistries.SOUND_EVENT.getResourceKey(sound)
                .map(key -> {
                    String path = key.identifier().getPath();
                    // Exclude background music, jukebox records (music_disc.*),
                    // and looping sounds (e.g. ambient.underwater.loop,
                    // entity.minecart.inside) so they can't be swapped in.
                    return path.startsWith("music") || path.startsWith("records")
                            || path.contains("loop") || path.contains("inside");
                })
                .orElse(false);
    }

    public static SoundEvent getRandomizedSound(SoundEvent originalSound) {
        // Never randomize music, records, or looping sounds — pass them through.
        if (wyr$isExcludedSound(originalSound)) {
            return originalSound;
        }

        if (allSoundEvents == null) {
            // Pool excludes music/records/loops so a sound is never swapped
            // for one of those.
            allSoundEvents = BuiltInRegistries.SOUND_EVENT.stream()
                    .filter(sound -> !wyr$isExcludedSound(sound))
                    .toList();
        }

        if (allSoundEvents.isEmpty()) {
            return originalSound;
        }

        // Consistent mapping: each original sound always maps to the same
        // random replacement (e.g. chest-open always becomes an explosion).
        return SOUND_MAP.computeIfAbsent(originalSound,
                k -> allSoundEvents.get(RANDOM.nextInt(allSoundEvents.size())));
    }

    public static void setHasStackedLogs(boolean status) {
        hasStackedLogs = status;
    }

    public static boolean hasStackedLogs() {
        return hasStackedLogs;
    }

    public static void setHasSeeMobHp(boolean status) {
        hasSeeMobHp = status;
    }
    public static boolean hasSeeMobHp() {
        return hasSeeMobHp;
    }

    public static void setScrambleState(int state) {
        scrambleState = state;
    }
    public static int getScrambleState() {
        return scrambleState;
    }
    public static void setHasRandomSounds(boolean status) {
        hasRandomSounds = status;
        SOUND_MAP.clear();
    }

    public static void setFoodTimerSeconds(int seconds) {
        foodTimerSeconds = seconds;
    }
    public static int getFoodTimerSeconds() {
        return foodTimerSeconds;
    }

    public static void setShaderTimerSeconds(int seconds) {
        shaderTimerSeconds = seconds;
    }

    public static int getShaderTimerSeconds() {
        return shaderTimerSeconds;
    }

    public static boolean hasRandomCrosshair() {
        return hasRandomCrosshair;
    }

    public static void setHasRandomCrosshair(boolean status) {
        hasRandomCrosshair = status;
    }

    public static long getCrosshairSeed() {
        return crosshairSeed;
    }

    public static void setCrosshairSeed(long seed) {
        crosshairSeed = seed;
    }
    public static boolean hasRandomSounds() {
        return hasRandomSounds;
    }
    public static void setHasRandomShader(boolean status) {
        hasRandomShader = status;
    }
    public static boolean hasRandomShader() {
        return hasRandomShader;
    }
    public static void setHasWaterWalk(boolean status) { // Add this method
        hasWaterWalk = status;
    }
    public static boolean hasWaterWalk() { // Add this method
        return hasWaterWalk;
    }
    public static void setHasNoJump(boolean status) { // Add this method
        hasNoJump = status;
    }
    public static boolean hasNoJump() { // Add this method
        return hasNoJump;
    }

    public static boolean isOreHighlightActive() {
        return isOreHighlightActive;
    }

    public static void toggleOreHighlight() {

        isOreHighlightActive = !isOreHighlightActive;
        WYR.LOGGER.info("[Highlight Ores] Highlighting toggled to: {}", isOreHighlightActive);
    }

    public static void setHasNoHungerBar(boolean status) {
        hasNoHungerBar = status;
    }

    public static boolean hasNoHungerBar() {
        return hasNoHungerBar;
    }

    public static boolean hasHighlightOres() {
        return hasHighlightOres;
    }

    public static void setHasHighlightOres(boolean status) {
        hasHighlightOres = status;
        // If the effect is turned off, also disable the highlight vision
        if (!status) {
            isOreHighlightActive = false;
        }
    }

    public static void setHasTextureScramble(boolean status) {
        if (hasTextureScramble != status) {
            hasTextureScramble = status;

            if (!status) {
                BLOCK_STATE_MAP.clear();
            }

            // Force a full chunk rebuild so the scramble applies immediately
            // when enabled and reverts to default textures when removed.
            // This render pipeline has no public "mark all sections dirty" API:
            // clearVisibleSections() only clears the visibility queue, and both
            // invalidateCompiledGeometry() and resetLevelRenderData() dispose the
            // section dispatcher/view area outright (leaving a void world).
            // reloadResourcePacks() (the F3+T path) recreates the block model set
            // — re-running our scramble mixin — and rebuilds all chunk geometry.
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                mc.reloadResourcePacks();
            }
        }
    }

    public static boolean hasTextureScramble() {
        return hasTextureScramble;
    }

    public static boolean hasItemTextureScramble() {
        return hasItemTextureScramble;
    }

    public static void setHasItemTextureScramble(boolean status) {
        if (hasItemTextureScramble != status) {
            hasItemTextureScramble = status;
            ITEM_MAP.clear();
        }
    }

    public static Item getRandomizedItem(Item originalItem) {
        if (!REGISTRIES_READY) {
            return originalItem;
        }

        return ITEM_MAP.computeIfAbsent(originalItem, item -> {
            if (allGameItems == null) {
                allGameItems = BuiltInRegistries.ITEM.stream()
                        .filter(i -> i != Items.AIR)
                        .collect(Collectors.toList());
            }
            if (allGameItems.isEmpty()) {
                return item;
            }
            return allGameItems.get(RANDOM.nextInt(allGameItems.size()));
        });
    }
    public static void setHasStrengthII(boolean status) {
        hasStrengthII = status;
    }

    public static boolean hasStrengthII() {
        return hasStrengthII;
    }

    public static void setHasFovChange(boolean status) {
        if (status && !hasFovChange) {
            originalFov = Minecraft.getInstance().options.fov().get();
        } else if (!status && hasFovChange) {
            if (originalFov != -1) {
                Minecraft.getInstance().options.fov().set(originalFov);
                Minecraft.getInstance().options.save();
                originalFov = -1;
            }
        }
        hasFovChange = status;
    }

    public static void setHasHalfHunger(boolean status) {
        hasHalfHunger = status;
    }
    public static boolean hasHalfHunger() {
        return hasHalfHunger;
    }

    public static boolean hasFovChange() { return hasFovChange; }

    public static BlockState getRandomizedBlockState(BlockState originalState) {
        if (!REGISTRIES_READY) {
            return originalState;
        }

        return BLOCK_STATE_MAP.computeIfAbsent(originalState, state -> {
            if (allGameBlocks == null) {
                // Only pick full-cube opaque blocks so a block keeps a normal
                // cube shape and just gets a different texture (e.g. dirt ->
                // sand), instead of turning into stairs/fences/etc.
                allGameBlocks = BuiltInRegistries.BLOCK.stream()
                        .filter(block -> block != Blocks.AIR)
                        .filter(block -> {
                            BlockState def = block.defaultBlockState();
                            return def.isSolidRender() && def.getRenderShape() == RenderShape.MODEL;
                        })
                        .collect(Collectors.toList());
                // Fallback: if the solid-render filter produced nothing (e.g.
                // the method behaves differently in this version), fall back
                // to any non-air block with a normal model so the scramble
                // still works instead of silently returning the original.
                if (allGameBlocks.isEmpty()) {
                    allGameBlocks = BuiltInRegistries.BLOCK.stream()
                            .filter(block -> block != Blocks.AIR)
                            .filter(block -> block.defaultBlockState().getRenderShape() == RenderShape.MODEL)
                            .collect(Collectors.toList());
                }
            }
            if (allGameBlocks.isEmpty()) {
                return state;
            }
            // Get a random full-cube block state
            Block randomBlock = allGameBlocks.get(RANDOM.nextInt(allGameBlocks.size()));
            return randomBlock.defaultBlockState();
        });
    }


    public static void setHasLowRenderDistance(boolean status) {
        Minecraft mc = Minecraft.getInstance();
        if (status && !hasLowRenderDistance) {
            // Capturing the option value is safe even before the level loads.
            originalRenderDistance = mc.options.renderDistance().get();
        }
        // Defer heavy render ops until the level/levelRenderer is fully initialized.
        // Calling renderDistance().set() or clearVisibleSections() during world load
        // can deadlock the render thread (needsUpdate() waits on the chunk compile queue).
        // The per-tick enforcement in ClientSideChoiceEffects will apply the
        // render distance once the level is ready.
        if (mc.level == null) {
            hasLowRenderDistance = status;
            return;
        }
        if (status && !hasLowRenderDistance) {
            mc.options.renderDistance().set(5);
            mc.levelRenderer.clearVisibleSections();
        } else if (!status && hasLowRenderDistance) {
            if (originalRenderDistance != -1) {
                mc.options.renderDistance().set(originalRenderDistance);
                mc.levelRenderer.clearVisibleSections();
                originalRenderDistance = -1;
            }
        }
        hasLowRenderDistance = status;
    }

    public static boolean hasLowRenderDistance() { return hasLowRenderDistance; }

    public static void setMuted(boolean status) {
        isMuted = status;
    }
    public static boolean isMuted() {
        return isMuted;
    }
    private static Set<String> activeEffectsSet = new HashSet<>();

    public static void syncEffects(Set<String> effects) {
        activeEffectsSet = effects;
    }

    public static boolean hasEffect(String effectId) {
        if (activeEffectsSet.contains(effectId)) {
            return true;
        }
        return switch (effectId) {
            case "random_sounds", "hero_of_village_random_sounds" -> hasRandomSounds;
            case "water_walk" -> hasWaterWalk;
            case "see_mob_hp_double_health" -> hasSeeMobHp();
            case "cheap_trades_half_hunger" -> hasHalfHunger;
            case "fast_swim_muted" -> isMuted;
            case "Highlight_ores_no_ladders" -> hasHighlightOres;
            case "wool_stack_random_crosshair" -> hasRandomCrosshair;
            default -> false;
        };
    }
}
