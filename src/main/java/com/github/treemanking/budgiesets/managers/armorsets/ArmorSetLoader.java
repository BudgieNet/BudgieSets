package com.github.treemanking.budgiesets.managers.armorsets;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.managers.configuration.ConfigurationManager;
import com.github.treemanking.budgiesets.utilities.effects.PotionEffectService;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;

import java.util.Arrays;
import java.util.List;

import static com.budgienet.chatutil.ChatUtil.*;

/**
 * Handles the runtime lifecycle of armor sets: registering their listeners, unregistering
 * them again, and reloading the plugin's armor set state as a whole.
 */
public class ArmorSetLoader {

    private final BudgieSets plugin;
    private final ConfigurationManager configurationManager;
    private final ArmorSetFiles files;

    /**
     * Constructs an ArmorSetLoader.
     *
     * @param plugin               the BudgieSets plugin instance
     * @param configurationManager the configuration manager used to read armor set configs
     * @param files                the armor set file helper
     */
    public ArmorSetLoader(BudgieSets plugin, ConfigurationManager configurationManager, ArmorSetFiles files) {
        this.plugin = plugin;
        this.configurationManager = configurationManager;
        this.files = files;
    }

    /**
     * Loads an armor set, registering its listener and marking it as enabled.
     * <p>
     * A name carrying the unloaded prefix is renamed back to its real name first, so the
     * returned result reports the name the set is actually known by afterwards.
     *
     * @param armorSetName the armor set to load, with or without the unloaded prefix
     * @return the outcome of the load, along with the resolved armor set name
     */
    public LoadResult load(String armorSetName) {
        String resolvedName = armorSetName;

        if (isEnabled(resolvedName)) {
            warn(resolvedName + " has already been loaded. Loading terminated.");
            return new LoadResult(LoadStatus.ALREADY_LOADED, resolvedName);
        }

        if (resolvedName.startsWith(ArmorSetFiles.UNLOADED_PREFIX)) {
            String strippedName = resolvedName.substring(ArmorSetFiles.UNLOADED_PREFIX.length());
            if (files.rename(resolvedName, strippedName)) {
                resolvedName = strippedName;
            }
        }

        FileConfiguration armorSetConfig = configurationManager.getConfig(ArmorSetFiles.configPath(resolvedName));

        if (armorSetConfig == null) {
            return new LoadResult(LoadStatus.NOT_FOUND, resolvedName);
        }

        try {
            plugin.getServer().getPluginManager().registerEvents(
                    new ArmorSetListener(resolvedName, armorSetConfig, plugin), plugin);
            ArmorSetManager.addEnabledArmorSet(resolvedName);
            log(resolvedName + " has been loaded into the server.");
            return new LoadResult(LoadStatus.LOADED, resolvedName);
        } catch (Exception exception) {
            error(resolvedName + " did not register and ran into an error!");
            return new LoadResult(LoadStatus.ERROR, resolvedName);
        }
    }

    /**
     * Unloads an armor set, unregistering its listener and marking the file as disabled.
     * <p>
     * Note that this currently clears permanent potion effects for every player, not just
     * those granted by the set being unloaded.
     *
     * @param armorSetName the armor set to unload
     * @return the outcome of the unload
     */
    public UnloadStatus unload(String armorSetName) {
        if (!isEnabled(armorSetName)) {
            warn(armorSetName + " is not currently loaded. Unloading terminated.");
            return UnloadStatus.NOT_LOADED;
        }

        PotionEffectService.removeAllTrackedEffects();

        for (RegisteredListener registeredListener : HandlerList.getRegisteredListeners(plugin)) {
            if (registeredListener.getListener() instanceof ArmorSetListener armorSetListener
                    && armorSetListener.getArmorSetName().equals(armorSetName)) {
                HandlerList.unregisterAll(armorSetListener);
            }
        }

        ArmorSetManager.removeEnabledArmorSet(armorSetName);

        if (!files.rename(armorSetName, ArmorSetFiles.UNLOADED_PREFIX + armorSetName)) {
            error(armorSetName + " was unregistered but its file could not be renamed.");
            return UnloadStatus.RENAME_FAILED;
        }

        log(armorSetName + " has been unloaded from the server.");
        return UnloadStatus.UNLOADED;
    }

    /**
     * Reloads every armor set, discarding all currently registered listeners and enabled state
     * before re-registering from disk.
     */
    public void reload() {
        HandlerList.unregisterAll(plugin);
        ArmorSetManager.clearEnabledArmorSets();
        new ArmorSetManager(plugin, configurationManager);
        PotionEffectService.removeAllTrackedEffects();
    }

    /**
     * @param armorSetName the armor set name
     * @return {@code true} if the armor set is currently enabled
     */
    private boolean isEnabled(String armorSetName) {
        List<String> enabledArmorSets = Arrays.asList(ArmorSetManager.getEnabledArmorSets());
        return enabledArmorSets.contains(armorSetName);
    }

    /** The possible outcomes of loading an armor set. */
    public enum LoadStatus {
        /** The armor set was registered successfully. */
        LOADED,
        /** The armor set was already loaded. */
        ALREADY_LOADED,
        /** No configuration could be found for the armor set. */
        NOT_FOUND,
        /** The armor set failed to register. */
        ERROR
    }

    /** The possible outcomes of unloading an armor set. */
    public enum UnloadStatus {
        /** The armor set was unregistered and its file marked as disabled. */
        UNLOADED,
        /** The armor set was not loaded to begin with. */
        NOT_LOADED,
        /** The armor set was unregistered but its file could not be renamed. */
        RENAME_FAILED
    }

    /**
     * The outcome of a load attempt.
     *
     * @param status       the load status
     * @param armorSetName the name the armor set is known by after loading
     */
    public record LoadResult(LoadStatus status, String armorSetName) {}
}

