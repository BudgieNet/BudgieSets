package com.github.treemanking.budgiesets.commands.subcommands;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.commands.SubCommand;
import com.github.treemanking.budgiesets.managers.armorsets.ArmorSetFiles;
import com.github.treemanking.budgiesets.managers.armorsets.ArmorSetLoader;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.ChatColor;

import java.io.IOException;

/**
 * Creates a new armor set configuration and immediately loads it.
 */
public class CreateSubCommand implements SubCommand {

    private final ArmorSetFiles files;
    private final ArmorSetLoader loader;

    /**
     * Constructs the create subcommand.
     *
     * @param files  the armor set file helper
     * @param loader the armor set loader
     */
    public CreateSubCommand(ArmorSetFiles files, ArmorSetLoader loader) {
        this.files = files;
        this.loader = loader;
    }

    @Override
    public CommandAPICommand build() {
        return new CommandAPICommand("create")
                .withArguments(new StringArgument("name"))
                .withPermission("budgiesets.create")
                .executes((sender, args) -> {
                    String armorSetName = (String) args.get("name");

                    try {
                        switch (files.create(armorSetName, ArmorSetFiles.defaultYaml())) {
                            case CREATED -> {
                                loader.load(armorSetName);
                                ChatUtil.sendMsg(sender, ChatColor.GREEN + armorSetName + " was successfully generated and loaded.", false);
                            }
                            case ALREADY_EXISTS -> ChatUtil.sendMsg(sender, ChatColor.RED + armorSetName
                                    + " already exists and was not overwritten.", false);
                        }
                    } catch (IOException exception) {
                        ChatUtil.sendMsg(sender, ChatColor.RED + "Failed to create " + armorSetName
                                + ". See the server console for details.", false);
                    }
                });
    }
}

