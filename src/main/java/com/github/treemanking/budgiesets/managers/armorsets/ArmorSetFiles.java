package com.github.treemanking.budgiesets.managers.armorsets;

import com.budgienet.chatutil.ChatUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Predicate;

import static com.budgienet.chatutil.ChatUtil.*;

/**
 * The single source of truth for the location, naming and on-disk manipulation of
 * armor set configuration files.
 * <p>
 * Every other class that needs to know where armor sets live, or what an armor set
 * file is called, should go through this class rather than rebuilding the path itself.
 */
public class ArmorSetFiles {

    /** The name of the folder, inside the plugin data folder, that holds all armor set configurations. */
    public static final String FOLDER_NAME = "ArmorSets";

    /** The file extension used by armor set configurations. */
    public static final String FILE_EXTENSION = ".yml";

    /** The prefix applied to an armor set file to mark it as unloaded/disabled. */
    public static final String UNLOADED_PREFIX = "--";

    private final JavaPlugin plugin;
    private final File folder;

    /**
     * Constructs an ArmorSetFiles bound to the given plugin's ArmorSets folder.
     *
     * @param plugin the plugin whose data folder holds the ArmorSets folder
     */
    public ArmorSetFiles(JavaPlugin plugin) {
        this.plugin = plugin;
        this.folder = folderIn(plugin);
    }

    /**
     * Resolves the ArmorSets folder for the given plugin.
     *
     * @param plugin the plugin whose data folder should be used
     * @return the ArmorSets folder (which may not exist yet)
     */
    public static File folderIn(JavaPlugin plugin) {
        return new File(plugin.getDataFolder(), FOLDER_NAME);
    }

    /**
     * Builds the data-folder-relative configuration path for an armor set, as expected by
     * {@link com.github.treemanking.budgiesets.managers.configuration.ConfigurationManager#getConfig(String)}.
     *
     * @param armorSetName the armor set name, with or without the ".yml" extension
     * @return the path in the form "ArmorSets/name.yml"
     */
    public static String configPath(String armorSetName) {
        return FOLDER_NAME + "/" + withExtension(armorSetName);
    }

    /**
     * Ensures the given name carries the ".yml" extension.
     *
     * @param armorSetName the armor set name
     * @return the name guaranteed to end in ".yml"
     */
    public static String withExtension(String armorSetName) {
        return armorSetName.endsWith(FILE_EXTENSION) ? armorSetName : armorSetName + FILE_EXTENSION;
    }

    /**
     * Removes the ".yml" extension from a file name if present.
     *
     * @param fileName the file name
     * @return the name without its ".yml" extension
     */
    public static String stripExtension(String fileName) {
        return fileName.endsWith(FILE_EXTENSION)
                ? fileName.substring(0, fileName.length() - FILE_EXTENSION.length())
                : fileName;
    }

    /**
     * @return the ArmorSets folder backing this instance
     */
    public File folder() {
        return folder;
    }

    /**
     * Resolves the configuration file for a given armor set name.
     *
     * @param armorSetName the armor set name, with or without the ".yml" extension
     * @return the file for that armor set (which may not exist)
     */
    public File fileFor(String armorSetName) {
        return new File(folder, withExtension(armorSetName));
    }

    /**
     * @param armorSetName the armor set name
     * @return {@code true} if a configuration file exists for that armor set
     */
    public boolean exists(String armorSetName) {
        return fileFor(armorSetName).exists();
    }

    /**
     * @return the names of every armor set file, loaded or unloaded, without their extensions
     */
    public String[] allNames() {
        return names(name -> true);
    }

    /**
     * @return the names of armor set files that are not marked as unloaded
     */
    public String[] loadedNames() {
        return names(name -> !name.startsWith(UNLOADED_PREFIX));
    }

    /**
     * @return the names of armor set files that are marked as unloaded, including the "--" prefix
     */
    public String[] unloadedNames() {
        return names(name -> name.startsWith(UNLOADED_PREFIX));
    }

    /**
     * Lists armor set file names matching the given filter.
     *
     * @param filter a predicate applied to the lower-cased file name
     * @return the matching file names without their ".yml" extensions
     */
    private String[] names(Predicate<String> filter) {
        File[] files = folder.listFiles((dir, name) -> {
            String lowerCaseName = name.toLowerCase();
            return lowerCaseName.endsWith(FILE_EXTENSION) && filter.test(lowerCaseName);
        });

        if (files == null) {
            return new String[0];
        }

        return Arrays.stream(files)
                .map(file -> stripExtension(file.getName()))
                .toArray(String[]::new);
    }

    /**
     * Creates a new armor set configuration file with the given YAML content.
     * An existing file is never overwritten.
     *
     * @param armorSetName the name of the armor set
     * @param yamlContent  the YAML content to write
     * @return the outcome of the creation attempt
     * @throws IOException if the file could not be written
     */
    public CreateResult create(String armorSetName, String yamlContent) throws IOException {
        if (folder.mkdirs()) {
            log(FOLDER_NAME + " folder created.");
        }

        File file = fileFor(armorSetName);

        if (file.exists()) {
            warn("The file " + file.getName() + " already exists and will not be overwritten.");
            return CreateResult.ALREADY_EXISTS;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(yamlContent);
        } catch (IOException exception) {
            error("Failed to make " + armorSetName + " configuration.");
            throw exception;
        }

        return CreateResult.CREATED;
    }

    /**
     * Deletes the configuration file for the given armor set.
     *
     * @param armorSetName the name of the armor set to delete
     * @return the outcome of the deletion attempt
     */
    public DeleteResult delete(String armorSetName) {
        File file = fileFor(armorSetName);

        if (!file.exists()) {
            warn(armorSetName + " does not exist and therefore cannot be deleted.");
            return DeleteResult.NOT_FOUND;
        }

        if (!file.delete()) {
            error(armorSetName + " exists but could not be deleted.");
            return DeleteResult.FAILED;
        }

        log(armorSetName + " has been deleted.");
        return DeleteResult.DELETED;
    }

    /**
     * Renames an armor set configuration file.
     *
     * @param oldName the current armor set name (without the ".yml" extension)
     * @param newName the new armor set name (without the ".yml" extension)
     * @return {@code true} if the file was renamed, {@code false} if it was missing or the rename failed
     */
    public boolean rename(String oldName, String newName) {
        File oldFile = fileFor(oldName);
        File newFile = fileFor(newName);

        if (!oldFile.exists()) {
            return false;
        }

        return oldFile.renameTo(newFile);
    }

    /**
     * @return the default YAML content used when generating a brand new armor set
     */
    public static String defaultYaml() {
        return """
                # Please see https://github.com/TreemanKing/BudgieSets/wiki for events and effects
                Events:
                  - JUMP:
                      Chance: 0.5
                      Cooldown: 5
                      Effects:
                        - PARTICLE:
                          - Particle: REDSTONE
                            Count: 20
                            Offset: 1.0
                            Data:
                              Hex-Color: "#fcba03"
                              Size: 5.0""";
    }

    /** The possible outcomes of creating an armor set file. */
    public enum CreateResult {
        /** The file was written successfully. */
        CREATED,
        /** A file with that name already existed and was left untouched. */
        ALREADY_EXISTS
    }

    /** The possible outcomes of deleting an armor set file. */
    public enum DeleteResult {
        /** The file was deleted. */
        DELETED,
        /** No file existed with that name. */
        NOT_FOUND,
        /** The file existed but could not be deleted. */
        FAILED
    }
}

