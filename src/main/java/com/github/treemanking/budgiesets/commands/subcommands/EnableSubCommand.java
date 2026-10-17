package com.github.treemanking.budgiesets.commands.subcommands;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.commands.SubCommand;
import com.github.treemanking.budgiesets.managers.armorsets.ArmorSetFiles;
import com.github.treemanking.budgiesets.managers.armorsets.ArmorSetLoader;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.ChatColor;

import java.util.concurrent.CompletableFuture;

/**
 * Loads a previously unloaded armor set back into the server.
 */
public class EnableSubCommand implements SubCommand {

    private final ArmorSetFiles files;
    private final ArmorSetLoader loader;

    /**
     * Constructs the enable subcommand.
     *
     * @param files  the armor set file helper
     * @param loader the armor set loader
     */
    public EnableSubCommand(ArmorSetFiles files, ArmorSetLoader loader) {
        this.files = files;
        this.loader = loader;
    }

    @Override
    public CommandAPICommand build() {
        return new CommandAPICommand("enableset")
                .withPermission("budgiesets.enableset")
                .withAliases("loadset", "enable", "load")
                .withArguments(new StringArgument("disabledSet")
                        .replaceSuggestions(ArgumentSuggestions.stringsAsync(
                                info -> CompletableFuture.supplyAsync(files::unloadedNames))))
                .executes((sender, args) -> {
                    String armorSetName = (String) args.get("disabledSet");
                    ArmorSetLoader.LoadResult result = loader.load(armorSetName);
                    String resolvedName = result.armorSetName();

                    switch (result.status()) {
                        case LOADED -> ChatUtil.sendMsg(sender, ChatColor.GREEN + resolvedName
                                + " has been loaded into the server.", false);
                        case ALREADY_LOADED -> ChatUtil.sendMsg(sender, ChatColor.RED + resolvedName
                                + " has already been loaded.", false);
                        case NOT_FOUND -> ChatUtil.sendMsg(sender, ChatColor.RED + resolvedName
                                + " does not exist and therefore cannot be loaded.", false);
                        case ERROR -> ChatUtil.sendMsg(sender, ChatColor.RED + resolvedName
                                + " failed to load. See the server console for details.", false);
                    }
                });
    }
}

