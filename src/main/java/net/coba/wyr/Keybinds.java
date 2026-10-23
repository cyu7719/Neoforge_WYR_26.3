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
    public static final String KEY_CATEGORY_WYR = "key.categories.wouldyourather";
    // The name for the teleport keybind itself.
    public static final String KEY_TELEPORT = "Teleport";
    public static final String KEY_PROMPT_LIST = "Prompt List";
    public static final String KEY_OPEN_BACKPACK = "Open Backpack";

    // The actual KeyMapping object. This is what Minecraft uses to track the key.
    public static final KeyMapping TELEPORT_KEY = new KeyMapping(
            "key.wouldyourather.teleport",
            InputConstants.KEY_G,
            KeyMapping.Category.MISC
    );

    public static final KeyMapping PROMPT_LIST_KEY = new KeyMapping(
            "key.wouldyourather.prompt_list",
            InputConstants.KEY_K,
            KeyMapping.Category.MISC
    );

    public static final KeyMapping OPEN_BACKPACK_KEY = new KeyMapping(
            "key.wouldyourather.open_backpack",
            InputConstants.KEY_B,
            KeyMapping.Category.MISC
    );
}
