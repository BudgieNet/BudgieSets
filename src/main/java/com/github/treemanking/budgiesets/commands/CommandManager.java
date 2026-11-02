package com.github.treemanking.budgiesets.commands;

import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.commands.subcommands.*;
import com.github.treemanking.budgiesets.managers.armorsets.ArmorSetFiles;
import com.github.treemanking.budgiesets.managers.armorsets.ArmorSetLoader;
import dev.jorel.commandapi.CommandAPICommand;

import java.util.List;

/**
 * Registers the "/budgiesets" command and all of its subcommands.
 * <p>
 * Subcommands are built from a list of {@link SubCommand} implementations, so adding a new
 * one only requires adding it to {@link #subCommands(ArmorSetFiles, ArmorSetLoader)}.
 */
public class CommandManager {

    // TODO: Add help messages to each sub command

    /**
     * Constructs the command manager and registers the plugin's commands.
     *
     * @param plugin the BudgieSets plugin instance
     */
    public CommandManager(BudgieSets plugin) {
        ArmorSetFiles files = new ArmorSetFiles(plugin);
        ArmorSetLoader loader = new ArmorSetLoader(plugin, BudgieSets.getConfigurationManager(), files);

        CommandAPICommand budgieSetsCommand = new CommandAPICommand("budgiesets");

        for (SubCommand subCommand : subCommands(files, loader)) {
            budgieSetsCommand.withAliases("bsets").withSubcommand(subCommand.build());
        }

        budgieSetsCommand.register(plugin);
    }

    /**
     * Builds every subcommand belonging to "/budgiesets".
     *
     * @param files  the armor set file helper
     * @param loader the armor set loader
     * @return the subcommands to register
     */
    private List<SubCommand> subCommands(ArmorSetFiles files, ArmorSetLoader loader) {
        return List.of(
                new CreateSubCommand(files, loader),
                new RemoveSubCommand(files),
                new ReloadSubCommand(loader),
                new RenameSubCommand(files),
                new EnableSubCommand(files, loader),
                new DisableSubCommand(loader)
        );
    }
}
