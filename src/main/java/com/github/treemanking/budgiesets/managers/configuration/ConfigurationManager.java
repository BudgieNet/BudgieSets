package com.github.treemanking.budgiesets.managers.configuration;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.managers.armorsets.ArmorSetFiles;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;

import static com.budgienet.chatutil.ChatUtil.*;

/**
 * The ConfigurationManager class handles the loading, saving, and initialization
 * of configuration files and directories for the BudgieSets plugin.
 */
public class ConfigurationManager {

    private final JavaPlugin plugin;

    /**
     * Constructs a ConfigurationManager for the given plugin.
     * It initializes the default configuration and creates the ArmorSets folder.
     *
     * @param plugin the JavaPlugin instance
     */
    public ConfigurationManager(JavaPlugin plugin) {
        this.plugin = plugin;
        initializeConfig();
        initializeArmorSetsFolder();
    }

    /**
     * Saves the default configuration file from the plugin's resources to the plugin's data folder.
     */
    private void initializeConfig() {
        plugin.saveDefaultConfig();
    }

    /**
     * Creates the ArmorSets folder within the plugin's data folder if it does not already exist.
     */
    private void initializeArmorSetsFolder() {
        File armorSetsFolder = ArmorSetFiles.folderIn(plugin);

        if (armorSetsFolder.mkdirs()) {
            log(ArmorSetFiles.FOLDER_NAME + " folder created.");
        }
    }

    /**
     * Retrieves a FileConfiguration for a specified file name.
     * If the file does not exist, it is copied from the plugin's resources.
     *
     * @param fileName the name of the configuration file
     * @return the FileConfiguration associated with the specified file
     */
    public FileConfiguration getConfig(String fileName) {
        File configFile = new File(plugin.getDataFolder(), ArmorSetFiles.withExtension(fileName));

        if (!configFile.exists()) {
            if (configFile.getName().equalsIgnoreCase("config.yml")) {
                plugin.saveResource(fileName, false);
            } else {
                warn(fileName + " does not exist.");
                return null;
            }

        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Saves the provided FileConfiguration to a specified file name within the plugin's data folder.
     *
     * @param config the FileConfiguration to save
     * @param fileName the name to save the configuration file as
     */
    public void saveConfig(FileConfiguration config, String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);

        try {
            config.save(configFile);
        } catch (IOException e) {
            error("Could not save configuration file: " + fileName);
        }
    }

    /**
     * Retrieves all .yml files within the ArmorSets folder.
     *
     * @return an array of Files in .yml format located in the ArmorSets folder
     */
    public File[] getArmorSetFiles() {
        File armorSetsFolder = ArmorSetFiles.folderIn(plugin);
        return armorSetsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(ArmorSetFiles.FILE_EXTENSION)
                && !name.toLowerCase().startsWith(ArmorSetFiles.UNLOADED_PREFIX));
    }
}
