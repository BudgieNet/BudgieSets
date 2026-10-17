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
 * Deletes an armor set configuration file.
 */
public class RemoveSubCommand implements SubCommand {

    private final ArmorSetFiles files;

    /**
     * Constructs the remove subcommand.
     *
     * @param files the armor set file helper
     */
    public RemoveSubCommand(ArmorSetFiles files) {
        this.files = files;
    }

    @Override
    public CommandAPICommand build() {
        return new CommandAPICommand("remove")
                .withArguments(new StringArgument("name")
                        .replaceSuggestions(ArgumentSuggestions.stringsAsync(
                                info -> CompletableFuture.supplyAsync(files::allNames))))
                .withPermission("budgiesets.remove")
                .withAliases("delete", "yeet")
                .executes((sender, args) -> {
                    String armorSetName = (String) args.get("name");

                    switch (files.delete(armorSetName)) {
                        case DELETED -> ChatUtil.sendMsg(sender, ChatColor.GREEN + armorSetName + " was successfully removed.", false);
                        case NOT_FOUND -> ChatUtil.sendMsg(sender, ChatColor.RED + armorSetName
                                + " does not exist and therefore cannot be deleted.", false);
                        case FAILED -> ChatUtil.sendMsg(sender, ChatColor.RED + armorSetName
                                + " exists but could not be deleted. See the server console for details.", false);
                    }
                });
    }
}

