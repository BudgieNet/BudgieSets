package com.github.treemanking.budgiesets.hook;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.BudgieSets;
import org.bukkit.Bukkit;

/**
 * BudgieCore, if it's installed: set bonuses in budgie/sets/ (BudgieCore effects that run while a set is worn), the
 * set_bonus_active condition, and each bonus's name and description on its set's pieces. Without it this does nothing;
 * only {@link BudgieCoreHook} names BudgieCore's classes, and that class is loaded only after BudgieCore is found.
 */
public final class Core {
    private Core() {}

    public static void init(BudgieSets plugin) {
        if (!Bukkit.getPluginManager().isPluginEnabled("BudgieCore")) return;
        try {
            BudgieCoreHook.init(plugin);
            ChatUtil.log("BudgieCore found: set bonuses from budgie/sets/, set_bonus_active, and set bonus lines in tooltips.");
        } catch (LinkageError | RuntimeException e) {
            ChatUtil.warn("BudgieCore is installed but couldn't be hooked (" + e + "); running without it.");
        }
    }
}
