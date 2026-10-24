package com.github.treemanking.budgiesets.utilities;

import com.github.treemanking.budgiesets.utilities.effects.AttributeService;
import com.github.treemanking.budgiesets.utilities.effects.PotionEffectService;

/**
 * Cleanup routines run when the plugin is disabled.
 *
 * <p>Replaces the former {@code OnPluginDisable} mixin interface, which had to be instantiated
 * anonymously ({@code new OnPluginDisable(){}}) to be usable.</p>
 */
public final class ShutdownTasks {

    private ShutdownTasks() {
        throw new AssertionError("ShutdownTasks is a utility class and must not be instantiated");
    }

    /**
     * Runs every shutdown task: strips plugin-granted effects and modifiers from online
     * players and releases the attribute scheduler.
     */
    public static void runAll() {
        removeAllPermPotionEffects();
        removeAllPlayersAttributes();
    }

    /**
     * Removes every plugin-granted permanent potion effect from every online player.
     */
    public static void removeAllPermPotionEffects() {
        PotionEffectService.removeAllTrackedEffects();
    }

    /**
     * Removes every plugin-granted attribute modifier from every online player.
     */
    public static void removeAllPlayersAttributes() {
        AttributeService.removeAllTrackedAttributes();
    }
}

