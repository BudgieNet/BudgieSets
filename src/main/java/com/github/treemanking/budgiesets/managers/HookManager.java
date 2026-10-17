package com.github.treemanking.budgiesets.managers;

import com.budgienet.chatutil.ChatUtil;
import org.bukkit.plugin.java.JavaPlugin;

import static com.budgienet.chatutil.ChatUtil.log;
import static com.budgienet.chatutil.ChatUtil.warn;

/**
 * The HookManager class is responsible for checking the availability of
 * required dependencies such as Paper and PlaceholderAPI and managing
 * hooks for these dependencies.
 */
public final class HookManager {

    private static boolean placeholderAPIEnabled = false;

    private HookManager() {
    }

    /**
     * Checks and manages hooks for required dependencies.
     *
     * @param plugin the JavaPlugin instance of the BudgieSets plugin
     */
    public static void checkHooks(JavaPlugin plugin) {
        setPlaceholderAPIEnabled(isPlaceholderAPIAvailable(plugin));
        isPaperEnabled(plugin);
    }

    /**
     * Checks if Paper is enabled by verifying the presence of a Paper-specific class.
     * Disables the plugin if Paper is not found.
     *
     * @param plugin the JavaPlugin instance of the BudgieSets plugin
     */
    public static void isPaperEnabled(JavaPlugin plugin) {
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
        } catch (ClassNotFoundException ignored) {
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    /**
     * Checks if PlaceholderAPI is available by verifying the presence of the PlaceholderAPIPlugin class.
     * Logs a warning if PlaceholderAPI is not found.
     *
     * @param plugin the JavaPlugin instance of the BudgieSets plugin
     * @return true if PlaceholderAPI is available, false otherwise
     */
    public static boolean isPlaceholderAPIAvailable(JavaPlugin plugin) {
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPIPlugin");
            log("PlaceholderAPI Hooked!");
            return true;
        } catch (ClassNotFoundException ignored) {
            warn("PlaceholderAPI is missing, conditions will not work!");
            return false;
        }
    }

    /**
     * Indicates whether PlaceholderAPI is enabled on the server.
     *
     * @return true if PlaceholderAPI is enabled, false otherwise
     */
    public static boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    /**
     * Sets the state indicating whether PlaceholderAPI is enabled.
     *
     * @param enabled true if PlaceholderAPI is enabled, false otherwise
     */
    public static void setPlaceholderAPIEnabled(boolean enabled) {
        placeholderAPIEnabled = enabled;
    }

}
