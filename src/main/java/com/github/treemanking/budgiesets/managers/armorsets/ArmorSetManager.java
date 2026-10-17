package com.github.treemanking.budgiesets.managers.armorsets;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.managers.configuration.ConfigurationManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.*;

import static com.budgienet.chatutil.ChatUtil.*;

/**
 * The ArmorSetManager class manages the registration of ArmorSetListeners
 * for each armor set configuration file found in the ArmorSets folder.
 */
public class ArmorSetManager {

    public static List<String> enabledArmorSets = new ArrayList<>();
    private final BudgieSets plugin;
    private final ConfigurationManager configurationManager;

    /**
     * Constructs an ArmorSetManager for the given plugin and configuration manager.
     * It registers listeners for all armor sets defined in the configuration files.
     *
     * @param plugin the BudgieSets plugin instance
     * @param configurationManager the ConfigurationManager instance
     */
    public ArmorSetManager(BudgieSets plugin, ConfigurationManager configurationManager) {
        this.plugin = plugin;
        this.configurationManager = configurationManager;
        registerArmorSetListeners();
    }

    /**
     * Registers ArmorSetListeners for each armor set configuration file found in the ArmorSets folder.
     */
    private void registerArmorSetListeners() {
        File[] armorSetFiles = configurationManager.getArmorSetFiles();

        for (File configFile : armorSetFiles) {
            if (configFile.getName().startsWith(ArmorSetFiles.UNLOADED_PREFIX)) continue;
            FileConfiguration armorSetConfig = configurationManager.getConfig(ArmorSetFiles.configPath(configFile.getName()));
            String armorSetName = ArmorSetFiles.stripExtension(configFile.getName());

            registerArmorSetListener(armorSetName, armorSetConfig);
        }
    }

    /**
     * Registers an ArmorSetListener for a specific armor set.
     *
     * @param armorSetName the name of the armor set
     * @param armorSetConfig the configuration file for the armor set
     */
    private void registerArmorSetListener(String armorSetName, FileConfiguration armorSetConfig) {
        if (armorSetName == null || armorSetConfig == null) {
            return;
        } try {
            plugin.getServer().getPluginManager().registerEvents(
                    new ArmorSetListener(armorSetName, armorSetConfig, plugin), plugin);
            log(armorSetName + " Registered");
            enabledArmorSets.add(armorSetName);
        } catch (Exception exception) {
            error(armorSetName + " did not register and ran into an error!");
        }
    }

    public static String[] getEnabledArmorSets() {
        return enabledArmorSets.toArray(new String[0]);
    }

    public static void addEnabledArmorSet(String armorSetName) {
        enabledArmorSets.add(armorSetName);
    }

    public static void removeEnabledArmorSet(String armorSetName) {
        enabledArmorSets.remove(armorSetName);
    }

    /**
     * Clears every tracked enabled armor set, so that a reload can rebuild the list from disk
     * without duplicating entries.
     */
    public static void clearEnabledArmorSets() {
        enabledArmorSets.clear();
    }
}
