package com.github.treemanking.budgiesets.commands.subcommands;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.commands.SubCommand;
import com.github.treemanking.budgiesets.managers.armorsets.ArmorSetFiles;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.ChatColor;

import java.util.concurrent.CompletableFuture;

/**
 * Renames an armor set configuration file.
 */
public class RenameSubCommand implements SubCommand {

    private final ArmorSetFiles files;

    /**
     * Constructs the rename subcommand.
     *
     * @param files the armor set file helper
     */
    public RenameSubCommand(ArmorSetFiles files) {
        this.files = files;
    }

    @Override
    public CommandAPICommand build() {
        return new CommandAPICommand("rename")
                .withPermission("budgiesets.rename")
                .withArguments(new StringArgument("oldName")
                        .replaceSuggestions(ArgumentSuggestions.stringsAsync(
                                info -> CompletableFuture.supplyAsync(files::allNames))))
                .withArguments(new StringArgument("newName"))
                .executes((sender, args) -> {
                    String oldName = (String) args.get("oldName");
                    String newName = (String) args.get("newName");

                    if (files.exists(newName)) {
                        ChatUtil.sendMsg(sender, ChatColor.RED + newName + " already exists, so " + oldName
                                + " was not renamed.", false);
                        return;
                    }

                    if (files.rename(oldName, newName)) {
                        ChatUtil.sendMsg(sender, ChatColor.GREEN + oldName + " was renamed to " + newName + ".", false);
                        return;
                    }

                    ChatUtil.sendMsg(sender, ChatColor.RED + oldName + " could not be renamed.", false);
                });
    }
}

