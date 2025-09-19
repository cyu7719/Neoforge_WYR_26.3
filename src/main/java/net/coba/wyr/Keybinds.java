package net.coba.wyr;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

/**
 * This class defines all the custom keybindings for the mod.
 * A central place for keybinds makes them easier to manage and reference.
 */
public class Keybinds {
    // A unique category name for the keybinds in the Controls menu.
    public static final String KEY_CATEGORY_WYR = "Would You Rather";
    // The name for the teleport keybind itself.
    public static final String KEY_TELEPORT = "Teleport";
    public static final String KEY_PROMPT_LIST = "Prompt List";

    // The actual KeyMapping object. This is what Minecraft uses to track the key.
    // We set a default key (T) and specify it should only be active when in-game.
    public static final KeyMapping TELEPORT_KEY = new KeyMapping(
            KEY_TELEPORT,
            KeyConflictContext.IN_GAME,
            InputConstants.getKey(InputConstants.KEY_T, -1),
            KEY_CATEGORY_WYR
    );

    public static final KeyMapping PROMPT_LIST_KEY = new KeyMapping(
            KEY_PROMPT_LIST,
            KeyConflictContext.IN_GAME,
            InputConstants.getKey(InputConstants.KEY_K, -1), // Default to K
            KEY_CATEGORY_WYR
    );
}
