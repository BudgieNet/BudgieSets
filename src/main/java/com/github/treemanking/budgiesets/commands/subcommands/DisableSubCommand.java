package com.github.treemanking.budgiesets.commands.subcommands;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.commands.SubCommand;
import com.github.treemanking.budgiesets.managers.armorsets.ArmorSetLoader;
import com.github.treemanking.budgiesets.managers.armorsets.ArmorSetManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.ChatColor;

import java.util.concurrent.CompletableFuture;

/**
 * Unloads a currently enabled armor set from the server.
 */
public class DisableSubCommand implements SubCommand {

    private final ArmorSetLoader loader;

    /**
     * Constructs the disable subcommand.
     *
     * @param loader the armor set loader
     */
    public DisableSubCommand(ArmorSetLoader loader) {
        this.loader = loader;
    }

    @Override
    public CommandAPICommand build() {
        return new CommandAPICommand("disableset")
                .withPermission("budgiesets.disableset")
                .withAliases("unloadset", "disable", "unload")
                .withArguments(new StringArgument("enabledSet")
                        .replaceSuggestions(ArgumentSuggestions.stringsAsync(
                                info -> CompletableFuture.supplyAsync(ArmorSetManager::getEnabledArmorSets))))
                .executes((sender, args) -> {
                    String armorSetName = (String) args.get("enabledSet");

                    switch (loader.unload(armorSetName)) {
                        case UNLOADED -> ChatUtil.sendMsg(sender, ChatColor.GREEN + armorSetName
                                + " has been unloaded from the server.", false);
                        case NOT_LOADED -> ChatUtil.sendMsg(sender, ChatColor.RED + armorSetName
                                + " is not currently loaded.", false);
                        case RENAME_FAILED -> ChatUtil.sendMsg(sender, ChatColor.RED + armorSetName
                                + " was unloaded but its file could not be renamed. See the server console for details.", false);
                    }
                });
    }
}

