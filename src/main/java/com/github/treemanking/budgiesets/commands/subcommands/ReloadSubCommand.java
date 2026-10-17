package com.github.treemanking.budgiesets.commands.subcommands;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.commands.SubCommand;
import com.github.treemanking.budgiesets.managers.armorsets.ArmorSetLoader;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.ChatColor;

/**
 * Reloads every armor set from disk.
 */
public class ReloadSubCommand implements SubCommand {

    private final ArmorSetLoader loader;

    /**
     * Constructs the reload subcommand.
     *
     * @param loader the armor set loader
     */
    public ReloadSubCommand(ArmorSetLoader loader) {
        this.loader = loader;
    }

    @Override
    public CommandAPICommand build() {
        return new CommandAPICommand("reload")
                .withPermission("budgiesets.reload")
                .executes((sender, args) -> {
                    loader.reload();
                    ChatUtil.sendMsg(sender, ChatColor.GREEN + "BudgieSets was successfully reloaded.", false);
                });
    }
}

