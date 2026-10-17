package com.github.treemanking.budgiesets.effects.processors;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.utilities.EquipStatus;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.effects.PlayerEffectProcessor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static com.budgienet.chatutil.ChatUtil.warn;
import static com.github.treemanking.budgiesets.utilities.ConfigUtils.getConfigValue;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.AMOUNT_KEY;

/**
 * The ExpLevelProcessor class processes experience level effects for players based on their armor equip status.
 */
public class ExpLevelProcessor implements PlayerEffectProcessor {

    /**
     * Processes experience level effects based on the provided configuration.
     *
     * @param expLevels   A list of experience level configurations.
     * @param player      The player to apply the effects to.
     * @param equipStatus The equip status of the player's armor.
     * @param event       The event triggering the effect.
     */
    @Override
    public void processEffect(List<?> expLevels, Player player, EquipStatus equipStatus, Event event) {
        for (Object expLevel : expLevels) {
            if (expLevel instanceof Map<?, ?> expLevelMap) {
                if (validateExpLevelConfig(expLevelMap)) {
                    if (equipStatus.equals(EquipStatus.NOT_EQUIPPED)
                            || equipStatus.equals(EquipStatus.NULL)) return;

                    Integer amount = getConfigValue(expLevelMap, AMOUNT_KEY, Integer.class, 1);

                    if (amount != null && amount != 0) {
                        setExpLevel(player, amount);
                    }
                } else {
                    // Log an error about the invalid configuration
                    warn("Invalid exp level configuration: " + expLevelMap);
                }
            }
        }
    }

    /**
     * Sets the experience level for the player.
     *
     * @param player The player to set the experience level for.
     * @param amount The amount to adjust the experience level by.
     */
    private void setExpLevel(@NotNull Player player, int amount) {
        int currentExpLevel = player.getLevel();

        if (amount < 0) {
            // Handle case where amount is negative (EXP Level is being subtracted)
            player.setLevel(Math.max(currentExpLevel + amount, 0));
        } else {
            player.setLevel(currentExpLevel + amount);
        }
    }

    /**
     * Validates the experience level configuration.
     *
     * @param expLevelMap The experience level configuration map.
     * @return True if the configuration is valid, false otherwise.
     */
    private boolean validateExpLevelConfig(Map<?, ?> expLevelMap) {
        return expLevelMap.containsKey(AMOUNT_KEY) && expLevelMap.get(AMOUNT_KEY) instanceof Integer;
    }
}
